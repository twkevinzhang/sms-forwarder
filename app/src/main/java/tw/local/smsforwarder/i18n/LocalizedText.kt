package tw.local.smsforwarder.i18n

import android.content.Context
import android.content.res.Resources
import tw.local.smsforwarder.R
import tw.local.smsforwarder.domain.*

object LocalizedText {
    fun notice(context: Context, notice: Notice): String = notice(context.resources, notice)

    fun notice(resources: Resources, notice: Notice): String {
        val resource =
            when (notice.code) {
                Code.INVALID_NUMBER -> R.string.notice_invalid_number
                Code.RECEIVE_SIM_UNAVAILABLE -> R.string.notice_receive_sim_unavailable
                Code.SEND_SIM_UNAVAILABLE -> R.string.notice_send_sim_unavailable
                Code.PERMISSION_REQUIRED -> R.string.notice_permission_required
                Code.TOO_MANY_PARTS -> R.string.notice_too_many_parts
                Code.RATE_LIMIT -> R.string.notice_rate_limit
                Code.SEND_EXCEPTION -> R.string.notice_send_exception
                Code.PENDING -> R.string.notice_pending
                Code.PARTIAL -> R.string.notice_partial
                Code.SENT -> R.string.notice_sent
                Code.DELIVERED -> R.string.notice_delivered
                Code.DELIVERY_UNCONFIRMED -> R.string.notice_delivery_unconfirmed
                Code.SEND_FAILED -> R.string.notice_send_failed
                Code.DELIVERY_FAILED -> R.string.notice_delivery_failed
                Code.RESULT_UNKNOWN -> R.string.notice_result_unknown
                Code.DELIVERY_UNKNOWN -> R.string.notice_delivery_unknown
                Code.DUPLICATE_OR_LIMIT -> R.string.notice_duplicate_or_limit
                Code.LEGACY_UNKNOWN -> R.string.notice_legacy_unknown
                Code.PERMISSIONS_READY -> R.string.notice_permissions_ready
                Code.PERMISSIONS_DENIED -> R.string.notice_permissions_denied
                Code.SAVED_ENABLED -> R.string.notice_saved_enabled
                Code.SAVED_DISABLED -> R.string.notice_saved_disabled
                Code.SAVE_FAILED -> R.string.notice_save_failed
                Code.SUBMITTED -> R.string.notice_submitted
            }
        return when (notice.code) {
            Code.SEND_FAILED ->
                resources.getString(resource, failure(resources, notice.detail), notice.detail)
            Code.DELIVERY_FAILED -> resources.getString(resource, notice.detail)
            Code.SUBMITTED ->
                resources.getQuantityString(
                    R.plurals.submitted,
                    notice.detail,
                    notice.detail,
                )
            else -> resources.getString(resource)
        }
    }

    private fun failure(resources: Resources, code: Int) =
        resources.getString(
            when (code) {
                2 -> R.string.failure_radio_off
                4 -> R.string.failure_no_service
                5 -> R.string.failure_limit
                6 -> R.string.failure_fixed_dialing
                else -> R.string.failure_generic
            }
        )

    fun kind(context: Context, kind: Kind): String = kind(context.resources, kind)

    fun kind(resources: Resources, kind: Kind) =
        resources.getString(
            when (kind) {
                Kind.AUTOMATIC -> R.string.kind_automatic
                Kind.TEST -> R.string.kind_test
                Kind.SKIPPED -> R.string.kind_skipped
                Kind.UNKNOWN -> R.string.kind_unknown
            }
        )

    fun testMessage(context: Context) = context.getString(R.string.sms_test)

    fun forwardMessage(context: Context, sender: String, body: String) =
        ForwardPolicy.message(sender, body, context.getString(R.string.sms_from))
}
