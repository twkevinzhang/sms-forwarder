package tw.local.smsforwarder.domain

data class Settings(
    val enabled: Boolean = false,
    val receiveId: Int = -1,
    val sendId: Int = -1,
    val destination: String = "",
)

object ForwardPolicy {
    const val MARKER = "[SMS轉寄]"

    enum class Delivery {
        DELIVERED,
        PENDING,
        UNCONFIRMED,
        FAILED,
    }

    fun delivery(status: Int): Delivery =
        when {
            status == 0 -> Delivery.DELIVERED
            status in 1..31 -> Delivery.UNCONFIRMED
            status in 32..63 -> Delivery.PENDING
            else -> Delivery.FAILED
        }

    fun sendFailure(code: Int): String =
        when (code) {
            2 -> "行動通訊關閉，請檢查飛航模式"
            4 -> "目前沒有行動通訊服務"
            5 -> "已達系統簡訊發送限制"
            6 -> "固定撥號設定不允許此門號"
            else -> "電信或裝置拒絕發送"
        }

    fun normalize(number: String) = number.filterNot { it.isWhitespace() || it in "()-" }

    fun validNumber(number: String) = Regex("\\+?[0-9]{5,15}").matches(number)

    fun configurationError(s: Settings, active: Set<Int>): String? =
        when {
            !validNumber(s.destination) -> "請輸入 5–15 位手機門號（可加國碼 +）"
            s.receiveId !in active -> "偵測 SIM 不可用，請重新選擇"
            s.sendId !in active -> "發送 SIM 不可用，請重新選擇"
            else -> null
        }

    fun shouldForward(s: Settings, subscription: Int, sender: String, body: String): Boolean =
        s.enabled && subscription == s.receiveId && !body.startsWith(MARKER)

    fun message(sender: String, body: String) = "$MARKER\n來自：$sender\n$body"
}
