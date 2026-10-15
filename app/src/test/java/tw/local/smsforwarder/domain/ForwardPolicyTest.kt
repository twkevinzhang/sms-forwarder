package tw.local.smsforwarder.domain

import org.junit.Assert.*
import org.junit.Test

class ForwardPolicyTest {
    private val settings = Settings(true, 7, 9, "+12025550101")

    @Test
    fun deliveryCodesAreConservative() {
        assertEquals(ForwardPolicy.Delivery.DELIVERED, ForwardPolicy.delivery(0))
        assertEquals(ForwardPolicy.Delivery.UNCONFIRMED, ForwardPolicy.delivery(1))
        assertEquals(ForwardPolicy.Delivery.UNCONFIRMED, ForwardPolicy.delivery(2))
        assertEquals(ForwardPolicy.Delivery.PENDING, ForwardPolicy.delivery(32))
        assertEquals(ForwardPolicy.Delivery.FAILED, ForwardPolicy.delivery(64))
    }

    @Test
    fun onlySelectedSimCanForward() {
        assertTrue(ForwardPolicy.shouldForward(settings, 7, "+12025550102", "hello"))
        assertFalse(ForwardPolicy.shouldForward(settings, 9, "+12025550102", "hello"))
        assertFalse(ForwardPolicy.shouldForward(settings, -1, "+12025550102", "hello"))
    }

    @Test
    fun disabledAndLoopMessagesAreRejected() {
        assertFalse(
            ForwardPolicy.shouldForward(settings.copy(enabled = false), 7, "+12025550102", "hello")
        )
        assertTrue(ForwardPolicy.shouldForward(settings, 7, "+12025550101", "hello"))
        assertFalse(
            ForwardPolicy.shouldForward(
                settings,
                7,
                "+12025550102",
                ForwardPolicy.message("sender", "hello", "From: "),
            )
        )
    }

    @Test
    fun unavailableSimNeverFallsBack() {
        assertEquals(
            Code.SEND_SIM_UNAVAILABLE,
            ForwardPolicy.configurationError(settings, setOf(7)),
        )
        assertNull(ForwardPolicy.configurationError(settings, setOf(7, 9)))
    }

    @Test
    fun forwardingPreservesBodyAndRecognizesBothMarkers() {
        val body = "  line one\n第二行\n\n"
        assertTrue(ForwardPolicy.message("sender", body, "From: ").endsWith(body))
        assertFalse(ForwardPolicy.shouldForward(settings, 7, "sender", "[SMSForwarder] content"))
        assertFalse(ForwardPolicy.shouldForward(settings, 7, "sender", "[SMS轉寄] content"))
    }

    @Test
    fun destinationValidationRejectsServiceAndMalformedNumbers() {
        assertFalse(ForwardPolicy.validNumber("112"))
        assertFalse(ForwardPolicy.validNumber("++123456"))
        assertFalse(ForwardPolicy.validNumber(ForwardPolicy.normalize("12345;999")))
        assertFalse(ForwardPolicy.validNumber(ForwardPolicy.normalize("12345abc999")))
        assertEquals("+12025550101", ForwardPolicy.normalize("+1 (202) 555-0101"))
        assertTrue(ForwardPolicy.validNumber("+12025550101"))
    }
}
