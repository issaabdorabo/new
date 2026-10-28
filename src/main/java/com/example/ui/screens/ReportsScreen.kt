package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
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
        // Report Selector Chips
        Text(
            text = "📈 تقارير المتجر والمستودع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

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

        // Export and Share Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تقرير: ${reportTitles[selectedReportType]}",
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Button(
                onClick = {
                    exportAndShareReport(
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
                modifier = Modifier.testTag("btn_export_report")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير ومشاركة", fontSize = 12.sp)
            }
        }

        HorizontalDivider()

        // Report Content View
        when (selectedReportType) {
            0 -> CurrentInventoryReport(partsWithInventory)
            1 -> LowStockReport(partsWithInventory.filter { it.isLowStock })
            2 -> CustomerOrdersReport(customerOrders)
            3 -> StockIssuesReport(stockIssuesWithDetails)
            4 -> SupplierOrdersReport(supplierOrdersWithPart)
        }
    }
}

@Composable
fun CurrentInventoryReport(parts: List<PartWithInventory>) {
    val totalQty = parts.sumOf { it.availableQty }
    val totalVal = parts.sumOf { it.availableQty * it.part.price }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryLighter),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي الأصناف", fontSize = 11.sp, color = PrimaryBlue)
                    Text("${parts.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي القطع", fontSize = 11.sp, color = PrimaryBlue)
                    Text("$totalQty", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("القيمة التقديرية", fontSize = 11.sp, color = PrimaryBlue)
                    Text("$totalVal ر.س", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(parts) { item ->
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
                            Text("${item.part.carType} - ${item.part.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.availableQty} قطعة", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text("${item.availableQty * item.part.price} ر.س", fontSize = 11.sp, color = AmberSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockReport(lowParts: List<PartWithInventory>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (lowParts.isEmpty()) EmeraldLight else RoseLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (lowParts.isEmpty()) "🎉 ممتاز! لا توجد أصناف تحت الحد الأدنى حالياً"
                else "⚠️ يوجد ${lowParts.size} أصناف وصلت لمرحلة الخطر أو أقل من الحد الأدنى",
                fontWeight = FontWeight.Bold,
                color = if (lowParts.isEmpty()) EmeraldSuccess else RoseDanger,
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lowParts) { item ->
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
                            Text("${item.part.carType} - ${item.part.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("المتوفر: ${item.availableQty}", fontWeight = FontWeight.Bold, color = RoseDanger)
                            Text("الحد الأدنى: ${item.part.minStock}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOrdersReport(orders: List<CustomerWithOrder>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(orders) { custWithOrder ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "العميل: ${custWithOrder.customer.name}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "${custWithOrder.totalQty} قطعة",
                            fontWeight = FontWeight.Bold,
                            color = AmberSecondary
                        )
                    }
                    if (custWithOrder.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        custWithOrder.items.forEach { item ->
                            Text(
                                text = "• ${item.part.name}: ${item.item.quantity} قطعة (${item.part.price * item.item.quantity} ر.س)",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockIssuesReport(issues: List<StockIssueWithDetails>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(issues) { item ->
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
                        Text(item.partName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "الجهة: ${item.partyName} | المستلم: ${item.issue.personName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("التاريخ: ${item.issue.issueDate}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text("- ${item.issue.quantity} قطعة", fontWeight = FontWeight.Bold, color = RoseDanger)
                }
            }
        }
    }
}

@Composable
fun SupplierOrdersReport(orders: List<SupplierOrderWithPart>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { item ->
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
                        Text("الحالة: ${order.status} | تاريخ الطلب: ${order.orderDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مطلوب: ${order.qtyOrdered}", fontSize = 12.sp)
                        Text("متبقي: ${order.qtyRemaining}", fontWeight = FontWeight.Bold, color = if (order.qtyRemaining > 0) RoseDanger else EmeraldSuccess)
                    }
                }
            }
        }
    }
}

fun exportAndShareReport(
    context: Context,
    reportType: Int,
    reportTitle: String,
    partsWithInventory: List<PartWithInventory>,
    customerOrders: List<CustomerWithOrder>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>
) {
    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine("📊 تقرير: $reportTitle - قطع غيار Pro")
    sb.appendLine("التاريخ: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
    sb.appendLine("==========================================")

    when (reportType) {
        0 -> {
            sb.appendLine("الصنف | السيارة | المتوفر | السعر | الإجمالي")
            partsWithInventory.forEach { item ->
                sb.appendLine("${item.part.name} | ${item.part.carType} ${item.part.carModel} | ${item.availableQty} | ${item.part.price} | ${item.availableQty * item.part.price}")
            }
            sb.appendLine("------------------------------------------")
            sb.appendLine("إجمالي المخزون: ${partsWithInventory.sumOf { it.availableQty }} قطعة")
            sb.appendLine("القيمة التقديرية: ${partsWithInventory.sumOf { it.availableQty * it.part.price }} ر.س")
        }
        1 -> {
            sb.appendLine("الأصناف تحت الحد الأدنى:")
            val low = partsWithInventory.filter { it.isLowStock }
            low.forEach { item ->
                sb.appendLine("- ${item.part.name} (${item.part.carType}): متوفر ${item.availableQty} / الحد الأدنى ${item.part.minStock}")
            }
            sb.appendLine("العدد الإجمالي للأصناف الناقصة: ${low.size}")
        }
        2 -> {
            customerOrders.forEach { c ->
                sb.appendLine("العميل: ${c.customer.name} (هاتف: ${c.customer.phone})")
                c.items.forEach { item ->
                    sb.appendLine("  • ${item.part.name}: ${item.item.quantity} قطعة")
                }
                sb.appendLine("  مجموع القطع: ${c.totalQty}")
                sb.appendLine("------------------------------------------")
            }
        }
        3 -> {
            sb.appendLine("سجل عمليات الصرف:")
            stockIssuesWithDetails.forEach { issue ->
                sb.appendLine("- التاريخ: ${issue.issue.issueDate} | الصنف: ${issue.partName} | الكمية: ${issue.issue.quantity} | الجهة: ${issue.partyName} | المستلم: ${issue.issue.personName}")
            }
        }
        4 -> {
            sb.appendLine("طلبات الموردين:")
            supplierOrdersWithPart.forEach { o ->
                sb.appendLine("- الصنف: ${o.part?.name} | مطلوب: ${o.supplierOrder.qtyOrdered} | واصل: ${o.supplierOrder.qtyReceived} | متبقي: ${o.supplierOrder.qtyRemaining} | الحالة: ${o.supplierOrder.status}")
            }
        }
    }

    sb.appendLine("==========================================")
    sb.appendLine("تم الاستخراج عبر تطبيق قطع غيار Pro")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة $reportTitle"))
}
