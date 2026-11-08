package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.WarehouseItemStock
import com.example.data.repository.StockSnapshot
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: InventoryViewModel,
    snapshot: StockSnapshot?
) {
    val context = LocalContext.current
    val appUserName by viewModel.userName.collectAsState()
    val orgName by viewModel.orgName.collectAsState()

    var selectedWarehouseId by remember { mutableStateOf<Long?>(null) } // null = All Warehouses
    var reportType by remember { mutableStateOf("INVENTORY") } // INVENTORY or LOW_STOCK

    // Selected columns filter
    val selectedColumns = remember {
        mutableStateListOf(
            ReportGenerator.ReportColumn.SERIAL,
            ReportGenerator.ReportColumn.ITEM_NAME,
            ReportGenerator.ReportColumn.ITEM_UNIT,
            ReportGenerator.ReportColumn.WAREHOUSE,
            ReportGenerator.ReportColumn.CURRENT_STOCK,
            ReportGenerator.ReportColumn.MIN_STOCK,
            ReportGenerator.ReportColumn.PURCHASE_PRICE,
            ReportGenerator.ReportColumn.TOTAL_VALUE
        )
    }

    val warehouses = snapshot?.warehouses ?: emptyList()
    val rawData = snapshot?.warehouseItemStocks ?: emptyList()

    // Filter and Alphabetical Sort
    val reportData = remember(rawData, selectedWarehouseId, reportType) {
        val filtered = rawData.filter { itemStock ->
            val matchesWarehouse = selectedWarehouseId == null || itemStock.warehouseId == selectedWarehouseId
            val matchesType = if (reportType == "LOW_STOCK") itemStock.currentStock <= itemStock.minStockLevel else true
            matchesWarehouse && matchesType
        }
        // Strict alphabetical sorting (أ-ي)
        filtered.sortedBy { it.itemName.lowercase(Locale.getDefault()) }
    }

    val selectedWarehouseName = remember(warehouses, selectedWarehouseId) {
        if (selectedWarehouseId == null) "كافة المخازن"
        else warehouses.find { it.id == selectedWarehouseId }?.name ?: "المخزن المحدد"
    }

    var lastGeneratedPdfFile by remember { mutableStateOf<File?>(null) }
    var lastGeneratedExcelFile by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Report Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إعدادات وتخصيص التقرير",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Report Type Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = reportType == "INVENTORY",
                            onClick = { reportType = "INVENTORY" },
                            label = { Text("جرد المخزون العام") }
                        )
                        FilterChip(
                            selected = reportType == "LOW_STOCK",
                            onClick = { reportType = "LOW_STOCK" },
                            label = { Text("أصناف نقص المخزون") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Warehouse Dropdown
                    var warehouseDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = warehouseDropdownExpanded,
                        onExpandedChange = { warehouseDropdownExpanded = !warehouseDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedWarehouseName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المخزن المستهدف بالتقرير") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = warehouseDropdownExpanded,
                            onDismissRequest = { warehouseDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("كافة المخازن (تقرير شامل)") },
                                onClick = {
                                    selectedWarehouseId = null
                                    warehouseDropdownExpanded = false
                                }
                            )
                            warehouses.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text(w.name) },
                                    onClick = {
                                        selectedWarehouseId = w.id
                                        warehouseDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Column Selector Checkboxes
                    Text(
                        text = "تحديد الأعمدة المطلوب إظهارها في التقرير:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val allCols = ReportGenerator.ReportColumn.values()
                    val chunked = allCols.toList().chunked(2)
                    chunked.forEach { rowCols ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCols.forEach { col ->
                                val isChecked = selectedColumns.contains(col)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedColumns.add(col)
                                            else if (selectedColumns.size > 1) selectedColumns.remove(col)
                                        }
                                    )
                                    Text(
                                        text = col.titleAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Export & Share Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "تصدير ومشاركة التقرير",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val file = ReportGenerator.generatePdf(
                                    context = context,
                                    title = if (reportType == "LOW_STOCK") "تقرير نواقص المخزون" else "تقرير جرد المخزون",
                                    warehouseName = selectedWarehouseName,
                                    appUserName = appUserName,
                                    orgName = orgName,
                                    data = reportData,
                                    selectedColumns = selectedColumns.toSet()
                                )
                                lastGeneratedPdfFile = file
                                ReportGenerator.viewFile(context, file, "application/pdf")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير PDF", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val file = ReportGenerator.generateExcel(
                                    context = context,
                                    title = if (reportType == "LOW_STOCK") "تقرير نواقص المخزون" else "تقرير جرد المخزون",
                                    warehouseName = selectedWarehouseName,
                                    appUserName = appUserName,
                                    data = reportData,
                                    selectedColumns = selectedColumns.toSet()
                                )
                                lastGeneratedExcelFile = file
                                Toast.makeText(context, "تم حفظ تقرير إكسل بصيغة CSV بنجاح", Toast.LENGTH_SHORT).show()
                                ReportGenerator.shareFile(context, file, "text/csv", "مشاركة تقرير إكسل")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير Excel", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val file = lastGeneratedPdfFile ?: ReportGenerator.generatePdf(
                                    context = context,
                                    title = if (reportType == "LOW_STOCK") "تقرير نواقص المخزون" else "تقرير جرد المخزون",
                                    warehouseName = selectedWarehouseName,
                                    appUserName = appUserName,
                                    orgName = orgName,
                                    data = reportData,
                                    selectedColumns = selectedColumns.toSet()
                                )
                                ReportGenerator.shareFile(context, file, "application/pdf", "مشاركة تقرير المخزون PDF")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_report_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Live On-Screen Report Preview Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المعاينة المباشرة للتقرير (${reportData.size} صنف)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "مستخدم التطبيق: $appUserName",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeCols = ReportGenerator.ReportColumn.values().filter { selectedColumns.contains(it) }

                    // Horizontal scrolling table
                    val horizontalScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                activeCols.forEach { col ->
                                    val colWidth = when (col) {
                                        ReportGenerator.ReportColumn.SERIAL -> 48.dp
                                        ReportGenerator.ReportColumn.ITEM_NAME -> 160.dp
                                        ReportGenerator.ReportColumn.ITEM_UNIT -> 70.dp
                                        ReportGenerator.ReportColumn.WAREHOUSE -> 130.dp
                                        ReportGenerator.ReportColumn.CURRENT_STOCK -> 90.dp
                                        ReportGenerator.ReportColumn.MIN_STOCK -> 80.dp
                                        ReportGenerator.ReportColumn.PURCHASE_PRICE -> 90.dp
                                        ReportGenerator.ReportColumn.TOTAL_VALUE -> 100.dp
                                    }
                                    Text(
                                        text = col.titleAr,
                                        modifier = Modifier.width(colWidth).padding(horizontal = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Table Data Rows
                            reportData.forEachIndexed { index, item ->
                                val rowBg = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface

                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    activeCols.forEach { col ->
                                        val colWidth = when (col) {
                                            ReportGenerator.ReportColumn.SERIAL -> 48.dp
                                            ReportGenerator.ReportColumn.ITEM_NAME -> 160.dp
                                            ReportGenerator.ReportColumn.ITEM_UNIT -> 70.dp
                                            ReportGenerator.ReportColumn.WAREHOUSE -> 130.dp
                                            ReportGenerator.ReportColumn.CURRENT_STOCK -> 90.dp
                                            ReportGenerator.ReportColumn.MIN_STOCK -> 80.dp
                                            ReportGenerator.ReportColumn.PURCHASE_PRICE -> 90.dp
                                            ReportGenerator.ReportColumn.TOTAL_VALUE -> 100.dp
                                        }
                                        val text = when (col) {
                                            ReportGenerator.ReportColumn.SERIAL -> "${index + 1}"
                                            ReportGenerator.ReportColumn.ITEM_NAME -> item.itemName
                                            ReportGenerator.ReportColumn.ITEM_UNIT -> item.itemUnit
                                            ReportGenerator.ReportColumn.WAREHOUSE -> item.warehouseName
                                            ReportGenerator.ReportColumn.CURRENT_STOCK -> "%.1f".format(item.currentStock)
                                            ReportGenerator.ReportColumn.MIN_STOCK -> "%.1f".format(item.minStockLevel)
                                            ReportGenerator.ReportColumn.PURCHASE_PRICE -> "%.2f $".format(item.purchasePrice)
                                            ReportGenerator.ReportColumn.TOTAL_VALUE -> "%.2f $".format(item.currentStock * item.purchasePrice)
                                        }
                                        Text(
                                            text = text,
                                            modifier = Modifier.width(colWidth).padding(horizontal = 6.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (col == ReportGenerator.ReportColumn.ITEM_NAME) FontWeight.Bold else FontWeight.Normal
                                        )
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
