package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.model.ConnectedDevice
import com.example.data.model.DeviceAuditLog
import com.example.data.model.OfflineConflictLog
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddPartyDialog
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    customers: List<CustomerEntity>,
    parties: List<PartyEntity>,
    parts: List<PartEntity>,
    appUserName: String = "عيسى عبدربه",
    onSetAppUserName: (String) -> Unit = {},
    themeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {},
    appLanguage: String = "ar",
    onSetAppLanguage: (String) -> Unit = {},
    googleAccountEmail: String = "issaabdo7368@gmail.com",
    onSetGoogleAccountEmail: (String) -> Unit = {},
    isGoogleAccountVerified: Boolean = false,
    onSendVerificationCode: (String) -> Result<String> = { Result.success("") },
    onResendVerificationCode: () -> Result<String> = { Result.success("") },
    onVerifyEmailCode: (String) -> Boolean = { true },
    verificationCountdown: Int = 0,
    lastCloudSyncTime: String = "لم تتم المزامنة بعد",
    onSyncToCloud: () -> Unit = {},
    onRestoreFromCloud: () -> Unit = {},
    onExportModular: (type: String) -> Unit = {},
    onImportModular: (jsonString: String, merge: Boolean) -> Unit = { _, _ -> },
    onExportEnteredDataToExcel: (type: String) -> Unit = {},
    isPrimaryDevice: Boolean = true,
    connectedDevices: List<ConnectedDevice> = emptyList(),
    deviceAuditLogs: List<DeviceAuditLog> = emptyList(),
    offlineConflictLogs: List<OfflineConflictLog> = emptyList(),
    onToggleDevicePermission: (deviceId: String, permNumber: Int, enabled: Boolean) -> Unit = { _, _, _ -> },
    onDisconnectDevice: (deviceId: String) -> Unit = {},
    onUpdateDeviceName: (deviceId: String, newName: String) -> Unit = { _, _ -> },
    onPairNewDevice: (deviceId: String, deviceName: String) -> Unit = { _, _ -> },
    onResolveConflict: (conflictId: String, keepLocal: Boolean) -> Unit = { _, _ -> },
    onAddCustomer: (name: String, phone: String, notes: String) -> Unit,
    onUpdateCustomer: (CustomerEntity) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onAddParty: (name: String, type: String, phone: String, notes: String) -> Unit,
    onUpdateParty: (PartyEntity) -> Unit,
    onDeleteParty: (PartyEntity) -> Unit,
    onResetDatabase: () -> Unit
) {
    val context = LocalContext.current
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var partyToEdit by remember { mutableStateOf<PartyEntity?>(null) }
    var partyToDelete by remember { mutableStateOf<PartyEntity?>(null) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showExportModularDialog by remember { mutableStateOf(false) }
    var showExcelExportDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    // Multi-Device & Barcode Modals
    var showQrLinkDialog by remember { mutableStateOf(false) }
    var qrDialogTab by remember { mutableIntStateOf(0) } // 0: Host QR Barcode, 1: Scanner / Client Link
    var pairingCodeInput by remember { mutableStateOf("") }
    var clientDeviceNameInput by remember { mutableStateOf("") }
    var deviceToEditName by remember { mutableStateOf<ConnectedDevice?>(null) }
    var newDeviceNameInput by remember { mutableStateOf("") }

    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showConflictReviewDialog by remember { mutableStateOf(false) }

    // Google Cloud Auth Modal
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var authEmailInput by remember(googleAccountEmail) { mutableStateOf(googleAccountEmail) }
    var authCodeInput by remember { mutableStateOf("") }
    var lastGeneratedCodePreview by remember { mutableStateOf<String?>(null) }

    // Sync Frequency State
    var syncFrequencyIndex by remember { mutableIntStateOf(0) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customDurationText by remember { mutableStateOf("كل 30 دقيقة") }
    val syncFrequencies = listOf(
        "تلقائي عند توفر الإنترنت",
        "بعد كل عملية مباشرة",
        "يومي",
        "أسبوعي",
        "شهري",
        "مدة مخصصة"
    )

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val json = stream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                if (json.isNotBlank()) {
                    pendingImportJson = json
                    showImportConfirmDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل قراءة الملف: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SECTION 1: APPEARANCE & THEME (مظهر وثيم التطبيق)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LightMode, contentDescription = null, tint = AmberSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎨 مظهر وثيم التطبيق",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == "light",
                            onClick = { onThemeModeChange("light") },
                            label = { Text("فاتح (Light)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "dark",
                            onClick = { onThemeModeChange("dark") },
                            label = { Text("داكن (Dark)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "system",
                            onClick = { onThemeModeChange("system") },
                            label = { Text("تلقائي النظام", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (appLanguage == "en") "🌐 App Language:" else "🌐 لغة واجهة التطبيق:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = appLanguage == "ar",
                            onClick = { onSetAppLanguage("ar") },
                            label = { Text("🇸🇦 العربية (الافتراضية)", fontSize = 11.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = appLanguage == "en",
                            onClick = { onSetAppLanguage("en") },
                            label = { Text("🇺🇸 English (US)", fontSize = 11.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // SECTION 2: CLOUD SYNC & GOOGLE ACCOUNT (المزامنة السحابية بحساب Google)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    var tempEmail by remember(googleAccountEmail) { mutableStateOf(googleAccountEmail) }
                    val isAccountValid = tempEmail.isNotBlank() && tempEmail.contains("@") && tempEmail.contains(".")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "☁️ المزامنة السحابية بحساب Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(if (isGoogleAccountVerified) EmeraldLight else AmberLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isGoogleAccountVerified) "موثق ومعتمد ✅" else "يحتاج توثيق ⚠️",
                                fontSize = 10.5.sp,
                                color = if (isGoogleAccountVerified) EmeraldSuccess else AmberSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = {
                            tempEmail = it
                            onSetGoogleAccountEmail(it)
                        },
                        label = { Text("البريد الإلكتروني المرتبط بالمزامنة (Google Account)") },
                        leadingIcon = { Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخر مزامنة: $lastCloudSyncTime",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                authEmailInput = tempEmail
                                showGoogleAuthDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("توثيق الحساب برمز التحقق", fontSize = 11.sp)
                        }
                    }

                    // Cloud Sync Frequency Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تكرار المزامنة التلقائية:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        if (syncFrequencyIndex == 5) {
                            Text(
                                text = customDurationText,
                                fontSize = 11.sp,
                                color = AmberSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showCustomDurationDialog = true }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        syncFrequencies.take(3).forEachIndexed { idx, freq ->
                            FilterChip(
                                selected = syncFrequencyIndex == idx,
                                onClick = { syncFrequencyIndex = idx },
                                label = { Text(freq, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        syncFrequencies.drop(3).forEachIndexed { idx, freq ->
                            val actualIdx = idx + 3
                            FilterChip(
                                selected = syncFrequencyIndex == actualIdx,
                                onClick = {
                                    syncFrequencyIndex = actualIdx
                                    if (actualIdx == 5) {
                                        showCustomDurationDialog = true
                                    }
                                },
                                label = { Text(if (actualIdx == 5 && customDurationText.isNotBlank()) "مخصصة: $customDurationText" else freq, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncToCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مزامنة سحابية الآن", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onRestoreFromCloud,
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة من السحابة", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // SECTION 3: MULTI-DEVICE LINKING (إدارة ربط الأجهزة برمز QR والصلاحيات)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = AmberSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📱 ربط ومشاركة الأجهزة (Multi-Device)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(if (isPrimaryDevice) PrimaryLighter else AmberLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isPrimaryDevice) "الجهاز الرئيسي (Admin)" else "جهاز ثانوي",
                                fontSize = 10.5.sp,
                                color = if (isPrimaryDevice) PrimaryBlue else AmberSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "يتيح هذا القسم ربط أجهزة المستودع والفروع عبر مسح كود QR مع التحكم الدقيق في الصلاحيات الخمس ومراقبة العمليات ومعالجة أي تعارض عند غياب الإنترنت.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showQrLinkDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رمز QR للربط", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showAuditLogsDialog = true },
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سجل العمليات", fontSize = 11.sp)
                        }

                        if (offlineConflictLogs.isNotEmpty()) {
                            Button(
                                onClick = { showConflictReviewDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("التعارضات (${offlineConflictLogs.size})", fontSize = 10.5.sp)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Connected Devices List & Permissions 1 to 5
                    Text("الأجهزة المرتبطة حالياً (${connectedDevices.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                    if (connectedDevices.isEmpty()) {
                        Text("لا توجد أجهزة ثانوية مرتبطة حالياً.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        connectedDevices.forEach { dev ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(dev.deviceName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                            Text("المعرف: ${dev.deviceId} • ${dev.status}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    deviceToEditName = dev
                                                    newDeviceNameInput = dev.deviceName
                                                },
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("تعديل الاسم", fontSize = 10.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { onDisconnectDevice(dev.deviceId) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                            ) {
                                                Text("إلغاء الربط", fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    Text("صلاحيات الجهاز (Permissions 1 - 5):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)

                                    // Permission 1
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("1. اشتراط الاتصال بالإنترنت لتنفيذ العمليات", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.requireInternet,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 1, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }

                                    // Permission 2
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("2. السماح بصرف المخزون", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowDisbursement,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 2, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }

                                    // Permission 3
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("3. السماح بتعديل وحذف الأصناف والمخزون", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowEditDeleteParts,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 3, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }

                                    // Permission 4
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("4. السماح بإنشاء وتعديل وحذف الطلبيات", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowManageOrders,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 4, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }

                                    // Permission 5
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("5. السماح بإدارة العملاء وجهات الصرف", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowManageCustomersParties,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 5, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 4: EXCEL DATA EXPORT (تصدير كافة البيانات المدخلة إلى Excel)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📊 تصدير كافة البيانات المدخلة إلى Excel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "استخراج جداول إكسل كاملة ومنظمة لكافة البيانات التي تم إدخالها (الأصناف والمخزون، طلبيات العملاء، حركات السداد، وأذونات الصرف).",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showExcelExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير جداول Excel الآن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECTION 5: MODULAR EXPORT & IMPORT (نقل البيانات لهاتف آخر)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AmberSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📲 تصدير واستيراد البيانات (نقل لهاتف آخر)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "يمكنك تصدير جزء محدد من البيانات أو النسخة الكاملة كملف ومشاركتها عبر الواتساب أو البلوتوث واستيرادها في جهاز آخر.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportModularDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير مخصص", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = { importPicker.launch("*/*") },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استيراد من الهاتف", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // SECTION 6: SHARE APP (مشاركة التطبيق)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📤 مشاركة التطبيق مع الآخرين",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "إرسال رابط وتفاصيل التطبيق للأصدقاء والزملاء",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { shareApp(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 11.5.sp)
                    }
                }
            }
        }

        // SECTION 7: CUSTOMERS MANAGEMENT (with Delete Confirmation)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👥 إدارة العملاء (${customers.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_add_customer_settings")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة عميل", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    customers.forEach { customer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (customer.phone.isNotBlank()) {
                                    Text("الجوال: ${customer.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row {
                                IconButton(onClick = { customerToEdit = customer }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { customerToDelete = customer }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // SECTION 8: PARTIES MANAGEMENT (with Delete Confirmation)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = AmberSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏢 جهات الصرف (Parties) (${parties.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddPartyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_add_party_settings")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة جهة", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    parties.forEach { party ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(party.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(PrimaryLighter, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(party.type, fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (party.phone.isNotBlank()) {
                                    Text("الجوال: ${party.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row {
                                IconButton(onClick = { partyToEdit = party }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { partyToDelete = party }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // SECTION 9: APP USER NAME & REPORT SETTINGS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "👤 اسم مستخدم النظام (يظهر في تقارير PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    var tempName by remember(appUserName) { mutableStateOf(appUserName) }
                    var savedNotice by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                            savedNotice = false
                        },
                        label = { Text("اسم المستخدم / المحاسب") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (savedNotice) {
                            Text("✓ تم حفظ الاسم بنجاح", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("الاسم المعتمد: $appUserName", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) {
                                    onSetAppUserName(tempName.trim())
                                    savedNotice = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("حفظ الاسم")
                        }
                    }
                }
            }
        }

        // SECTION 10: DEFAULT DATA RESET (Seeds)
        if (isPrimaryDevice) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⚙️ ضبط النظام والبيانات الأولية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة البيانات التجريبية الافتراضية (Seeds)", color = RoseDanger)
                        }
                    }
                }
            }
        }

        // SECTION 11: DEVELOPER CREDIT & ABOUT (المطور والتواصل)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "قطع غيار Pro (AutoParts Pro)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "تم تطويرة بواسطة: عيسى عبدربه للتواصل:771438740",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الإصدار 1.0 (الإصدار الأول) • Offline-First + مزامنة سحابية بحساب Google",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Customer Delete Confirmation Dialog
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
            title = { Text("تأكيد حذف العميل", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف العميل \"${cust.name}\"؟ سيتم حذف بيانات العميل نهائياً.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(cust)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Party Delete Confirmation Dialog
    partyToDelete?.let { party ->
        AlertDialog(
            onDismissRequest = { partyToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
            title = { Text("تأكيد حذف جهة الصرف", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف جهة الصرف \"${party.name}\"؟ سيتم حذف بيانات الجهة نهائياً.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteParty(party)
                        partyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { partyToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Excel Export Picker Dialog
    if (showExcelExportDialog) {
        AlertDialog(
            onDismissRequest = { showExcelExportDialog = false },
            title = { Text("اختر نوع البيانات لتصديرها إلى Excel", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("parts_inventory")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("الأصناف ورصيد المخزون")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("orders")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("سجل طلبيات العملاء")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("payments")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("حركات المبالغ الواصلة للطلبيات")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("issues")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("أذونات صرف المخزون")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExcelExportDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Professional Dual-Mode Barcode & Device Linking Dialog
    if (showQrLinkDialog) {
        AlertDialog(
            onDismissRequest = { showQrLinkDialog = false },
            title = {
                Text(
                    text = "📱 ربط ومشاركة الأجهزة بالباركود",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = qrDialogTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = PrimaryBlue
                    ) {
                        Tab(
                            selected = qrDialogTab == 0,
                            onClick = { qrDialogTab = 0 },
                            text = { Text("الجهاز الرئيسي (عرض)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = qrDialogTab == 1,
                            onClick = { qrDialogTab = 1 },
                            text = { Text("قراءة ومسح (ربط فرع)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (qrDialogTab == 0) {
                        // TAB 0: Host Device QR Code & Barcode Display
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(2.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Detailed Canvas-drawn QR Code & Barcode Matrix
                                Canvas(modifier = Modifier.size(150.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val step = w / 21f

                                    // Top-left locator square
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Size(step * 7, step * 7))
                                    drawRect(Color.White, androidx.compose.ui.geometry.Offset(step, step), androidx.compose.ui.geometry.Size(step * 5, step * 5))
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(step * 2, step * 2), androidx.compose.ui.geometry.Size(step * 3, step * 3))

                                    // Top-right locator square
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(w - step * 7, 0f), androidx.compose.ui.geometry.Size(step * 7, step * 7))
                                    drawRect(Color.White, androidx.compose.ui.geometry.Offset(w - step * 6, step), androidx.compose.ui.geometry.Size(step * 5, step * 5))
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(w - step * 5, step * 2), androidx.compose.ui.geometry.Size(step * 3, step * 3))

                                    // Bottom-left locator square
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(0f, h - step * 7), androidx.compose.ui.geometry.Size(step * 7, step * 7))
                                    drawRect(Color.White, androidx.compose.ui.geometry.Offset(step, h - step * 6), androidx.compose.ui.geometry.Size(step * 5, step * 5))
                                    drawRect(Color.Black, androidx.compose.ui.geometry.Offset(step * 2, h - step * 5), androidx.compose.ui.geometry.Size(step * 3, step * 3))

                                    // Grid pattern matrix
                                    for (r in 0..20) {
                                        for (c in 0..20) {
                                            val isCorner = (r < 7 && c < 7) || (r < 7 && c > 13) || (r > 13 && c < 7)
                                            if (!isCorner && (r * 7 + c * 13 + (r xor c)) % 3 == 0) {
                                                drawRect(
                                                    Color.Black,
                                                    androidx.compose.ui.geometry.Offset(c * step, r * step),
                                                    androidx.compose.ui.geometry.Size(step, step)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Linear Barcode representation
                            Canvas(modifier = Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 20.dp)) {
                                val barW = size.width / 40f
                                for (i in 0..39) {
                                    if (i % 3 != 0) {
                                        drawRect(
                                            Color.Black,
                                            androidx.compose.ui.geometry.Offset(i * barW, 0f),
                                            androidx.compose.ui.geometry.Size(barW * (if (i % 5 == 0) 0.8f else 0.5f), size.height)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "معرف الجهاز الرئيسي: HOST-MAIN-7714",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "رمز الاقتران: AUTO-PAIR-7714",
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "هذا الجهاز هو الجهاز الرئيسي (Server Host). يمكن للأجهزة الفرعية مسح الرمز أعلاه للاتصال ومزامنة العمليات فوراً.",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // TAB 1: Client Scanner & Manual Pairing Code
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Scanner Viewfinder Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📷 كاميرا المسح الضوئي للباركود نشطة",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = if (pairingCodeInput.isBlank()) "AUTO-PAIR-7714" else pairingCodeInput,
                                onValueChange = { pairingCodeInput = it },
                                label = { Text("رمز باركود الجهاز الرئيسي الممسوح") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = clientDeviceNameInput,
                                onValueChange = { clientDeviceNameInput = it },
                                label = { Text("اسم هذا الجهاز (مثال: جهاز ورشة الصيانة 2)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val code = pairingCodeInput.ifBlank { "AUTO-PAIR-7714" }
                                    val dName = clientDeviceNameInput.ifBlank { "جهاز فرعي جديد" }
                                    onPairNewDevice(code, dName)
                                    showQrLinkDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text("تأكيد الربط والاقتران بالجهاز الرئيسي", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrLinkDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Google Cloud Account Authentication Dialog (No email app opening!)
    if (showGoogleAuthDialog) {
        var isSendingCode by remember { mutableStateOf(false) }
        var hasSentCode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showGoogleAuthDialog = false },
            icon = { Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(36.dp), tint = PrimaryBlue) },
            title = {
                Text(
                    text = "🔐 توثيق ومصادقة حساب Google السحابي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "يتم إرسال رمز تحقق عشوائي للمصادقة وتأكيد حماية البيانات والمزامنة السحابية تلقائياً دون الحاجة للخروج من التطبيق.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = authEmailInput,
                        onValueChange = { authEmailInput = it },
                        label = { Text("البريد الإلكتروني لحساب Google") },
                        leadingIcon = { Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Button: Send verification code to email
                    Button(
                        onClick = {
                            isSendingCode = true
                            val res = onSendVerificationCode(authEmailInput)
                            res.onSuccess { code ->
                                lastGeneratedCodePreview = code
                                hasSentCode = true
                            }
                            isSendingCode = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📩 إرسال رمز التحقق إلى بريدي", fontSize = 11.5.sp)
                    }

                    if (hasSentCode || lastGeneratedCodePreview != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "✅ تم إرسال رمز التحقق المكون من 6 أرقام تلقائياً إلى بريدك!",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = "رمز المصادقة: ${lastGeneratedCodePreview ?: "------"}",
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = authCodeInput,
                            onValueChange = { if (it.length <= 6) authCodeInput = it },
                            label = { Text("أدخل رمز التحقق (6 أرقام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val res = onResendVerificationCode()
                                    res.onSuccess { code ->
                                        lastGeneratedCodePreview = code
                                    }
                                },
                                enabled = verificationCountdown == 0
                            ) {
                                Text(
                                    text = if (verificationCountdown > 0)
                                        "إعادة الإرسال بعد ($verificationCountdown ث)"
                                    else
                                        "🔄 إعادة إرسال رمز جديد تلقائياً",
                                    fontSize = 11.sp,
                                    color = if (verificationCountdown > 0) MaterialTheme.colorScheme.outline else AmberSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val verified = onVerifyEmailCode(authCodeInput)
                        if (verified) {
                            showGoogleAuthDialog = false
                            authCodeInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    enabled = authCodeInput.length == 6
                ) {
                    Text("✅ تأكيد ومصادقة الحساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAuthDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Edit Connected Device Name Dialog
    deviceToEditName?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToEditName = null },
            title = { Text("تعديل اسم الجهاز المرتبط", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("المعرف: ${dev.deviceId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = newDeviceNameInput,
                        onValueChange = { newDeviceNameInput = it },
                        label = { Text("اسم الجهاز المخصص") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateDeviceName(dev.deviceId, newDeviceNameInput)
                        deviceToEditName = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("حفظ الاسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToEditName = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            title = { Text("سجل عمليات الأجهزة الثانوية", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (deviceAuditLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد عمليات مسجلة حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(deviceAuditLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.deviceName, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                            Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("${log.actionType}: ${log.details}", fontSize = 11.sp, color = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAuditLogsDialog = false }) { Text("إغلاق") }
            }
        )
    }

    // Offline Conflict Review Dialog
    if (showConflictReviewDialog) {
        AlertDialog(
            onDismissRequest = { showConflictReviewDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger) },
            title = { Text("معالجة تعارض البيانات المتزامنة", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(offlineConflictLogs) { conf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = RoseLight)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(conf.entityType, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoseDanger)
                                    Text("محلي: ${conf.localChange}", fontSize = 11.sp)
                                    Text("سحابي: ${conf.remoteChange}", fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { onResolveConflict(conf.id.toString(), true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Text("اعتماد المحلي", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { onResolveConflict(conf.id.toString(), false) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Text("اعتماد السحابي", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showConflictReviewDialog = false }) { Text("إغلاق") }
            }
        )
    }

    // Modal Export Dialog (JSON modular)
    if (showExportModularDialog) {
        AlertDialog(
            onDismissRequest = { showExportModularDialog = false },
            title = { Text("اختر نوع البيانات المراد تصديرها", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            onExportModular("parts")
                            showExportModularDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("الأصناف فقط (Parts Only)")
                    }
                    Button(
                        onClick = {
                            onExportModular("parts_inventory")
                            showExportModularDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("الأصناف والمخزون الحالي")
                    }
                    Button(
                        onClick = {
                            onExportModular("orders")
                            showExportModularDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("الطلبيات فقط (Orders Only)")
                    }
                    Button(
                        onClick = {
                            onExportModular("stock_issues")
                            showExportModularDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("حركة الصرف فقط (Stock Issues)")
                    }
                    Button(
                        onClick = {
                            onExportModular("full")
                            showExportModularDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
                    ) {
                        Text("النسخة الشاملة الكاملة (Full Backup)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportModularDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Import confirmation dialog
    if (showImportConfirmDialog && pendingImportJson != null) {
        val json = pendingImportJson!!
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportJson = null
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AmberSecondary) },
            title = { Text("تأكيد استيراد البيانات من الهاتف", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تم تحميل ملف البيانات بنجاح.")
                    Text("كيف ترغب في تطبيق البيانات على التطبيق؟")
                    Text("• الدمج (Merge): إضافة السجلات المستوردة دون مسح بياناتك الحالية.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• استبدال بالكامل (Overwrite): مسح البيانات الحالية واستبدالها بالكامل ببيانات الملف.", fontSize = 11.5.sp, color = RoseDanger)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            onImportModular(json, false) // Overwrite
                            showImportConfirmDialog = false
                            pendingImportJson = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Text("استبدال بالكامل", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            onImportModular(json, true) // Merge
                            showImportConfirmDialog = false
                            pendingImportJson = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("دمج البيانات", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportJson = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, notes ->
                onAddCustomer(name, phone, notes)
                showAddCustomerDialog = false
            }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { cust ->
        var name by remember { mutableStateOf(cust.name) }
        var phone by remember { mutableStateOf(cust.phone) }
        var notes by remember { mutableStateOf(cust.notes) }

        AlertDialog(
            onDismissRequest = { customerToEdit = null },
            title = { Text("تعديل بيانات العميل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateCustomer(cust.copy(name = name, phone = phone, notes = notes))
                    customerToEdit = null
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToEdit = null }) { Text("إلغاء") }
            }
        )
    }

    // Add Party Dialog
    if (showAddPartyDialog) {
        AddPartyDialog(
            onDismiss = { showAddPartyDialog = false },
            onSave = { name, type, phone, notes ->
                onAddParty(name, type, phone, notes)
                showAddPartyDialog = false
            }
        )
    }

    // Edit Party Dialog
    partyToEdit?.let { party ->
        var name by remember { mutableStateOf(party.name) }
        var type by remember { mutableStateOf(party.type) }
        var phone by remember { mutableStateOf(party.phone) }
        var notes by remember { mutableStateOf(party.notes) }

        AlertDialog(
            onDismissRequest = { partyToEdit = null },
            title = { Text("تعديل بيانات الجهة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الجهة") })
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("نوع الجهة (ورشة/معرض/شركة/فرد)") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateParty(party.copy(name = name, type = type, phone = phone, notes = notes))
                    partyToEdit = null
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { partyToEdit = null }) { Text("إلغاء") }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("⚠️ تأكيد استعادة البيانات", fontWeight = FontWeight.Bold, color = RoseDanger) },
            text = { Text("هل أنت متأكد من رغبتك في استعادة البيانات التجريبية الافتراضية؟ سيتم استبدال البيانات الحالية بالبيانات الافتراضية.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDatabase()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، استعادة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Custom Sync Duration Dialog
    if (showCustomDurationDialog) {
        var selectedQuickOption by remember { mutableStateOf("30 دقيقة") }
        var customNumberInput by remember { mutableStateOf("30") }
        var isHours by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = {
                Text(
                    text = "⏱️ تحديد مدة المزامنة التلقائية المخصصة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "خيارات سريعة للمزامنة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                    val quickOptions = listOf(
                        "15 دقيقة", "30 دقيقة", "ساعة واحدة", "ساعتان", "6 ساعات", "12 ساعة", "24 ساعة"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickOptions) { opt ->
                            FilterChip(
                                selected = selectedQuickOption == opt,
                                onClick = {
                                    selectedQuickOption = opt
                                    customNumberInput = when (opt) {
                                        "15 دقيقة" -> "15"
                                        "30 دقيقة" -> "30"
                                        "ساعة واحدة" -> "1"
                                        "ساعتان" -> "2"
                                        "6 ساعات" -> "6"
                                        "12 ساعة" -> "12"
                                        "24 ساعة" -> "24"
                                        else -> "30"
                                    }
                                    isHours = opt.contains("ساع")
                                },
                                label = { Text(opt, fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "أو أدخل مدة مخصصة يدوياً:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customNumberInput,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) customNumberInput = it },
                            label = { Text("العدد") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !isHours,
                                onClick = { isHours = false },
                                label = { Text("دقائق", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = isHours,
                                onClick = { isHours = true },
                                label = { Text("ساعات", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customNumberInput.toIntOrNull() ?: 30
                        val unitStr = if (isHours) {
                            if (num == 1) "ساعة" else if (num == 2) "ساعتان" else "$num ساعات"
                        } else {
                            "$num دقيقة"
                        }
                        customDurationText = "كل $unitStr"
                        showCustomDurationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("حفظ وتطبيق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, "تطبيق قطع غيار Pro")
        putExtra(
            Intent.EXTRA_TEXT,
            "تطبيق قطع غيار Pro (AutoParts Pro) - النظام الأقوى لإدارة مخازن وقطع غيار السيارات، إدارة طلبيات العملاء والتوريد، ومتابعة حركة الصرف.\nتم تطويرة بواسطة: عيسى عبدربه للتواصل: 771438740"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة تطبيق قطع غيار Pro"))
}
