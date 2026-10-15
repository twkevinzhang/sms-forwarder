package tw.local.smsforwarder.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tw.local.smsforwarder.domain.*

class StoreTest {
    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    private fun store() = Store(context, "forwarder_test")

    @Before
    @After
    fun reset() {
        context.getSharedPreferences("forwarder_test", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun settingsSurviveNewRepositoryInstance() {
        val settings = Settings(true, 3, 4, "+12025550101")
        assertTrue(store().save(settings))
        assertEquals(settings, store().settings())
    }

    @Test
    fun segmentedCallbacksRequireAllPartsAndFailureStaysFailed() {
        val repository = store()
        repository.add("a", Kind.TEST, 2)
        repository.callback("a", 0, false, true, -1)
        assertEquals(Code.PARTIAL, repository.statusNotice(repository.records().first()).code)
        repository.callback("a", 0, false, true, -1)
        assertEquals(1, repository.records().first().getJSONArray("sent").length())
        repository.callback("a", 1, false, true, -1)
        assertEquals(Code.SENT, repository.statusNotice(repository.records().first()).code)
        repository.callback("a", 0, true, false, 1)
        repository.callback("a", 1, true, true, -1)
        assertEquals(
            Code.DELIVERY_FAILED,
            repository.statusNotice(repository.records().first()).code,
        )
    }

    @Test
    fun unconfirmedDeliveryNeverOverwritesFailureOrBecomesDelivered() {
        val repository = store()
        repository.add("a", Kind.TEST, 1)
        repository.deliveryUnconfirmed("a", 0, 1)
        repository.callback("a", 0, false, true, -1)
        assertEquals(
            Code.DELIVERY_UNCONFIRMED,
            repository.statusNotice(repository.records().first()).code,
        )
        repository.callback("a", 0, true, false, 64)
        repository.deliveryUnconfirmed("a", 0, 1)
        assertEquals(
            Code.DELIVERY_FAILED,
            repository.statusNotice(repository.records().first()).code,
        )
    }

    @Test
    fun dedupAndHourlyLimitPersist() {
        val repository = store()
        assertTrue(repository.reserve("first", 1000))
        assertFalse(store().reserve("first", 1001))
        for (index in 1..29) assertTrue(repository.reserve("key$index", 1001))
        assertFalse(repository.reserve("overflow", 1002))
        assertTrue(repository.reserve("later", 3601002))
    }

    @Test
    fun manualAndAutomaticSendSharePersistentLimit() {
        repeat(30) { assertTrue(store().reserveSend(1000)) }
        assertFalse(store().reserveSend(1001))
        assertTrue(store().reserveSend(3601001))
    }

    @Test
    fun historyIsBoundedAndContainsNoBody() {
        val repository = store()
        repeat(60) { repository.add("$it", Kind.TEST, 1) }
        assertEquals(50, repository.records().size)
        assertEquals("59", repository.records().first().getString("id"))
        assertFalse(repository.records().first().has("body"))
    }

    @Test
    fun missingCallbackTimesOutWithoutPretendingFailureOrSuccess() {
        val repository = store()
        repository.add("a", Kind.TEST, 1)
        val record = repository.records().first()
        assertEquals(
            Code.RESULT_UNKNOWN,
            repository.statusNotice(record, record.getLong("time") + 120001).code,
        )
        repository.callback("a", 4, false, true, -1)
        assertEquals(0, repository.records().first().getJSONArray("sent").length())
    }

    @Test
    fun legacyMigrationPreservesIdentityAndDiscardsUnknownText() {
        val legacy = JSONArray()
        legacy.put(
            JSONObject()
                .put("id", "known")
                .put("time", 1000)
                .put("kind", "手動測試")
                .put("parts", 1)
                .put("status", "已確認送達")
                .put("sent", JSONArray("[0]"))
                .put("delivered", JSONArray("[0]"))
        )
        legacy.put(
            JSONObject()
                .put("id", "unknown")
                .put("time", 2000)
                .put("kind", "private kind")
                .put("parts", 1)
                .put("status", "unrecognized exception with private content")
        )
        legacy.put(
            JSONObject()
                .put("id", "failed")
                .put("time", 3000)
                .put("kind", "自動轉寄")
                .put("parts", 2)
                .put("status", "已確認送達")
                .put("failed", true)
        )
        context
            .getSharedPreferences("forwarder_test", Context.MODE_PRIVATE)
            .edit()
            .putString("records", legacy.toString())
            .commit()
        val records = store().records()
        assertEquals(Code.SEND_EXCEPTION, store().statusNotice(records[0]).code)
        assertEquals(Code.LEGACY_UNKNOWN, store().statusNotice(records[1]).code)
        assertEquals(Code.DELIVERED, store().statusNotice(records[2]).code)
        assertEquals("known", records[2].getString("id"))
        assertEquals(1000L, records[2].getLong("time"))
        assertEquals(1, records[2].getJSONArray("delivered").length())
        val persisted =
            context
                .getSharedPreferences("forwarder_test", Context.MODE_PRIVATE)
                .getString("records", "")!!
        assertFalse(persisted.contains("private"))
        assertFalse(persisted.contains("已確認"))
        assertTrue(records.all { it.getInt("schema") == 2 })
    }

    @Test
    fun sentDeliveryTimeoutRemainsSentAndFailureHasPriority() {
        val repository = store()
        repository.add("a", Kind.TEST, 1)
        repository.callback("a", 0, false, true, -1)
        val record = repository.records().first()
        assertEquals(
            Code.DELIVERY_UNKNOWN,
            repository.statusNotice(record, record.getLong("time") + 86400001).code,
        )
        repository.callback("a", 0, true, false, 64)
        repository.status("a", Notice(Code.LEGACY_UNKNOWN))
        assertEquals(
            Code.DELIVERY_FAILED,
            repository.statusNotice(repository.records().first(), Long.MAX_VALUE).code,
        )
    }

    @Test
    fun sendExceptionRemainsTerminalAfterLateSuccessForNewAndLegacyRecords() {
        val repository = store()
        repository.add("new", Kind.TEST, 1)
        repository.status("new", Notice(Code.SEND_EXCEPTION))
        repository.callback("new", 0, false, true, -1)
        repository.callback("new", 0, true, true, 0)
        assertEquals(
            Code.SEND_EXCEPTION,
            repository.statusNotice(repository.records().first()).code,
        )
        val legacy =
            JSONArray()
                .put(
                    JSONObject()
                        .put("id", "old")
                        .put("time", 1000)
                        .put("kind", "手動測試")
                        .put("parts", 1)
                        .put("status", "發送失敗：UnknownException；不自動重送")
                )
        context
            .getSharedPreferences("forwarder_test", Context.MODE_PRIVATE)
            .edit()
            .putString("records", legacy.toString())
            .commit()
        repository.callback("old", 0, false, true, -1)
        repository.callback("old", 0, true, true, 0)
        assertEquals(
            Code.SEND_EXCEPTION,
            repository.statusNotice(repository.records().first()).code,
        )
        assertFalse(repository.records().first().toString().contains("UnknownException"))
    }
}
