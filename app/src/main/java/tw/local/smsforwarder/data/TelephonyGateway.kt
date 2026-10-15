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
import tw.local.smsforwarder.domain.*
import tw.local.smsforwarder.domain.Settings
import tw.local.smsforwarder.i18n.AppLanguages
import tw.local.smsforwarder.i18n.LocalizedText

data class Sim(val id: Int, val slot: Int, val name: String)

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
                .map { Sim(it.subscriptionId, it.simSlotIndex + 1, it.displayName.toString()) }
        } catch (_: SecurityException) {
            emptyList()
        }

    // All required permissions are checked immediately before calling the platform API.
    @SuppressLint("MissingPermission")
    fun send(s: Settings, text: String, kind: Kind): Notice {
        if (!permissionReady()) throw ForwardException(Notice(Code.PERMISSION_REQUIRED))
        ForwardPolicy.configurationError(s, sims().map { it.id }.toSet())?.let {
            throw ForwardException(Notice(it))
        }
        val manager =
            if (Build.VERSION.SDK_INT >= 31)
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(s.sendId)
            else SmsManager.getSmsManagerForSubscriptionId(s.sendId)
        val parts = manager.divideMessage(text)
        if (parts.size > 10) throw ForwardException(Notice(Code.TOO_MANY_PARTS))
        val id = UUID.randomUUID().toString()
        val store = Store(context)
        if (!store.reserveSend()) throw ForwardException(Notice(Code.RATE_LIMIT))
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
            store.status(id, Notice(Code.SEND_EXCEPTION))
            throw ForwardException(Notice(Code.SEND_EXCEPTION), recorded = true)
        }
        return Notice(Code.SUBMITTED, parts.size)
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
            if (!gateway.permissionReady()) Code.PERMISSION_REQUIRED
            else ForwardPolicy.configurationError(s, gateway.sims().map { it.id }.toSet())
        if (error != null) {
            store.add(id, Kind.AUTOMATIC, 0)
            store.status(id, Notice(error))
            return
        }
        if (!store.reserve("$subscription|$sender|${messages.first().timestampMillis}|$body")) {
            store.add(id, Kind.SKIPPED, 0)
            store.status(id, Notice(Code.DUPLICATE_OR_LIMIT))
            return
        }
        try {
            gateway.send(
                s,
                LocalizedText.forwardMessage(
                    AppLanguages(context).localizedContext(),
                    sender,
                    body,
                ),
                Kind.AUTOMATIC,
            )
        } catch (e: Exception) {
            if (e !is ForwardException || !e.recorded) {
                store.add(id, Kind.AUTOMATIC, 0)
                store.status(
                    id,
                    if (e is ForwardException) e.notice else Notice(Code.SEND_EXCEPTION),
                )
            }
        }
    }
}
