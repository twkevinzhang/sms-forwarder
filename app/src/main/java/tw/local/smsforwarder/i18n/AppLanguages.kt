package tw.local.smsforwarder.i18n

import android.app.Application
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/** API 33 owns its preference; older versions have one explicit, synchronous preference. */
class AppLanguages(
    private val context: Context,
    private val preferences: String = "app_language",
    private val usePlatform: Boolean = true,
) {
    private val prefs = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)

    fun selection(): String =
        if (usePlatform && Build.VERSION.SDK_INT >= 33) {
            canonical(
                context
                    .getSystemService(LocaleManager::class.java)
                    .applicationLocales
                    .toLanguageTags()
            )
        } else canonical(prefs.getString("language", "").orEmpty())

    fun select(tag: String) {
        val selected = canonical(tag)
        if (usePlatform && Build.VERSION.SDK_INT >= 33) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selected))
        } else {
            check(prefs.edit().putString("language", selected).commit())
            if (usePlatform)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selected))
        }
    }

    fun initialize() {
        if (!usePlatform) return
        if (Build.VERSION.SDK_INT >= 33) {
            synchronized(MIGRATION_LOCK) {
                if (!prefs.getBoolean("platform_migrated", false)) {
                    val manager = context.getSystemService(LocaleManager::class.java)
                    val previous = canonical(prefs.getString("language", "").orEmpty())
                    if (manager.applicationLocales.isEmpty && previous.isNotEmpty()) {
                        manager.applicationLocales = LocaleList.forLanguageTags(previous)
                    }
                    prefs.edit().putBoolean("platform_migrated", true).remove("language").commit()
                }
            }
        } else
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selection()))
    }

    /** Safe for a cold manifest receiver: never depends on an Activity or Locale.setDefault. */
    fun localizedContext(): Context {
        val requested = selection()
        val locales =
            if (usePlatform && Build.VERSION.SDK_INT >= 33) {
                val manager = context.getSystemService(LocaleManager::class.java)
                manager.applicationLocales.takeUnless { it.isEmpty } ?: manager.systemLocales
            } else if (requested.isNotEmpty()) LocaleList.forLanguageTags(requested)
            else Resources.getSystem().configuration.locales
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(locales)
        return context.createConfigurationContext(configuration)
    }

    companion object {
        private val MIGRATION_LOCK = Any()

        fun canonical(tag: String): String {
            val locale = Locale.forLanguageTag(tag.substringBefore(','))
            return when {
                locale.language == "en" -> "en"
                locale.language == "zh" &&
                    (locale.script == "Hant" ||
                        (locale.script.isEmpty() && locale.country in setOf("TW", "HK", "MO"))) ->
                    "zh-Hant"
                else -> ""
            }
        }
    }
}

class ForwarderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLanguages(this).initialize()
    }
}
