package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.dao.VoucherWithItems
import com.example.data.model.Beneficiary
import com.example.data.model.Item
import com.example.data.model.Voucher
import com.example.data.model.VoucherItem
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import com.example.ui.components.SimpleConfirmDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.util.ReportGenerator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherFormDialog(
    voucherType: VoucherType,
    initialVoucherWithItems: VoucherWithItems?,
    warehouses: List<Warehouse>,
    items: List<Item>,
    beneficiaries: List<Beneficiary>,
    onAddNewBeneficiary: (Beneficiary, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Voucher, List<VoucherItem>) -> Unit
) {
    val context = LocalContext.current
    val initialV = initialVoucherWithItems?.voucher

    var voucherNumber by remember {
        mutableStateOf(
            initialV?.voucherNumber ?: when (voucherType) {
                VoucherType.INWARD -> "IN-${System.currentTimeMillis() % 100000}"
                VoucherType.TRANSFER -> "TR-${System.currentTimeMillis() % 100000}"
                VoucherType.DISBURSEMENT -> "OUT-${System.currentTimeMillis() % 100000}"
            }
        )
    }

    var selectedDate by remember {
        mutableStateOf(initialV?.date ?: System.currentTimeMillis())
    }

    var sourceWarehouseId by remember {
        mutableStateOf(initialV?.sourceWarehouseId ?: warehouses.firstOrNull()?.id)
    }

    var destWarehouseId by remember {
        mutableStateOf(
            initialV?.destinationWarehouseId ?: warehouses.getOrNull(1)?.id ?: warehouses.firstOrNull()?.id
        )
    }

    var supplierName by remember { mutableStateOf(initialV?.supplierName ?: "") }
    var receiverName by remember { mutableStateOf(initialV?.receiverName ?: "") }
    var disburserName by remember { mutableStateOf(initialV?.disburserName ?: "") }
    var selectedBeneficiaryId by remember { mutableStateOf(initialV?.beneficiaryId ?: beneficiaries.firstOrNull()?.id) }
    var notes by remember { mutableStateOf(initialV?.notes ?: "") }

    var attachmentUri by remember { mutableStateOf(initialV?.attachmentUri) }
    var attachmentType by remember { mutableStateOf(initialV?.attachmentType) }

    // Voucher Items state
    val lineItems = remember {
        mutableStateListOf<VoucherItem>().apply {
            if (initialVoucherWithItems != null && initialVoucherWithItems.items.isNotEmpty()) {
                addAll(initialVoucherWithItems.items)
            } else if (items.isNotEmpty()) {
                val firstItem = items.first()
                add(
                    VoucherItem(
                        voucherId = 0L,
                        itemId = firstItem.id,
                        itemName = firstItem.name,
                        itemUnit = firstItem.unit,
                        quantity = 1.0,
                        unitPrice = firstItem.purchasePrice
                    )
                )
            }
        }
    }

    var errorMessage by remember { mutableStateOf("") }
    var showDeleteAttachmentDialog by remember { mutableStateOf(false) }
    var showAddBeneficiaryDialog by remember { mutableStateOf(false) }

    // Media pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copied = copyUriToInternalStorage(context, it, "img_${System.currentTimeMillis()}.jpg")
            attachmentUri = copied
            attachmentType = "IMAGE"
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copied = copyUriToInternalStorage(context, it, "doc_${System.currentTimeMillis()}.pdf")
            attachmentUri = copied
            attachmentType = "PDF"
        }
    }

    val dialogTitle = when (voucherType) {
        VoucherType.INWARD -> if (initialV == null) "إضافة أمر توريد وارد جديد" else "تعديل أمر التوريد"
        VoucherType.TRANSFER -> if (initialV == null) "إضافة أمر تحويل بين المخازن" else "تعديل أمر التحويل"
        VoucherType.DISBURSEMENT -> if (initialV == null) "إضافة أمر صرف لمستفيد" else "تعديل أمر الصرف"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voucher Number & Date
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = voucherNumber,
                        onValueChange = { voucherNumber = it },
                        label = { Text("رقم السند/الأمر") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )

                    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(selectedDate))
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("التاريخ") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Warehouse Pickers based on type
                when (voucherType) {
                    VoucherType.INWARD -> {
                        WarehouseDropdown(
                            label = "اسم المخزن المورد إليه *",
                            warehouses = warehouses,
                            selectedId = destWarehouseId,
                            onSelect = { destWarehouseId = it }
                        )

                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("اسم المورد / الشركة الموردة *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { receiverName = it },
                            label = { Text("اسم المستلم في المخزن") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    VoucherType.TRANSFER -> {
                        WarehouseDropdown(
                            label = "مخزن الصرف (المصدر) *",
                            warehouses = warehouses,
                            selectedId = sourceWarehouseId,
                            onSelect = { sourceWarehouseId = it }
                        )

                        WarehouseDropdown(
                            label = "مخزن الاستلام (الوجهة) *",
                            warehouses = warehouses,
                            selectedId = destWarehouseId,
                            onSelect = { destWarehouseId = it }
                        )

                        OutlinedTextField(
                            value = disburserName,
                            onValueChange = { disburserName = it },
                            label = { Text("اسم القائم بالصرف والتحويل") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { receiverName = it },
                            label = { Text("اسم المستلم في مخزن الوجهة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    VoucherType.DISBURSEMENT -> {
                        WarehouseDropdown(
                            label = "مخزن الصرف *",
                            warehouses = warehouses,
                            selectedId = sourceWarehouseId,
                            onSelect = { sourceWarehouseId = it }
                        )

                        // Beneficiary dropdown + "+ إضافة مستفيد جديد"
                        BeneficiaryDropdown(
                            beneficiaries = beneficiaries,
                            selectedId = selectedBeneficiaryId,
                            onSelect = { selectedBeneficiaryId = it },
                            onAddNew = { showAddBeneficiaryDialog = true }
                        )

                        OutlinedTextField(
                            value = disburserName,
                            onValueChange = { disburserName = it },
                            label = { Text("اسم القائم بالصرف") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { receiverName = it },
                            label = { Text("اسم المستلم الفعلي") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Line Items Header & Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأصناف والكميات (${lineItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = {
                            val candidate = items.firstOrNull { i -> lineItems.none { it.itemId == i.id } } ?: items.firstOrNull()
                            if (candidate != null) {
                                lineItems.add(
                                    VoucherItem(
                                        voucherId = 0L,
                                        itemId = candidate.id,
                                        itemName = candidate.name,
                                        itemUnit = candidate.unit,
                                        quantity = 1.0,
                                        unitPrice = candidate.purchasePrice
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة صنف آخر", fontSize = 12.sp)
                    }
                }

                // Line items list
                lineItems.forEachIndexed { index, lineItem ->
                    LineItemEditor(
                        index = index,
                        item = lineItem,
                        allItems = items,
                        onUpdate = { updated -> lineItems[index] = updated },
                        onRemove = {
                            if (lineItems.size > 1) {
                                lineItems.removeAt(index)
                            }
                        },
                        canRemove = lineItems.size > 1
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Attachment Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "المرفقات (صورة أو ملف PDF)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (attachmentUri.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إرفاق صورة", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إرفاق PDF", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (attachmentType == "IMAGE") Icons.Default.Image else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (attachmentType == "IMAGE") "مرفق صورة تم حفظه" else "مرفق مستند PDF تم حفظه",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "انقر للمعاينة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            attachmentUri?.let { path ->
                                                val file = File(path)
                                                if (file.exists()) {
                                                    val mime = if (attachmentType == "IMAGE") "image/*" else "application/pdf"
                                                    ReportGenerator.viewFile(context, file, mime)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "معاينة", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            // Show confirmation dialog before deleting attachment!
                                            showDeleteAttachmentDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف المرفق", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Text(text = errorMessage, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (lineItems.isEmpty()) {
                        errorMessage = "يجب إضافة صنف واحد على الأقل"
                        return@Button
                    }
                    if (voucherType == VoucherType.TRANSFER && sourceWarehouseId == destWarehouseId) {
                        errorMessage = "لا يمكن أن يكون مخزن الصرف ومخزن الاستلام متطابقين"
                        return@Button
                    }

                    val beneficiaryName = beneficiaries.find { it.id == selectedBeneficiaryId }?.name ?: ""

                    val v = (initialV ?: Voucher(
                        voucherNumber = voucherNumber,
                        voucherType = voucherType
                    )).copy(
                        voucherNumber = voucherNumber.trim(),
                        voucherType = voucherType,
                        date = selectedDate,
                        sourceWarehouseId = sourceWarehouseId,
                        destinationWarehouseId = destWarehouseId,
                        supplierName = supplierName.trim(),
                        receiverName = receiverName.trim(),
                        beneficiaryId = selectedBeneficiaryId,
                        beneficiaryName = beneficiaryName,
                        disburserName = disburserName.trim(),
                        notes = notes.trim(),
                        attachmentUri = attachmentUri,
                        attachmentType = attachmentType
                    )
                    onSave(v, lineItems.toList())
                },
                modifier = Modifier.testTag("save_voucher_btn")
            ) {
                Text("حفظ السند")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )

    // Separate Confirmation Dialog for Deleting Attachment
    if (showDeleteAttachmentDialog) {
        SimpleConfirmDialog(
            title = "تأكيد حذف المرفق",
            message = "هل أنت متأكد من حذف الملف المرفق من هذا السند؟",
            confirmButtonText = "نعم، احذف المرفق",
            isDestructive = true,
            onConfirm = {
                attachmentUri = null
                attachmentType = null
                showDeleteAttachmentDialog = false
            },
            onDismiss = { showDeleteAttachmentDialog = false }
        )
    }

    // Inline Add Beneficiary Dialog
    if (showAddBeneficiaryDialog) {
        AddBeneficiaryQuickDialog(
            onDismiss = { showAddBeneficiaryDialog = false },
            onSave = { ben ->
                onAddNewBeneficiary(ben) { newId ->
                    selectedBeneficiaryId = newId
                    showAddBeneficiaryDialog = false
                }
            }
        )
    }
}

@Composable
private fun LineItemEditor(
    index: Int,
    item: VoucherItem,
    allItems: List<Item>,
    onUpdate: (VoucherItem) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الصنف #${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "حذف", tint = DangerRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Item selection dropdown
            ItemDropdown(
                items = allItems,
                selectedId = item.itemId,
                onSelect = { selectedItem ->
                    onUpdate(
                        item.copy(
                            itemId = selectedItem.id,
                            itemName = selectedItem.name,
                            itemUnit = selectedItem.unit,
                            unitPrice = selectedItem.purchasePrice
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (item.quantity == 0.0) "" else item.quantity.toString(),
                    onValueChange = {
                        val q = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(quantity = q))
                    },
                    label = { Text("الكمية (${item.itemUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                    onValueChange = {
                        val p = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPrice = p))
                    },
                    label = { Text("سعر الوحدة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehouseDropdown(
    label: String,
    warehouses: List<Warehouse>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedWarehouse = warehouses.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedWarehouse?.name ?: "اختر المخزن",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            warehouses.forEach { w ->
                DropdownMenuItem(
                    text = { Text(w.name) },
                    onClick = {
                        onSelect(w.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDropdown(
    items: List<Item>,
    selectedId: Long,
    onSelect: (Item) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = items.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = current?.name ?: "اختر الصنف",
            onValueChange = {},
            readOnly = true,
            label = { Text("اسم الصنف") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { itm ->
                DropdownMenuItem(
                    text = { Text("${itm.name} (${itm.unit})") },
                    onClick = {
                        onSelect(itm)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeneficiaryDropdown(
    beneficiaries: List<Beneficiary>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = beneficiaries.find { it.id == selectedId }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = current?.name ?: "اختر المستفيد من القائمة",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اسم المستفيد *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    beneficiaries.forEach { ben ->
                        DropdownMenuItem(
                            text = { Text("${ben.name} - ${ben.department}") },
                            onClick = {
                                onSelect(ben.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onAddNew,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .size(44.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مستفيد جديد", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AddBeneficiaryQuickDialog(
    onDismiss: () -> Unit,
    onSave: (Beneficiary) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مستفيد جديد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = { Text("اسم المستفيد *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("القسم / الجهة المستفيدة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (error.isNotBlank()) {
                    Text(error, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "يرجى كتابة اسم المستفيد"
                        return@Button
                    }
                    onSave(
                        Beneficiary(
                            name = name.trim(),
                            department = department.trim(),
                            phone = phone.trim()
                        )
                    )
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

private fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String {
    val attachmentsDir = File(context.filesDir, "attachments")
    if (!attachmentsDir.exists()) attachmentsDir.mkdirs()
    val destFile = File(attachmentsDir, fileName)
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
            input.copyTo(output)
        }
    }
    return destFile.absolutePath
}
