package tw.local.smsforwarder.data

import android.content.Context
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject
import tw.local.smsforwarder.domain.*
import tw.local.smsforwarder.domain.Settings

class Store(context: Context, name: String = "forwarder") {
    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun settings() =
        synchronized(LOCK) {
            Settings(
                prefs.getBoolean("enabled", false),
                prefs.getInt("receive", -1),
                prefs.getInt("send", -1),
                prefs.getString("destination", "")!!,
            )
        }

    fun save(s: Settings) =
        synchronized(LOCK) {
            prefs
                .edit()
                .putBoolean("enabled", s.enabled)
                .putInt("receive", s.receiveId)
                .putInt("send", s.sendId)
                .putString("destination", s.destination)
                .commit()
        }

    fun statusNotice(r: JSONObject, now: Long = System.currentTimeMillis()): Notice {
        val code =
            runCatching { Code.valueOf(r.optString("status")) }.getOrDefault(Code.LEGACY_UNKNOWN)
        if (r.optBoolean("failed"))
            return Notice(
                if (code in setOf(Code.SEND_FAILED, Code.DELIVERY_FAILED, Code.SEND_EXCEPTION)) code
                else Code.SEND_EXCEPTION,
                r.optInt("detail", -1),
            )
        val age = now - r.optLong("time", now)
        return Notice(
            when {
                age > 86_400_000 && code == Code.SENT -> Code.DELIVERY_UNKNOWN
                age > 120_000 && code in setOf(Code.PENDING, Code.PARTIAL) -> Code.RESULT_UNKNOWN
                else -> code
            },
            r.optInt("detail"),
        )
    }

    fun reserveSend(now: Long = System.currentTimeMillis()): Boolean =
        synchronized(LOCK) {
            val old = JSONArray(prefs.getString("sendTimes", "[]"))
            val a = JSONArray()
            for (i in 0 until old.length()) if (now - old.getLong(i) < 3_600_000)
                a.put(old.getLong(i))
            if (a.length() >= 30) return false
            a.put(now)
            prefs.edit().putString("sendTimes", a.toString()).commit()
        }

    fun records(): List<JSONObject> =
        synchronized(LOCK) {
            val a = recordArray()
            (0 until a.length()).map { a.getJSONObject(it) }.reversed()
        }

    fun add(id: String, kind: Kind, parts: Int) =
        synchronized(LOCK) {
            val a = recordArray()
            a.put(
                JSONObject()
                    .put("id", id)
                    .put("time", System.currentTimeMillis())
                    .put("kind", kind.name)
                    .put("parts", parts)
                    .put("status", Code.PENDING.name)
                    .put("schema", 2)
                    .put("detail", 0)
                    .put("sent", JSONArray())
                    .put("delivered", JSONArray())
            )
            while (a.length() > 50) a.remove(0)
            prefs.edit().putString("records", a.toString()).commit()
        }

    fun status(id: String, notice: Notice) =
        edit(id) { r ->
            if (!r.optBoolean("failed"))
                r.put("status", notice.code.name).put("detail", notice.detail)
            if (notice.code in setOf(Code.SEND_FAILED, Code.DELIVERY_FAILED, Code.SEND_EXCEPTION))
                r.put("failed", true)
        }

    fun deliveryUnconfirmed(id: String, index: Int, code: Int) =
        edit(id) { r ->
            if (index !in 0 until r.getInt("parts")) return@edit
            r.put("unconfirmed", true)
            if (!r.optBoolean("failed"))
                r.put("status", Code.DELIVERY_UNCONFIRMED.name).put("detail", code)
        }

    fun callback(id: String, index: Int, delivery: Boolean, success: Boolean, code: Int) =
        edit(id) { r ->
            if (index !in 0 until r.getInt("parts")) return@edit
            val a = r.getJSONArray(if (delivery) "delivered" else "sent")
            if ((0 until a.length()).none { a.getInt(it) == index }) a.put(index)
            if (!success)
                r.put("failed", true)
                    .put("status", (if (delivery) Code.DELIVERY_FAILED else Code.SEND_FAILED).name)
                    .put("detail", code)
            else if (!r.optBoolean("failed")) {
                val parts = r.getInt("parts")
                r.put(
                    "status",
                    when {
                        r.getJSONArray("delivered").length() == parts -> Code.DELIVERED
                        r.optBoolean("unconfirmed") -> Code.DELIVERY_UNCONFIRMED
                        r.getJSONArray("sent").length() == parts -> Code.SENT
                        else -> Code.PARTIAL
                    }.name,
                )
            }
        }

