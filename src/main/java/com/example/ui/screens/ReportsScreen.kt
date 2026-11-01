package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.CustomerWithOrder
import com.example.data.model.PartWithInventory
import com.example.data.model.StockIssueWithDetails
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.util.ExportUtils

@Composable
fun ReportsScreen(
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var selectedReportType by remember { mutableIntStateOf(0) }
    var pendingExportIsPdf by remember { mutableStateOf<Boolean?>(null) }
    var showTabSettingsDialog by remember { mutableStateOf(false) }

    // Privacy mask state for financial figures
    var hideFinancials by remember { mutableStateOf(false) }

    val reportTitles = listOf(
        "المخزون الحالي",
        "القطع الناقصة",
        "طلبات العملاء",
        "حركة الصرف",
        "طلبات الموردين",
        "تقرير تفصيلي شامل"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Screen Header & Dual Export Buttons & Settings Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📈 تقارير المتجر والمستودع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تخصيص التقارير، الرؤية المالية، وتصدير Excel أو PDF",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eye toggle for financial privacy
                IconButton(
                    onClick = { hideFinancials = !hideFinancials },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (hideFinancials) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "إخفاء/إظهار الأسعار",
                        tint = if (hideFinancials) RoseDanger else PrimaryBlue
                    )
                }

                // Tab settings icon
                IconButton(
                    onClick = { showTabSettingsDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "إعدادات وتصفية التقرير",
                        tint = AmberSecondary
                    )
                }

                // Export Excel
                OutlinedButton(
                    onClick = { pendingExportIsPdf = false },
                    modifier = Modifier.height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Excel", fontSize = 11.sp)
                }

                // Export PDF
                Button(
                    onClick = { pendingExportIsPdf = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("PDF", fontSize = 11.sp)
                }
            }
        }

        // Report Selector Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reportTitles.indices.toList()) { idx ->
                FilterChip(
                    selected = selectedReportType == idx,
                    onClick = { selectedReportType = idx },
                    label = { Text(reportTitles[idx], fontSize = 11.5.sp) },
                    modifier = Modifier.testTag("report_chip_$idx")
                )
            }
        }

        // Report Content Views
        Box(modifier = Modifier.weight(1f)) {
            when (selectedReportType) {
                0 -> InventoryReportView(partsWithInventory, hideFinancials)
                1 -> LowStockReportView(partsWithInventory)
                2 -> CustomerOrdersReportView(customerOrders, hideFinancials)
                3 -> StockIssuesReportView(stockIssuesWithDetails)
                4 -> SupplierOrdersReportView(supplierOrdersWithPart, hideFinancials)
                5 -> DetailedComprehensiveReportView(partsWithInventory, customerOrders, supplierOrdersWithPart, hideFinancials)
            }
        }
    }

    // Tab-specific Settings & Filtering Dialog
    if (showTabSettingsDialog) {
        TabSettingsDialog(
            tabIndex = selectedReportType,
            tabTitle = reportTitles[selectedReportType],
            onDismiss = { showTabSettingsDialog = false }
        )
    }

    // Export Customization Dialog (User chooses columns & summary items)
    pendingExportIsPdf?.let { isPdf ->
        val rawData = getReportRawData(
            reportType = selectedReportType,
            partsWithInventory = partsWithInventory,
            customerOrders = customerOrders,
            stockIssuesWithDetails = stockIssuesWithDetails,
            supplierOrdersWithPart = supplierOrdersWithPart,
            hideFinancials = hideFinancials
        )
        ExportCustomizationDialog(
            reportTitle = reportTitles[selectedReportType],
            isPdf = isPdf,
            allHeaders = rawData.first,
            allSummaries = rawData.third,
            onDismiss = { pendingExportIsPdf = null },
            onConfirmExport = { selectedColIndices, selectedSumIndices ->
                val finalHeaders = selectedColIndices.map { rawData.first[it] }
                val finalRows = rawData.second.map { row ->
                    selectedColIndices.map { colIdx -> row.getOrElse(colIdx) { "" } }
                }
                val finalSummaries = selectedSumIndices.map { rawData.third[it] }

                if (isPdf) {
                    ExportUtils.exportToPdf(
                        context = context,
                        fileName = "Report_${reportTitles[selectedReportType].replace(" ", "_")}",
                        title = "تقرير: ${reportTitles[selectedReportType]}",
                        subtitle = "تقرير صادر وموثق من نظام قطع غيار Pro",
                        headers = finalHeaders,
                        rows = finalRows,
                        summaryLines = finalSummaries,
                        appUserName = appUserName
                    )
                } else {
                    ExportUtils.exportToExcelCsv(
                        context = context,
                        fileName = "Report_${reportTitles[selectedReportType].replace(" ", "_")}",
                        title = "تقرير: ${reportTitles[selectedReportType]}",
                        headers = finalHeaders,
                        rows = finalRows,
                        summaryLines = finalSummaries,
                        appUserName = appUserName
                    )
                }
                pendingExportIsPdf = null
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// Tab 0: Inventory Report View (with purchase price & selling price, eye toggle ***, search, sort)
// -----------------------------------------------------------------------------------------
@Composable
fun InventoryReportView(items: List<PartWithInventory>, hideFinancials: Boolean) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableIntStateOf(0) } // 0: Name, 1: Qty desc, 2: Selling Price, 3: Purchase Price

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    val filteredItems = remember(items, searchQuery, sortBy) {
        val list = if (searchQuery.isBlank()) items else {
            items.filter {
                it.part.name.contains(searchQuery, ignoreCase = true) ||
                it.part.carType.contains(searchQuery, ignoreCase = true) ||
                it.part.carModel.contains(searchQuery, ignoreCase = true) ||
                it.part.id.toString().contains(searchQuery)
            }
        }
        when (sortBy) {
            1 -> list.sortedByDescending { it.availableQty }
            2 -> list.sortedByDescending { it.part.price }
            3 -> list.sortedByDescending { it.part.purchasePrice }
            else -> list.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
        }
    }

    val totalQty = filteredItems.sumOf { it.availableQty }
    val totalPurchaseVal = filteredItems.sumOf { it.availableQty * it.part.purchasePrice }
    val totalSellingVal = filteredItems.sumOf { it.availableQty * it.part.price }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي القطع المتوفرة", fontSize = 11.sp)
                        Text("$totalQty قطعة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("القيمة بسعر الشراء", fontSize = 11.sp)
                        Text(
                            text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", totalPurchaseVal)} ر.س",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryBlue
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("القيمة بسعر البيع", fontSize = 11.sp)
                        Text(
                            text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", totalSellingVal)} ر.س",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmberSecondary
                        )
                    }
                }
            }
        }

        // Search and Quick Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن صنف أو سيارة...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ترتيب:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(selected = sortBy == 0, onClick = { sortBy = 0 }, label = { Text("الاسم", fontSize = 10.5.sp) })
            FilterChip(selected = sortBy == 1, onClick = { sortBy = 1 }, label = { Text("الكمية", fontSize = 10.5.sp) })
            FilterChip(selected = sortBy == 3, onClick = { sortBy = 3 }, label = { Text("سعر الشراء", fontSize = 10.5.sp) })
            FilterChip(selected = sortBy == 2, onClick = { sortBy = 2 }, label = { Text("سعر البيع", fontSize = 10.5.sp) })
        }

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد أصناف تطابق البحث", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredItems) { item ->
                    val p = item.part
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${p.carType} • ${p.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${item.availableQty} قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (hideFinancials) "الشراء: ***" else "الشراء: ${p.purchasePrice} ر.س",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (hideFinancials) "البيع: ***" else "البيع: ${p.price} ر.س",
                                    fontSize = 11.sp,
                                    color = AmberSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (hideFinancials) "الإجمالي: ***" else "الإجمالي: ${String.format(java.util.Locale.US, "%.1f", item.availableQty * p.price)} ر.س",
                                    fontSize = 11.sp,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 1: Low Stock Report View
// -----------------------------------------------------------------------------------------
@Composable
fun LowStockReportView(items: List<PartWithInventory>) {
    var searchQuery by remember { mutableStateOf("") }
    val lowStock = remember(items, searchQuery) {
        val base = items.filter { it.isLowStock }
        if (searchQuery.isBlank()) base else {
            base.filter {
                it.part.name.contains(searchQuery, ignoreCase = true) ||
                it.part.carType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RoseLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الأصناف التي وصلت للحد الأدنى", fontWeight = FontWeight.Bold, color = RoseDanger)
                Text("${lowStock.size} أصناف", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoseDanger)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في الأصناف الناقصة...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        if (lowStock.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد أصناف ناقصة عن الحد الأدنى", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(lowStock) { item ->
                    val deficit = maxOf(0, item.part.minStock - item.availableQty)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.part.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${item.part.carType} • الحد الأدنى: ${item.part.minStock}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "المتوفر: ${item.availableQty}",
                                    fontWeight = FontWeight.Bold,
                                    color = RoseDanger,
                                    fontSize = 13.sp
                                )
                                Text("نقص: $deficit قطعة", fontSize = 11.sp, color = RoseDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 2: Customer Orders Report View (with expandable customer cards, items, filter)
// -----------------------------------------------------------------------------------------
@Composable
fun CustomerOrdersReportView(customerOrders: List<CustomerWithOrder>, hideFinancials: Boolean) {
    var dateFilter by remember { mutableStateOf("") }
    var customerFilter by remember { mutableStateOf("") }
    var partFilter by remember { mutableStateOf("") }

    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    val filteredOrders = remember(customerOrders, dateFilter, customerFilter, partFilter) {
        customerOrders.filter { c ->
            val matchesCustomer = customerFilter.isBlank() || c.customer.name.contains(customerFilter, ignoreCase = true)
            val matchesDate = dateFilter.isBlank() || c.allOrders.any { it.order.orderDate.contains(dateFilter) } || (c.activeOrder?.orderDate?.contains(dateFilter) == true)
            val matchesPart = partFilter.isBlank() || c.items.any { it.part.name.contains(partFilter, ignoreCase = true) }
            matchesCustomer && matchesDate && matchesPart
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = customerFilter,
                onValueChange = { customerFilter = it },
                placeholder = { Text("العميل...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = dateFilter,
                onValueChange = { dateFilter = it },
                placeholder = { Text("تاريخ...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = partFilter,
                onValueChange = { partFilter = it },
                placeholder = { Text("الصنف...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد طلبات تطابق معايير التصفية", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredOrders) { c ->
                    val isExpanded = expandedMap[c.customer.id] ?: false
                    val totalCost = c.items.sumOf { it.item.quantity * it.part.price }
                    val paid = c.allOrders.sumOf { it.order.paidAmount }.let { if (it > 0) it else (c.activeOrder?.paidAmount ?: 0.0) }
                    val remaining = maxOf(0.0, totalCost - paid)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header with Expand / Collapse Arrow
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedMap[c.customer.id] = !isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "طي" else "توسيع",
                                        tint = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(c.customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (c.customer.phone.isNotBlank()) {
                                            Text(c.customer.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${c.totalQty} قطعة", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 13.sp)
                                    Text(
                                        text = if (hideFinancials) "*** ر.س" else "$totalCost ر.س",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AmberSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Payment strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (hideFinancials) "الواصل: ***" else "الواصل: $paid ر.س",
                                    fontSize = 11.sp,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (hideFinancials) "المتبقي: ***" else "المتبقي: $remaining ر.س",
                                    fontSize = 11.sp,
                                    color = if (remaining > 0) RoseDanger else EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Expandable Details (items)
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    HorizontalDivider()
                                    Text("الأصناف المطلوبة:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = PrimaryBlue)
                                    c.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("• ${item.part.name} (${item.part.carType})", fontSize = 11.sp)
                                            Text("${item.item.quantity} ق × ${item.part.price} ر.س = ${item.item.quantity * item.part.price} ر.س", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 3: Stock Issues Report View
// -----------------------------------------------------------------------------------------
@Composable
fun StockIssuesReportView(issues: List<StockIssueWithDetails>) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableIntStateOf(0) } // 0: Date, 1: Part name, 2: Party

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    val filteredSortedIssues = remember(issues, searchQuery, sortBy) {
        val filtered = if (searchQuery.isBlank()) issues else {
            issues.filter {
                it.partName.contains(searchQuery, ignoreCase = true) ||
                it.partyName.contains(searchQuery, ignoreCase = true) ||
                it.issue.personName.contains(searchQuery, ignoreCase = true) ||
                it.issue.issueDate.contains(searchQuery)
            }
        }
        when (sortBy) {
            1 -> filtered.sortedWith { i1, i2 -> collator.compare(i1.partName, i2.partName) }
            2 -> filtered.sortedWith { i1, i2 -> collator.compare(i1.partyName, i2.partyName) }
            else -> filtered.sortedByDescending { it.issue.issueDate }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث بالتاريخ، الصنف، الجهة، أو المستلم...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ترتيب حسب:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(selected = sortBy == 0, onClick = { sortBy = 0 }, label = { Text("التاريخ", fontSize = 11.sp) })
            FilterChip(selected = sortBy == 1, onClick = { sortBy = 1 }, label = { Text("اسم الصنف", fontSize = 11.sp) })
            FilterChip(selected = sortBy == 2, onClick = { sortBy = 2 }, label = { Text("الجهة", fontSize = 11.sp) })
        }

        if (filteredSortedIssues.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد عمليات صرف مطابقة للبحث", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredSortedIssues) { issue ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(issue.partName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("إلى: ${issue.partyName} (المستلم: ${issue.issue.personName})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("التاريخ: ${issue.issue.issueDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("-${issue.issue.quantity} قطعة", fontWeight = FontWeight.Bold, color = RoseDanger, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 4: Supplier Orders Report View
// -----------------------------------------------------------------------------------------
@Composable
fun SupplierOrdersReportView(supplierOrders: List<SupplierOrderWithPart>, hideFinancials: Boolean) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(supplierOrders, searchQuery) {
        if (searchQuery.isBlank()) supplierOrders else {
            supplierOrders.filter {
                (it.part?.name ?: "").contains(searchQuery, ignoreCase = true) ||
                it.supplierOrder.orderDate.contains(searchQuery) ||
                it.supplierOrder.status.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن طلبية بالصنف، التاريخ، الحالة...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد طلبيات موردين مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(filtered) { item ->
                    val order = item.supplierOrder
                    val p = item.part
                    val statusAr = when (order.status) {
                        "completed" -> "مكتمل"
                        "partial" -> "مكتمل جزئياً"
                        "cancelled" -> "ملغي"
                        "pending" -> "قيد الانتظار"
                        else -> "مؤكد"
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p?.name ?: "صنف #${order.partId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("الحالة: $statusAr • طلب: ${order.orderDate}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (p != null) {
                                    val priceToShow = if (p.purchasePrice > 0) p.purchasePrice else p.price
                                    Text(
                                        text = if (hideFinancials) "السعر: ***" else "سعر الشراء: $priceToShow ر.س",
                                        fontSize = 10.sp,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("مطلوب: ${order.qtyOrdered}", fontSize = 12.sp)
                                Text("واصل: ${order.qtyReceived}", fontSize = 11.sp, color = EmeraldSuccess)
                                Text("متبقي: ${order.qtyRemaining}", fontWeight = FontWeight.Bold, color = if (order.qtyRemaining > 0) RoseDanger else EmeraldSuccess)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 5: Detailed Comprehensive Report View
// -----------------------------------------------------------------------------------------
@Composable
fun DetailedComprehensiveReportView(
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    hideFinancials: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredParts = remember(partsWithInventory, searchQuery) {
        if (searchQuery.isBlank()) partsWithInventory else {
            partsWithInventory.filter {
                it.part.name.contains(searchQuery, ignoreCase = true) ||
                it.part.carType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val totalInvPieces = filteredParts.sumOf { it.availableQty }
    val totalInvPurchaseVal = filteredParts.sumOf { it.availableQty * it.part.purchasePrice }
    val totalInvSellingVal = filteredParts.sumOf { it.availableQty * it.part.price }
    val totalCustDemand = customerOrders.sumOf { it.totalQty }
    val totalSuppPending = supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "📊 ملخص التقرير الشامل للمتجر والمستودع",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 13.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("قيمة الشراء", fontSize = 10.5.sp)
                        Text(
                            text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", totalInvPurchaseVal)} ر.س",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                    }
                    Column {
                        Text("قيمة البيع", fontSize = 10.5.sp)
                        Text(
                            text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", totalInvSellingVal)} ر.س",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AmberSecondary
                        )
                    }
                    Column {
                        Text("القطع المتوفرة", fontSize = 10.5.sp)
                        Text("$totalInvPieces ق", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSuccess)
                    }
                    Column {
                        Text("طلب العملاء", fontSize = 10.5.sp)
                        Text("$totalCustDemand ق", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                    }
                    Column {
                        Text("قيد التوريد", fontSize = 10.5.sp)
                        Text("$totalSuppPending ق", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoseDanger)
                    }
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن صنف بالتقرير الشامل...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
            items(filteredParts) { item ->
                val p = item.part
                val lineVal = item.availableQty * p.price
                val linePurchaseVal = item.availableQty * p.purchasePrice
                val neededByCustomers = customerOrders.sumOf { co ->
                    co.items.filter { it.part.id == p.id }.sumOf { it.item.quantity }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${p.carType} • ${p.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (hideFinancials) "*** ر.س" else "$lineVal ر.س",
                                    fontWeight = FontWeight.Bold,
                                    color = AmberSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (hideFinancials) "شراء: ***" else "شراء: $linePurchaseVal ر.س",
                                    fontSize = 10.5.sp,
                                    color = PrimaryBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المتوفر: ${item.availableQty}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("مطلوب: $neededByCustomers", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            val isLow = item.availableQty <= p.minStock
                            Text(if (isLow) "نقص مخزون" else "مخزون آمن", fontSize = 11.sp, color = if (isLow) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Tab Settings Dialog (Tab-specific customization)
// ----------------------------------------------------
@Composable
fun TabSettingsDialog(
    tabIndex: Int,
    tabTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعدادات وتخصيص: $tabTitle", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "يمكنك تخصيص وتصفية بيانات هذا التقرير بالبحث والتصنيف مباشرة، واستخدام أيقونة العين بالأعلى لإخفاء/إظهار الأسعار المالية.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("• يدعم التقرير التصدير بصيغة Excel و PDF مع تضمين التوقيع والترويس الرسمي.", fontSize = 11.sp)
                Text("• عند الضغط على تصدير يمكنك استبعاد أي أعمدة لا ترغب بظهورها في الملف النهائي.", fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("حسناً")
            }
        }
    )
}

// ----------------------------------------------------
// Export Raw Data Extraction (Headers, Rows, Summaries for all 6 reports)
// ----------------------------------------------------
fun getReportRawData(
    reportType: Int,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    hideFinancials: Boolean = false
): Triple<List<String>, List<List<String>>, List<String>> {
    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
        strength = java.text.Collator.PRIMARY
    }

    return when (reportType) {
        0 -> { // Inventory
            val sorted = partsWithInventory.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
            val headers = listOf("م", "الصنف", "السيارة", "الكمية", "سعر الشراء", "سعر البيع", "إجمالي القيمة")
            val rows = sorted.mapIndexed { index, item ->
                listOf(
                    (index + 1).toString(),
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    if (hideFinancials) "***" else "${item.part.purchasePrice} ر.س",
                    if (hideFinancials) "***" else "${item.part.price} ر.س",
                    if (hideFinancials) "***" else "${item.availableQty * item.part.price} ر.س"
                )
            }
            val summaries = listOf(
                "إجمالي الأصناف المسجلة: ${partsWithInventory.size} صنف",
                "إجمالي عدد القطع المتوفرة بالمستودع: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية بسعر الشراء: " + if (hideFinancials) "*** ر.س" else "${partsWithInventory.sumOf { it.availableQty * it.part.purchasePrice }} ر.س",
                "القيمة التقديرية بسعر البيع: " + if (hideFinancials) "*** ر.س" else "${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
        1 -> { // Low stock
            val low = partsWithInventory.filter { it.isLowStock }.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
            val headers = listOf("م", "الصنف", "السيارة", "المتوفر", "الحد الأدنى", "مقدار النقص")
            val rows = low.mapIndexed { index, item ->
                val deficit = maxOf(0, item.part.minStock - item.availableQty)
                listOf(
                    (index + 1).toString(),
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    item.part.minStock.toString(),
                    "$deficit قطعة"
                )
            }
            val summaries = listOf(
                "عدد الأصناف الناقصة عن الحد الأدنى: ${low.size} صنف",
                "ينصح بإنشاء طلبية توريد عاجلة لتعويض النقص في المستودع"
            )
            Triple(headers, rows, summaries)
        }
        2 -> { // Customer orders (Matrix Group View format when multiple customers)
            if (customerOrders.size > 1) {
                val allPartsInOrders = customerOrders.flatMap { it.items.map { i -> i.part } }
                    .distinctBy { it.id }
                    .sortedWith { p1, p2 -> collator.compare(p1.name, p2.name) }

                val headers = listOf("م", "الصنف / السيارة") + customerOrders.map { it.customer.name } + listOf("الإجمالي")
                val rows = allPartsInOrders.mapIndexed { index, part ->
                    val partDesc = "${part.name} (${part.carType})"
                    val custVals = customerOrders.map { co ->
                        val qty = co.items.filter { it.part.id == part.id }.sumOf { it.item.quantity }
                        qty.toString()
                    }
                    val rowTotal = customerOrders.sumOf { co ->
                        co.items.filter { it.part.id == part.id }.sumOf { it.item.quantity }
                    }
                    listOf((index + 1).toString(), partDesc) + custVals + listOf(rowTotal.toString())
                }
                val summaries = listOf(
                    "عدد العملاء: ${customerOrders.size} عميل",
                    "إجمالي الأصناف المطلوبة: ${allPartsInOrders.size} صنف",
                    "إجمالي القطع المطلوبة لكافة العملاء: ${customerOrders.sumOf { it.totalQty }} قطعة"
                )
                Triple(headers, rows, summaries)
            } else {
                val headers = listOf("م", "العميل", "الهاتف", "إجمالي القطع", "القيمة الإجمالية", "المبلغ الواصل", "المتبقي", "حالة السداد")
                val rows = customerOrders.mapIndexed { index, c ->
                    val order = c.activeOrder
                    val cost = c.items.sumOf { it.item.quantity * it.part.price }
                    val paid = order?.paidAmount ?: 0.0
                    val remaining = maxOf(0.0, cost - paid)
                    listOf(
                        (index + 1).toString(),
                        c.customer.name,
                        c.customer.phone,
                        c.totalQty.toString(),
                        if (hideFinancials) "***" else "$cost ر.س",
                        if (hideFinancials) "***" else "$paid ر.س",
                        if (hideFinancials) "***" else "$remaining ر.س",
                        order?.paymentStatus ?: "غير مسدد"
                    )
                }
                val summaries = listOf(
                    "إجمالي عدد طلبات العملاء: ${customerOrders.size}",
                    "إجمالي القطع المطلوبة للعملاء: ${customerOrders.sumOf { it.totalQty }} قطعة"
                )
                Triple(headers, rows, summaries)
            }
        }
        3 -> { // Stock issues
            val sortedIssues = stockIssuesWithDetails.sortedWith { i1, i2 -> collator.compare(i1.partName, i2.partName) }
            val headers = listOf("م", "التاريخ", "الصنف", "الكمية المصروفة", "الجهة المصروفة لها", "المستلم", "ملاحظات")
            val rows = sortedIssues.mapIndexed { index, issue ->
                listOf(
                    (index + 1).toString(),
                    issue.issue.issueDate,
                    issue.partName,
                    issue.issue.quantity.toString(),
                    issue.partyName,
                    issue.issue.personName,
                    issue.issue.note
                )
            }
            val summaries = listOf(
                "عدد عمليات الصرف المنفذة: ${stockIssuesWithDetails.size}",
                "إجمالي القطع المصروفة: ${stockIssuesWithDetails.sumOf { it.issue.quantity }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
        4 -> { // Supplier orders
            val sortedSupp = supplierOrdersWithPart.sortedWith { o1, o2 -> collator.compare(o1.part?.name ?: "", o2.part?.name ?: "") }
            val headers = listOf("م", "الصنف", "السيارة", "المطلوب", "الواصل", "المتبقي", "سعر الشراء", "تاريخ الطلب", "الحالة")
            val rows = sortedSupp.mapIndexed { index, o ->
                val s = o.supplierOrder
                val p = o.part
                val statusAr = when (s.status) {
                    "completed" -> "مكتمل"
                    "partial" -> "مكتمل جزئياً"
                    "cancelled" -> "ملغي"
                    "pending" -> "قيد الانتظار"
                    else -> "مؤكد"
                }
                listOf(
                    (index + 1).toString(),
                    p?.name ?: "صنف #${s.partId}",
                    p?.carType ?: "",
                    s.qtyOrdered.toString(),
                    s.qtyReceived.toString(),
                    s.qtyRemaining.toString(),
                    if (hideFinancials) "***" else "${p?.purchasePrice ?: 0.0} ر.س",
                    s.orderDate,
                    statusAr
                )
            }
            val summaries = listOf(
                "إجمالي عدد طلبيات التوريد: ${supplierOrdersWithPart.size}",
                "إجمالي المطلوب من الموردين: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
                "إجمالي المستلم بالمستودع: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyReceived }} قطعة",
                "المتبقي قيد التوريد: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
        else -> { // 5: Detailed Comprehensive Report
            val sorted = partsWithInventory.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
            val headers = listOf("م", "الصنف", "السيارة", "المتوفر بالمخزن", "سعر الشراء", "سعر البيع", "إجمالي القيمة", "طلب العملاء", "حالة الرصيد")
            val rows = sorted.mapIndexed { index, item ->
                val p = item.part
                val lineVal = item.availableQty * p.price
                val neededByCustomers = customerOrders.sumOf { co ->
                    co.items.filter { it.part.id == p.id }.sumOf { it.item.quantity }
                }
                listOf(
                    (index + 1).toString(),
                    p.name,
                    "${p.carType} ${p.carModel}".trim(),
                    item.availableQty.toString(),
                    if (hideFinancials) "***" else "${p.purchasePrice} ر.س",
                    if (hideFinancials) "***" else "${p.price} ر.س",
                    if (hideFinancials) "***" else "$lineVal ر.س",
                    "$neededByCustomers قطعة",
                    if (item.availableQty <= p.minStock) "نقص مخزون" else "متوفر وآمن"
                )
            }
            val summaries = listOf(
                "إجمالي أصناف المستودع: ${partsWithInventory.size} صنف",
                "إجمالي القطع المتوفرة: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "إجمالي قيمة المخزون بسعر الشراء: " + if (hideFinancials) "*** ر.س" else "${partsWithInventory.sumOf { it.availableQty * it.part.purchasePrice }} ر.س",
                "إجمالي قيمة المخزون بسعر البيع: " + if (hideFinancials) "*** ر.س" else "${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
    }
}

// ----------------------------------------------------
// Export Customization Dialog (User chooses columns & summary items)
// ----------------------------------------------------
@Composable
fun ExportCustomizationDialog(
    reportTitle: String,
    isPdf: Boolean,
    allHeaders: List<String>,
    allSummaries: List<String>,
    onDismiss: () -> Unit,
    onConfirmExport: (selectedColIndices: List<Int>, selectedSumIndices: List<Int>) -> Unit
) {
    val selectedColumns = remember { mutableStateListOf<Int>().apply { addAll(allHeaders.indices) } }
    val selectedSummaries = remember { mutableStateListOf<Int>().apply { addAll(allSummaries.indices) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تخصيص تصدير: $reportTitle (${if (isPdf) "PDF" else "Excel"})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "حدد الأعمدة التي ترغب في إظهارها في الملف:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PrimaryBlue
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(allHeaders.indices.toList()) { colIdx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedColumns.contains(colIdx)) {
                                        if (selectedColumns.size > 1) selectedColumns.remove(colIdx)
                                    } else {
                                        selectedColumns.add(colIdx)
                                    }
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedColumns.contains(colIdx),
                                onCheckedChange = { chk ->
                                    if (chk) selectedColumns.add(colIdx)
                                    else if (selectedColumns.size > 1) selectedColumns.remove(colIdx)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(allHeaders[colIdx], fontSize = 12.sp)
                        }
                    }
                }

                if (allSummaries.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "عناصر الملخص والإحصائيات:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(allSummaries.indices.toList()) { sumIdx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedSummaries.contains(sumIdx)) selectedSummaries.remove(sumIdx)
                                        else selectedSummaries.add(sumIdx)
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedSummaries.contains(sumIdx),
                                    onCheckedChange = { chk ->
                                        if (chk) selectedSummaries.add(sumIdx) else selectedSummaries.remove(sumIdx)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(allSummaries[sumIdx], fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmExport(selectedColumns.sorted(), selectedSummaries.sorted())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تصدير الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
