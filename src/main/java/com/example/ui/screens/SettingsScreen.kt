package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun SettingsScreen(
    customers: List<CustomerEntity>,
    parties: List<PartyEntity>,
    parts: List<PartEntity>,
    appUserName: String = "عيسى عبدربه",
    onSetAppUserName: (String) -> Unit = {},
    themeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {},
    googleAccountEmail: String = "issaabdo7368@gmail.com",
    lastCloudSyncTime: String = "لم تتم المزامنة بعد",
    onSyncToCloud: () -> Unit = {},
    onRestoreFromCloud: () -> Unit = {},
    onExportModular: (type: String) -> Unit = {},
    onImportModular: (jsonString: String, merge: Boolean) -> Unit = { _, _ -> },
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
    var showAddPartyDialog by remember { mutableStateOf(false) }
    var partyToEdit by remember { mutableStateOf<PartyEntity?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    var showExportModularDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: APPEARANCE & THEME (مظهر التطبيق)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == "light",
                            onClick = { onThemeModeChange("light") },
                            label = { Text("فاتح (Light)", fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "dark",
                            onClick = { onThemeModeChange("dark") },
                            label = { Text("داكن (Dark)", fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeMode == "system",
                            onClick = { onThemeModeChange("system") },
                            label = { Text("تلقائي النظام", fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // SECTION 2: CLOUD SYNC WITH GOOGLE ACCOUNT (المزامنة السحابية بحساب Google)
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
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "☁️ المزامنة السحابية (Google Sync)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(EmeraldLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("نشط وموثق", fontSize = 10.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "الحساب المرتبط: $googleAccountEmail",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "آخر مزامنة: $lastCloudSyncTime",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                            Text("مزامنة سحابية الآن", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = onRestoreFromCloud,
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة من السحابة", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // SECTION 3: MODULAR EXPORT & IMPORT (نقل البيانات لهاتف آخر)
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
                            onClick = { importPicker.launch("application/json") },
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

        // SECTION 4: SHARE APP (مشاركة التطبيق)
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

        // SECTION 5: CUSTOMERS MANAGEMENT
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
                                IconButton(onClick = { onDeleteCustomer(customer) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // SECTION 6: PARTIES MANAGEMENT
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
                                IconButton(onClick = { onDeleteParty(party) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // SECTION 7: APP USER NAME & REPORT SETTINGS
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

        // SECTION 8: DEFAULT DATA RESET
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

        // SECTION 9: DEVELOPER CREDIT & ABOUT (المطور والتواصل)
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
                        text = "الإصدار 2.0 • Offline-First + مزامنة سحابية بحساب Google",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Export Dialog
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
