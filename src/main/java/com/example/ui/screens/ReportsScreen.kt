package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.data.model.VoucherType
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
    val suppliers by viewModel.suppliers.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val beneficiaries by viewModel.beneficiaries.collectAsState()

    // 1. Report Category
    // Options: "REMAINING" (متبقي/جرد), "INWARD" (وارد), "DISBURSEMENT" (منصرف), "TRANSFER" (تحويل), "ITEM_FLOW" (حركة صنف)
    var selectedReportCategory by remember { mutableStateOf("REMAINING") }

    // 2. Filters State
    var searchVoucherNumber by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf<Long?>(null) }

    // Date range filter: 0L means no lower/upper bound
    var dateRangeFilter by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "WEEK", "MONTH"

    // Multi-selection sets
    val selectedSuppliers = remember { mutableStateListOf<String>() }
    val selectedReceivers = remember { mutableStateListOf<String>() }
    val selectedBeneficiaryEntities = remember { mutableStateListOf<String>() }
    val selectedItemIds = remember { mutableStateListOf<Long>() }

    // Filter Dialogs
    var showSupplierFilterDialog by remember { mutableStateOf(false) }
    var showReceiverFilterDialog by remember { mutableStateOf(false) }
    var showBeneficiaryFilterDialog by remember { mutableStateOf(false) }
    var showItemFilterDialog by remember { mutableStateOf(false) }

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
    val allItems = snapshot?.items ?: emptyList()
    val allVouchers = snapshot?.vouchers ?: emptyList()

    // Compute Date Range Boundaries
    val currentTime = System.currentTimeMillis()
    val minTimestamp = when (dateRangeFilter) {
        "TODAY" -> currentTime - 86400000L
        "WEEK" -> currentTime - 86400000L * 7
        "MONTH" -> currentTime - 86400000L * 30
        else -> 0L
    }

    // Filter and Build Report Data Rows
    val reportData = remember(
        snapshot,
        selectedReportCategory,
        selectedWarehouseId,
        searchVoucherNumber,
        dateRangeFilter,
        selectedSuppliers.size,
        selectedReceivers.size,
        selectedBeneficiaryEntities.size,
        selectedItemIds.size
    ) {
        val list = mutableListOf<WarehouseItemStock>()

        when (selectedReportCategory) {
            "REMAINING" -> {
                val raw = snapshot?.warehouseItemStocks ?: emptyList()
                val filtered = raw.filter { itemStock ->
                    val matchesWarehouse = selectedWarehouseId == null || itemStock.warehouseId == selectedWarehouseId
                    val matchesItems = selectedItemIds.isEmpty() || selectedItemIds.contains(itemStock.itemId)
                    matchesWarehouse && matchesItems
                }
                list.addAll(filtered)
            }
            "INWARD", "DISBURSEMENT", "TRANSFER" -> {
                val targetType = when (selectedReportCategory) {
                    "INWARD" -> VoucherType.INWARD
                    "DISBURSEMENT" -> VoucherType.DISBURSEMENT
                    else -> VoucherType.TRANSFER
                }
                val matchingVouchers = allVouchers.filter { vWithItems ->
                    val v = vWithItems.voucher
                    val matchesType = v.voucherType == targetType
                    val matchesVoucherNum = searchVoucherNumber.isBlank() || v.voucherNumber.contains(searchVoucherNumber, ignoreCase = true)
                    val matchesDate = v.date >= minTimestamp
                    val matchesWarehouse = selectedWarehouseId == null ||
                        v.sourceWarehouseId == selectedWarehouseId ||
                        v.destinationWarehouseId == selectedWarehouseId

                    val matchesSuppliers = selectedSuppliers.isEmpty() || selectedSuppliers.contains(v.supplierName)
                    val matchesReceivers = selectedReceivers.isEmpty() || selectedReceivers.contains(v.receiverName) || selectedReceivers.contains(v.disburserName)
                    val matchesBeneficiaries = selectedBeneficiaryEntities.isEmpty() || selectedBeneficiaryEntities.contains(v.beneficiaryName)

                    matchesType && matchesVoucherNum && matchesDate && matchesWarehouse && matchesSuppliers && matchesReceivers && matchesBeneficiaries
                }

                for (vWithItems in matchingVouchers) {
                    val v = vWithItems.voucher
                    val wName = when (targetType) {
                        VoucherType.INWARD -> warehouses.find { it.id == v.destinationWarehouseId }?.name ?: "المخزن المستلم"
                        VoucherType.TRANSFER -> {
                            val src = warehouses.find { it.id == v.sourceWarehouseId }?.name ?: ""
                            val dst = warehouses.find { it.id == v.destinationWarehouseId }?.name ?: ""
                            "من $src إلى $dst"
                        }
                        VoucherType.DISBURSEMENT -> warehouses.find { it.id == v.sourceWarehouseId }?.name ?: "مخزن الصرف"
                    }

                    for (itm in vWithItems.items) {
                        if (selectedItemIds.isEmpty() || selectedItemIds.contains(itm.itemId)) {
                            list.add(
                                WarehouseItemStock(
                                    warehouseId = v.destinationWarehouseId ?: v.sourceWarehouseId ?: 0L,
                                    warehouseName = "$wName (${v.voucherNumber})",
                                    itemId = itm.itemId,
                                    itemName = itm.itemName,
                                    itemUnit = itm.itemUnit,
                                    packageUnit = itm.packageUnit,
                                    unitsPerPackage = itm.unitsPerPackage,
                                    currentStock = itm.quantity,
                                    minStockLevel = 0.0,
                                    purchasePrice = itm.unitPrice
                                )
                            )
                        }
                    }
                }
            }
            "ITEM_FLOW" -> {
                // Inward & Outward for specific selected items
                for (vWithItems in allVouchers) {
                    val v = vWithItems.voucher
                    if (v.date >= minTimestamp) {
                        for (itm in vWithItems.items) {
                            if (selectedItemIds.isEmpty() || selectedItemIds.contains(itm.itemId)) {
                                val direction = when (v.voucherType) {
                                    VoucherType.INWARD -> "وارد (+)"
                                    VoucherType.DISBURSEMENT -> "منصرف (-)"
                                    VoucherType.TRANSFER -> "تحويل"
                                    else -> ""
                                }
                                val wName = warehouses.find { it.id == (v.destinationWarehouseId ?: v.sourceWarehouseId) }?.name ?: ""
                                list.add(
                                    WarehouseItemStock(
                                        warehouseId = v.destinationWarehouseId ?: v.sourceWarehouseId ?: 0L,
                                        warehouseName = "$wName [$direction - ${v.voucherNumber}]",
                                        itemId = itm.itemId,
                                        itemName = itm.itemName,
                                        itemUnit = itm.itemUnit,
                                        packageUnit = itm.packageUnit,
                                        unitsPerPackage = itm.unitsPerPackage,
                                        currentStock = itm.quantity,
                                        minStockLevel = 0.0,
                                        purchasePrice = itm.unitPrice
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Alphabetical Sorting (أ-ي)
        list.sortedBy { it.itemName.lowercase(Locale.getDefault()) }
    }

    // Dynamic title based on report type as requested in Requirement 8
    val dynamicReportTitle = remember(selectedReportCategory) {
        when (selectedReportCategory) {
            "REMAINING" -> "تقرير جرد المخزون الفعلي المتبقي"
            "INWARD" -> "تقرير حركة التوريد والمشتريات"
            "DISBURSEMENT" -> "تقرير أوامر الصرف للجهات المستفيدة"
            "TRANSFER" -> "تقرير التحويلات المخزنية"
            "ITEM_FLOW" -> "تقرير حركة الوارد والمنصرف للأصناف"
            else -> "تقرير المخزون"
        }
    }

    val selectedWarehouseName = remember(warehouses, selectedWarehouseId) {
        if (selectedWarehouseId == null) "كافة المخازن"
        else warehouses.find { it.id == selectedWarehouseId }?.name ?: "المخزن المحدد"
    }

    var lastGeneratedPdfFile by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("reports_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Report Type Tabs (وارد، منصرف، تحويل، متبقي، حركة صنف)
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("نوع التقرير المطلوب استخراجه:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        item {
                            FilterChip(
                                selected = selectedReportCategory == "REMAINING",
                                onClick = { selectedReportCategory = "REMAINING" },
                                label = { Text("رصيد متبقي / جرد") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedReportCategory == "INWARD",
                                onClick = { selectedReportCategory = "INWARD" },
                                label = { Text("أوامر الوارد") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedReportCategory == "DISBURSEMENT",
                                onClick = { selectedReportCategory = "DISBURSEMENT" },
                                label = { Text("المنصرف للجهات") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedReportCategory == "TRANSFER",
                                onClick = { selectedReportCategory = "TRANSFER" },
                                label = { Text("التحويلات المخزنية") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedReportCategory == "ITEM_FLOW",
                                onClick = { selectedReportCategory = "ITEM_FLOW" },
                                label = { Text("حركة الوارد والمنصرف للأصناف") }
                            )
                        }
                    }
                }
            }
        }

        // 2. Search & Date Filter Bar
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("فلاتر البحث والتاريخ:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Voucher number search
                    OutlinedTextField(
                        value = searchVoucherNumber,
                        onValueChange = { searchVoucherNumber = it },
                        placeholder = { Text("البحث برقم السند (مثال: IN-1001، OUT-1001)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("search_voucher_number_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Date range quick chips (من - إلى)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("الفترة:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        FilterChip(
                            selected = dateRangeFilter == "ALL",
                            onClick = { dateRangeFilter = "ALL" },
                            label = { Text("كل الفترات", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = dateRangeFilter == "TODAY",
                            onClick = { dateRangeFilter = "TODAY" },
                            label = { Text("اليوم", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = dateRangeFilter == "WEEK",
                            onClick = { dateRangeFilter = "WEEK" },
                            label = { Text("آخر أسبوع", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = dateRangeFilter == "MONTH",
                            onClick = { dateRangeFilter = "MONTH" },
                            label = { Text("آخر شهر", fontSize = 11.sp) }
                        )
                    }

                    // Warehouse filter dropdown
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
                            label = { Text("المخزن") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = warehouseDropdownExpanded,
                            onDismissRequest = { warehouseDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("كافة المخازن") },
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

                    // Advanced Multi-Selection Filter Buttons (الموردين، المستلمين، الجهات، الأصناف)
                    Text("تصفية متعددة إضافية:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSupplierFilterDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val count = selectedSuppliers.size
                            Text(if (count == 0) "الموردين" else "الموردين ($count)", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showReceiverFilterDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val count = selectedReceivers.size
                            Text(if (count == 0) "المستلمين" else "المستلمين ($count)", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showBeneficiaryFilterDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val count = selectedBeneficiaryEntities.size
                            Text(if (count == 0) "الجهات" else "الجهات ($count)", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showItemFilterDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val count = selectedItemIds.size
                            Text(if (count == 0) "الأصناف" else "الأصناف ($count)", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 3. Columns Selector Checkboxes
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("تحديد الأعمدة المطلوب إظهارها في التقرير:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val allCols = ReportGenerator.ReportColumn.values()
                    val chunked = allCols.toList().chunked(2)
                    chunked.forEach { rowCols ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCols.forEach { col ->
                                val isChecked = selectedColumns.contains(col)
                                Row(
                                    modifier = Modifier.weight(1f).padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedColumns.add(col)
                                            else if (selectedColumns.size > 1) selectedColumns.remove(col)
                                        }
                                    )
                                    Text(text = col.titleAr, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Export & Share Action Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = dynamicReportTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val file = ReportGenerator.generatePdf(
                                    context = context,
                                    title = dynamicReportTitle,
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
                            modifier = Modifier.weight(1f).testTag("export_pdf_btn"),
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
                                    title = dynamicReportTitle,
                                    warehouseName = selectedWarehouseName,
                                    appUserName = appUserName,
                                    data = reportData,
                                    selectedColumns = selectedColumns.toSet()
                                )
                                Toast.makeText(context, "تم تصدير ملف إكسل CSV بنجاح", Toast.LENGTH_SHORT).show()
                                ReportGenerator.shareFile(context, file, "text/csv", "مشاركة تقرير إكسل")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f).testTag("export_excel_btn"),
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
                                    title = dynamicReportTitle,
                                    warehouseName = selectedWarehouseName,
                                    appUserName = appUserName,
                                    orgName = orgName,
                                    data = reportData,
                                    selectedColumns = selectedColumns.toSet()
                                )
                                ReportGenerator.shareFile(context, file, "application/pdf", "مشاركة التقرير")
                            },
                            modifier = Modifier.weight(1f).testTag("share_report_btn"),
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

        // 5. Direct Live On-Screen Table Preview (RTL Aligned)
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المعاينة المباشرة للتقرير (${reportData.size} سجل)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "المستخدم: $appUserName",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeCols = ReportGenerator.ReportColumn.values().filter { selectedColumns.contains(it) }

                    // Horizontal scrolling RTL preview
                    val horizontalScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            // Header Row
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
                                        ReportGenerator.ReportColumn.WAREHOUSE -> 150.dp
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

                            // Data Rows
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
                                            ReportGenerator.ReportColumn.WAREHOUSE -> 150.dp
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
                                            ReportGenerator.ReportColumn.CURRENT_STOCK -> "%.0f".format(item.currentStock)
                                            ReportGenerator.ReportColumn.MIN_STOCK -> "%.0f".format(item.minStockLevel)
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

    // Suppliers Multi-Selection Dialog
    if (showSupplierFilterDialog) {
        MultiSelectDialog(
            title = "اختيار مورد أو أكثر",
            allItems = suppliers.map { it.name }.sorted(),
            selectedItems = selectedSuppliers,
            onDismiss = { showSupplierFilterDialog = false }
        )
    }

    // Receivers Multi-Selection Dialog
    if (showReceiverFilterDialog) {
        MultiSelectDialog(
            title = "اختيار اسم مستلم أو أكثر",
            allItems = parties.map { it.name }.sorted(),
            selectedItems = selectedReceivers,
            onDismiss = { showReceiverFilterDialog = false }
        )
    }

    // Beneficiaries Multi-Selection Dialog
    if (showBeneficiaryFilterDialog) {
        MultiSelectDialog(
            title = "اختيار جهة مستفيدة أو أكثر",
            allItems = beneficiaries.map { it.name }.sorted(),
            selectedItems = selectedBeneficiaryEntities,
            onDismiss = { showBeneficiaryFilterDialog = false }
        )
    }

    // Items Multi-Selection Dialog
    if (showItemFilterDialog) {
        AlertDialog(
            onDismissRequest = { showItemFilterDialog = false },
            title = { Text("اختيار صنف واحد أو أكثر للتقرير", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    allItems.forEach { itm ->
                        val isChecked = selectedItemIds.contains(itm.id)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (isChecked) selectedItemIds.remove(itm.id)
                                else selectedItemIds.add(itm.id)
                            }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedItemIds.add(itm.id)
                                    else selectedItemIds.remove(itm.id)
                                }
                            )
                            Text(text = "${itm.name} (${itm.unit})", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showItemFilterDialog = false }) { Text("تأكيد") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    selectedItemIds.clear()
                    showItemFilterDialog = false
                }) { Text("تحديد الكل") }
            }
        )
    }
}

@Composable
private fun MultiSelectDialog(
    title: String,
    allItems: List<String>,
    selectedItems: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                allItems.forEach { itemText ->
                    val isChecked = selectedItems.contains(itemText)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isChecked) selectedItems.remove(itemText)
                            else selectedItems.add(itemText)
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (it) selectedItems.add(itemText)
                                else selectedItems.remove(itemText)
                            }
                        )
                        Text(text = itemText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("تأكيد") }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                selectedItems.clear()
                onDismiss()
            }) { Text("إلغاء التحديد") }
        }
    )
}
