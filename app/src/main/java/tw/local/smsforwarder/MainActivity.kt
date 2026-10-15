package tw.local.smsforwarder

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import tw.local.smsforwarder.data.*
import tw.local.smsforwarder.domain.*
import tw.local.smsforwarder.domain.ForwardPolicy
import tw.local.smsforwarder.domain.Settings
import tw.local.smsforwarder.i18n.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            val colors =
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    if (dark) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                } else if (dark) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) { ForwardScreen() }
        }
    }

    @Composable
    private fun ForwardScreen() {
        val context = LocalContext.current
        val resources = LocalResources.current
        val configuration = LocalConfiguration.current
        fun t(id: Int) = resources.getString(id)
        val languages = remember { AppLanguages(this) }
        val gateway = remember { TelephonyGateway(this) }
        val store = remember { Store(this) }
        var savedSettings by remember { mutableStateOf(store.settings()) }
        val initial = remember { savedSettings }
        var enabled by rememberSaveable { mutableStateOf(initial.enabled) }
        var receiveId by rememberSaveable { mutableIntStateOf(initial.receiveId) }
        var sendId by rememberSaveable { mutableIntStateOf(initial.sendId) }
        var destination by rememberSaveable { mutableStateOf(initial.destination) }
        var ready by remember { mutableStateOf(gateway.permissionReady()) }
        var sims by remember { mutableStateOf(gateway.sims()) }
        var resultName by rememberSaveable { mutableStateOf<String?>(null) }
        var resultDetail by rememberSaveable { mutableIntStateOf(0) }
        fun show(notice: Notice) {
            resultName = notice.code.name
            resultDetail = notice.detail
        }
        var records by remember { mutableStateOf(store.records()) }
        val permissions =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                ready = gateway.permissionReady()
                sims = gateway.sims()
                show(Notice(if (ready) Code.PERMISSIONS_READY else Code.PERMISSIONS_DENIED))
            }
        val owner = LocalLifecycleOwner.current
        DisposableEffect(owner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    ready = gateway.permissionReady()
                    sims = gateway.sims()
                }
            }
            owner.lifecycle.addObserver(observer)
            onDispose { owner.lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(owner) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    records = store.records()
                    delay(1500)
                }
            }
        }
        val draft = Settings(enabled, receiveId, sendId, ForwardPolicy.normalize(destination))
        val dirty = draft != savedSettings
        Scaffold { padding ->
            Column(
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(t(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(t(R.string.intro), style = MaterialTheme.typography.bodyMedium)
                LanguageSelector(languages.selection()) {
                    try {
                        languages.select(it)
                    } catch (_: Exception) {
                        show(Notice(Code.SAVE_FAILED))
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    t(R.string.automatic),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    if (savedSettings.enabled) t(R.string.saved_enabled)
                                    else t(R.string.saved_disabled)
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it },
                                modifier =
                                    Modifier.semantics {
                                        contentDescription = t(R.string.switch_description)
                                    },
                            )
                        }
                        SimSelector(t(R.string.receive_sim), receiveId, sims) { receiveId = it }
                        SimSelector(t(R.string.send_sim), sendId, sims) { sendId = it }
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text(t(R.string.destination)) },
                            placeholder = { Text(t(R.string.destination_example)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                if (!ready && enabled) show(Notice(Code.PERMISSION_REQUIRED))
                                else {
                                    val error =
                                        if (enabled)
                                            ForwardPolicy.configurationError(
                                                draft,
                                                sims.map { it.id }.toSet(),
                                            )
                                        else null
                                    if (error != null) show(Notice(error))
                                    else {
                                        if (store.save(draft)) {
                                            savedSettings = draft
                                            show(
                                                Notice(
                                                    if (enabled) Code.SAVED_ENABLED
                                                    else Code.SAVED_DISABLED
                                                )
                                            )
                                        } else show(Notice(Code.SAVE_FAILED))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(t(R.string.save))
                        }
                        OutlinedButton(
                            onClick = {
                                show(
                                    try {
                                        gateway.send(
                                            store.settings(),
                                            LocalizedText.testMessage(
                                                AppLanguages(context).localizedContext()
                                            ),
                                            Kind.TEST,
                                        )
                                    } catch (e: ForwardException) {
                                        e.notice
                                    } catch (_: Exception) {
                                        Notice(Code.SEND_EXCEPTION)
                                    }
                                )
                                records = store.records()
                            },
                            enabled = !dirty && ready,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(t(R.string.test))
                        }
                        Text(
                            if (dirty) t(R.string.unsaved) else t(R.string.test_hint),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                resultName?.let {
                    Text(
                        LocalizedText.notice(
                            resources,
                            Notice(
                                runCatching { Code.valueOf(it) }.getOrDefault(Code.LEGACY_UNKNOWN),
                                resultDetail,
                            ),
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    t(R.string.permissions_background),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    if (ready)
                        resources.getQuantityString(
                            R.plurals.sim_count,
                            sims.size,
                            sims.size,
                        )
                    else t(R.string.permissions_explanation)
                )
                if (!ready) {
                    val rationale =
                        arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                            )
                            .any { shouldShowRequestPermissionRationale(it) }
                    if (rationale) Text(t(R.string.rationale))
                    Button(
                        onClick = {
                            permissions.launch(
                                arrayOf(
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.READ_PHONE_STATE,
                                )
                            )
                        }
                    ) {
                        Text(t(R.string.grant_permissions))
                    }
                    TextButton(
                        onClick = {
                            startActivity(
                                Intent(
                                    AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:$packageName"),
                                )
                            )
                        }
                    ) {
                        Text(t(R.string.app_permission_settings))
                    }
                }
                Text(
                    t(R.string.background_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(
                    onClick = {
                        startActivity(
                            Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    }
                ) {
                    Text(t(R.string.battery_settings))
                }
                Text(
                    t(R.string.cost_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(t(R.string.recent_records), style = MaterialTheme.typography.titleLarge)
                Text(
                    t(R.string.records_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (records.isEmpty()) Text(t(R.string.no_records))
                records.forEach { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${LocalizedText.kind(resources, runCatching { Kind.valueOf(r.getString("kind")) }.getOrDefault(Kind.UNKNOWN))} · ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, configuration.locales[0]).format(Date(r.getLong("time")))}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(LocalizedText.notice(resources, store.statusNotice(r)))
                            if (r.getInt("parts") > 0)
                                Text(
                                    resources.getQuantityString(
                                        R.plurals.part_count,
                                        r.getInt("parts"),
                                        r.getInt("parts"),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimSelector(label: String, selected: Int, sims: List<Sim>, onSelect: (Int) -> Unit) {
    val resources = LocalResources.current
    fun t(id: Int) = resources.getString(id)
    fun label(sim: Sim) = resources.getString(R.string.sim_label, sim.slot, sim.name)
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    sims.firstOrNull { it.id == selected }?.let { label(it) }
                        ?: if (selected >= 0) t(R.string.sim_unavailable)
                        else t(R.string.choose_sim)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (sims.isEmpty())
                    DropdownMenuItem(
                        text = { Text(t(R.string.no_sim)) },
                        onClick = { expanded = false },
                        enabled = false,
                    )
                sims.forEach { sim ->
                    DropdownMenuItem(
                        text = { Text(label(sim)) },
                        onClick = {
                            onSelect(sim.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(selected: String, onSelect: (String) -> Unit) {
    val resources = LocalResources.current
    val options =
        listOf(
            "" to R.string.language_system,
            "zh-Hant" to R.string.language_zh,
            "en" to R.string.language_en,
        )
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(resources.getString(R.string.language), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(resources.getString(options.first { it.first == selected }.second))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (tag, label) ->
                    DropdownMenuItem(
                        text = { Text(resources.getString(label)) },
                        onClick = {
                            expanded = false
                            onSelect(tag)
                        },
                    )
                }
            }
        }
    }
}
