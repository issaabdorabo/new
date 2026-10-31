package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PartEntity
import com.example.data.model.PartWithInventory
import com.example.data.model.StockStatus
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.theme.WarningLight
import com.example.ui.theme.WarningYellow

@Composable
fun InventoryScreen(
    partsWithInventory: List<PartWithInventory>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCarFilter: String?,
    onCarFilterSelected: (String?) -> Unit,
    showLowStockOnly: Boolean,
    onToggleLowStockOnly: () -> Unit,
    onIssuePart: (PartWithInventory) -> Unit,
    onAdjustStock: (partId: Long, newQty: Int, note: String) -> Unit,
    onUpdatePart: (PartEntity) -> Unit,
    onDeletePart: (PartEntity) -> Unit,
    onDeleteCarType: (String) -> Unit,
    onAddNewPart: () -> Unit
) {
    var partToAdjust by remember { mutableStateOf<PartWithInventory?>(null) }
    var partToEdit by remember { mutableStateOf<PartEntity?>(null) }
    var partToDelete by remember { mutableStateOf<PartEntity?>(null) }
    var showManageCarTypesDialog by remember { mutableStateOf(false) }

    // Unique car types for chips
    val distinctCarTypes = remember(partsWithInventory) {
        partsWithInventory.map { it.part.carType }.distinct().filter { it.isNotBlank() }
    }
    val allCarTypes = remember(distinctCarTypes) {
        listOf("الكل") + distinctCarTypes
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Header & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📦 سجل المخزون والأصناف",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${partsWithInventory.size} صنف مسجل في المستودع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showManageCarTypesDialog = true },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أنواع السيارات", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onAddNewPart,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صنف", fontSize = 11.sp)
                    }
                }
            }

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_inventory_search"),
                placeholder = { Text("بحث باسم القطعة، نوع السيارة، الموديل...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter Chips Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فلترة حسب نوع السيارة:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilterChip(
                        selected = showLowStockOnly,
                        onClick = onToggleLowStockOnly,
                        label = { Text("⚠️ النواقص فقط", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseLight,
                            selectedLabelColor = RoseDanger
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCarTypes) { carType ->
                        val isSelected = if (carType == "الكل") selectedCarFilter == null else selectedCarFilter == carType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (carType == "الكل") {
                                    onCarFilterSelected(null)
                                } else {
                                    onCarFilterSelected(carType)
                                }
                            },
                            label = { Text(carType, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            // Inventory Parts List
            if (partsWithInventory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد أصناف مطابقة للبحث أو الفلتر")
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onAddNewPart) {
                            Text("➕ إضافة صنف جديد")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(partsWithInventory) { item ->
                        PartInventoryCard(
                            item = item,
                            onIssue = { onIssuePart(item) },
                            onAdjust = { partToAdjust = item },
                            onEdit = { partToEdit = item.part },
                            onDelete = { partToDelete = item.part }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddNewPart,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_part"),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة صنف")
        }
    }

    // Adjust Stock Dialog
    partToAdjust?.let { item ->
        AdjustStockDialog(
            part = item.part,
            currentQty = item.availableQty,
            onDismiss = { partToAdjust = null },
            onConfirm = { newQty, note ->
                onAdjustStock(item.part.id, newQty, note)
                partToAdjust = null
            }
        )
    }

    // Edit Part Details Dialog
    partToEdit?.let { part ->
        EditPartDetailsDialog(
            part = part,
            onDismiss = { partToEdit = null },
            onConfirm = { updated ->
                onUpdatePart(updated)
                partToEdit = null
            }
        )
    }

    // Delete Part Confirmation Dialog
    partToDelete?.let { part ->
        AlertDialog(
            onDismissRequest = { partToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف الصنف نهائياً", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف الصنف (${part.name})؟ سيتم حذف رصيد المخزون المرتبط به وسجلات الطلبات الخاصة بهذا الصنف.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePart(part)
                        partToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("تأكيد الحذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { partToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Manage & Delete Car Types Dialog
    if (showManageCarTypesDialog) {
        ManageCarTypesDialog(
            carTypes = distinctCarTypes,
            parts = partsWithInventory.map { it.part },
            onDismiss = { showManageCarTypesDialog = false },
            onDeleteCarType = { carType ->
                onDeleteCarType(carType)
                if (selectedCarFilter == carType) {
                    onCarFilterSelected(null)
                }
            }
        )
    }
}

@Composable
fun PartInventoryCard(
    item: PartWithInventory,
    onIssue: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val part = item.part
    val qty = item.availableQty
    val minStock = part.minStock

    val (statusBg, statusText, statusColor) = when (item.stockStatus) {
        StockStatus.OUT_OF_STOCK -> Triple(RoseLight, "منتهي بالمخزون", RoseDanger)
        StockStatus.LOW_STOCK -> Triple(RoseLight, "ناقص (تحت الحد)", RoseDanger)
        StockStatus.MEDIUM_STOCK -> Triple(WarningLight, "متوسط", WarningYellow)
        StockStatus.GOOD -> Triple(EmeraldLight, "متوفر بشكل كافي", EmeraldSuccess)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = part.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${part.carType} • ${part.carModel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities and Price stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("الكمية المتوفرة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$qty قطعة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (qty <= minStock) RoseDanger else PrimaryBlue
                    )
                }

                Column {
                    Text("الحد الأدنى", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$minStock",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text("سعر البيع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${part.price} ر.س",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AmberSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Issue, Adjust, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Issue Stock
                Button(
                    onClick = onIssue,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCartCheckout,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صرف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Adjust Quantity
                OutlinedButton(
                    onClick = onAdjust,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("تعديل", fontSize = 11.sp)
                }

                // Edit Details
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل البيانات", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }

                // Delete Part
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = RoseDanger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ManageCarTypesDialog(
    carTypes: List<String>,
    parts: List<PartEntity>,
    onDismiss: () -> Unit,
    onDeleteCarType: (String) -> Unit
) {
    var carTypeToDeleteConfirm by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إدارة وحذف أنواع السيارات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "قائمة أنواع ومصنعي السيارات المعرفة. حذف أي نوع سيؤدي لحذف الأصناف التابعة له من المستودع:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (carTypes.isEmpty()) {
                    Text("لا توجد أنواع سيارات مسجلة", fontSize = 13.sp)
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(carTypes) { type ->
                            val partsCount = parts.count { it.carType.equals(type, ignoreCase = true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(type, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$partsCount صنف مسجل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(
                                    onClick = { carTypeToDeleteConfirm = type },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف النوع", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )

    carTypeToDeleteConfirm?.let { carType ->
        val partsCount = parts.count { it.carType.equals(carType, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { carTypeToDeleteConfirm = null },
            title = {
                Text("تأكيد حذف نوع السيارة: $carType", fontWeight = FontWeight.Bold, color = RoseDanger)
            },
            text = {
                Text("هل أنت متأكد من حذف ($carType)؟ سيتم حذف جميع الأصناف التابعة له ($partsCount صنف) وكافة أرصدة المخزون المتعلقة بها نهائياً.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCarType(carType)
                        carTypeToDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("حذف نهائي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { carTypeToDeleteConfirm = null }) {
                    Text("تراجع")
                }
            }
        )
    }
}

@Composable
fun AdjustStockDialog(
    part: PartEntity,
    currentQty: Int,
    onDismiss: () -> Unit,
    onConfirm: (newQty: Int, note: String) -> Unit
) {
    var qtyText by remember { mutableStateOf(currentQty.toString()) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل المخزون اليدوي", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الصنف: ${part.name} (${part.carType})", fontWeight = FontWeight.Medium)
                Text("الكمية الحالية المسجلة: $currentQty قطعة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("الكمية الجديدة الفعلية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("سبب التعديل / ملاحظات") },
                    placeholder = { Text("مثلاً: جرد دوري، تالف، تسوية مخزنية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newQty = qtyText.toIntOrNull() ?: currentQty
                    onConfirm(newQty, note)
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

@Composable
fun EditPartDetailsDialog(
    part: PartEntity,
    onDismiss: () -> Unit,
    onConfirm: (PartEntity) -> Unit
) {
    var name by remember { mutableStateOf(part.name) }
    var carType by remember { mutableStateOf(part.carType) }
    var carModel by remember { mutableStateOf(part.carModel) }
    var priceText by remember { mutableStateOf(part.price.toString()) }
    var minStockText by remember { mutableStateOf(part.minStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل بيانات الصنف", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف / القطعة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = carType,
                    onValueChange = { carType = it },
                    label = { Text("نوع السيارة (تويوتا، هونداي...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("الموديل / سنة الصنع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("سعر البيع ر.س") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("الحد الأدنى") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: part.price
                    val minStock = minStockText.toIntOrNull() ?: part.minStock
                    onConfirm(
                        part.copy(
                            name = name.trim(),
                            carType = carType.trim(),
                            carModel = carModel.trim(),
                            price = price,
                            minStock = minStock
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ التعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
