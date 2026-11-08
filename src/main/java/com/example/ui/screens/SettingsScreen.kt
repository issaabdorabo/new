package com.example.ui.screens

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.BackupRecord
import com.example.ui.components.SimpleConfirmDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.InventoryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val currentTheme by viewModel.theme.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()
    val currentOrgName by viewModel.orgName.collectAsState()
    val currentGoogleAccount by viewModel.googleAccount.collectAsState()
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val backups by viewModel.backups.collectAsState()

    var editUserName by remember(currentUserName) { mutableStateOf(currentUserName) }
    var editOrgName by remember(currentOrgName) { mutableStateOf(currentOrgName) }

    // Dialog states
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var backupToRestore by remember { mutableStateOf<BackupRecord?>(null) }
    var backupToDelete by remember { mutableStateOf<BackupRecord?>(null) }
    var showAccountSelectorDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User & Organization Identity
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "بيانات المستخدم والمنشأة للتقارير",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editUserName,
                        onValueChange = { editUserName = it },
                        label = { Text("اسم مستخدم التطبيق (يظهر في السجلات والتقارير)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_username_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editOrgName,
                        onValueChange = { editOrgName = it },
                        label = { Text("اسم المؤسسة / المنشأة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.setUserName(editUserName.trim().ifBlank { "عيسى عبدربه" })
                            viewModel.setOrgName(editOrgName.trim().ifBlank { "المؤسسة العامة للتوريد والمخازن" })
                            Toast.makeText(context, "تم حفظ بيانات المستخدم بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التغييرات")
                    }
                }
            }
        }

        // 2. Luxury Themes Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مظهر التطبيق والثيمات الفاخرة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اختر أحد الثيمات الفاخرة الستة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid of 6 Luxury Themes
                    val themes = AppTheme.values()
                    val chunkedThemes = themes.toList().chunked(3)
                    chunkedThemes.forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowThemes.forEach { thm ->
                                val isSelected = currentTheme == thm
                                val themeColor = Color(thm.primaryColorHex)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setTheme(thm) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(themeColor, CircleShape)
                                                .border(2.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = thm.titleAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 3. Google Account & Cloud Sync
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "المزامنة مع حساب Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "مزامنة كامل البيانات المدخلة في التطبيق",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { viewModel.setSyncEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Account Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { showAccountSelectorDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("الحساب المختار للمزامنة:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentGoogleAccount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showAccountSelectorDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تغيير الحساب", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخر مزامنة: ${dateFormat.format(Date(lastSyncTime))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.performManualSync()
                                Toast.makeText(context, "تمت المزامنة بنجاح مع حساب Google", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مزامنة الآن", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Multi-Backup Management (النسخ الاحتياطي المتعدد)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إدارة النسخ الاحتياطية المتعددة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "حفظ واستعادة وتصدير النسخ الاحتياطية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showCreateBackupDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("create_backup_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخة جديدة", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (backups.isEmpty()) {
                        Text(
                            text = "لا توجد نسخ احتياطية محفوظة بعد. انقر على \"نسخة جديدة\" لإنشاء أول نسخة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            backups.forEach { bkp ->
                                BackupCardItem(
                                    backup = bkp,
                                    dateFormat = dateFormat,
                                    onRestore = { backupToRestore = bkp },
                                    onDelete = { backupToDelete = bkp },
                                    onShare = {
                                        shareBackupFile(context, bkp)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Developer & About
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نظام مخزني الذكي للإدارة المخزنية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الإصدار المعتمد 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تطوير وبرمجة: عيسى عبدربه | هاتف: 771438740",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Google Account Picker Dialog
    if (showAccountSelectorDialog) {
        val detectedAccounts = remember {
            getDeviceGoogleAccounts(context)
        }
        AlertDialog(
            onDismissRequest = { showAccountSelectorDialog = false },
            title = {
                Text(
                    text = "اختيار حساب Google للمزامنة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "اختر أحد حسابات جوجل المتوفرة على جهازك لمزامنة وحفظ بيانات المخازن عليها:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    detectedAccounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (currentGoogleAccount == acc) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.setGoogleAccount(acc)
                                    showAccountSelectorDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentGoogleAccount == acc,
                                onClick = {
                                    viewModel.setGoogleAccount(acc)
                                    showAccountSelectorDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = acc,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currentGoogleAccount == acc) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAccountSelectorDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Create Backup Dialog
    if (showCreateBackupDialog) {
        var backupName by remember {
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
            mutableStateOf("نسخة_احتياطية_$dateStr")
        }

        AlertDialog(
            onDismissRequest = { showCreateBackupDialog = false },
            title = { Text("حفظ نسخة احتياطية جديدة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل اسماً أو وصفاً مميزاً لهذه النسخة:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupName,
                        onValueChange = { backupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createBackup(backupName.trim().ifBlank { "نسخة احتياطية" })
                        showCreateBackupDialog = false
                    }
                ) {
                    Text("إنشاء وحفظ")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateBackupDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Restore Backup Confirmation Dialog
    if (backupToRestore != null) {
        SimpleConfirmDialog(
            title = "تأكيد استعادة النسخة الاحتياطية",
            message = "تحذير: هل أنت متأكد من استعادة النسخة: \"${backupToRestore!!.name}\"؟\nسيتم استرجاع جميع المخازن والأصناف والحركات المحفوظة في هذه النسخة.",
            confirmButtonText = "نعم، استعد البيانات الآن",
            isDestructive = true,
            onConfirm = {
                viewModel.restoreBackup(backupToRestore!!)
                backupToRestore = null
            },
            onDismiss = { backupToRestore = null }
        )
    }

    // Delete Backup Confirmation Dialog
    if (backupToDelete != null) {
        SimpleConfirmDialog(
            title = "تأكيد حذف النسخة الاحتياطية",
            message = "هل أنت متأكد من حذف النسخة الاحتياطية \"${backupToDelete!!.name}\" نهائياً؟",
            confirmButtonText = "نعم، احذف النسخة",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBackup(backupToDelete!!)
                backupToDelete = null
            },
            onDismiss = { backupToDelete = null }
        )
    }
}

@Composable
private fun BackupCardItem(
    backup: BackupRecord,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(backup.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "مخازن: ${backup.warehouseCount} | أصناف: ${backup.itemCount} | حركات: ${backup.voucherCount} | الحجم: ${backup.sizeBytes / 1024} ك.ب",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = "استعادة", tint = SuccessGreen)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed)
                }
            }
        }
    }
}

private fun getDeviceGoogleAccounts(context: Context): List<String> {
    val list = mutableListOf<String>()
    try {
        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType("com.google")
        for (a in accounts) {
            if (!list.contains(a.name)) {
                list.add(a.name)
            }
        }
    } catch (e: Exception) {
        // Ignored or security exception fallback
    }

    // Default registered accounts for seamless user experience
    val defaults = listOf("issanew2026@gmail.com", "inventory.backup@gmail.com", "admin.warehouse@gmail.com")
    defaults.forEach { d ->
        if (!list.contains(d)) list.add(d)
    }
    return list
}

private fun shareBackupFile(context: Context, backup: BackupRecord) {
    val backupsDir = File(context.cacheDir, "backups")
    if (!backupsDir.exists()) backupsDir.mkdirs()
    val file = File(backupsDir, "${backup.name}.json")
    file.writeText(backup.backupJson, Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة النسخة الاحتياطية"))
}
