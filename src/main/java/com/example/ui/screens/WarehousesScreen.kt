package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Warehouse
import com.example.data.repository.StockSnapshot
import com.example.ui.components.DoubleWarningConfirmDialog
import com.example.ui.components.SimpleConfirmDialog
import com.example.ui.theme.DangerRed
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@Composable
fun WarehousesScreen(
    viewModel: InventoryViewModel,
    snapshot: StockSnapshot?
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var warehouseToEdit by remember { mutableStateOf<Warehouse?>(null) }

    // Deletion confirmation states
    var warehouseToDelete by remember { mutableStateOf<Warehouse?>(null) }
    var showSimpleDeleteDialog by remember { mutableStateOf(false) }
    var doubleConfirmStep by remember { mutableIntStateOf(0) } // 0 = off, 1 = first warning, 2 = final warning

    val warehouses = snapshot?.warehouses ?: emptyList()
    val filteredWarehouses = remember(warehouses, searchQuery) {
        if (searchQuery.isBlank()) warehouses
        else warehouses.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.managerName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    warehouseToEdit = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_warehouse_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مخزن جديد")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن اسم المخزن، الموقع، أو المسؤول...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("warehouse_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredWarehouses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "لا توجد مخازن مضافة بعد. اضغط + لإضافة مخزن."
                            else "لا توجد نتائج تطابق بحثك",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWarehouses, key = { it.id }) { warehouse ->
                        val summary = snapshot?.warehouseSummaries?.find { it.warehouseId == warehouse.id }
                        WarehouseCard(
                            warehouse = warehouse,
                            totalStock = summary?.totalQuantity ?: 0.0,
                            itemsCount = summary?.totalItemsCount ?: 0,
                            totalValue = summary?.totalValue ?: 0.0,
                            onEdit = {
                                warehouseToEdit = warehouse
                                showEditDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    val hasStock = viewModel.checkWarehouseHasStock(warehouse.id)
                                    warehouseToDelete = warehouse
                                    if (hasStock) {
                                        // Trigger Double Warning Confirm!
                                        doubleConfirmStep = 1
                                    } else {
                                        showSimpleDeleteDialog = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Warehouse Dialog
    if (showEditDialog) {
        WarehouseFormDialog(
            initialWarehouse = warehouseToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { warehouse ->
                viewModel.saveWarehouse(warehouse) {
                    showEditDialog = false
                }
            }
        )
    }

    // Single simple delete confirmation (when warehouse has NO stock)
    if (showSimpleDeleteDialog && warehouseToDelete != null) {
        SimpleConfirmDialog(
            title = "تأكيد حذف المخزن",
            message = "هل أنت متأكد من حذف مخزن \"${warehouseToDelete!!.name}\"؟ لا توجد به بضائع حالياً.",
            confirmButtonText = "نعم، احذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteWarehouse(warehouseToDelete!!)
                showSimpleDeleteDialog = false
                warehouseToDelete = null
            },
            onDismiss = {
                showSimpleDeleteDialog = false
                warehouseToDelete = null
            }
        )
    }

    // Double Confirmation Warning Dialog (when warehouse HAS stock!)
    if (doubleConfirmStep > 0 && warehouseToDelete != null) {
        DoubleWarningConfirmDialog(
            step = doubleConfirmStep,
            itemName = warehouseToDelete!!.name,
            reasonText = "هذا المخزن يحتوي حالياً على بضائع ومخزون فعلي مسجل! حذفه سيؤثر على حركة وأرصدة البضائع.",
            onProceedToStepTwo = {
                doubleConfirmStep = 2
            },
            onFinalConfirm = {
                viewModel.deleteWarehouse(warehouseToDelete!!)
                doubleConfirmStep = 0
                warehouseToDelete = null
            },
            onDismiss = {
                doubleConfirmStep = 0
                warehouseToDelete = null
            }
        )
    }
}

@Composable
private fun WarehouseCard(
    warehouse: Warehouse,
    totalStock: Double,
    itemsCount: Int,
    totalValue: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("warehouse_card_${warehouse.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = warehouse.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (warehouse.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = warehouse.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_warehouse_${warehouse.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_warehouse_${warehouse.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warehouse Manager & Phone
            if (warehouse.managerName.isNotBlank() || warehouse.phone.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (warehouse.managerName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(warehouse.managerName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (warehouse.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(warehouse.phone, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Stock Stats Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي الرصيد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.0f وحدة".format(totalStock), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الأصناف المتوفرة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$itemsCount صنف", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي القيمة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.0f $".format(totalValue), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WarehouseFormDialog(
    initialWarehouse: Warehouse?,
    onDismiss: () -> Unit,
    onSave: (Warehouse) -> Unit
) {
    var name by remember { mutableStateOf(initialWarehouse?.name ?: "") }
    var location by remember { mutableStateOf(initialWarehouse?.location ?: "") }
    var managerName by remember { mutableStateOf(initialWarehouse?.managerName ?: "") }
    var phone by remember { mutableStateOf(initialWarehouse?.phone ?: "") }
    var notes by remember { mutableStateOf(initialWarehouse?.notes ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialWarehouse == null) "إضافة مخزن جديد" else "تعديل بيانات المخزن",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    label = { Text("اسم المخزن *") },
                    isError = errorMessage.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("warehouse_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("موقع المخزن / العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("اسم أمين / مدير المخزن") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف المخزن") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "يرجى إدخال اسم المخزن"
                        return@Button
                    }
                    val updated = (initialWarehouse ?: Warehouse(name = name)).copy(
                        name = name.trim(),
                        location = location.trim(),
                        managerName = managerName.trim(),
                        phone = phone.trim(),
                        notes = notes.trim()
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_warehouse_btn")
            ) {
                Text("حفظ المخزن")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
