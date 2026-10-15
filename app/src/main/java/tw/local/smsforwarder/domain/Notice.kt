package tw.local.smsforwarder.domain

enum class Code {
    INVALID_NUMBER,
    RECEIVE_SIM_UNAVAILABLE,
    SEND_SIM_UNAVAILABLE,
    PERMISSION_REQUIRED,
    TOO_MANY_PARTS,
    RATE_LIMIT,
    SEND_EXCEPTION,
    PENDING,
    PARTIAL,
    SENT,
    DELIVERED,
    DELIVERY_UNCONFIRMED,
    SEND_FAILED,
    DELIVERY_FAILED,
    RESULT_UNKNOWN,
    DELIVERY_UNKNOWN,
    DUPLICATE_OR_LIMIT,
    LEGACY_UNKNOWN,
    PERMISSIONS_READY,
    PERMISSIONS_DENIED,
    SAVED_ENABLED,
    SAVED_DISABLED,
    SAVE_FAILED,
    SUBMITTED,
}

data class Notice(val code: Code, val detail: Int = 0)

enum class Kind {
    AUTOMATIC,
    TEST,
    SKIPPED,
    UNKNOWN,
}

class ForwardException(val notice: Notice, val recorded: Boolean = false) : RuntimeException()
