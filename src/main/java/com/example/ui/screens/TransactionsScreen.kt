package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.dao.VoucherWithItems
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import com.example.data.repository.StockSnapshot
import com.example.ui.components.SimpleConfirmDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    viewModel: InventoryViewModel,
    snapshot: StockSnapshot?,
    initialTypeFilter: VoucherType? = null
) {
    val context = LocalContext.current
    var selectedTypeFilter by remember { mutableStateOf<VoucherType?>(initialTypeFilter) }
    var selectedWarehouseFilterId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var activeVoucherTypeForCreate by remember { mutableStateOf<VoucherType?>(null) }
    var voucherToEdit by remember { mutableStateOf<VoucherWithItems?>(null) }

    // Deletion states
    var voucherToDelete by remember { mutableStateOf<VoucherWithItems?>(null) }
    var attachmentToDeleteVoucherId by remember { mutableStateOf<Long?>(null) }

    val vouchers = snapshot?.vouchers ?: emptyList()
    val warehouses = snapshot?.warehouses ?: emptyList()
    val items = snapshot?.items ?: emptyList()
    val beneficiaries = viewModel.beneficiaries.value

    val filteredVouchers = remember(vouchers, selectedTypeFilter, selectedWarehouseFilterId, searchQuery) {
        vouchers.filter { vWithItems ->
            val v = vWithItems.voucher
            val matchesType = selectedTypeFilter == null || v.voucherType == selectedTypeFilter
            val matchesWarehouse = selectedWarehouseFilterId == null ||
                v.sourceWarehouseId == selectedWarehouseFilterId ||
                v.destinationWarehouseId == selectedWarehouseFilterId

            val matchesSearch = searchQuery.isBlank() ||
                v.voucherNumber.contains(searchQuery, ignoreCase = true) ||
                v.supplierName.contains(searchQuery, ignoreCase = true) ||
                v.beneficiaryName.contains(searchQuery, ignoreCase = true) ||
                v.notes.contains(searchQuery, ignoreCase = true) ||
                vWithItems.items.any { it.itemName.contains(searchQuery, ignoreCase = true) }

            matchesType && matchesWarehouse && matchesSearch
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeVoucherTypeForCreate = VoucherType.INWARD },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_inward_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("وارد جديد", fontSize = 12.sp)
                }

                Button(
                    onClick = { activeVoucherTypeForCreate = VoucherType.DISBURSEMENT },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_disbursement_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صرف مستفيد", fontSize = 12.sp)
                }

                Button(
                    onClick = { activeVoucherTypeForCreate = VoucherType.TRANSFER },
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_transfer_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحويل مخزني", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث برقم السند، المورد، المستفيد، أو الصنف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vouchers_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips by Voucher Type
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("الكل (${vouchers.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == VoucherType.INWARD,
                        onClick = { selectedTypeFilter = VoucherType.INWARD },
                        label = { Text("أوامر الوارد") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == VoucherType.DISBURSEMENT,
                        onClick = { selectedTypeFilter = VoucherType.DISBURSEMENT },
                        label = { Text("صرف المستفيدين") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == VoucherType.TRANSFER,
                        onClick = { selectedTypeFilter = VoucherType.TRANSFER },
                        label = { Text("التحويلات") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredVouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "لا توجد حركات مسجلة بهذا الفلتر"
                        else "لا توجد نتائج مطابقة للبحث",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredVouchers, key = { it.voucher.id }) { vWithItems ->
                        VoucherCard(
                            vWithItems = vWithItems,
                            warehouses = warehouses,
                            dateFormat = dateFormat,
                            onEdit = { voucherToEdit = vWithItems },
                            onDelete = { voucherToDelete = vWithItems },
                            onDeleteAttachment = {
                                attachmentToDeleteVoucherId = vWithItems.voucher.id
                            },
                            onViewAttachment = { path, type ->
                                val file = File(path)
                                if (file.exists()) {
                                    val mime = if (type == "IMAGE") "image/*" else "application/pdf"
                                    ReportGenerator.viewFile(context, file, mime)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog for creating new voucher
    if (activeVoucherTypeForCreate != null) {
        VoucherFormDialog(
            voucherType = activeVoucherTypeForCreate!!,
            initialVoucherWithItems = null,
            warehouses = warehouses,
            items = items,
            beneficiaries = beneficiaries,
            onAddNewBeneficiary = { ben, onDone ->
                viewModel.saveBeneficiary(ben, onDone)
            },
            onDismiss = { activeVoucherTypeForCreate = null },
            onSave = { voucher, lineItems ->
                viewModel.saveVoucher(voucher, lineItems) {
                    activeVoucherTypeForCreate = null
                }
            }
        )
    }

    // Dialog for editing existing voucher
    if (voucherToEdit != null) {
        VoucherFormDialog(
            voucherType = voucherToEdit!!.voucher.voucherType,
            initialVoucherWithItems = voucherToEdit,
            warehouses = warehouses,
            items = items,
            beneficiaries = beneficiaries,
            onAddNewBeneficiary = { ben, onDone ->
                viewModel.saveBeneficiary(ben, onDone)
            },
            onDismiss = { voucherToEdit = null },
            onSave = { voucher, lineItems ->
                viewModel.saveVoucher(voucher, lineItems) {
                    voucherToEdit = null
                }
            }
        )
    }

    // Confirm Voucher Deletion Dialog
    if (voucherToDelete != null) {
        val v = voucherToDelete!!.voucher
        val actionTitle = when (v.voucherType) {
            VoucherType.INWARD -> "حذف أمر التوريد"
            VoucherType.TRANSFER -> "حذف أمر التحويل"
            VoucherType.DISBURSEMENT -> "حذف أمر الصرف"
        }
        SimpleConfirmDialog(
            title = "تأكيد $actionTitle",
            message = "هل أنت متأكد من حذف السند \"${v.voucherNumber}\"؟ سيتم تحديث رصيد المخزون فورياً بناءً على الحذف.",
            confirmButtonText = "نعم، احذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteVoucher(v.id)
                voucherToDelete = null
            },
            onDismiss = { voucherToDelete = null }
        )
    }

    // Confirm Attachment Deletion Dialog
    if (attachmentToDeleteVoucherId != null) {
        SimpleConfirmDialog(
            title = "تأكيد حذف المرفق",
            message = "هل أنت متأكد من حذف الملف المرفق مع هذا السند؟",
            confirmButtonText = "نعم، احذف المرفق",
            isDestructive = true,
            onConfirm = {
                viewModel.removeAttachment(attachmentToDeleteVoucherId!!)
                attachmentToDeleteVoucherId = null
            },
            onDismiss = { attachmentToDeleteVoucherId = null }
        )
    }
}

@Composable
private fun VoucherCard(
    vWithItems: VoucherWithItems,
    warehouses: List<Warehouse>,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDeleteAttachment: () -> Unit,
    onViewAttachment: (String, String) -> Unit
) {
    val v = vWithItems.voucher
    val badgeColor = when (v.voucherType) {
        VoucherType.INWARD -> SuccessGreen
        VoucherType.TRANSFER -> InfoBlue
        VoucherType.DISBURSEMENT -> WarningOrange
    }
    val typeIcon = when (v.voucherType) {
        VoucherType.INWARD -> Icons.Default.ArrowDownward
        VoucherType.TRANSFER -> Icons.Default.SwapHoriz
        VoucherType.DISBURSEMENT -> Icons.Default.ArrowUpward
    }

    val srcWarehouse = warehouses.find { it.id == v.sourceWarehouseId }?.name ?: "مخزن غير محدد"
    val destWarehouse = warehouses.find { it.id == v.destinationWarehouseId }?.name ?: "مخزن غير محدد"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voucher_card_${v.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(typeIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = v.voucherNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = v.voucherType.titleAr,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = dateFormat.format(Date(v.date)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Parties & Warehouses details
            when (v.voucherType) {
                VoucherType.INWARD -> {
                    Text(
                        text = "المخزن المستلم: $destWarehouse",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (v.supplierName.isNotBlank()) {
                        Text(
                            text = "المورد: ${v.supplierName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (v.receiverName.isNotBlank()) {
                        Text(
                            text = "مستلم المخزن: ${v.receiverName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                VoucherType.TRANSFER -> {
                    Text(
                        text = "من: $srcWarehouse ⬅️ إلى: $destWarehouse",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (v.disburserName.isNotBlank()) {
                        Text(
                            text = "القائم بالتحويل: ${v.disburserName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                VoucherType.DISBURSEMENT -> {
                    Text(
                        text = "مخزن الصرف: $srcWarehouse",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "المستفيد: ${v.beneficiaryName.ifEmpty { "مستفيد غير محدد" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (v.disburserName.isNotBlank()) {
                        Text(
                            text = "القائم بالصرف: ${v.disburserName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Line Items preview
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                vWithItems.items.forEach { vi ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${vi.itemName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${vi.quantity} ${vi.itemUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            if (v.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات: ${v.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attachment view / delete bar
            if (!v.attachmentUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onViewAttachment(v.attachmentUri, v.attachmentType ?: "IMAGE")
                        }
                    ) {
                        Icon(
                            imageVector = if (v.attachmentType == "PDF") Icons.Default.Description else Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مرفق (${if (v.attachmentType == "PDF") "مستند PDF" else "صورة"}) - انقر للمعاينة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDeleteAttachment,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف المرفق",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
