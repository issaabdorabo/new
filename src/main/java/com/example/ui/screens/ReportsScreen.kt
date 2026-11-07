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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.MultiSelectSelector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.ui.util.AttachmentUtils
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
import com.example.ui.util.Text
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
import com.example.data.model.DailyMovementItem
import com.example.data.model.PartWithInventory
import com.example.data.model.StockIssueWithDetails
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechCyanLight
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
    dailyMovements: List<DailyMovementItem> = emptyList(),
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
        "تقرير تفصيلي شامل",
        "تقرير الحركات اليومية المفصل"
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
                6 -> DailyMovementsReportView(dailyMovements, hideFinancials, appUserName)
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
        ExportCustomizationDialog(
            reportType = selectedReportType,
            reportTitle = reportTitles[selectedReportType],
            isPdf = isPdf,
            partsWithInventory = partsWithInventory,
            customerOrders = customerOrders,
            stockIssuesWithDetails = stockIssuesWithDetails,
            supplierOrdersWithPart = supplierOrdersWithPart,
            dailyMovements = dailyMovements,
            hideFinancials = hideFinancials,
            onDismiss = { pendingExportIsPdf = null },
            onConfirmExport = { finalHeaders, finalRows, finalSummaries ->
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
                            color = TechCyan
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
                                    color = TechCyan,
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
    val allCustomerNames = remember(customerOrders) { customerOrders.map { it.customer.name }.filter { it.isNotBlank() }.distinct().sorted() }
    val allPartNames = remember(customerOrders) { customerOrders.flatMap { it.items }.map { it.part.name }.filter { it.isNotBlank() }.distinct().sorted() }

    var selectedCustomerNames by remember { mutableStateOf(setOf<String>()) }
    var selectedPartNames by remember { mutableStateOf(setOf<String>()) }

    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    val filteredOrders = remember(customerOrders, dateFilter, selectedCustomerNames, selectedPartNames) {
        customerOrders.filter { c ->
            val matchesCustomer = selectedCustomerNames.isEmpty() || selectedCustomerNames.contains(c.customer.name)
            val matchesDate = dateFilter.isBlank() || c.allOrders.any { it.order.orderDate.contains(dateFilter) } || (c.activeOrder?.orderDate?.contains(dateFilter) == true)
            val matchesPart = selectedPartNames.isEmpty() || c.items.any { selectedPartNames.contains(it.part.name) }
            matchesCustomer && matchesDate && matchesPart
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = dateFilter,
            onValueChange = { dateFilter = it },
            placeholder = { Text("تصفية بتاريخ الطلبية (yyyy-MM-dd)...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MultiSelectSelector(
                label = "العملاء",
                allOptions = allCustomerNames,
                selectedOptions = selectedCustomerNames,
                onSelectionChanged = { selectedCustomerNames = it },
                modifier = Modifier.weight(1f)
            )
            MultiSelectSelector(
                label = "الأصناف",
                allOptions = allPartNames,
                selectedOptions = selectedPartNames,
                onSelectionChanged = { selectedPartNames = it },
                modifier = Modifier.weight(1f)
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
                                        color = TechCyan
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

                            // Expandable Details (all orders and items with order date)
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HorizontalDivider()
                                    Text(
                                        "تفاصيل كافة طلبيات العميل وتواريخ طلب الأصناف:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = PrimaryBlue
                                    )
                                    if (c.allOrders.isNotEmpty()) {
                                        c.allOrders.sortedByDescending { it.order.orderDate }.forEach { ordDetail ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("📦 طلبية بتاريخ: ${ordDetail.order.orderDate}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryBlue)
                                                        Text(
                                                            text = if (ordDetail.order.batchId.isNotBlank()) "دفعة: ${ordDetail.order.batchId}" else "طلبية مفردة",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    ordDetail.items.forEach { itm ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("• ${itm.part.name} (${itm.part.carType} ${itm.part.carModel})", fontSize = 11.sp)
                                                            Text(
                                                                text = "${itm.item.quantity} قطعة × ${itm.part.price} ر.س • ${ordDetail.order.orderDate}",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        c.items.forEach { item ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("• ${item.part.name} (${item.part.carType})", fontSize = 11.sp)
                                                Text("${item.item.quantity} ق × ${item.part.price} ر.س • ${c.activeOrder?.orderDate ?: "غير محدد"}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    var selectedStatus by remember { mutableStateOf("الكل") }
    val statusChips = listOf("الكل", "مؤكد", "مكتمل جزئياً", "مكتمل", "ملغي")
    val expandedOrders = remember { mutableStateMapOf<Long, Boolean>() }

    val filtered = remember(supplierOrders, searchQuery, selectedStatus) {
        supplierOrders.filter { item ->
            val order = item.supplierOrder
            val p = item.part
            val statusMatches = when (selectedStatus) {
                "الكل" -> true
                "مؤكد" -> order.status == "confirmed" || order.status == "pending"
                "مكتمل جزئياً" -> order.status == "partial" || (order.qtyReceived > 0 && order.qtyRemaining > 0)
                "مكتمل" -> order.status == "completed" || order.qtyRemaining == 0
                "ملغي" -> order.status == "cancelled"
                else -> true
            }
            val queryMatches = searchQuery.isBlank() ||
                    (p?.name ?: "").contains(searchQuery, ignoreCase = true) ||
                    (p?.carType ?: "").contains(searchQuery, ignoreCase = true) ||
                    (p?.carModel ?: "").contains(searchQuery, ignoreCase = true) ||
                    order.orderDate.contains(searchQuery) ||
                    order.batchId.contains(searchQuery)
            statusMatches && queryMatches
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن صنف، سيارة، تاريخ طلبية...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(14.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // FilterChips for status below search bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(statusChips) { st ->
                FilterChip(
                    selected = selectedStatus == st,
                    onClick = { selectedStatus = st },
                    label = { Text(st, fontSize = 11.sp) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد طلبيات موردين مسجلة تطابق التصفية", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(filtered) { item ->
                    val order = item.supplierOrder
                    val p = item.part
                    val isExpanded = expandedOrders[order.id] ?: false
                    val statusAr = when (order.status) {
                        "completed" -> "مكتمل"
                        "partial" -> "مكتمل جزئياً"
                        "cancelled" -> "ملغي"
                        "pending" -> "قيد الانتظار"
                        else -> "مؤكد"
                    }
                    val (statusBg, statusClr) = when (order.status) {
                        "completed" -> Pair(EmeraldLight, EmeraldSuccess)
                        "partial" -> Pair(TechCyanLight, TechCyan)
                        "cancelled" -> Pair(RoseLight, RoseDanger)
                        else -> Pair(PrimaryLighter, PrimaryBlue)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedOrders[order.id] = !isExpanded },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "طي" else "توسيع",
                                        tint = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(p?.name ?: "صنف #${order.partId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${p?.carType} • ${p?.carModel} • طلب: ${order.orderDate}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(statusAr, fontSize = 10.5.sp, color = statusClr, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المطلوب: ${order.qtyOrdered} ق", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                Text("المستلم: ${order.qtyReceived} ق", fontSize = 11.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                Text("المتبقي: ${order.qtyRemaining} ق", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (order.qtyRemaining > 0) RoseDanger else EmeraldSuccess)
                            }

                            // Expandable Breakdown
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HorizontalDivider()

                                    // Batch and confirmation metadata
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("رمز الدفعة: ${if (order.batchId.isNotBlank()) order.batchId else "مفردة"}", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                                        if (order.confirmedDate.isNotBlank()) {
                                            Text("تاريخ التأكيد: ${order.confirmedDate}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (order.notes.isNotBlank()) {
                                        Text("ملاحظات: ${order.notes}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Customers who ordered this part in this batch
                                    if (item.customersWhoOrdered.isNotEmpty()) {
                                        Text("العملاء الذين طلبوا هذا الصنف:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryBlue)
                                        item.customersWhoOrdered.forEach { req ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("👤 ${req.customerName} (${req.customerPhone})", fontSize = 10.5.sp)
                                                Text("${req.quantity} قطعة", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TechCyan)
                                            }
                                        }
                                    }

                                    // Delivery receipts recorded
                                    if (item.receipts.isNotEmpty()) {
                                        Text("سجل دفعات التوريد المستلمة بالمستودع:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldSuccess)
                                        item.receipts.forEachIndexed { rIdx, rec ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("${rIdx + 1}. بتاريخ ${rec.receiptDate} (المستلم: ${rec.receiverName.ifBlank { "المستودع" }})", fontSize = 10.5.sp)
                                                Text("+${rec.quantity} ق", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldSuccess)
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
                            color = TechCyan
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
                                    color = TechCyan,
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
// Export Raw Data Extraction (Headers, Rows, Summaries for all 7 reports)
// ----------------------------------------------------
fun getReportRawData(
    reportType: Int,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    dailyMovements: List<DailyMovementItem> = emptyList(),
    hideFinancials: Boolean = false,
    selectedPartNames: Set<String> = emptySet(),
    selectedCustomerNames: Set<String> = emptySet(),
    selectedPartyNames: Set<String> = emptySet(),
    selectedReceiverNames: Set<String> = emptySet(),
    selectedStatuses: Set<String> = emptySet(),
    fromDate: String = "",
    toDate: String = ""
): Triple<List<String>, List<List<String>>, List<String>> {
    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
        strength = java.text.Collator.PRIMARY
    }

    return when (reportType) {
        0 -> { // Inventory
            val filtered = partsWithInventory.filter {
                selectedPartNames.isEmpty() || selectedPartNames.contains(it.part.name)
            }
            val sorted = filtered.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
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
                "إجمالي الأصناف المحددة: ${sorted.size} صنف",
                "إجمالي عدد القطع المتوفرة: ${sorted.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية بسعر الشراء: " + if (hideFinancials) "*** ر.س" else "${sorted.sumOf { it.availableQty * it.part.purchasePrice }} ر.س",
                "القيمة التقديرية بسعر البيع: " + if (hideFinancials) "*** ر.س" else "${sorted.sumOf { it.availableQty * it.part.price }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
        1 -> { // Low stock
            val low = partsWithInventory.filter {
                it.isLowStock && (selectedPartNames.isEmpty() || selectedPartNames.contains(it.part.name))
            }.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
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
        2 -> { // Customer orders
            val filteredCustomers = customerOrders.filter { co ->
                val custMatch = selectedCustomerNames.isEmpty() || selectedCustomerNames.contains(co.customer.name)
                val dateMatch = (fromDate.isBlank() || (co.activeOrder?.orderDate ?: "") >= fromDate) &&
                                (toDate.isBlank() || (co.activeOrder?.orderDate ?: "") <= toDate)
                val partMatch = selectedPartNames.isEmpty() || co.items.any { selectedPartNames.contains(it.part.name) }
                custMatch && dateMatch && partMatch
            }

            if (filteredCustomers.size > 1) {
                val allPartsInOrders = filteredCustomers.flatMap { it.items.map { i -> i.part } }
                    .distinctBy { it.id }
                    .filter { selectedPartNames.isEmpty() || selectedPartNames.contains(it.name) }
                    .sortedWith { p1, p2 -> collator.compare(p1.name, p2.name) }

                val headers = listOf("م", "الصنف / السيارة") + filteredCustomers.map { it.customer.name } + listOf("الإجمالي")
                val rows = allPartsInOrders.mapIndexed { index, part ->
                    val partDesc = "${part.name} (${part.carType})"
                    val custVals = filteredCustomers.map { co ->
                        val qty = co.items.filter { it.part.id == part.id }.sumOf { it.item.quantity }
                        qty.toString()
                    }
                    val rowTotal = filteredCustomers.sumOf { co ->
                        co.items.filter { it.part.id == part.id }.sumOf { it.item.quantity }
                    }
                    listOf((index + 1).toString(), partDesc) + custVals + listOf(rowTotal.toString())
                }
                val summaries = listOf(
                    "عدد العملاء المشمولين: ${filteredCustomers.size} عميل",
                    "إجمالي الأصناف المطلوبة: ${allPartsInOrders.size} صنف",
                    "إجمالي القطع المطلوبة لكافة العملاء: ${filteredCustomers.sumOf { it.totalQty }} قطعة"
                )
                Triple(headers, rows, summaries)
            } else {
                val headers = listOf("م", "العميل", "الهاتف", "إجمالي القطع", "القيمة الإجمالية", "المبلغ الواصل", "المتبقي", "حالة السداد")
                val rows = filteredCustomers.mapIndexed { index, c ->
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
                    "إجمالي عدد طلبات العملاء: ${filteredCustomers.size}",
                    "إجمالي القطع المطلوبة للعملاء: ${filteredCustomers.sumOf { it.totalQty }} قطعة"
                )
                Triple(headers, rows, summaries)
            }
        }
        3 -> { // Stock issues
            val filteredIssues = stockIssuesWithDetails.filter { issue ->
                val partMatch = selectedPartNames.isEmpty() || selectedPartNames.contains(issue.partName)
                val partyMatch = selectedPartyNames.isEmpty() || selectedPartyNames.contains(issue.partyName)
                val receiverMatch = selectedReceiverNames.isEmpty() || selectedReceiverNames.contains(issue.issue.personName)
                val dateMatch = (fromDate.isBlank() || issue.issue.issueDate >= fromDate) &&
                                (toDate.isBlank() || issue.issue.issueDate <= toDate)
                partMatch && partyMatch && receiverMatch && dateMatch
            }
            val sortedIssues = filteredIssues.sortedWith { i1, i2 -> collator.compare(i1.partName, i2.partName) }
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
                "عدد عمليات الصرف المنفذة: ${sortedIssues.size}",
                "إجمالي القطع المصروفة: ${sortedIssues.sumOf { it.issue.quantity }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
        4 -> { // Supplier orders
            val filteredSupp = supplierOrdersWithPart.filter { o ->
                val p = o.part
                val s = o.supplierOrder
                val partMatch = selectedPartNames.isEmpty() || (p != null && selectedPartNames.contains(p.name))
                val statusMatch = selectedStatuses.isEmpty() || selectedStatuses.contains(s.status)
                val dateMatch = (fromDate.isBlank() || s.orderDate >= fromDate) &&
                                (toDate.isBlank() || s.orderDate <= toDate)
                partMatch && statusMatch && dateMatch
            }
            val sortedSupp = filteredSupp.sortedWith { o1, o2 -> collator.compare(o1.part?.name ?: "", o2.part?.name ?: "") }
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
                "إجمالي عدد طلبيات التوريد: ${sortedSupp.size}",
                "إجمالي المطلوب من الموردين: ${sortedSupp.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
                "إجمالي المستلم بالمستودع: ${sortedSupp.sumOf { it.supplierOrder.qtyReceived }} قطعة",
                "المتبقي قيد التوريد: ${sortedSupp.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
        6 -> { // 6: Detailed Daily Movements Report (تقرير الحركات اليومية المفصل)
            val filteredDaily = dailyMovements.filter { m ->
                val partMatch = selectedPartNames.isEmpty() || selectedPartNames.contains(m.partName)
                val entityMatch = selectedPartyNames.isEmpty() || selectedPartyNames.contains(m.entityName) || selectedCustomerNames.contains(m.entityName)
                val receiverMatch = selectedReceiverNames.isEmpty() || selectedReceiverNames.contains(m.personName)
                val dateMatch = (fromDate.isBlank() || m.date >= fromDate) && (toDate.isBlank() || m.date <= toDate)
                val typeMatch = selectedStatuses.isEmpty() || selectedStatuses.contains(m.type)
                partMatch && entityMatch && receiverMatch && dateMatch && typeMatch
            }
            val headers = listOf("م", "التاريخ", "نوع الحركة", "الصنف", "الكمية", "الجهة / العميل", "المستلم / المسؤول", "سعر الوحدة", "الإجمالي", "الدفعة / الفاتورة", "ملاحظات")
            val rows = filteredDaily.mapIndexed { index, m ->
                listOf(
                    (index + 1).toString(),
                    m.date,
                    m.type,
                    "${m.partName} (${m.carType})",
                    m.quantity.toString(),
                    m.entityName,
                    m.personName,
                    if (hideFinancials) "***" else "${m.unitPrice} ر.س",
                    if (hideFinancials) "***" else "${m.totalPrice} ر.س",
                    m.batchOrInvoice,
                    m.notes
                )
            }
            val summaries = listOf(
                "إجمالي الحركات اليومية المنفذة: ${filteredDaily.size} حركة",
                "إجمالي قطع المنصرف: ${filteredDaily.filter { it.type == "صرف مخزون" }.sumOf { it.quantity }} قطعة",
                "إجمالي قطع التوريد: ${filteredDaily.filter { it.type == "توريد بضاعة" }.sumOf { it.quantity }} قطعة",
                "إجمالي المبالغ المالية: " + if (hideFinancials) "*** ر.س" else "${filteredDaily.sumOf { it.totalPrice }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
        else -> { // 5: Detailed Comprehensive Report
            val filtered = partsWithInventory.filter {
                selectedPartNames.isEmpty() || selectedPartNames.contains(it.part.name)
            }
            val sorted = filtered.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
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
                "إجمالي أصناف المستودع المحددة: ${sorted.size} صنف",
                "إجمالي القطع المتوفرة: ${sorted.sumOf { it.availableQty }} قطعة",
                "إجمالي قيمة المخزون بسعر الشراء: " + if (hideFinancials) "*** ر.س" else "${sorted.sumOf { it.availableQty * it.part.purchasePrice }} ر.س",
                "إجمالي قيمة المخزون بسعر البيع: " + if (hideFinancials) "*** ر.س" else "${sorted.sumOf { it.availableQty * it.part.price }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
    }
}

// ----------------------------------------------------
// Export Customization Dialog with Multi-Select Filters
// ----------------------------------------------------
@Composable
fun ExportCustomizationDialog(
    reportType: Int,
    reportTitle: String,
    isPdf: Boolean,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    dailyMovements: List<DailyMovementItem> = emptyList(),
    hideFinancials: Boolean,
    onDismiss: () -> Unit,
    onConfirmExport: (finalHeaders: List<String>, finalRows: List<List<String>>, finalSummaries: List<String>) -> Unit
) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    val selectedParts = remember { mutableStateListOf<String>() }
    val selectedCustomers = remember { mutableStateListOf<String>() }
    val selectedParties = remember { mutableStateListOf<String>() }
    val selectedReceivers = remember { mutableStateListOf<String>() }
    val selectedStatuses = remember { mutableStateListOf<String>() }

    val allPartNames = remember(partsWithInventory, dailyMovements) {
        (partsWithInventory.map { it.part.name } + dailyMovements.map { it.partName }).filter { it.isNotBlank() }.distinct().sorted()
    }
    val allCustomerNames = remember(customerOrders, dailyMovements) {
        (customerOrders.map { it.customer.name } + dailyMovements.map { it.entityName }).filter { it.isNotBlank() }.distinct().sorted()
    }
    val allParties = remember(stockIssuesWithDetails, dailyMovements) {
        (stockIssuesWithDetails.map { it.partyName } + dailyMovements.map { it.entityName }).filter { it.isNotBlank() }.distinct().sorted()
    }
    val allReceivers = remember(stockIssuesWithDetails, dailyMovements) {
        (stockIssuesWithDetails.map { it.issue.personName } + dailyMovements.map { it.personName }).filter { it.isNotBlank() }.distinct().sorted()
    }
    val statusMap = remember {
        mapOf("مؤكد" to "confirmed", "مكتمل جزئياً" to "partial", "مكتمل" to "completed", "ملغي" to "cancelled")
    }

    // Baseline headers and summaries for column selection
    val baseRaw = remember(reportType) {
        getReportRawData(
            reportType = reportType,
            partsWithInventory = partsWithInventory,
            customerOrders = customerOrders,
            stockIssuesWithDetails = stockIssuesWithDetails,
            supplierOrdersWithPart = supplierOrdersWithPart,
            dailyMovements = dailyMovements,
            hideFinancials = hideFinancials
        )
    }

    val selectedColumns = remember(baseRaw.first) {
        mutableStateListOf<Int>().apply { addAll(baseRaw.first.indices) }
    }
    val selectedSummaries = remember(baseRaw.third) {
        mutableStateListOf<Int>().apply { addAll(baseRaw.third.indices) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تخصيص وتصفية التصدير: $reportTitle (${if (isPdf) "PDF" else "Excel"})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Multi-select filters
                Text(
                    text = "فلاتر التقرير المخصصة (متعددة الاختيار):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryBlue
                )

                if (reportType in listOf(2, 3, 4)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = { fromDate = it },
                            label = { Text("من تاريخ (YYYY-MM-DD)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = toDate,
                            onValueChange = { toDate = it },
                            label = { Text("إلى تاريخ (YYYY-MM-DD)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (allPartNames.isNotEmpty()) {
                    MultiSelectSelector(
                        label = "الأصناف",
                        allOptions = allPartNames,
                        selectedOptions = selectedParts.toSet(),
                        onSelectionChanged = { set ->
                            selectedParts.clear()
                            selectedParts.addAll(set)
                        }
                    )
                }

                if (reportType == 2 && allCustomerNames.isNotEmpty()) {
                    MultiSelectSelector(
                        label = "العملاء",
                        allOptions = allCustomerNames,
                        selectedOptions = selectedCustomers.toSet(),
                        onSelectionChanged = { set ->
                            selectedCustomers.clear()
                            selectedCustomers.addAll(set)
                        }
                    )
                }

                if (reportType == 3) {
                    if (allParties.isNotEmpty()) {
                        MultiSelectSelector(
                            label = "الجهات المصروفة لها",
                            allOptions = allParties,
                            selectedOptions = selectedParties.toSet(),
                            onSelectionChanged = { set ->
                                selectedParties.clear()
                                selectedParties.addAll(set)
                            }
                        )
                    }
                    if (allReceivers.isNotEmpty()) {
                        MultiSelectSelector(
                            label = "المستلمين",
                            allOptions = allReceivers,
                            selectedOptions = selectedReceivers.toSet(),
                            onSelectionChanged = { set ->
                                selectedReceivers.clear()
                                selectedReceivers.addAll(set)
                            }
                        )
                    }
                }

                if (reportType == 4) {
                    MultiSelectSelector(
                        label = "حالة الطلبية",
                        allOptions = statusMap.keys.toList(),
                        selectedOptions = selectedStatuses.toSet(),
                        onSelectionChanged = { set ->
                            selectedStatuses.clear()
                            selectedStatuses.addAll(set)
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "الأعمدة المضمنة في الملف:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryBlue
                )

                baseRaw.first.forEachIndexed { colIdx, headerTitle ->
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
                        Text(headerTitle, fontSize = 12.sp)
                    }
                }

                if (baseRaw.third.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "عناصر الملخص والإحصائيات:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                    baseRaw.third.forEachIndexed { sumIdx, summaryTitle ->
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
                            Text(summaryTitle, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val convertedStatuses = selectedStatuses.mapNotNull { statusMap[it] }.toSet()
                    val raw = getReportRawData(
                        reportType = reportType,
                        partsWithInventory = partsWithInventory,
                        customerOrders = customerOrders,
                        stockIssuesWithDetails = stockIssuesWithDetails,
                        supplierOrdersWithPart = supplierOrdersWithPart,
                        dailyMovements = dailyMovements,
                        hideFinancials = hideFinancials,
                        selectedPartNames = selectedParts.toSet(),
                        selectedCustomerNames = selectedCustomers.toSet(),
                        selectedPartyNames = selectedParties.toSet(),
                        selectedReceiverNames = selectedReceivers.toSet(),
                        selectedStatuses = convertedStatuses,
                        fromDate = fromDate.trim(),
                        toDate = toDate.trim()
                    )

                    val validCols = selectedColumns.sorted().filter { it < raw.first.size }
                    val finalHeaders = if (validCols.isNotEmpty()) validCols.map { raw.first[it] } else raw.first
                    val finalRows = raw.second.map { row ->
                        if (validCols.isNotEmpty()) validCols.map { if (it < row.size) row[it] else "" } else row
                    }
                    val validSums = selectedSummaries.sorted().filter { it < raw.third.size }
                    val finalSummaries = if (validSums.isNotEmpty()) validSums.map { raw.third[it] } else raw.third

                    onConfirmExport(finalHeaders, finalRows, finalSummaries)
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

// -----------------------------------------------------------------------------------------
// Tab 6: Detailed Daily Movements Report (تقرير الحركات اليومية المفصل)
// -----------------------------------------------------------------------------------------
@Composable
fun DailyMovementsReportView(
    dailyMovements: List<DailyMovementItem>,
    hideFinancials: Boolean,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var showAdvancedFilters by remember { mutableStateOf(false) }

    val selectedParts = remember { mutableStateListOf<String>() }
    val selectedEntities = remember { mutableStateListOf<String>() }
    val selectedRecipients = remember { mutableStateListOf<String>() }

    val allMovementTypes = remember {
        listOf("الكل", "تسوية جردية", "سند قبض", "وارد توريد", "صرف مخزني")
    }

    val allParts = remember(dailyMovements) {
        dailyMovements.map { it.partName }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val allEntities = remember(dailyMovements) {
        dailyMovements.map { it.entityName }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val allRecipients = remember(dailyMovements) {
        dailyMovements.map { it.personName }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredMovements = remember(
        dailyMovements, searchQuery, selectedTypeFilter, fromDate, toDate,
        selectedParts.toList(), selectedEntities.toList(), selectedRecipients.toList()
    ) {
        dailyMovements.filter { item ->
            // Search Query Filter
            val queryMatch = searchQuery.isBlank() ||
                item.partName.contains(searchQuery, ignoreCase = true) ||
                item.carType.contains(searchQuery, ignoreCase = true) ||
                item.carModel.contains(searchQuery, ignoreCase = true) ||
                item.entityName.contains(searchQuery, ignoreCase = true) ||
                item.personName.contains(searchQuery, ignoreCase = true) ||
                item.type.contains(searchQuery, ignoreCase = true) ||
                item.batchOrInvoice.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true)

            // Movement Type Filter (exact matching types)
            val typeMatch = selectedTypeFilter == "الكل" || item.type == selectedTypeFilter

            // Date Range Filter
            val dateMatch = (fromDate.isBlank() || item.date >= fromDate.trim()) &&
                (toDate.isBlank() || item.date <= toDate.trim())

            // Multi-select Filters
            val partMatch = selectedParts.isEmpty() || selectedParts.contains(item.partName)
            val entityMatch = selectedEntities.isEmpty() || selectedEntities.contains(item.entityName)
            val recipientMatch = selectedRecipients.isEmpty() || selectedRecipients.contains(item.personName)

            queryMatch && typeMatch && dateMatch && partMatch && entityMatch && recipientMatch
        }
    }

    val totalOutgoingQty = remember(filteredMovements) {
        filteredMovements.filter { it.type == "صرف مخزني" }.sumOf { it.quantity }
    }
    val totalIncomingQty = remember(filteredMovements) {
        filteredMovements.filter { it.type == "وارد توريد" }.sumOf { it.quantity }
    }
    val totalMoneyValue = remember(filteredMovements) {
        filteredMovements.sumOf { it.totalPrice }
    }

    val expandedMovements = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Stats Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📊 ملخص حركات السجل اليومي المستخرجة",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي الحركات", fontSize = 11.sp)
                        Text("${filteredMovements.size} حركة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                    }
                    Column {
                        Text("الوارد (توريد)", fontSize = 11.sp)
                        Text("$totalIncomingQty قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                    }
                    Column {
                        Text("المنصرف (صرف)", fontSize = 11.sp)
                        Text("$totalOutgoingQty قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoseDanger)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إجمالي القيمة", fontSize = 11.sp)
                        Text(
                            text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", totalMoneyValue)} ر.س",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TechCyan
                        )
                    }
                }
            }
        }

        // Search Bar & Filter Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث ذكي: صنف، جهة، عميل، مستلم، فاتورة...", fontSize = 11.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            OutlinedButton(
                onClick = { showAdvancedFilters = !showAdvancedFilters },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                colors = if (showAdvancedFilters) ButtonDefaults.outlinedButtonColors(containerColor = PrimaryLighter) else ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصفية", fontSize = 11.sp, color = PrimaryBlue)
            }
        }

        // Quick Movement Type Filter Chips - Named exactly like the movements
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allMovementTypes) { type ->
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = type },
                    label = { Text(type, fontSize = 11.sp) },
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        // Advanced Filter Drawer (Date range + multi-select for parts, entities, recipients)
        AnimatedVisibility(visible = showAdvancedFilters) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔍 معايير البحث والتصفية المتقدمة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                    // Date range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = { fromDate = it },
                            label = { Text("من تاريخ (yyyy-MM-dd)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = toDate,
                            onValueChange = { toDate = it },
                            label = { Text("إلى تاريخ (yyyy-MM-dd)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Multi-select for parts, entities, recipients
                    MultiSelectSelector(
                        label = "تحديد الأصناف (صنف أو أكثر)",
                        allOptions = allParts,
                        selectedOptions = selectedParts.toSet(),
                        onSelectionChanged = { set ->
                            selectedParts.clear()
                            selectedParts.addAll(set)
                        }
                    )

                    MultiSelectSelector(
                        label = "تحديد الجهات / العملاء (جهة أو أكثر)",
                        allOptions = allEntities,
                        selectedOptions = selectedEntities.toSet(),
                        onSelectionChanged = { set ->
                            selectedEntities.clear()
                            selectedEntities.addAll(set)
                        }
                    )

                    MultiSelectSelector(
                        label = "تحديد المستلمين (مستلم أو أكثر)",
                        allOptions = allRecipients,
                        selectedOptions = selectedRecipients.toSet(),
                        onSelectionChanged = { set ->
                            selectedRecipients.clear()
                            selectedRecipients.addAll(set)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                fromDate = ""
                                toDate = ""
                                selectedParts.clear()
                                selectedEntities.clear()
                                selectedRecipients.clear()
                                selectedTypeFilter = "الكل"
                                searchQuery = ""
                            }
                        ) {
                            Text("إعادة تعيين الفلاتر", fontSize = 11.sp, color = RoseDanger)
                        }
                    }
                }
            }
        }

        // Daily Movements List
        if (filteredMovements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("لا توجد حركات يومية تطابق معايير البحث والفلترة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMovements) { item ->
                    val isExpanded = expandedMovements[item.id] ?: false
                    val (badgeBg, badgeColor) = when (item.type) {
                        "وارد توريد" -> Pair(EmeraldLight, EmeraldSuccess)
                        "صرف مخزني" -> Pair(RoseLight, RoseDanger)
                        "سند قبض" -> Pair(TechCyanLight, TechCyan)
                        "تسوية جردية" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMovements[item.id] = !isExpanded },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Row: Type Badge + Date & Time + Arrow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(item.type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                    }
                                    if (item.batchOrInvoice.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item.batchOrInvoice, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.dateTimeStr, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "إخفاء التفاصيل" else "عرض التفاصيل",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Part and Vehicle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.partName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    if (item.carType.isNotBlank() || item.carModel.isNotBlank()) {
                                        Text("${item.carType} • ${item.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (item.quantity > 0) {
                                        Text("${item.quantity} قطعة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                    }
                                    if (item.unitPrice > 0) {
                                        Text(
                                            text = if (hideFinancials) "***" else "الوحدة: ${item.unitPrice} ر.س",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Entity and Person Details Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.entityName.isNotBlank()) "🏢 الجهة/العميل: ${item.entityName}" else "—",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                if (item.personName.isNotBlank()) {
                                    Text("👤 القائم بالحركة: ${item.personName}", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                            }

                            if (item.notes.isNotBlank() && !isExpanded) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📝 ملاحظات: ${item.notes}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }

                            // Full Expanded Details Dropdown / Drawer
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "📋 كافة تفاصيل الحركة اليومية:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PrimaryBlue
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                    // 1. وقت وتاريخ الحركة
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("⏰ وقت وتاريخ الحركة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(item.dateTimeStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    // 2. اسم المستخدم
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("👤 اسم المستخدم:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(item.userName.ifBlank { appUserName }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    }

                                    // 3. نوع الحركة
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🏷️ نوع الحركة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(item.type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                    }

                                    // 4. اسم الصنف وموديل السيارة والباركود
                                    if (item.partName.isNotBlank() && item.partName != "دفعة حساب عميل") {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🔧 الصنف والسيارة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${item.partName} (${item.carType} ${item.carModel})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    if (item.barcode.isNotBlank()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🔢 رقم الباركود:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(item.barcode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechCyan)
                                        }
                                    }

                                    // 5. الكمية والأسعار
                                    if (item.quantity > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("📦 الكمية الصادرة/المستلمة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${item.quantity} قطعة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                        }
                                    }
                                    if (item.totalPrice > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("💰 القيمة المالية الإجمالية:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (hideFinancials) "*** ر.س" else "${String.format(java.util.Locale.US, "%.1f", item.totalPrice)} ر.س",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess
                                            )
                                        }
                                    }

                                    // 6. الجهة والشخص
                                    if (item.entityName.isNotBlank()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🏢 الجهة / العميل:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(item.entityName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    if (item.personName.isNotBlank()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("👤 المستلم / القائم بالحركة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(item.personName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    // 7. رقم السند أو الدفعة
                                    if (item.batchOrInvoice.isNotBlank()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🧾 رقم السند / المعاملة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(item.batchOrInvoice, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // 8. الملاحظات الكاملة
                                    if (item.notes.isNotBlank()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("📝 الملاحظات والبيان:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(item.notes, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    // 9. زر مطالعة المرفق إذا وجد
                                    if (!item.attachmentUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { AttachmentUtils.openAttachment(context, item.attachmentUri) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            modifier = Modifier.fillMaxWidth().height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("معاينة سند المرفق داخل التطبيق", fontSize = 11.sp)
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
