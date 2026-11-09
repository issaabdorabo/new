package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Item
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import com.example.data.model.WarehouseItemStock
import com.example.data.repository.StockSnapshot
import com.example.ui.components.SimpleConfirmDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseDetailsScreen(
    warehouse: Warehouse,
    viewModel: InventoryViewModel,
    snapshot: StockSnapshot?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var sortByStock by remember { mutableStateOf(false) } // false = alphabetical, true = by stock descending

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showDisbursementDialog by remember { mutableStateOf(false) }

    // Edit item stock inside warehouse
    var itemToAdjust by remember { mutableStateOf<WarehouseItemStock?>(null) }
    var itemToDeleteFromWarehouse by remember { mutableStateOf<WarehouseItemStock?>(null) }

    val rawItemsInWarehouse = snapshot?.warehouseItemStocks?.filter { it.warehouseId == warehouse.id } ?: emptyList()
    val allCatalogItems = snapshot?.items ?: emptyList()
    val beneficiaries by viewModel.beneficiaries.collectAsState()

    val filteredItems = remember(rawItemsInWarehouse, searchQuery, sortByStock) {
        val list = if (searchQuery.isBlank()) rawItemsInWarehouse
        else rawItemsInWarehouse.filter {
            it.itemName.contains(searchQuery, ignoreCase = true)
        }

        if (sortByStock) {
            list.sortedByDescending { it.currentStock }
        } else {
            list.sortedBy { it.itemName.lowercase(Locale.getDefault()) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = warehouse.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أمين المخزن: ${warehouse.managerName.ifBlank { "غير محدد" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Action Buttons Row: Add Item To Warehouse & Quick Disbursement
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("warehouse_add_item_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة صنف للمخزن", fontSize = 12.sp)
                }

                Button(
                    onClick = { showDisbursementDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("warehouse_disbursement_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صرف من المخزن", fontSize = 12.sp)
                }
            }

            // Search Bar & Sort Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث عن صنف داخل هذا المخزن...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("warehouse_items_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                FilterChip(
                    selected = sortByStock,
                    onClick = { sortByStock = !sortByStock },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(if (sortByStock) "حسب المخزون" else "أبجدياً (أ-ي)", fontSize = 11.sp)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "لا توجد أصناف في هذا المخزن بعد. اضغط \"إضافة صنف للمخزن\"."
                            else "لا توجد نتائج تطابق بحثك",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredItems, key = { it.itemId }) { itemStock ->
                        WarehouseItemCard(
                            itemStock = itemStock,
                            onEdit = { itemToAdjust = itemStock },
                            onDelete = { itemToDeleteFromWarehouse = itemStock }
                        )
                    }
                }
            }
        }
    }

    // Add Item Directly To Warehouse Dialog
    if (showAddItemDialog) {
        AddItemToWarehouseDialog(
            catalogItems = allCatalogItems,
            onDismiss = { showAddItemDialog = false },
            onAdd = { itemId, quantity, minStockAlert ->
                viewModel.addItemDirectlyToWarehouse(warehouse.id, itemId, quantity, minStockAlert) {
                    showAddItemDialog = false
                }
            }
        )
    }

    // Adjust Item Stock Dialog (Requires Mandatory Reason Note)
    if (itemToAdjust != null) {
        AdjustStockQuantityDialog(
            itemStock = itemToAdjust!!,
            onDismiss = { itemToAdjust = null },
            onConfirm = { newQty, reasonNote ->
                viewModel.adjustWarehouseStock(
                    warehouseId = warehouse.id,
                    itemId = itemToAdjust!!.itemId,
                    newQuantity = newQty,
                    reasonNote = reasonNote
                ) {
                    itemToAdjust = null
                }
            }
        )
    }

    // Delete item from warehouse confirmation
    if (itemToDeleteFromWarehouse != null) {
        SimpleConfirmDialog(
            title = "تأكيد حذف الصنف من المخزن",
            message = "هل أنت متأكد من تصفير وحذف صنف \"${itemToDeleteFromWarehouse!!.itemName}\" من مخزن \"${warehouse.name}\"؟",
            confirmButtonText = "نعم، احذف الصنف",
            isDestructive = true,
            onConfirm = {
                viewModel.adjustWarehouseStock(
                    warehouseId = warehouse.id,
                    itemId = itemToDeleteFromWarehouse!!.itemId,
                    newQuantity = 0.0,
                    reasonNote = "حذف وتصفير الصنف من المخزن"
                ) {
                    itemToDeleteFromWarehouse = null
                }
            },
            onDismiss = { itemToDeleteFromWarehouse = null }
        )
    }

    // Disbursement Voucher Dialog with Fixed Warehouse
    if (showDisbursementDialog) {
        var autoVoucherNumber by remember { mutableStateOf("") }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            autoVoucherNumber = viewModel.getNextVoucherNumber(VoucherType.DISBURSEMENT)
        }

        if (autoVoucherNumber.isNotBlank()) {
            VoucherFormDialog(
                voucherType = VoucherType.DISBURSEMENT,
                initialVoucherWithItems = null,
                fixedSourceWarehouseId = warehouse.id,
                fixedVoucherNumber = autoVoucherNumber,
                warehouses = listOf(warehouse),
                items = allCatalogItems,
                beneficiaries = beneficiaries,
                onAddNewBeneficiary = { ben, onDone ->
                    viewModel.saveBeneficiary(ben, onDone)
                },
                onDismiss = { showDisbursementDialog = false },
                onSave = { voucher, lineItems ->
                    viewModel.saveVoucher(voucher, lineItems) {
                        showDisbursementDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun WarehouseItemCard(
    itemStock: WarehouseItemStock,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOutOfStock = itemStock.isOutOfStock
    val isLowStock = itemStock.isLowStock

    val badgeColor = when {
        isOutOfStock -> DangerRed
        isLowStock -> WarningOrange
        else -> SuccessGreen
    }

    val badgeText = when {
        isOutOfStock -> "نفدت الكمية"
        isLowStock -> "بحاجة طلب"
        else -> "متوفر"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("warehouse_item_card_${itemStock.itemId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            Icon(
                                imageVector = if (isOutOfStock || isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = itemStock.itemName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (itemStock.unitsPerPackage > 1.0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${itemStock.unitsPerPackage.toInt()} ${itemStock.itemUnit} لكل ${itemStock.packageUnit})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الكمية", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الرصيد الفعلي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val formattedQty = if (itemStock.unitsPerPackage > 1.0 && itemStock.currentStock >= itemStock.unitsPerPackage) {
                        val boxes = (itemStock.currentStock / itemStock.unitsPerPackage).toInt()
                        val remPieces = (itemStock.currentStock % itemStock.unitsPerPackage).toInt()
                        if (remPieces > 0) "$boxes ${itemStock.packageUnit} + $remPieces ${itemStock.itemUnit}"
                        else "$boxes ${itemStock.packageUnit} (${itemStock.currentStock.toInt()} ${itemStock.itemUnit})"
                    } else {
                        "${itemStock.currentStock.toInt()} ${itemStock.itemUnit}"
                    }
                    Text(formattedQty, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = badgeColor)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("حد إعادة الطلب", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${itemStock.minStockLevel.toInt()} ${itemStock.itemUnit}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("سعر الوحدة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.2f $".format(itemStock.purchasePrice), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemToWarehouseDialog(
    catalogItems: List<Item>,
    onDismiss: () -> Unit,
    onAdd: (Long, Double, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(catalogItems.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var minStockText by remember { mutableStateOf(selectedItem?.minStockLevel?.toInt()?.toString() ?: "5") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة صنف داخل المخزن", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dropdown to pick item
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "اختر الصنف من القائمة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصنف *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        catalogItems.forEach { itm ->
                            DropdownMenuItem(
                                text = { Text("${itm.name} (${itm.unit})") },
                                onClick = {
                                    selectedItem = itm
                                    minStockText = itm.minStockLevel.toInt().toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        // Integer only without decimals as requested
                        quantityText = it.filter { char -> char.isDigit() }
                        error = ""
                    },
                    label = { Text("الكمية (${selectedItem?.unit ?: "حبة"}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = minStockText,
                    onValueChange = {
                        minStockText = it.filter { char -> char.isDigit() }
                        error = ""
                    },
                    label = { Text("حد إعادة الطلب *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val itm = selectedItem
                    if (itm == null) {
                        error = "يرجى اختيار صنف"
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    if (qty <= 0) {
                        error = "يرجى إدخال كمية صحيحة أكبر من صفر"
                        return@Button
                    }
                    val minStock = minStockText.toDoubleOrNull() ?: 5.0
                    onAdd(itm.id, qty, minStock)
                }
            ) {
                Text("إضافة للمخزن")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun AdjustStockQuantityDialog(
    itemStock: WarehouseItemStock,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var newQtyText by remember { mutableStateOf(itemStock.currentStock.toInt().toString()) }
    var reasonNote by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تعديل كمية المخزون: ${itemStock.itemName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الرصيد الحالي: ${itemStock.currentStock.toInt()} ${itemStock.itemUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newQtyText,
                    onValueChange = {
                        newQtyText = it.filter { char -> char.isDigit() }
                        error = ""
                    },
                    label = { Text("الكمية الفعلية الجديدة *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reasonNote,
                    onValueChange = {
                        reasonNote = it
                        error = ""
                    },
                    label = { Text("سبب تعديل المخزون (إجباري) *") },
                    placeholder = { Text("مثال: تسوية جردية، تصحيح إدخال سابق، تلف...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    isError = error.isNotBlank()
                )

                if (error.isNotBlank()) {
                    Text(error, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = newQtyText.toDoubleOrNull()
                    if (qty == null) {
                        error = "يرجى إدخال كمية صحيحة"
                        return@Button
                    }
                    if (reasonNote.isBlank()) {
                        error = "إضافة سبب تعديل المخزون إجباري ومطلوب!"
                        return@Button
                    }
                    onConfirm(qty, reasonNote.trim())
                }
            ) {
                Text("تأكيد التعديل")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
