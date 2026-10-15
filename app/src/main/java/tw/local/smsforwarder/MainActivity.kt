package tw.local.smsforwarder

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import tw.local.smsforwarder.data.*
import tw.local.smsforwarder.domain.ForwardPolicy
import tw.local.smsforwarder.domain.Settings

class MainActivity : ComponentActivity() {
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
        var result by rememberSaveable { mutableStateOf("") }
        var records by remember { mutableStateOf(store.records()) }
        val permissions =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                ready = gateway.permissionReady()
                sims = gateway.sims()
                result =
                    if (ready) "權限已就緒，請選擇 SIM 並儲存"
                    else "權限未完整授予；自動轉寄與測試無法執行。需要接收簡訊、指定 SIM 及發送簡訊；若不再顯示授權視窗，請開啟系統設定。"
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
                Text("簡訊自動轉寄", style = MaterialTheme.typography.headlineMedium)
                Text("選擇接收與發送 SIM，將新收到的簡訊轉寄到指定門號。", style = MaterialTheme.typography.bodyMedium)
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("自動轉寄", style = MaterialTheme.typography.titleMedium)
                                Text(if (savedSettings.enabled) "已儲存：啟用" else "已儲存：停用")
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it },
                                modifier =
                                    Modifier.semantics { contentDescription = "自動轉寄開關，儲存後生效" },
                            )
                        }
                        SimSelector("偵測 SIM", receiveId, sims) { receiveId = it }
                        SimSelector("發送 SIM", sendId, sims) { sendId = it }
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("收件手機門號") },
                            placeholder = { Text("例如 +12025550101") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                if (!ready && enabled) result = "請先授予必要權限"
                                else {
                                    val error =
                                        if (enabled)
                                            ForwardPolicy.configurationError(
                                                draft,
                                                sims.map { it.id }.toSet(),
                                            )
                                        else null
                                    if (error != null) result = error
                                    else
                                        result =
                                            if (store.save(draft)) {
                                                savedSettings = draft
                                                "設定已儲存；${if(enabled) "自動轉寄已啟用" else "自動轉寄已停用"}"
                                            } else "儲存失敗，請重試"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("儲存設定")
                        }
                        OutlinedButton(
                            onClick = {
                                result =
                                    try {
                                        gateway.send(
                                            store.settings(),
                                            "[SMS轉寄測試] 這是簡訊自動轉寄的測試訊息。",
                                            "手動測試",
                                        )
                                    } catch (e: Exception) {
                                        e.message ?: "測試失敗"
                                    }
                                records = store.records()
                            },
                            enabled = !dirty && ready,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("發送測試簡訊")
                        }
                        Text(
                            if (dirty) "有未儲存的變更；儲存後才能測試。" else "測試使用已儲存設定，即使自動轉寄停用仍可測試。",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (result.isNotEmpty()) Text(result, color = MaterialTheme.colorScheme.primary)
                Text("權限與背景運作", style = MaterialTheme.typography.titleLarge)
                Text(if (ready) "必要權限已授予 · 可用 SIM：${sims.size} 張" else "需要簡訊收發與電話狀態權限，以辨識及指定 SIM。")
                if (!ready) {
                    val rationale =
                        arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                            )
                            .any { shouldShowRequestPermissionRationale(it) }
                    if (rationale) Text("先前拒絕的權限是收取新簡訊、辨識 SIM 與指定 SIM 發送所必需；你也可以保持停用。")
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
                        Text("授予必要權限")
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
                        Text("開啟 App 權限設定")
                    }
                }
                Text(
                    "鎖屏及關閉畫面後仍接收新簡訊。重新開機後請先解鎖一次。強制停止 App 後，需重新開啟。手機的省電與背景管理設定可能限制背景運作。",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(
                    onClick = {
                        startActivity(
                            Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    }
                ) {
                    Text("檢查電池最佳化設定")
                }
                Text(
                    "轉寄會產生電信費。每則最多 10 段、包含測試每小時最多 30 則；失敗不自動重送。已有轉寄標記的簡訊會略過以降低循環風險。請勿設定成本機任一 SIM 的門號，亦勿建立互相轉寄規則。",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("最近轉寄紀錄", style = MaterialTheme.typography.titleLarge)
                Text(
                    "最多保留 50 筆狀態，不保存簡訊內容。送出與送達不同；送達回報須電信商支援。",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (records.isEmpty()) Text("尚無轉寄紀錄")
                records.forEach { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${r.getString("kind")} · ${SimpleDateFormat("MM/dd HH:mm:ss", Locale.TAIWAN).format(Date(r.getLong("time")))}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(store.statusText(r))
                            if (r.getInt("parts") > 0)
                                Text(
                                    "${r.getInt("parts")} 段",
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
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    sims.firstOrNull { it.id == selected }?.label
                        ?: if (selected >= 0) "原 SIM 已不可用，請重新選擇" else "請選擇 SIM"
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (sims.isEmpty())
                    DropdownMenuItem(
                        text = { Text("沒有可用 SIM，請檢查權限與 SIM 狀態") },
                        onClick = { expanded = false },
                        enabled = false,
                    )
                sims.forEach { sim ->
                    DropdownMenuItem(
                        text = { Text(sim.label) },
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