    /** Rewrite legacy translated strings once, discarding all unknown text and extra fields. */
    private fun recordArray(): JSONArray {
        val source =
            runCatching { JSONArray(prefs.getString("records", "[]")) }.getOrDefault(JSONArray())
        var migrated = false
        val result = JSONArray()
        for (i in maxOf(0, source.length() - 50) until source.length()) {
            val old = source.optJSONObject(i) ?: continue
            if (old.optInt("schema") == 2) {
                result.put(old)
                continue
            }
            migrated = true
            val text = old.optString("status")
            val status =
                when (text) {
                    "等待電信回報" -> Code.PENDING
                    "分段傳送中" -> Code.PARTIAL
                    "已送出；等待送達確認" -> Code.SENT
                    "已確認送達" -> Code.DELIVERED
                    "電信已處理；送達未確認" -> Code.DELIVERY_UNCONFIRMED
                    "已送出；送達未確認" -> Code.DELIVERY_UNKNOWN
                    "重複訊息或已達每小時 30 則上限" -> Code.DUPLICATE_OR_LIMIT
                    "缺少簡訊或電話權限",
                    "請先授予電話與簡訊權限" -> Code.PERMISSION_REQUIRED
                    "請輸入 5–15 位手機門號（可加國碼 +）" -> Code.INVALID_NUMBER
                    "偵測 SIM 不可用，請重新選擇" -> Code.RECEIVE_SIM_UNAVAILABLE
                    "發送 SIM 不可用，請重新選擇" -> Code.SEND_SIM_UNAVAILABLE
                    "訊息超過 10 段，已停止以限制費用" -> Code.TOO_MANY_PARTS
                    "已達每小時 30 則發送上限（含測試）" -> Code.RATE_LIMIT
                    else ->
                        when {
                            text.startsWith("部分或全部失敗") && text.contains("電信送達失敗") ->
                                Code.DELIVERY_FAILED
                            text.startsWith("部分或全部失敗") -> Code.SEND_FAILED
                            text.startsWith("電信已處理；送達未確認（代碼 ") -> Code.DELIVERY_UNCONFIRMED
                            text.startsWith("發送失敗") -> Code.SEND_EXCEPTION
                            text.startsWith("結果未確認") -> Code.RESULT_UNKNOWN
                            else -> Code.LEGACY_UNKNOWN
                        }
                }
            val kind =
                when (old.optString("kind")) {
                    "自動轉寄" -> Kind.AUTOMATIC
                    "手動測試" -> Kind.TEST
                    "略過" -> Kind.SKIPPED
                    else -> Kind.UNKNOWN
                }
            val failure =
                old.optBoolean("failed") ||
                    status in setOf(Code.SEND_FAILED, Code.DELIVERY_FAILED, Code.SEND_EXCEPTION)
            result.put(
                JSONObject()
                    .put("schema", 2)
                    .put("id", old.optString("id"))
                    .put("time", old.optLong("time"))
                    .put("parts", old.optInt("parts"))
                    .put("kind", kind.name)
                    .put(
                        "status",
                        (if (
                                failure &&
                                    status !in
                                        setOf(
                                            Code.SEND_FAILED,
                                            Code.DELIVERY_FAILED,
                                            Code.SEND_EXCEPTION,
                                        )
                            )
                                Code.SEND_EXCEPTION
                            else status)
                            .name,
                    )
                    .put(
                        "detail",
                        Regex("代碼 (-?[0-9]+)").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: -1,
                    )
                    .put("failed", failure)
                    .put("unconfirmed", old.optBoolean("unconfirmed"))
                    .put("sent", old.optJSONArray("sent") ?: JSONArray())
                    .put("delivered", old.optJSONArray("delivered") ?: JSONArray())
            )
        }
        if (migrated || source.length() > 50)
            prefs.edit().putString("records", result.toString()).commit()
        return result
    }

    private fun edit(id: String, block: (JSONObject) -> Unit) =
        synchronized(LOCK) {
            val a = recordArray()
            for (i in 0 until a.length()) if (a.getJSONObject(i).getString("id") == id)
                block(a.getJSONObject(i))
            prefs.edit().putString("records", a.toString()).commit()
        }

    // Reserve before sending: crashes cannot replay an incoming SMS. Only hashes/timestamps
    // persist.
    fun reserve(key: String, now: Long = System.currentTimeMillis()): Boolean =
        synchronized(LOCK) {
            val hash =
                MessageDigest.getInstance("SHA-256").digest(key.toByteArray()).joinToString("") {
                    "%02x".format(it)
                }
            val old = JSONArray(prefs.getString("seen", "[]"))
            val a = JSONArray()
            for (i in 0 until old.length()) {
                val item = old.getJSONObject(i)
                if (now - item.getLong("time") < 86_400_000) a.put(item)
            }
            if ((0 until a.length()).any { a.getJSONObject(it).getString("hash") == hash })
                return false
            val recent =
                (0 until a.length()).count { now - a.getJSONObject(it).getLong("time") < 3_600_000 }
            if (recent >= 30) return false
            a.put(JSONObject().put("hash", hash).put("time", now))
            while (a.length() > 200) a.remove(0)
            prefs.edit().putString("seen", a.toString()).commit()
        }

    companion object {
        private val LOCK = Any()
    }
}
