package tw.local.smsforwarder.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tw.local.smsforwarder.domain.Settings

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
        repository.add("a", "test", 2)
        repository.callback("a", 0, false, true, -1)
        assertEquals("分段傳送中", repository.records().first().getString("status"))
        repository.callback("a", 0, false, true, -1)
        assertEquals(1, repository.records().first().getJSONArray("sent").length())
        repository.callback("a", 1, false, true, -1)
        assertEquals("已送出；等待送達確認", repository.records().first().getString("status"))
        repository.callback("a", 0, true, false, 1)
        repository.callback("a", 1, true, true, -1)
        assertTrue(repository.records().first().getString("status").contains("失敗"))
    }

    @Test
    fun unconfirmedDeliveryNeverOverwritesFailureOrBecomesDelivered() {
        val repository = store()
        repository.add("a", "test", 1)
        repository.deliveryUnconfirmed("a", 0, 1)
        repository.callback("a", 0, false, true, -1)
        assertEquals("電信已處理；送達未確認", repository.records().first().getString("status"))
        repository.callback("a", 0, true, false, 64)
        repository.deliveryUnconfirmed("a", 0, 1)
        assertTrue(repository.records().first().getString("status").contains("失敗"))
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
        repeat(60) { repository.add("$it", "test", 1) }
        assertEquals(50, repository.records().size)
        assertEquals("59", repository.records().first().getString("id"))
        assertFalse(repository.records().first().has("body"))
    }

    @Test
    fun missingCallbackTimesOutWithoutPretendingFailureOrSuccess() {
        val repository = store()
        repository.add("a", "test", 1)
        val record = repository.records().first()
        assertTrue(
            repository.statusText(record, record.getLong("time") + 120001).startsWith("結果未確認")
        )
        repository.callback("a", 4, false, true, -1)
        assertEquals(0, repository.records().first().getJSONArray("sent").length())
    }
}
