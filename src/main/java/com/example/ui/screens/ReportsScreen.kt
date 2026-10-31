package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Screen Header & Dual Export Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📈 تقارير المتجر والمستودع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تخصيص الأعمدة والملخص واستخراج بصيغة Excel أو PDF",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Export Excel Button (opens customization dialog)
                OutlinedButton(
                    onClick = { pendingExportIsPdf = false },
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }

                // Export PDF Button (opens customization dialog)
                Button(
                    onClick = { pendingExportIsPdf = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
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
                    label = { Text(reportTitles[idx], fontSize = 12.sp) },
                    modifier = Modifier.testTag("report_chip_$idx")
                )
            }
        }

        // Report Content Cards
        when (selectedReportType) {
            0 -> InventoryReportView(partsWithInventory)
            1 -> LowStockReportView(partsWithInventory)
            2 -> CustomerOrdersReportView(customerOrders)
            3 -> StockIssuesReportView(stockIssuesWithDetails)
            4 -> SupplierOrdersReportView(supplierOrdersWithPart)
            5 -> DetailedComprehensiveReportView(partsWithInventory, customerOrders, supplierOrdersWithPart)
        }
    }

    // Export Customization Dialog (User chooses columns & summary items)
    pendingExportIsPdf?.let { isPdf ->
        val rawData = getReportRawData(
            reportType = selectedReportType,
            partsWithInventory = partsWithInventory,
            customerOrders = customerOrders,
            stockIssuesWithDetails = stockIssuesWithDetails,
            supplierOrdersWithPart = supplierOrdersWithPart
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

@Composable
fun InventoryReportView(items: List<PartWithInventory>) {
    val totalQty = items.sumOf { it.availableQty }
    val totalEstimatedValue = items.sumOf { it.availableQty * it.part.price }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي القطع بالمخزن", fontSize = 11.sp)
                    Text("$totalQty قطعة", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("القيمة التقديرية للمخزون", fontSize = 11.sp)
                    Text("$totalEstimatedValue ر.س", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AmberSecondary)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items) { item ->
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
                            Text("${item.part.carType} • ${item.part.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.availableQty} قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${item.availableQty * item.part.price} ر.س", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockReportView(items: List<PartWithInventory>) {
    val lowStock = items.filter { it.isLowStock }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RoseLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الأصناف التي وصلت للحد الأدنى", fontWeight = FontWeight.Bold, color = RoseDanger)
                Text("${lowStock.size} أصناف", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoseDanger)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(lowStock) { item ->
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
                        Text(
                            text = "المتبقي: ${item.availableQty}",
                            fontWeight = FontWeight.Bold,
                            color = RoseDanger,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOrdersReportView(customerOrders: List<CustomerWithOrder>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(customerOrders) { c ->
            val order = c.activeOrder
            val totalCost = c.items.sumOf { it.item.quantity * it.part.price }
            val paid = order?.paidAmount ?: 0.0
            val remaining = maxOf(0.0, totalCost - paid)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(c.customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${c.totalQty} قطعة", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                    Text(
                        text = "الإجمالي: $totalCost ر.س | الواصل: $paid ر.س | المتبقي: $remaining ر.س",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    c.items.forEach { item ->
                        Text("• ${item.part.name}: ${item.item.quantity} قطعة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StockIssuesReportView(issues: List<StockIssueWithDetails>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(issues) { issue ->
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

@Composable
fun SupplierOrdersReportView(supplierOrders: List<SupplierOrderWithPart>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(supplierOrders) { item ->
            val order = item.supplierOrder
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
                        Text(item.part?.name ?: "صنف #${order.partId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("الحالة: ${order.status} • تاريخ الطلب: ${order.orderDate} • التأكيد: ${order.confirmedDate}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
fun DetailedComprehensiveReportView(
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>
) {
    val totalPartsCount = partsWithInventory.size
    val totalInvPieces = partsWithInventory.sumOf { it.availableQty }
    val totalInvValue = partsWithInventory.sumOf { it.availableQty * it.part.price }
    val totalCustDemand = customerOrders.sumOf { it.totalQty }
    val totalSuppPending = supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // High-level summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📊 ملخص التقرير التفصيلي الشامل للمتجر والمستودع",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 13.5.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("قيمة المخزون", fontSize = 11.sp)
                        Text("$totalInvValue ر.س", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AmberSecondary)
                    }
                    Column {
                        Text("القطع المتوفرة", fontSize = 11.sp)
                        Text("$totalInvPieces قطعة", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = EmeraldSuccess)
                    }
                    Column {
                        Text("طلب العملاء", fontSize = 11.sp)
                        Text("$totalCustDemand قطعة", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlue)
                    }
                    Column {
                        Text("قيد التوريد", fontSize = 11.sp)
                        Text("$totalSuppPending قطعة", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoseDanger)
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(partsWithInventory) { item ->
                val p = item.part
                val lineVal = item.availableQty * p.price
                val neededByCustomers = customerOrders.sumOf { co ->
                    co.items.filter { it.part.id == p.id }.sumOf { it.item.quantity }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Text("${p.carType} • ${p.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$lineVal ر.س", fontWeight = FontWeight.Bold, color = AmberSecondary)
                                Text("${p.price} ر.س / قطعة", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("المتوفر بالمخزن: ${item.availableQty}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("مطلوب للعملاء: $neededByCustomers", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            val isLow = item.availableQty <= p.minStock
                            Text(if (isLow) "نقص في المخزون" else "مخزون آمن", fontSize = 11.sp, color = if (isLow) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Export Raw Data Extraction (Headers, Rows, Summaries for all 6 reports)
// ----------------------------------------------------

fun getReportRawData(
    reportType: Int,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>
): Triple<List<String>, List<List<String>>, List<String>> {
    return when (reportType) {
        0 -> { // Inventory
            val headers = listOf("الصنف", "السيارة", "الكمية", "السعر", "الإجمالي")
            val rows = partsWithInventory.map { item ->
                listOf(
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    "${item.part.price} ر.س",
                    "${item.availableQty * item.part.price} ر.س"
                )
            }
            val summaries = listOf(
                "إجمالي الأصناف المسجلة: ${partsWithInventory.size} صنف",
                "إجمالي عدد القطع المتوفرة بالمستودع: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية الإجمالية للمخزون: ${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س"
            )
            Triple(headers, rows, summaries)
        }
        1 -> { // Low stock
            val low = partsWithInventory.filter { it.isLowStock }
            val headers = listOf("الصنف", "السيارة", "المتوفر", "الحد الأدنى", "مقدار النقص")
            val rows = low.map { item ->
                val deficit = maxOf(0, item.part.minStock - item.availableQty)
                listOf(
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
            val headers = listOf("العميل", "الهاتف", "إجمالي القطع", "القيمة الإجمالية", "المبلغ الواصل", "المتبقي", "حالة السداد")
            val rows = customerOrders.map { c ->
                val order = c.activeOrder
                val cost = c.items.sumOf { it.item.quantity * it.part.price }
                val paid = order?.paidAmount ?: 0.0
                val remaining = maxOf(0.0, cost - paid)
                listOf(
                    c.customer.name,
                    c.customer.phone,
                    c.totalQty.toString(),
                    "$cost ر.س",
                    "$paid ر.س",
                    "$remaining ر.س",
                    order?.paymentStatus ?: "غير مسدد"
                )
            }
            val summaries = listOf(
                "إجمالي عدد طلبات العملاء: ${customerOrders.size}",
                "إجمالي القطع المطلوبة للعملاء: ${customerOrders.sumOf { it.totalQty }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
        3 -> { // Stock issues
            val headers = listOf("التاريخ", "الصنف", "الكمية المصروفة", "الجهة المصروفة لها", "المستلم", "ملاحظات")
            val rows = stockIssuesWithDetails.map { issue ->
                listOf(
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
            val headers = listOf("رقم الطلب", "الصنف", "السيارة", "المطلوب", "الواصل", "المتبقي", "تاريخ الطلب", "تاريخ التأكيد", "الحالة")
            val rows = supplierOrdersWithPart.map { o ->
                listOf(
                    "#${o.supplierOrder.id}",
                    o.part?.name ?: "صنف #${o.supplierOrder.partId}",
                    o.part?.carType ?: "",
                    o.supplierOrder.qtyOrdered.toString(),
                    o.supplierOrder.qtyReceived.toString(),
                    o.supplierOrder.qtyRemaining.toString(),
                    o.supplierOrder.orderDate,
                    o.supplierOrder.confirmedDate,
                    o.supplierOrder.status
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
            val headers = listOf("الصنف", "السيارة", "المتوفر بالمخزن", "سعر القطعة", "إجمالي القيمة", "طلب العملاء", "حالة الرصيد")
            val rows = partsWithInventory.map { item ->
                val p = item.part
                val lineVal = item.availableQty * p.price
                val neededByCustomers = customerOrders.sumOf { co ->
                    co.items.filter { it.part.id == p.id }.sumOf { it.item.quantity }
                }
                listOf(
                    p.name,
                    "${p.carType} ${p.carModel}",
                    item.availableQty.toString(),
                    "${p.price} ر.س",
                    "$lineVal ر.س",
                    "$neededByCustomers قطعة",
                    if (item.availableQty <= p.minStock) "نقص مخزون" else "متوفر وآمن"
                )
            }
            val summaries = listOf(
                "إجمالي أصناف المستودع: ${partsWithInventory.size} صنف",
                "إجمالي القطع المتوفرة: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية لكافة المخزون: ${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س",
                "إجمالي طلبات العملاء المعلقة: ${customerOrders.sumOf { it.totalQty }} قطعة",
                "إجمالي القطع قيد التوريد من الموردين: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
            )
            Triple(headers, rows, summaries)
        }
    }
}

// ----------------------------------------------------
// Export Customization Dialog (User checks/unchecks columns & summary items)
// ----------------------------------------------------

@Composable
fun ExportCustomizationDialog(
    reportTitle: String,
    isPdf: Boolean,
    allHeaders: List<String>,
    allSummaries: List<String>,
    onDismiss: () -> Unit,
    onConfirmExport: (selectedColumnIndices: List<Int>, selectedSummaryIndices: List<Int>) -> Unit
) {
    val selectedHeaders = remember { mutableStateListOf(*allHeaders.indices.toList().toTypedArray()) }
    val selectedSummaries = remember { mutableStateListOf(*allSummaries.indices.toList().toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تخصيص تصدير ${if (isPdf) "PDF" else "Excel"}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "الأعمدة المراد إظهارها في التقرير:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                }
                items(allHeaders.indices.toList()) { idx ->
                    val isChecked = selectedHeaders.contains(idx)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    if (selectedHeaders.size > 1) selectedHeaders.remove(idx)
                                } else {
                                    selectedHeaders.add(idx)
                                }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) selectedHeaders.add(idx)
                                else if (selectedHeaders.size > 1) selectedHeaders.remove(idx)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(allHeaders[idx], fontSize = 12.5.sp)
                    }
                }

                if (allSummaries.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "عناصر ملخص التقرير المراد إظهارها:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                    }
                    items(allSummaries.indices.toList()) { idx ->
                        val isChecked = selectedSummaries.contains(idx)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedSummaries.remove(idx)
                                    else selectedSummaries.add(idx)
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedSummaries.add(idx)
                                    else selectedSummaries.remove(idx)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(allSummaries[idx], fontSize = 11.5.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmExport(selectedHeaders.sorted(), selectedSummaries.sorted())
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
