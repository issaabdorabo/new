package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    supplierOrdersWithPart: List<SupplierOrderWithPart>
) {
    val context = LocalContext.current
    var selectedReportType by remember { mutableIntStateOf(0) }

    val reportTitles = listOf(
        "المخزون الحالي",
        "القطع الناقصة",
        "طلبات العملاء",
        "حركة الصرف",
        "طلبات الموردين"
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
                    text = "اختر التقرير واستخرجه بصيغة Excel أو PDF",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Export Excel Button
                OutlinedButton(
                    onClick = {
                        generateAndExportExcel(
                            context = context,
                            reportType = selectedReportType,
                            reportTitle = reportTitles[selectedReportType],
                            partsWithInventory = partsWithInventory,
                            customerOrders = customerOrders,
                            stockIssuesWithDetails = stockIssuesWithDetails,
                            supplierOrdersWithPart = supplierOrdersWithPart
                        )
                    },
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }

                // Export PDF Button
                Button(
                    onClick = {
                        generateAndExportPdf(
                            context = context,
                            reportType = selectedReportType,
                            reportTitle = reportTitles[selectedReportType],
                            partsWithInventory = partsWithInventory,
                            customerOrders = customerOrders,
                            stockIssuesWithDetails = stockIssuesWithDetails,
                            supplierOrdersWithPart = supplierOrdersWithPart
                        )
                    },
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
        }
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

// ----------------------------------------------------
// PDF & Excel Report Generators
// ----------------------------------------------------

fun generateAndExportExcel(
    context: Context,
    reportType: Int,
    reportTitle: String,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>
) {
    val headers: List<String>
    val rows: List<List<String>>
    val summaries: List<String>

    when (reportType) {
        0 -> { // Current Inventory
            headers = listOf("الصنف", "السيارة", "الموديل", "الكمية المتوفرة", "الحد الأدنى", "السعر", "القيمة الإجمالية")
            rows = partsWithInventory.map { item ->
                listOf(
                    item.part.name,
                    item.part.carType,
                    item.part.carModel,
                    item.availableQty.toString(),
                    item.part.minStock.toString(),
                    "${item.part.price} ر.س",
                    "${item.availableQty * item.part.price} ر.س"
                )
            }
            summaries = listOf(
                "إجمالي عدد الأصناف: ${partsWithInventory.size}",
                "إجمالي القطع المتوفرة: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية الإجمالية للمخزون: ${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س"
            )
        }
        1 -> { // Low Stock
            val low = partsWithInventory.filter { it.isLowStock }
            headers = listOf("الصنف", "السيارة", "الكمية الحالية", "الحد الأدنى للمخزون", "النقص المطلوب تعويضه")
            rows = low.map { item ->
                val deficit = maxOf(0, item.part.minStock - item.availableQty)
                listOf(
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    item.part.minStock.toString(),
                    "$deficit قطعة"
                )
            }
            summaries = listOf(
                "عدد الأصناف الناقصة: ${low.size} صنف",
                "إجمالي النقص الإجمالي: ${low.sumOf { maxOf(0, it.part.minStock - it.availableQty) }} قطعة"
            )
        }
        2 -> { // Customer Orders
            headers = listOf("العميل", "الهاتف", "إجمالي القطع", "القيمة الإجمالية", "المبلغ الواصل", "المتبقي", "حالة السداد")
            rows = customerOrders.map { c ->
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
            summaries = listOf(
                "إجمالي عدد العملاء: ${customerOrders.size}",
                "إجمالي القطع المطلوبة: ${customerOrders.sumOf { it.totalQty }} قطعة"
            )
        }
        3 -> { // Stock Issues
            headers = listOf("التاريخ", "الصنف", "الكمية المصروفة", "الجهة المصروفة لها", "المستلم", "ملاحظات")
            rows = stockIssuesWithDetails.map { issue ->
                listOf(
                    issue.issue.issueDate,
                    issue.partName,
                    issue.issue.quantity.toString(),
                    issue.partyName,
                    issue.issue.personName,
                    issue.issue.note
                )
            }
            summaries = listOf(
                "عدد عمليات الصرف: ${stockIssuesWithDetails.size}",
                "إجمالي القطع المصروفة: ${stockIssuesWithDetails.sumOf { it.issue.quantity }} قطعة"
            )
        }
        else -> { // Supplier Orders
            headers = listOf("رقم الطلب", "الصنف", "السيارة", "المطلوب", "الواصل", "المتبقي", "تاريخ الطلب", "تاريخ التأكيد", "الحالة")
            rows = supplierOrdersWithPart.map { o ->
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
            summaries = listOf(
                "إجمالي عدد طلبيات التوريد: ${supplierOrdersWithPart.size}",
                "إجمالي المطلوب من الموردين: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
                "إجمالي المستلم بالمستودع: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyReceived }} قطعة",
                "المتبقي قيد التوريد: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
            )
        }
    }

    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Report_${reportTitle.replace(" ", "_")}",
        title = "تقرير: $reportTitle",
        headers = headers,
        rows = rows,
        summaryLines = summaries
    )
}

fun generateAndExportPdf(
    context: Context,
    reportType: Int,
    reportTitle: String,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>
) {
    val headers: List<String>
    val rows: List<List<String>>
    val summaries: List<String>

    when (reportType) {
        0 -> { // Inventory
            headers = listOf("الصنف", "السيارة", "الكمية", "السعر", "الإجمالي")
            rows = partsWithInventory.map { item ->
                listOf(
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    "${item.part.price} ر.س",
                    "${item.availableQty * item.part.price} ر.س"
                )
            }
            summaries = listOf(
                "إجمالي الأصناف المسجلة: ${partsWithInventory.size} صنف",
                "إجمالي عدد القطع المتوفرة: ${partsWithInventory.sumOf { it.availableQty }} قطعة",
                "القيمة التقديرية الإجمالية للمخزون: ${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س"
            )
        }
        1 -> { // Low stock
            val low = partsWithInventory.filter { it.isLowStock }
            headers = listOf("الصنف", "السيارة", "الحالي", "الحد الأدنى", "النقص")
            rows = low.map { item ->
                val deficit = maxOf(0, item.part.minStock - item.availableQty)
                listOf(
                    item.part.name,
                    item.part.carType,
                    item.availableQty.toString(),
                    item.part.minStock.toString(),
                    "$deficit قطعة"
                )
            }
            summaries = listOf(
                "الأصناف التي وصلت للحد الأدنى: ${low.size} صنف",
                "ينصح بإنشاء طلبية توريد عاجلة للموردين لتعويض النقص"
            )
        }
        2 -> { // Customer orders
            headers = listOf("العميل", "الهاتف", "القطع", "الإجمالي", "الواصل", "المتبقي")
            rows = customerOrders.map { c ->
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
                    "$remaining ر.س"
                )
            }
            summaries = listOf(
                "إجمالي عدد العملاء: ${customerOrders.size}",
                "إجمالي القطع المطلوبة: ${customerOrders.sumOf { it.totalQty }} قطعة"
            )
        }
        3 -> { // Stock issues
            headers = listOf("التاريخ", "الصنف", "الكمية", "الجهة", "المستلم")
            rows = stockIssuesWithDetails.map { issue ->
                listOf(
                    issue.issue.issueDate,
                    issue.partName,
                    issue.issue.quantity.toString(),
                    issue.partyName,
                    issue.issue.personName
                )
            }
            summaries = listOf(
                "إجمالي عمليات الصرف: ${stockIssuesWithDetails.size}",
                "إجمالي الكميات المصروفة: ${stockIssuesWithDetails.sumOf { it.issue.quantity }} قطعة"
            )
        }
        else -> { // Supplier orders
            headers = listOf("رقم الطلب", "الصنف", "المطلوب", "الواصل", "المتبقي", "تاريخ الطلب", "التأكيد")
            rows = supplierOrdersWithPart.map { o ->
                listOf(
                    "#${o.supplierOrder.id}",
                    o.part?.name ?: "صنف #${o.supplierOrder.partId}",
                    o.supplierOrder.qtyOrdered.toString(),
                    o.supplierOrder.qtyReceived.toString(),
                    o.supplierOrder.qtyRemaining.toString(),
                    o.supplierOrder.orderDate,
                    o.supplierOrder.confirmedDate
                )
            }
            summaries = listOf(
                "إجمالي طلبيات التوريد: ${supplierOrdersWithPart.size}",
                "إجمالي المطلوب: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
                "إجمالي المستلم: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyReceived }} قطعة",
                "المتبقي قيد التوريد: ${supplierOrdersWithPart.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
            )
        }
    }

    ExportUtils.exportToPdf(
        context = context,
        fileName = "Report_${reportTitle.replace(" ", "_")}",
        title = "تقرير: $reportTitle",
        subtitle = "تقرير صادر وموثق من نظام قطع غيار Pro",
        headers = headers,
        rows = rows,
        summaryLines = summaries
    )
}
