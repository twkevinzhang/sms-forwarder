package tw.local.smsforwarder.data

import android.content.Context
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject
import tw.local.smsforwarder.domain.ForwardPolicy
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

    fun statusText(r: JSONObject, now: Long = System.currentTimeMillis()): String {
        val status = r.getString("status")
        if (now - r.getLong("time") > 86_400_000 && status == "已送出；等待送達確認") return "已送出；送達未確認"
        return if (now - r.getLong("time") > 120_000 && (status == "等待電信回報" || status == "分段傳送中"))
            "結果未確認；不自動重送（原狀態：$status）"
        else status
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
            val a = JSONArray(prefs.getString("records", "[]"))
            (0 until a.length()).map { a.getJSONObject(it) }.reversed()
        }

    fun add(id: String, kind: String, parts: Int) =
        synchronized(LOCK) {
            val a = JSONArray(prefs.getString("records", "[]"))
            a.put(
                JSONObject()
                    .put("id", id)
                    .put("time", System.currentTimeMillis())
                    .put("kind", kind)
                    .put("parts", parts)
                    .put("status", "等待電信回報")
                    .put("sent", JSONArray())
                    .put("delivered", JSONArray())
            )
            while (a.length() > 50) a.remove(0)
            prefs.edit().putString("records", a.toString()).commit()
        }

    fun status(id: String, text: String) = edit(id) { it.put("status", text) }

    fun deliveryUnconfirmed(id: String, index: Int, code: Int) =
        edit(id) { record ->
            if (index !in 0 until record.getInt("parts")) return@edit
            record.put("unconfirmed", true)
            if (!record.optBoolean("failed")) record.put("status", "電信已處理；送達未確認（代碼 $code）")
        }

    fun callback(id: String, index: Int, delivery: Boolean, success: Boolean, code: Int) =
        edit(id) { r ->
            if (index !in 0 until r.getInt("parts")) return@edit
            val key = if (delivery) "delivered" else "sent"
            val a = r.getJSONArray(key)
            if ((0 until a.length()).none { a.getInt(it) == index }) a.put(index)
            if (!success)
                r.put("failed", true)
                    .put(
                        "status",
                        "部分或全部失敗：${if (delivery) "電信送達失敗" else ForwardPolicy.sendFailure(code)}（代碼 $code），不自動重送",
                    )
            else if (!r.optBoolean("failed")) {
                val parts = r.getInt("parts")
                r.put(
                    "status",
                    when {
                        r.getJSONArray("delivered").length() == parts -> "已確認送達"
                        r.optBoolean("unconfirmed") -> "電信已處理；送達未確認"
                        r.getJSONArray("sent").length() == parts -> "已送出；等待送達確認"
                        else -> "分段傳送中"
                    },
                )
            }
        }

    private fun edit(id: String, block: (JSONObject) -> Unit) =
        synchronized(LOCK) {
            val a = JSONArray(prefs.getString("records", "[]"))
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
