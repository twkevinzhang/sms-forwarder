package tw.local.smsforwarder.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import java.util.UUID
import tw.local.smsforwarder.domain.ForwardPolicy
import tw.local.smsforwarder.domain.Settings

data class Sim(val id: Int, val label: String)

class TelephonyGateway(private val context: Context) {
    fun permissionReady() =
        arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
            )
            .all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }

    // Subscription access is guarded by SecurityException and refreshed after every permission
    // change.
    @SuppressLint("MissingPermission")
    fun sims(): List<Sim> =
        try {
            context
                .getSystemService(SubscriptionManager::class.java)
                .activeSubscriptionInfoList
                .orEmpty()
                .map { Sim(it.subscriptionId, "SIM ${it.simSlotIndex + 1} · ${it.displayName}") }
        } catch (_: SecurityException) {
            emptyList()
        }

    // All required permissions are checked immediately before calling the platform API.
    @SuppressLint("MissingPermission")
    fun send(s: Settings, text: String, kind: String): String {
        check(permissionReady()) { "請先授予電話與簡訊權限" }
        ForwardPolicy.configurationError(s, sims().map { it.id }.toSet())?.let { error(it) }
        val manager =
            if (Build.VERSION.SDK_INT >= 31)
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(s.sendId)
            else SmsManager.getSmsManagerForSubscriptionId(s.sendId)
        val parts = manager.divideMessage(text)
        check(parts.size <= 10) { "訊息超過 10 段，已停止以限制費用" }
        val id = UUID.randomUUID().toString()
        val store = Store(context)
        check(store.reserveSend()) { "已達每小時 30 則發送上限（含測試）" }
        store.add(id, kind, parts.size)
        fun callbacks(delivery: Boolean) =
            ArrayList(
                parts.indices.map { index ->
                    val intent =
                        Intent(context, SmsStatusReceiver::class.java)
                            .setAction(if (delivery) "DELIVERED" else "SENT")
                            .setData(
                                Uri.parse(
                                    "smsforwarder://$id/$index/${if(delivery) "delivery" else "sent"}"
                                )
                            )
                            .putExtra("id", id)
                            .putExtra("index", index)
                            .putExtra("delivery", delivery)
                    // Sent intents are immutable. Delivery intents need radio-supplied PDU extras

                    // the explicit, non-exported component and unique URI constrain their target.
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        (if (delivery) 0 else PendingIntent.FLAG_ONE_SHOT) or
                            (if (delivery && Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE
                            else if (delivery) 0 else PendingIntent.FLAG_IMMUTABLE),
                    )
                }
            )
        try {
            manager.sendMultipartTextMessage(
                s.destination,
                null,
                parts,
                callbacks(false),
                callbacks(true),
            )
        } catch (e: Exception) {
            store.status(id, "發送失敗：${e.javaClass.simpleName}；不自動重送")
            throw e
        }
        return "已提交 ${parts.size} 段，請查看電信回報與收件手機"
    }
}

class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val delivery = intent.getBooleanExtra("delivery", false)
        val store = Store(context)
        if (delivery) {
            val pdu = intent.getByteArrayExtra("pdu") ?: return
            val message =
                SmsMessage.createFromPdu(pdu, intent.getStringExtra("format") ?: "3gpp") ?: return
            val status = message.status
            when (ForwardPolicy.delivery(status)) {
                ForwardPolicy.Delivery.PENDING -> return
                ForwardPolicy.Delivery.UNCONFIRMED ->
                    store.deliveryUnconfirmed(id, intent.getIntExtra("index", -1), status)
                ForwardPolicy.Delivery.DELIVERED ->
                    store.callback(id, intent.getIntExtra("index", -1), true, true, status)
                ForwardPolicy.Delivery.FAILED ->
                    store.callback(id, intent.getIntExtra("index", -1), true, false, status)
            }
            PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or
                        (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0),
                )
                ?.cancel()
        } else
            store.callback(
                id,
                intent.getIntExtra("index", -1),
                false,
                resultCode == Activity.RESULT_OK,
                resultCode,
            )
    }
}

class IncomingSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val store = Store(context)
        val s = store.settings()
        if (!s.enabled) return
        val subscription =
            intent.getIntExtra(
                SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
                intent.getIntExtra("subscription", -1),
            )
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return
        val sender = messages.first().originatingAddress ?: return
        if (messages.any { it.originatingAddress != sender }) return
        val body = messages.joinToString("") { it.messageBody.orEmpty() }
        if (!ForwardPolicy.shouldForward(s, subscription, sender, body)) return
        val id = UUID.randomUUID().toString()
        val gateway = TelephonyGateway(context)
        val error =
            if (!gateway.permissionReady()) "缺少簡訊或電話權限"
            else ForwardPolicy.configurationError(s, gateway.sims().map { it.id }.toSet())
        if (error != null) {
            store.add(id, "自動轉寄", 0)
            store.status(id, error)
            return
        }
        if (!store.reserve("$subscription|$sender|${messages.first().timestampMillis}|$body")) {
            store.add(id, "略過", 0)
            store.status(id, "重複訊息或已達每小時 30 則上限")
            return
        }
        try {
            gateway.send(s, ForwardPolicy.message(sender, body), "自動轉寄")
        } catch (e: Exception) {
            store.add(id, "自動轉寄", 0)
            store.status(id, e.message ?: "發送失敗")
        }
    }
}
