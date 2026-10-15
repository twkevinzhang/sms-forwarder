package tw.local.smsforwarder.i18n

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import tw.local.smsforwarder.R
import tw.local.smsforwarder.domain.*

class AppLanguagesTest {
    private val context
        get() = ApplicationProvider.getApplicationContext<Context>()

    private fun preferences() = context.getSharedPreferences("locale_test", Context.MODE_PRIVATE)

    private fun isolated() = AppLanguages(context, "locale_test", usePlatform = false)

    @Before
    @After
    fun clean() {
        preferences().edit().clear().commit()
    }

    @Test
    fun preferenceSurvivesColdHelperAndSystemReset() {
        isolated().select("zh-Hant")
        assertEquals("zh-Hant", isolated().selection())
        assertEquals("簡訊自動轉寄", isolated().localizedContext().getString(R.string.app_name))
        isolated().select("en")
        assertEquals("SMS Forwarder", isolated().localizedContext().getString(R.string.app_name))
        isolated().select("")
        assertEquals("", isolated().selection())
        isolated().select("unsupported")
        assertEquals("", isolated().selection())
    }

    @Test
    fun receiverTemplatesUseColdPreferenceAndPreserveBody() {
        val body = "  body\\n原文\n\n"
        isolated().select("zh-Hant")
        val zh = isolated().localizedContext()
        assertTrue(LocalizedText.testMessage(zh).startsWith(ForwardPolicy.MARKER))
        assertTrue(LocalizedText.forwardMessage(zh, "sender", body).contains("來自：sender"))
        isolated().select("en")
        val en = isolated().localizedContext()
        assertTrue(LocalizedText.forwardMessage(en, "sender", body).contains("From: sender"))
        assertTrue(LocalizedText.forwardMessage(en, "sender", body).endsWith(body))
        assertNotEquals(LocalizedText.testMessage(zh), LocalizedText.testMessage(en))
    }

    @Test
    fun everyNoticeFormatsInBothLanguagesAndEnglishFallback() {
        for (tag in listOf("en", "zh-Hant")) {
            isolated().select(tag)
            val localized = isolated().localizedContext()
            Code.entries.forEach {
                assertTrue(LocalizedText.notice(localized, Notice(it, 2)).isNotBlank())
            }
            Kind.entries.forEach { assertTrue(LocalizedText.kind(localized, it).isNotBlank()) }
            assertTrue(
                localized.resources.getQuantityString(R.plurals.part_count, 2, 2).contains("2")
            )
        }
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(Locale.FRENCH))
        assertEquals(
            "SMS Forwarder",
            context.createConfigurationContext(config).getString(R.string.app_name),
        )
        isolated().select("en")
        assertTrue(
            LocalizedText.notice(isolated().localizedContext(), Notice(Code.SUBMITTED, 1))
                .contains("1 message part.")
        )
    }

    @Test
    fun nativeExternalRegionsAreAuthoritativeAndMigrationRunsOnlyOnce() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val manager = context.getSystemService(LocaleManager::class.java)
        val original = manager.applicationLocales
        try {
            val helper = AppLanguages(context, "locale_test")
            preferences().edit().putString("language", "zh-Hant").commit()
            manager.applicationLocales = LocaleList.forLanguageTags("en-US")
            helper.initialize()
            assertEquals("en-US", manager.applicationLocales.toLanguageTags())
            assertEquals("en", helper.selection())
            assertEquals("SMS Forwarder", helper.localizedContext().getString(R.string.app_name))
            manager.applicationLocales = LocaleList.forLanguageTags("zh-Hant-TW,en-US")
            assertEquals("zh-Hant", helper.selection())
            assertEquals("簡訊自動轉寄", helper.localizedContext().getString(R.string.app_name))
            manager.applicationLocales = LocaleList.forLanguageTags("fr-FR")
            assertEquals("SMS Forwarder", helper.localizedContext().getString(R.string.app_name))
            assertEquals("fr-FR", manager.applicationLocales.toLanguageTags())
            manager.applicationLocales = LocaleList.getEmptyLocaleList()
            helper.initialize()
            assertTrue(manager.applicationLocales.isEmpty)
            assertEquals("", helper.selection())
        } finally {
            manager.applicationLocales = original
        }
    }
}
