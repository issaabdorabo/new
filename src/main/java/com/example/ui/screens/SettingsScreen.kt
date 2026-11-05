package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.model.CloudBackupRecord
import com.example.data.model.ConnectedDevice
import com.example.data.model.DeviceAuditLog
import com.example.data.model.OfflineConflictLog
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddPartyDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.DevicePairingQrDialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LuxuryEmeraldPrimary
import com.example.ui.theme.LuxuryGoldPrimary
import com.example.ui.theme.LuxuryRedPrimary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechCyanLight
import com.example.ui.theme.*

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
    onAppLanguageChange: (String) -> Unit = {},
    googleAccountEmail: String = "issaabdo7368@gmail.com",
    onSetGoogleAccountEmail: (String) -> Unit = {},
    isGoogleAccountVerified: Boolean = false,
    onSendVerificationCode: (email: String, onSuccess: () -> Unit) -> Unit = { _, _ -> },
    onConfirmVerificationCode: (code: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onAuthenticateWithGoogle: (String) -> Unit = {},
    onUnverifyGoogleAccount: () -> Unit = {},
    lastCloudSyncTime: String = "لم تتم المزامنة بعد",
    onSyncToCloud: () -> Unit = {},
    onRestoreFromCloud: () -> Unit = {},
    cloudBackups: List<CloudBackupRecord> = emptyList(),
    onRestoreCloudBackup: (CloudBackupRecord) -> Unit = {},
    onDeleteCloudBackup: (String) -> Unit = {},
    syncFrequencyIndex: Int = 0,
    syncCustomDurationText: String = "كل 30 دقيقة",
    onSyncFrequencyChange: (Int, String) -> Unit = { _, _ -> },
    onExportModular: (type: String) -> Unit = {},
    onExportCustomData: (modules: Set<String>, asExcel: Boolean) -> Unit = { _, _ -> },
    onImportModular: (jsonString: String, merge: Boolean) -> Unit = { _, _ -> },
    onExportEnteredDataToExcel: (type: String) -> Unit = {},
    isPrimaryDevice: Boolean = true,
    connectedDevices: List<ConnectedDevice> = emptyList(),
    deviceAuditLogs: List<DeviceAuditLog> = emptyList(),
    offlineConflictLogs: List<OfflineConflictLog> = emptyList(),
    onToggleDevicePermission: (deviceId: String, permNumber: Int, enabled: Boolean) -> Unit = { _, _, _ -> },
    onDisconnectDevice: (deviceId: String) -> Unit = {},
    onUpdateConnectedDeviceName: (deviceId: String, newName: String) -> Unit = { _, _ -> },
    onPairDeviceFromQr: (qrContent: String) -> Unit = {},
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

    // Import Modals & Warning
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var showImportChoiceDialog by remember { mutableStateOf(false) }
    var showOverwriteWarningDialog by remember { mutableStateOf(false) }

    // Cloud Backups Selection Modal
    var showCloudBackupsDialog by remember { mutableStateOf(false) }
    var selectedCloudBackup by remember { mutableStateOf<CloudBackupRecord?>(null) }
    var showRestoreCloudWarningDialog by remember { mutableStateOf(false) }

    // Multi-Device Modals
    var showQrLinkDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showConflictReviewDialog by remember { mutableStateOf(false) }
    var showVerifyEmailDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var showPairingScanner by remember { mutableStateOf(false) }
    var deviceToRename by remember { mutableStateOf<ConnectedDevice?>(null) }
    var showMustVerifyDialog by remember { mutableStateOf(false) }
    var tempEmail by remember(googleAccountEmail) { mutableStateOf(googleAccountEmail) }

    // Sync Frequency State
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    val syncFrequencies = listOf(
        "تلقائي عند توفر الإنترنت",
        "بعد كل عملية مباشرة",
        "يومي",
        "أسبوعي",
        "شهري",
        "مدة مخصصة"
    )

    // Check if secondary device exists
    val hasSecondaryDevice = remember(connectedDevices) {
        connectedDevices.any { !it.isBlocked && it.deviceId != "DEV-PRIMARY" }
    }

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val json = stream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                if (json.isNotBlank()) {
                    pendingImportJson = json
                    showImportChoiceDialog = true
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
        // SECTION 1: APPEARANCE & THEMES (الثيمات الفاخرة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = TechCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎨 مظهر وثيم التطبيق (الثيمات الفاخرة)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "اختر المظهر المفضل للتطبيق مع إمكانية التبديل بين الثيمات الملكية الفاخرة:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Row 1: Light, Dark, System
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

                    // Row 2: 3 Luxury Themes (Red, Emerald, Royal Gold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == "luxury_red",
                            onClick = { onThemeModeChange("luxury_red") },
                            label = { Text("أحمر فاخر", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryRedPrimary,
                                selectedLabelColor = Color.White
                            ),
                            leadingIcon = {
                                Box(modifier = Modifier.size(12.dp).background(LuxuryRedPrimary, CircleShape))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "luxury_emerald",
                            onClick = { onThemeModeChange("luxury_emerald") },
                            label = { Text("زمردي فاخر", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryEmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            leadingIcon = {
                                Box(modifier = Modifier.size(12.dp).background(LuxuryEmeraldPrimary, CircleShape))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "luxury_gold",
                            onClick = { onThemeModeChange("luxury_gold") },
                            label = { Text("ذهبي ملكي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryGoldPrimary,
                                selectedLabelColor = Color.Black
                            ),
                            leadingIcon = {
                                Box(modifier = Modifier.size(12.dp).background(LuxuryGoldPrimary, CircleShape))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // SECTION 1.5: APP LANGUAGE SELECTION (تغيير لغة التطبيق وحفظها في قاعدة البيانات)
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
                            Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🌐 لغة التطبيق / App Language",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(PrimaryLighter, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (appLanguage == "en") "English 🇺🇸" else "العربية 🇸🇦",
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "قم بتغيير لغة واجهة التطبيق بين العربية والإنجليزية. يتم حفظ تفضيلك مباشرة في قاعدة بيانات التطبيق.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Arabic Choice
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAppLanguageChange("ar") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (appLanguage == "ar") PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (appLanguage == "ar") BorderStroke(1.5.dp, PrimaryBlue) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🇸🇦", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("العربية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("عربي (RTL)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (appLanguage == "ar") {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // English Choice
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAppLanguageChange("en") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (appLanguage == "en") PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (appLanguage == "en") BorderStroke(1.5.dp, PrimaryBlue) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🇺🇸", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("English", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("الإنجليزية (LTR)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (appLanguage == "en") {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: CLOUD SYNC & GOOGLE ACCOUNT (المزامنة السحابية بحساب Google والمصادقة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val isAccountValid = tempEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(tempEmail.trim()).matches()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "☁️ المزامنة السحابية والمصادقة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (isGoogleAccountVerified) EmeraldLight else TechCyanLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isGoogleAccountVerified) "حساب موثق ومؤكد ✓" else "غير مؤكد",
                                fontSize = 10.5.sp,
                                color = if (isGoogleAccountVerified) EmeraldSuccess else PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // GOOGLE ACCOUNT AUTHENTICATION CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = if (isGoogleAccountVerified) BorderStroke(1.dp, EmeraldSuccess) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlue)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "المصادقة بواسطة حساب Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                                if (isGoogleAccountVerified) {
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldLight, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("موثق ✓", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }

                            Text(
                                text = "تسجيل الدخول والمصادقة المباشرة باستخدام حساب Google المعتمد في جهازك للمزامنة الفورية وتأمين البيانات.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        showGoogleAuthDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isGoogleAccountVerified) EmeraldSuccess else PrimaryBlue
                                    )
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGoogleAccountVerified) "تغيير حساب Google" else "المصادقة بواسطة حساب Google",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isGoogleAccountVerified) {
                                    OutlinedButton(
                                        onClick = onUnverifyGoogleAccount,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger)
                                    ) {
                                        Text("خروج", fontSize = 11.sp, color = RoseDanger)
                                    }
                                }
                            }
                        }
                    }

                    // EMAIL FIELD FOR MANUAL OTP VERIFICATION
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = {
                            tempEmail = it
                            onSetGoogleAccountEmail(it)
                        },
                        label = { Text("أو البريد الإلكتروني المعتمد للسحابة") },
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
                                val trimmed = tempEmail.trim()
                                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                                if (trimmed.isBlank() || !emailPattern.matcher(trimmed).matches()) {
                                    Toast.makeText(context, "الرجاء كتابة بريد إلكتروني صحيح ومعتمد أولاً (مثال: user@gmail.com)", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSendVerificationCode(trimmed) {
                                        showVerifyEmailDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGoogleAccountVerified) EmeraldSuccess else PrimaryBlue
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isGoogleAccountVerified) "البريد موثق ✓ (إعادة إرسال)" else "إرسال رمز التوثيق تلقائياً",
                                fontSize = 10.5.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Cloud Sync Frequency Selector (المزامنة التلقائية المحفوظة)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تكرار المزامنة التلقائية (محفوظ):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        if (syncFrequencyIndex == 5) {
                            Text(
                                text = syncCustomDurationText,
                                fontSize = 11.sp,
                                color = TechCyan,
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
                                onClick = { onSyncFrequencyChange(idx, "") },
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
                                    if (actualIdx == 5) {
                                        showCustomDurationDialog = true
                                    } else {
                                        onSyncFrequencyChange(actualIdx, "")
                                    }
                                },
                                label = { Text(if (actualIdx == 5 && syncCustomDurationText.isNotBlank()) "مخصص: $syncCustomDurationText" else freq, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Cloud Actions: Sync & Choose from Backup History
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (tempEmail.isBlank() || !isGoogleAccountVerified) {
                                    showMustVerifyDialog = true
                                } else {
                                    onSyncToCloud()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رفع نسخة للسحابة الآن", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (tempEmail.isBlank() || !isGoogleAccountVerified) {
                                    showMustVerifyDialog = true
                                } else {
                                    showCloudBackupsDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اختيار نسخة واستعادتها", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // SECTION 3: MULTI-DEVICE LINKING (الأجهزة الثانوية وسجل العمليات الخاص بها)
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
                            Icon(Icons.Default.Devices, contentDescription = null, tint = TechCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📱 ربط ومشاركة الأجهزة (Multi-Device)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(if (isPrimaryDevice) PrimaryLighter else TechCyanLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isPrimaryDevice) "الجهاز الرئيسي (Admin)" else "جهاز فرعي",
                                fontSize = 10.5.sp,
                                color = if (isPrimaryDevice) PrimaryBlue else TechCyan,
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
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1.1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كود هذا الجهاز", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showPairingScanner = true },
                            modifier = Modifier.weight(1.1f).height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp), tint = TechCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح كود جهاز", fontSize = 11.sp, color = TechCyan, fontWeight = FontWeight.Bold)
                        }

                        // Sجل العمليات: يظهر فقط في حال وجود جهاز مرتبط ثانوي كما طلب المستخدم
                        if (hasSecondaryDevice) {
                            OutlinedButton(
                                onClick = { showAuditLogsDialog = true },
                                modifier = Modifier.weight(0.9f).height(36.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("العمليات", fontSize = 10.5.sp)
                            }
                        }

                        if (offlineConflictLogs.isNotEmpty()) {
                            Button(
                                onClick = { showConflictReviewDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                                modifier = Modifier.weight(0.9f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تعارض (${offlineConflictLogs.size})", fontSize = 10.5.sp)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Connected Devices List & Permissions 1 to 5
                    Text("الأجهزة المرتبطة حالياً (${connectedDevices.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                    if (connectedDevices.isEmpty()) {
                        Text("لا يوجد أجهزة فرعية مرتبطة حالياً.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(dev.deviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { deviceToRename = dev },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "تعديل الاسم", tint = TechCyan, modifier = Modifier.size(15.dp))
                                                }
                                            }
                                            Text("الرمز: ${dev.deviceId} • الحالة: ${dev.status}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        OutlinedButton(
                                            onClick = { onDisconnectDevice(dev.deviceId) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Text("فصل الجهاز", fontSize = 10.sp)
                                        }
                                    }

                                    Text("صلاحيات الجهاز (Permissions 1 - 5):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)

                                    // Permission 1
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("1. اشتراط اتصال بالإنترنت", fontSize = 10.5.sp)
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
                                        Text("3. السماح بتعديل وحذف الأصناف", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowEditDeleteParts,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 3, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }
                                    // Permission 4
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("4. السماح بإدارة الطلبيات", fontSize = 10.5.sp)
                                        Switch(
                                            checked = dev.allowManageOrders,
                                            onCheckedChange = { onToggleDevicePermission(dev.deviceId, 4, it) },
                                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                        )
                                    }
                                    // Permission 5
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("5. السماح بإدارة العملاء والجهات", fontSize = 10.5.sp)
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

        // SECTION 4: EXCEL DATA EXPORT (تصدير سريع للإكسل)
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
                            text = "📊 تصدير البيانات إلى Excel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "تصدير فوري لكافة السجلات والمخزون والمدفوعات بصيغة CSV المتوافقة مع Microsoft Excel مع دعم اللغة العربية بنسبة 100%.",
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
                        Text("تصدير سريع لملف Excel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECTION 5: MODULAR EXPORT & IMPORT (النسخ الاحتياطي والاستيراد المخصص)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TechCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💾 تصدير واستيراد البيانات مع الهاتف",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "يمكنك تصدير نسخة احتياطية مخصصة بتحديد الأقسام المطلوبة أو استيراد ملف نسخة سابقة واستعادتها بأمان.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportModularDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير مخصص", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
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

        // SECTION 6: SHARE APP
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
                            text = "مشاركة التطبيق",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "مشاركة رابط وتفاصيل التطبيق مع المحلات والمخازن الأخرى",
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

        // SECTION 7: CUSTOMERS MANAGEMENT
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
                            Text("عميل جديد", fontSize = 11.sp)
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
                                    Text("هاتف: ${customer.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        // SECTION 8: PARTIES MANAGEMENT
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
                            Icon(Icons.Default.Business, contentDescription = null, tint = TechCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏢 جهات الصرف (Parties) (${parties.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showAddPartyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_add_party_settings")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("جهة جديدة", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
                                    Text("هاتف: ${party.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        // SECTION 9: APP USER NAME
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
                            text = "👤 اسم المستخدم (يظهر في تقارير PDF)",
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

        // SECTION 11: DEVELOPER CREDIT & ABOUT (تنسيق حديث وخرافي مع واتساب ورقم الإصدار 1.0)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(PrimaryLighter, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "قطع غيار pro",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = PrimaryBlue
                        )
                    }

                    // Version & Developer by row (الإصدار 1.0 • طور بواسطة عيسى عبدربة)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryBlue, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "الإصدار 1.0",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "طور بواسطة عيسى عبدربة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // WhatsApp Direct Contact Button (رقم الهاتف +967771438740)
                    Button(
                        onClick = {
                            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/967771438740")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(whatsappIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "تواصل واتساب: +967771438740", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // WhatsApp bubble symbol
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💬",
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "محادثة واتساب: +967771438740",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        text = "نظام Offline-First الذكي • كافة الميزات مدعومة محلياً وسحابياً",
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
            text = { Text("هل أنت متأكد من حذف العميل \"${cust.name}\" وكافة طلبياته وسجلاته نهائياً؟") },
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
            title = { Text("تأكيد حذف الجهة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف جهة الصرف \"${party.name}\" وسجلاتها نهائياً؟") },
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

    // Quick Excel Export Picker Dialog
    if (showExcelExportDialog) {
        AlertDialog(
            onDismissRequest = { showExcelExportDialog = false },
            title = { Text("تصدير سريع لملف Excel", fontWeight = FontWeight.Bold) },
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
                        Text("الأصناف والمخزون الحالي")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("orders")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("طلبيات العملاء")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("payments")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("سجل دفعات وتحصيلات العملاء")
                    }
                    Button(
                        onClick = {
                            onExportEnteredDataToExcel("issues")
                            showExcelExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("سندات الصرف والمستلمين")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExcelExportDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // REQUIREMENT 13: Cloud Backups Selection Dialog (اختيار أي نسخة احتياطية من السحابة)
    if (showCloudBackupsDialog) {
        AlertDialog(
            onDismissRequest = { showCloudBackupsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختر نسخة احتياطية من السحابة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "النسخ الاحتياطية المحفوظة في السحابة لحسابك (${cloudBackups.size}):",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (cloudBackups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text("لا توجد نسخ سحابية محفوظة حتى الآن. اضغط على 'رفع نسخة للسحابة' أولاً.", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(cloudBackups) { backup ->
                                val isSelected = selectedCloudBackup?.id == backup.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCloudBackup = backup }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) PrimaryLighter else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedCloudBackup = backup }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(backup.dateTimeStr, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface)
                                            Text(
                                                text = "${backup.partsCount} صنف • ${backup.ordersCount} طلبية • ${backup.issuesCount} سند صرف",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(backup.email, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                        IconButton(
                                            onClick = { onDeleteCloudBackup(backup.id) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف النسخة", tint = RoseDanger, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedCloudBackup != null) {
                            showRestoreCloudWarningDialog = true
                        } else {
                            Toast.makeText(context, "يرجى تحديد نسخة احتياطية من القائمة أولاً", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedCloudBackup != null,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("استعادة هذه النسخة المختارة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudBackupsDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Warning confirmation before restoring cloud backup
    if (showRestoreCloudWarningDialog && selectedCloudBackup != null) {
        val backup = selectedCloudBackup!!
        AlertDialog(
            onDismissRequest = { showRestoreCloudWarningDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger) },
            title = { Text("تحذير: استعادة النسخة السحابية", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أنت على وشك استعادة النسخة السحابية المؤرخة (${backup.dateTimeStr}).")
                    Text("سيتم مسح واستبدال كافة البيانات الحالية في التطبيق ببيانات هذه النسخة السحابية.", fontSize = 11.5.sp, color = RoseDanger, fontWeight = FontWeight.Bold)
                    Text("هل أنت متأكد من رغبتك في إتمام الاستعادة؟", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreCloudBackup(backup)
                        showRestoreCloudWarningDialog = false
                        showCloudBackupsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، استعادة واستبدال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreCloudWarningDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // REQUIREMENT 16: Customizable Modular Export Dialog (مع خيار تحديد الكل)
    if (showExportModularDialog) {
        val allModuleKeys = listOf(
            "parts" to "أصناف قطع الغيار وبياناتها",
            "inventory" to "كميات المخزون والرصيد الحالي",
            "customers" to "سجل العملاء وبيانات الاتصال",
            "parties" to "سجل الجهات والأطراف وورش الصيانة",
            "orders" to "طلبيات العملاء وبنودها",
            "payments" to "دفعات وتحصيلات العملاء",
            "supplier_orders" to "طلبيات الموردين وتفاصيلها",
            "supplier_receipts" to "توريدات وإيصالات الموردين",
            "issues" to "سندات الصرف المخزني والمستلمين"
        )
        val selectedExportModules = remember {
            mutableStateListOf<String>().apply { addAll(allModuleKeys.map { it.first }) }
        }

        AlertDialog(
            onDismissRequest = { showExportModularDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تحديد البيانات المراد تصديرها", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Select All / Deselect All Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اختر الأقسام المطلوبة:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    selectedExportModules.clear()
                                    selectedExportModules.addAll(allModuleKeys.map { it.first })
                                }
                            ) {
                                Text("تحديد الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                            TextButton(
                                onClick = { selectedExportModules.clear() }
                            ) {
                                Text("إلغاء التحديد", fontSize = 11.sp, color = RoseDanger)
                            }
                        }
                    }

                    HorizontalDivider()

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allModuleKeys) { (key, label) ->
                            val isChecked = selectedExportModules.contains(key)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedExportModules.remove(key) else selectedExportModules.add(key)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) selectedExportModules.add(key) else selectedExportModules.remove(key)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            onExportCustomData(selectedExportModules.toSet(), true)
                            showExportModularDialog = false
                        },
                        enabled = selectedExportModules.isNotEmpty()
                    ) {
                        Text("تصدير Excel")
                    }
                    Button(
                        onClick = {
                            onExportCustomData(selectedExportModules.toSet(), false)
                            showExportModularDialog = false
                        },
                        enabled = selectedExportModules.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("تصدير JSON")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportModularDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // REQUIREMENT 15: Warning Dialog when clicking "استبدال بالكامل" upon phone import
    if (showImportChoiceDialog && pendingImportJson != null) {
        val json = pendingImportJson!!
        AlertDialog(
            onDismissRequest = {
                showImportChoiceDialog = false
                pendingImportJson = null
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = TechCyan) },
            title = { Text("استيراد نسخة بيانات من الهاتف", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تم تحميل ملف النسخة الاحتياطية بنجاح.")
                    Text("اختر طريقة تطبيق البيانات:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• الدمج (Merge): إضافة السجلات المستوردة فوق البيانات الحالية دون حذف أي شيء.", fontSize = 11.5.sp)
                    Text("• استبدال بالكامل: حذف كل بيانات التطبيق وتعويضها بالملف المستورد.", fontSize = 11.5.sp, color = RoseDanger)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            // Show warning dialog as requested!
                            showOverwriteWarningDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Text("استبدال بالكامل", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            onImportModular(json, true) // Merge
                            showImportChoiceDialog = false
                            pendingImportJson = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("دمج (Merge)", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportChoiceDialog = false
                    pendingImportJson = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // The explicit warning alert required by Requirement 15
    if (showOverwriteWarningDialog && pendingImportJson != null) {
        val json = pendingImportJson!!
        AlertDialog(
            onDismissRequest = { showOverwriteWarningDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(36.dp)) },
            title = { Text("⚠️ تحذير: حذف كافة البيانات الحالية!", fontWeight = FontWeight.Bold, color = RoseDanger) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تنبيه خطير: عند الضغط على استبدال بالكامل، سيتم حذف ومسح كافة البيانات المخزنة حالياً في التطبيق (جميع الأصناف، المخزون، الطلبيات، الدفعات، وسندات الصرف) وتعويضها بالكامل بالبيانات الموجودة في الملف المستورد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "هل أنت متأكد تماماً من رغبتك في حذف كل البيانات المخزنة واستبدالها بالكامل؟",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = RoseDanger
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportModular(json, false) // Overwrite
                        showOverwriteWarningDialog = false
                        showImportChoiceDialog = false
                        pendingImportJson = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، حذف واستبدال بالكامل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteWarningDialog = false }) {
                    Text("إلغاء والتراجع")
                }
            }
        )
    }

    // REQUIREMENT 14: Secondary Device Audit Log Dialog (Only secondary device operations)
    if (showAuditLogsDialog) {
        val secondaryLogs = remember(deviceAuditLogs) {
            deviceAuditLogs.filter { it.deviceId != "DEV-PRIMARY" && (it.deviceId.contains("SEC") || it.deviceName.contains("فرعي") || it.deviceId.startsWith("DEV-")) }
        }
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سجل عمليات الأجهزة الثانوية", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (secondaryLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد عمليات مسجلة من الأجهزة الثانوية حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(secondaryLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.deviceName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                                            Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("${log.actionType}: ${log.details}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("كود الجهاز: ${log.deviceId}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAuditLogsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    Text("إغلاق")
                }
            }
        )
    }

    // REQUIREMENT 11: Email Verification Dialog (التحقق التلقائي بدون فتح البريد وعدم عرض الرمز لأمان أعلى)
    if (showVerifyEmailDialog) {
        var emailInput by remember { mutableStateOf(googleAccountEmail) }
        var codeInput by remember { mutableStateOf("") }
        var codeSent by remember { mutableStateOf(false) }
        var verificationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showVerifyEmailDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = TechCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توثيق البريد الإلكتروني بالسحابة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يتم إرسال رمز التحقق السري تلقائياً إلى بريدك الإلكتروني لتأكيد ملكية الحساب قبل المزامنة.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !codeSent
                    )

                    if (!codeSent) {
                        Button(
                            onClick = {
                                val trimmed = emailInput.trim()
                                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                                if (trimmed.isNotBlank() && emailPattern.matcher(trimmed).matches()) {
                                    onSetGoogleAccountEmail(trimmed)
                                    onSendVerificationCode(trimmed) {
                                        codeSent = true
                                        verificationError = null
                                    }
                                } else {
                                    verificationError = "يرجى كتابة بريد إلكتروني صحيح ومعتمد"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إرسال رمز التحقق تلقائياً إلى بريدي")
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "تم التحقق من صحة البريد وإرسال الرمز تلقائياً وبأمان إلى $emailInput. يرجى إدخال الرمز المكون من 6 أرقام أدناه:",
                                fontSize = 11.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { if (it.length <= 6) codeInput = it },
                            label = { Text("أدخل رمز التحقق (6 أرقام)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                onConfirmVerificationCode(
                                    codeInput.trim(),
                                    {
                                        showVerifyEmailDialog = false
                                        Toast.makeText(context, "تم توثيق وتأكيد البريد الإلكتروني بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    { err ->
                                        verificationError = err
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تأكيد وتوثيق الرمز")
                        }

                        TextButton(
                            onClick = {
                                onSendVerificationCode(emailInput.trim()) {
                                    verificationError = null
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("إعادة إرسال الرمز للبريد تلقائياً", fontSize = 11.sp)
                        }
                    }

                    if (verificationError != null) {
                        Text(verificationError!!, color = RoseDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVerifyEmailDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Google Account Direct Authentication Dialog
    if (showGoogleAuthDialog) {
        var googleInput by remember { mutableStateOf(if (googleAccountEmail.isNotBlank()) googleAccountEmail else tempEmail) }
        var inputError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showGoogleAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("المصادقة بواسطة حساب Google", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تسجيل الدخول والمصادقة المباشرة باستخدام حساب Google المعتمد في جهازك لربط السحابة بأمان تام وبدون الحاجة لرموز OTP.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = googleInput,
                        onValueChange = {
                            googleInput = it
                            inputError = null
                        },
                        label = { Text("بريد حساب Google المعتمد") },
                        placeholder = { Text("example@gmail.com") },
                        isError = inputError != null,
                        supportingText = inputError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = googleInput.trim()
                        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                        if (trimmed.isBlank() || !emailPattern.matcher(trimmed).matches()) {
                            inputError = "يرجى كتابة بريد إلكتروني صحيح لحساب Google"
                            return@Button
                        }
                        onAuthenticateWithGoogle(trimmed)
                        tempEmail = trimmed
                        showGoogleAuthDialog = false
                        Toast.makeText(context, "تمت المصادقة بحساب Google ($trimmed) بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("المصادقة وتأكيد الحساب", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAuthDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // QR Code Device Linking Dialog (Primary host)
    if (showQrLinkDialog) {
        DevicePairingQrDialog(
            isPrimaryDevice = isPrimaryDevice,
            deviceName = appUserName,
            deviceId = if (isPrimaryDevice) "DEV-PRIMARY-MASTER" else "DEV-SECONDARY-CLIENT",
            pairingCode = "AUTOPARTS_LINK:isPrimary=$isPrimaryDevice:dev=$appUserName:time=${System.currentTimeMillis()}",
            onDismiss = { showQrLinkDialog = false }
        )
    }

    // QR Scanner Dialog to pair another device
    if (showPairingScanner) {
        BarcodeScannerDialog(
            title = "مسح كود QR للجهاز المراد ربطه",
            onDismiss = { showPairingScanner = false },
            onBarcodeScanned = { scanned ->
                onPairDeviceFromQr(scanned)
                showPairingScanner = false
            }
        )
    }

    // Rename Connected Device Dialog
    deviceToRename?.let { dev ->
        EditDeviceNameDialog(
            device = dev,
            onDismiss = { deviceToRename = null },
            onConfirm = { newName ->
                onUpdateConnectedDeviceName(dev.deviceId, newName)
                deviceToRename = null
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
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم العميل") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") })
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
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("نوع الجهة (ورشة/مخزن)") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") })
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
            title = { Text("تأكيد إعادة ضبط النظام", fontWeight = FontWeight.Bold, color = RoseDanger) },
            text = { Text("سيتم مسح كافة البيانات المدخلة واستعادة البيانات النموذجية الافتراضية.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDatabase()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، إعادة ضبط", color = Color.White)
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
                    text = "تحديد مدة المزامنة المخصصة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "خيارات سريعة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                    val quickOptions = listOf(
                        "15 دقيقة", "30 دقيقة", "ساعة", "ساعتين", "6 ساعات", "12 ساعة", "24 ساعة"
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
                                        "ساعة" -> "1"
                                        "ساعتين" -> "2"
                                        "6 ساعات" -> "6"
                                        "12 ساعة" -> "12"
                                        "24 ساعة" -> "24"
                                        else -> "30"
                                    }
                                    isHours = opt.contains("ساعة") || opt.contains("ساعات") || opt.contains("ساعتين")
                                },
                                label = { Text(opt, fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "أو أدخل المدة يدوياً:",
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
                            label = { Text("الرقم") },
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
                            if (num == 1) "ساعة" else if (num == 2) "ساعتين" else "$num ساعات"
                        } else {
                            "$num دقيقة"
                        }
                        val customText = "كل $unitStr"
                        onSyncFrequencyChange(5, customText)
                        showCustomDurationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("حفظ المدة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Must Verify Google Email Dialog
    if (showMustVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showMustVerifyDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(32.dp)) },
            title = { Text("توثيق البريد الإلكتروني مطلوب", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يجب توثيق البريد الإلكتروني المعتمد للسحابة أولاً قبل رفع النسخ أو استعادتها لضمان ملكيتك وأمان حسابك.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tempEmail.isBlank()) {
                        Text(
                            text = "يرجى كتابة بريدك الإلكتروني في خانة 'البريد الإلكتروني المعتمد للسحابة' أولاً.",
                            fontSize = 11.5.sp,
                            color = RoseDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMustVerifyDialog = false
                        if (tempEmail.isNotBlank() && tempEmail.contains("@")) {
                            onSendVerificationCode(tempEmail.trim()) {
                                showVerifyEmailDialog = true
                            }
                        } else {
                            Toast.makeText(context, "الرجاء كتابة البريد الإلكتروني في الخانة أولاً", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("توثيق البريد الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMustVerifyDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, "تطبيق قطع غيارpro")
        putExtra(
            Intent.EXTRA_TEXT,
            "تطبيق قطع غيارpro (AutoParts Pro) - هاتف المطور: 771438740"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة تطبيق قطع غيارpro"))
}

@Composable
fun EditDeviceNameDialog(
    device: ConnectedDevice,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(device.deviceName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل اسم الجهاز", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("كود الجهاز: ${device.deviceId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("اسم الجهاز") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onConfirm(nameInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
