package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
    isPinProtected: Boolean,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
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
    var showPinDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: CUSTOMERS MANAGEMENT
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

        // SECTION 2: PARTIES MANAGEMENT (الجهات المصروفة لها)
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

        // SECTION 3: SECURITY & PIN PROTECTION
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
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "قفل التطبيق برمز سري (PIN)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isPinProtected) "الحماية مفعلة" else "غير مفعل",
                                    fontSize = 11.sp,
                                    color = if (isPinProtected) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPinProtected,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    showPinDialog = true
                                } else {
                                    onDisablePin()
                                }
                            }
                        )
                    }
                }
            }
        }

        // SECTION 4: BACKUP & DATA MANAGEMENT
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
                        text = "💾 النسخ الاحتياطي وإدارة البيانات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            exportBackupJson(context, parts, customers, parties)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير نسخة احتياطية من البيانات كـ JSON")
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استعادة البيانات الافتراضية (Seeds)", color = RoseDanger)
                    }
                }
            }
        }

        // SECTION 5: ABOUT APP
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("قطع غيار Pro (AutoParts Pro)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("الإصدار 1.0 - يعمل بالكامل بدون اتصال بالإنترنت (Offline-First)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("دعم كامل للغة العربية والـ Dark Mode والتخزين المحلي فائق السرعة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
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

    // PIN Setup Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("🔒 تعيين رمز PIN للتطبيق", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل رمزاً مكوناً من 4 إلى 6 أرقام لحماية التطبيق:")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it },
                        label = { Text("رمز PIN") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSetPin(pinInput)
                    showPinDialog = false
                }) {
                    Text("تأكيد وحفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

fun exportBackupJson(
    context: Context,
    parts: List<PartEntity>,
    customers: List<CustomerEntity>,
    parties: List<PartyEntity>
) {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"app\": \"AutoParts Pro\",")
    sb.appendLine("  \"version\": \"1.0\",")
    sb.appendLine("  \"exportedAt\": \"${System.currentTimeMillis()}\",")
    sb.appendLine("  \"partsCount\": ${parts.size},")
    sb.appendLine("  \"customersCount\": ${customers.size},")
    sb.appendLine("  \"partiesCount\": ${parties.size}")
    sb.appendLine("}")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "application/json"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة النسخة الاحتياطية"))
}
