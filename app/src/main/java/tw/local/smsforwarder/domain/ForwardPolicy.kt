package tw.local.smsforwarder.domain

data class Settings(
    val enabled: Boolean = false,
    val receiveId: Int = -1,
    val sendId: Int = -1,
    val destination: String = "",
)

object ForwardPolicy {
    const val MARKER = "[SMSForwarder]"
    const val LEGACY_MARKER = "[SMS轉寄]"

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

    fun normalize(number: String) = number.filterNot { it.isWhitespace() || it in "()-" }

    fun validNumber(number: String) = Regex("\\+?[0-9]{5,15}").matches(number)

    fun configurationError(s: Settings, active: Set<Int>): Code? =
        when {
            !validNumber(s.destination) -> Code.INVALID_NUMBER
            s.receiveId !in active -> Code.RECEIVE_SIM_UNAVAILABLE
            s.sendId !in active -> Code.SEND_SIM_UNAVAILABLE
            else -> null
        }

    fun shouldForward(s: Settings, subscription: Int, sender: String, body: String): Boolean =
        s.enabled &&
            subscription == s.receiveId &&
            !body.startsWith(MARKER) &&
            !body.startsWith(LEGACY_MARKER)

    fun message(sender: String, body: String, header: String) = "$MARKER\n$header$sender\n$body"
}
