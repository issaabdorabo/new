package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.model.CustomerWithOrder
import com.example.data.model.MatrixRow
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.CancelOrderDialog
import com.example.ui.components.ConfirmBulkSupplierOrderDialog
import com.example.ui.components.EditMatrixCellDialog
import com.example.ui.components.EditOrderDetailsDialog
import com.example.ui.components.EditPaidAmountDialog
import com.example.ui.components.EditSupplierOrderDialog
import com.example.ui.components.NewSupplierOrderDialog
import com.example.ui.components.ReceiveSupplierOrderDialog
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.util.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    currentSubTab: Int,
    onTabSelected: (Int) -> Unit,
    matrixRows: List<MatrixRow>,
    customers: List<CustomerEntity>,
    parts: List<PartEntity>,
    customerOrdersDetail: List<CustomerWithOrder>,
    supplierOrdersWithPart: List<SupplierOrderWithPart>,
    selectedCustomerIdForDetail: Long?,
    onSelectCustomerForDetail: (Long?) -> Unit,
    onSetMatrixCellQty: (customerId: Long, partId: Long, newQty: Int) -> Unit,
    onAddPartToCustomerOrder: (customerId: Long, partId: Long, qty: Int) -> Unit,
    onUpdateOrderItemQty: (itemId: Long, newQty: Int) -> Unit,
    onDeleteOrderItem: (OrderItemEntity) -> Unit,
    onCancelCustomerOrder: (orderId: Long, returnStock: Boolean) -> Unit,
    onDeleteCustomerOrderCard: (orderId: Long, returnStock: Boolean) -> Unit = { _, _ -> },
    onUpdateOrderPayment: (orderId: Long, paidAmount: Double, discount: Double) -> Unit,
    onUpdateOrderDetails: (orderId: Long, title: String, notes: String, orderDate: String) -> Unit,
    onConfirmBulkSupplierOrders: (orderDate: String, confirmedDate: String, notes: String) -> Unit,
    onCreateSupplierOrder: (partId: Long, qty: Int, date: String, notes: String, orderDate: String, confirmedDate: String) -> Unit,
    onUpdateSupplierOrder: (SupplierOrderEntity) -> Unit,
    onCancelSupplierOrder: (orderId: Long) -> Unit,
    onDeleteSupplierOrder: (orderId: Long) -> Unit = {},
    onReceiveSupplierOrder: (orderId: Long, qtyReceivedNow: Int) -> Unit,
    onReorderRemaining: (orderId: Long) -> Unit,
    onAddNewPartRequested: () -> Unit,
    onAddCustomer: (name: String, phone: String, notes: String) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    supplierSearchQuery: String,
    onSupplierSearchQueryChange: (String) -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    val tabs = listOf("العرض الجماعي", "العرض الفردي", "طلبات الموردين")

    var showAddCustomerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = currentSubTab,
            containerColor = Color(0xFF0F172A),
            contentColor = AmberSecondary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = currentSubTab == index,
                    onClick = {
                        onTabSelected(index)
                        if (index != 1) onSelectCustomerForDetail(null)
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (currentSubTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (currentSubTab == index) AmberSecondary else Color.White
                        )
                    },
                    modifier = Modifier.testTag("tab_orders_$index")
                )
            }
        }

        when (currentSubTab) {
            0 -> MatrixGroupView(
                matrixRows = matrixRows,
                customers = customers,
                onCellClick = onSetMatrixCellQty,
                onShareMatrix = { exportMatrixBoth(context, matrixRows, customers, appUserName) },
                onAddPart = onAddNewPartRequested,
                onAddCustomer = { showAddCustomerDialog = true },
                onConfirmBulkSupplierOrders = onConfirmBulkSupplierOrders
            )
            1 -> IndividualOrdersView(
                customerOrders = customerOrdersDetail,
                parts = parts,
                selectedCustomerId = selectedCustomerIdForDetail,
                onSelectCustomer = onSelectCustomerForDetail,
                onUpdateQty = onUpdateOrderItemQty,
                onDeleteOrderItem = onDeleteOrderItem,
                onAddPartToOrder = onAddPartToCustomerOrder,
                onAddNewPart = onAddNewPartRequested,
                onAddCustomer = { showAddCustomerDialog = true },
                onDeleteCustomer = onDeleteCustomer,
                onCancelOrder = onCancelCustomerOrder,
                onDeleteOrderCard = onDeleteCustomerOrderCard,
                onUpdatePayment = onUpdateOrderPayment,
                onUpdateOrderDetails = onUpdateOrderDetails,
                appUserName = appUserName
            )
            2 -> SupplierOrdersView(
                supplierOrders = supplierOrdersWithPart,
                parts = parts,
                searchQuery = supplierSearchQuery,
                onSearchQueryChange = onSupplierSearchQueryChange,
                onCreateSupplierOrder = onCreateSupplierOrder,
                onUpdateSupplierOrder = onUpdateSupplierOrder,
                onCancelSupplierOrder = onCancelSupplierOrder,
                onDeleteSupplierOrder = onDeleteSupplierOrder,
                onReceiveOrder = onReceiveSupplierOrder,
                onReorderRemaining = onReorderRemaining,
                appUserName = appUserName
            )
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, notes ->
                onAddCustomer(name, phone, notes)
                showAddCustomerDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// Tab A: "العرض الجماعي" (Matrix View with frozen first column & Bulk Confirmation)
// ----------------------------------------------------
@Composable
fun MatrixGroupView(
    matrixRows: List<MatrixRow>,
    customers: List<CustomerEntity>,
    onCellClick: (customerId: Long, partId: Long, newQty: Int) -> Unit,
    onShareMatrix: () -> Unit,
    onAddPart: () -> Unit,
    onAddCustomer: () -> Unit,
    onConfirmBulkSupplierOrders: (orderDate: String, confirmedDate: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var cellToEdit by remember { mutableStateOf<Triple<PartEntity, CustomerEntity, Int>?>(null) }
    var showBulkConfirmDialog by remember { mutableStateOf(false) }

    val customerGradients = listOf(
        listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
        listOf(Color(0xFF047857), Color(0xFF10B981)),
        listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)),
        listOf(Color(0xFFBE123C), Color(0xFFF43F5E))
    )

    val totalRequiredPieces = matrixRows.sumOf { it.totalQty }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "جدول الطلبات الجماعي للأصناف والعملاء",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "المطلوب: $totalRequiredPieces قطعة • انقر على أي خلية لتعديل الكمية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Bulk Confirm Supplier Order Button
                Button(
                    onClick = { showBulkConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تأكيد للموردين", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // Add Customer
                OutlinedButton(
                    onClick = onAddCustomer,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("عميل", fontSize = 11.sp)
                }

                // Export
                OutlinedButton(
                    onClick = onShareMatrix,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("تصدير", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Matrix Table Container
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val horizontalScrollState = rememberScrollState()

            Row(modifier = Modifier.fillMaxSize()) {
                // FROZEN COLUMN: Part Name & Details
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Top-Left Corner Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(PrimaryBlue)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "الصنف / السيارة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider()

                    // Frozen Part Names Column
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(matrixRows) { row ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        text = row.part.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${row.part.carType} • ${row.part.carModel}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }

                        // Summary label row
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "إجمالي الكميات",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // SCROLLABLE COLUMNS: Customer Columns + Total Column
                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                        // Header Row
                        Row(modifier = Modifier.height(52.dp)) {
                            customers.forEachIndexed { idx, customer ->
                                val gradient = customerGradients[idx % customerGradients.size]
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .fillMaxWidth()
                                        .background(Brush.verticalGradient(gradient))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Total Header
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .fillMaxWidth()
                                    .background(AmberSecondary)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "الإجمالي",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        HorizontalDivider()

                        // Scrollable Body Rows
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(matrixRows) { row ->
                                Row(
                                    modifier = Modifier.height(60.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    customers.forEach { customer ->
                                        val qty = row.customerQuantities[customer.id] ?: 0
                                        Box(
                                            modifier = Modifier
                                                .width(90.dp)
                                                .fillMaxWidth()
                                                .clickable {
                                                    cellToEdit = Triple(row.part, customer, qty)
                                                }
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (qty > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .background(PrimaryLighter, RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "$qty",
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryBlue,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "-",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    // Row Total
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .fillMaxWidth()
                                            .background(AmberLight.copy(alpha = 0.3f))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${row.totalQty}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.totalQty > 0) AmberSecondary else Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }

                            // Column Totals Row
                            item {
                                Row(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var grandTotal = 0
                                    customers.forEach { customer ->
                                        val colTotal = matrixRows.sumOf { it.customerQuantities[customer.id] ?: 0 }
                                        grandTotal += colTotal
                                        Box(
                                            modifier = Modifier
                                                .width(90.dp)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$colTotal",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    // Grand Total
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .background(AmberSecondary)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$grandTotal",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 15.sp
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

    cellToEdit?.let { (part, customer, qty) ->
        EditMatrixCellDialog(
            part = part,
            customer = customer,
            currentQty = qty,
            onDismiss = { cellToEdit = null },
            onConfirm = { newQty ->
                onCellClick(customer.id, part.id, newQty)
                cellToEdit = null
            }
        )
    }

    if (showBulkConfirmDialog) {
        ConfirmBulkSupplierOrderDialog(
            matrixRows = matrixRows,
            onDismiss = { showBulkConfirmDialog = false },
            onConfirm = { orderDate, confirmedDate, notes ->
                onConfirmBulkSupplierOrders(orderDate, confirmedDate, notes)
                showBulkConfirmDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// Tab B: "العرض الفردي" (Individual Orders with Cancel, Paid Amount, and Export)
// ----------------------------------------------------
@Composable
fun IndividualOrdersView(
    customerOrders: List<CustomerWithOrder>,
    parts: List<PartEntity>,
    selectedCustomerId: Long?,
    onSelectCustomer: (Long?) -> Unit,
    onUpdateQty: (itemId: Long, newQty: Int) -> Unit,
    onDeleteOrderItem: (OrderItemEntity) -> Unit,
    onAddPartToOrder: (customerId: Long, partId: Long, qty: Int) -> Unit,
    onAddNewPart: () -> Unit,
    onAddCustomer: () -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onCancelOrder: (orderId: Long, returnStock: Boolean) -> Unit,
    onDeleteOrderCard: (orderId: Long, returnStock: Boolean) -> Unit,
    onUpdatePayment: (orderId: Long, paidAmount: Double, discount: Double) -> Unit,
    onUpdateOrderDetails: (orderId: Long, title: String, notes: String, orderDate: String) -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showPartSelectorDialog by remember { mutableStateOf(false) }
    var showCancelOrderDialog by remember { mutableStateOf(false) }
    var showDeleteOrderCardDialog by remember { mutableStateOf(false) }
    var orderToDeleteFromList by remember { mutableStateOf<Pair<CustomerEntity, OrderEntity>?>(null) }
    var showEditPaidDialog by remember { mutableStateOf(false) }
    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var itemSearchQuery by remember { mutableStateOf("") }

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    if (selectedCustomerId != null) {
        // DETAIL VIEW for a specific customer's order
        val selectedCustomerWithOrder = customerOrders.find { it.customer.id == selectedCustomerId }

        BackHandler {
            onSelectCustomer(null)
        }

        if (selectedCustomerWithOrder == null) {
            onSelectCustomer(null)
            return
        }

        val customer = selectedCustomerWithOrder.customer
        val order = selectedCustomerWithOrder.activeOrder
        val rawItems = selectedCustomerWithOrder.items

        val items = remember(rawItems, itemSearchQuery) {
            val filtered = if (itemSearchQuery.isBlank()) rawItems else {
                rawItems.filter {
                    it.part.name.contains(itemSearchQuery, ignoreCase = true) ||
                    it.part.carType.contains(itemSearchQuery, ignoreCase = true) ||
                    it.part.carModel.contains(itemSearchQuery, ignoreCase = true)
                }
            }
            filtered.sortedWith { i1, i2 -> collator.compare(i1.part.name, i2.part.name) }
        }

        val totalPieces = rawItems.sumOf { it.item.quantity }
        val calculatedTotal = rawItems.sumOf { it.item.quantity * it.part.price }
        val paidAmount = order?.paidAmount ?: 0.0
        val discount = order?.discount ?: 0.0
        val remaining = maxOf(0.0, (calculatedTotal - discount) - paidAmount)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header with Back button and Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectCustomer(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "طلب العميل: ${customer.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (order != null) {
                            val (badgeBg, badgeColor, badgeText) = when (order.status) {
                                "cancelled" -> Triple(RoseLight, RoseDanger, "ملغي")
                                "completed" -> Triple(EmeraldLight, EmeraldSuccess, "مكتمل")
                                else -> Triple(AmberLight, AmberSecondary, "جاري")
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(badgeText, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = "الجوال: ${customer.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showPartSelectorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة صنف", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order Financial & Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إجمالي الفاتورة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$calculatedTotal ر.س", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                        }
                        Column {
                            Text("المبلغ الواصل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$paidAmount ر.س", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldSuccess)
                        }
                        Column {
                            Text("المتبقي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$remaining ر.س", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (remaining > 0) RoseDanger else EmeraldSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons for: Edit Paid, Edit Order Details, Cancel Order, Delete Order Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showEditPaidDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("الواصل", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showEditDetailsDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("تعديل", fontSize = 10.5.sp)
                        }

                        if (order != null && order.status != "cancelled") {
                            Button(
                                onClick = { showCancelOrderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("إلغاء", fontSize = 10.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (order != null) {
                            Button(
                                onClick = { showDeleteOrderCardDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("حذف", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smart Search inside Customer Order Items
            OutlinedTextField(
                value = itemSearchQuery,
                onValueChange = { itemSearchQuery = it },
                placeholder = { Text("بحث ذكي عن صنف في هذا الطلب...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (itemSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { itemSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Items List
            if (rawItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد أصناف في هذا الطلب حالياً",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { showPartSelectorDialog = true }) {
                            Text("➕ إضافة أول صنف للطلب")
                        }
                    }
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد صنف يطابق البحث \"$itemSearchQuery\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { itemWithPart ->
                        val item = itemWithPart.item
                        val part = itemWithPart.part
                        val lineTotal = item.quantity * part.price

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = part.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${part.carType} • ${part.carModel}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${part.price} ر.س / قطعة • الإجمالي: $lineTotal ر.س",
                                        fontSize = 11.sp,
                                        color = AmberSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQty(item.id, item.quantity - 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "نقص")
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    IconButton(
                                        onClick = { onUpdateQty(item.id, item.quantity + 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "زيادة")
                                    }
                                    IconButton(
                                        onClick = { onDeleteOrderItem(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Export & Share Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي القطع: $totalPieces قطعة",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "الصافي: ${calculatedTotal - discount} ر.س | الواصل: $paidAmount ر.س",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportCustomerOrderExcel(context, customer, order, rawItems, calculatedTotal, paidAmount, remaining, appUserName)
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Excel", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                exportCustomerOrderPdf(context, customer, order, rawItems, calculatedTotal, paidAmount, remaining, appUserName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Cancel order dialog with stock restore choice
        if (showCancelOrderDialog && order != null) {
            CancelOrderDialog(
                order = order,
                customerName = customer.name,
                onDismiss = { showCancelOrderDialog = false },
                onConfirmCancel = { returnStock ->
                    onCancelOrder(order.id, returnStock)
                    showCancelOrderDialog = false
                }
            )
        }

        // Delete order card dialog with stock restore choice
        if (showDeleteOrderCardDialog && order != null) {
            AlertDialog(
                onDismissRequest = { showDeleteOrderCardDialog = false },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
                title = { Text("حذف بطاقة طلبية العميل", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("هل أنت متأكد من حذف بطاقة طلبية العميل (${customer.name}) نهائياً؟")
                        Text(
                            text = "يمكنك اختيار ما إذا كنت ترغب في إعادة كميات القطع المطلوبة إلى المخزن أو حذف الطلب دون تغيير كميات المخزن.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteOrderCard(order.id, true)
                            showDeleteOrderCardDialog = false
                            onSelectCustomer(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("إعادة المخزن وحذف")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = {
                                onDeleteOrderCard(order.id, false)
                                showDeleteOrderCardDialog = false
                                onSelectCustomer(null)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger)
                        ) {
                            Text("حذف دون تعديل المخزن")
                        }
                        TextButton(onClick = { showDeleteOrderCardDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                }
            )
        }

        // Edit paid amount dialog
        if (showEditPaidDialog && order != null) {
            EditPaidAmountDialog(
                order = order,
                customerName = customer.name,
                onDismiss = { showEditPaidDialog = false },
                onConfirm = { paid, disc ->
                    onUpdatePayment(order.id, paid, disc)
                    showEditPaidDialog = false
                }
            )
        }

        // Edit order details dialog
        if (showEditDetailsDialog && order != null) {
            EditOrderDetailsDialog(
                order = order,
                customerName = customer.name,
                onDismiss = { showEditDetailsDialog = false },
                onConfirm = { newTitle, newNotes, newDate ->
                    onUpdateOrderDetails(order.id, newTitle, newNotes, newDate)
                    showEditDetailsDialog = false
                }
            )
        }

    } else {
        // LIST OF CUSTOMERS
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👥 بطاقات طلبات العملاء الفردية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختر عميلاً لعرض الأصناف والمدفوعات أو حذف بطاقة الطلب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAddCustomer,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة عميل", fontSize = 11.sp)
                    }
                }
            }

            items(customerOrders) { custWithOrder ->
                val customer = custWithOrder.customer
                val order = custWithOrder.activeOrder
                val pieces = custWithOrder.totalQty
                val totalCost = custWithOrder.items.sumOf { it.item.quantity * it.part.price }
                val paid = order?.paidAmount ?: 0.0
                val remaining = maxOf(0.0, totalCost - paid)

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCustomer(custWithOrder.customer.id) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(PrimaryLighter, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = customer.notes.ifBlank { "عميل معتمد" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (order != null && order.status == "cancelled") {
                                    Box(
                                        modifier = Modifier
                                            .background(RoseLight, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("ملغي", color = RoseDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(AmberLight, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$pieces قطعة",
                                            fontWeight = FontWeight.Bold,
                                            color = AmberSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (order != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            orderToDeleteFromList = Pair(customer, order)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف بطاقة الطلبية", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Financial overview row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الإجمالي: $totalCost ر.س", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("الواصل: $paid ر.س", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            Text("المتبقي: $remaining ر.س", fontSize = 11.sp, color = if (remaining > 0) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Delete order card dialog from outer list
        orderToDeleteFromList?.let { (cust, ord) ->
            AlertDialog(
                onDismissRequest = { orderToDeleteFromList = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
                title = { Text("حذف بطاقة طلبية العميل", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("هل أنت متأكد من حذف طلبية العميل (${cust.name})؟")
                        Text(
                            text = "يمكنك اختيار إعادة القطع إلى رصيد المخزن أو حذف الطلب دون تغيير رصيد المخزن.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteOrderCard(ord.id, true)
                            orderToDeleteFromList = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("إعادة المخزن وحذف")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = {
                                onDeleteOrderCard(ord.id, false)
                                orderToDeleteFromList = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger)
                        ) {
                            Text("حذف دون تعديل")
                        }
                        TextButton(onClick = { orderToDeleteFromList = null }) {
                            Text("إلغاء")
                        }
                    }
                }
            )
        }
    }

    if (showPartSelectorDialog && selectedCustomerId != null) {
        SelectPartForCustomerDialog(
            parts = parts,
            onDismiss = { showPartSelectorDialog = false },
            onPartSelected = { partId, qty ->
                onAddPartToOrder(selectedCustomerId, partId, qty)
                showPartSelectorDialog = false
            },
            onAddNewPart = onAddNewPart
        )
    }
}

// ----------------------------------------------------
// Tab C: "طلبات الموردين" (Grouped Supplier Orders with Detail View, Search, Delete, and Export)
// ----------------------------------------------------

data class SupplierOrderGroup(
    val groupKey: String,
    val orderDate: String,
    val confirmedDate: String,
    val notes: String,
    val items: List<SupplierOrderWithPart>
) {
    val totalOrdered get() = items.sumOf { it.supplierOrder.qtyOrdered }
    val totalReceived get() = items.sumOf { it.supplierOrder.qtyReceived }
    val totalRemaining get() = items.sumOf { it.supplierOrder.qtyRemaining }
    val status get() = when {
        items.all { it.supplierOrder.status == "cancelled" } -> "cancelled"
        items.all { it.supplierOrder.status == "completed" } -> "completed"
        items.any { it.supplierOrder.status == "partial" || it.supplierOrder.qtyReceived > 0 } -> "partial"
        else -> "confirmed"
    }
}

@Composable
fun SupplierOrdersView(
    supplierOrders: List<SupplierOrderWithPart>,
    parts: List<PartEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateSupplierOrder: (partId: Long, qty: Int, date: String, notes: String, orderDate: String, confirmedDate: String) -> Unit,
    onUpdateSupplierOrder: (SupplierOrderEntity) -> Unit,
    onCancelSupplierOrder: (orderId: Long) -> Unit,
    onDeleteSupplierOrder: (orderId: Long) -> Unit,
    onReceiveOrder: (orderId: Long, qtyReceivedNow: Int) -> Unit,
    onReorderRemaining: (orderId: Long) -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var orderToReceive by remember { mutableStateOf<SupplierOrderEntity?>(null) }
    var orderToEdit by remember { mutableStateOf<Pair<SupplierOrderEntity, String>?>(null) }
    var selectedGroupKey by remember { mutableStateOf<String?>(null) }
    var groupToDelete by remember { mutableStateOf<SupplierOrderGroup?>(null) }
    var inOrderSearchQuery by remember { mutableStateOf("") }

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    // Group items into batches by (orderDate, confirmedDate, notes)
    val orderGroups = remember(supplierOrders) {
        supplierOrders
            .groupBy { item ->
                val ord = item.supplierOrder
                val d = ord.orderDate.ifBlank { "unknown" }
                val cd = ord.confirmedDate.ifBlank { "unconfirmed" }
                val n = ord.notes.trim()
                "$d###$cd###$n"
            }
            .map { (key, list) ->
                val first = list.first().supplierOrder
                SupplierOrderGroup(
                    groupKey = key,
                    orderDate = first.orderDate,
                    confirmedDate = first.confirmedDate,
                    notes = first.notes,
                    items = list
                )
            }
            .sortedByDescending { it.orderDate }
    }

    if (selectedGroupKey != null) {
        // DETAIL VIEW for a specific supplier order
        val selectedGroup = orderGroups.find { it.groupKey == selectedGroupKey }

        BackHandler {
            selectedGroupKey = null
        }

        if (selectedGroup == null) {
            selectedGroupKey = null
            return
        }

        val filteredItems = remember(selectedGroup.items, inOrderSearchQuery) {
            val list = if (inOrderSearchQuery.isBlank()) selectedGroup.items else {
                selectedGroup.items.filter { item ->
                    val pName = item.part?.name ?: ""
                    val cType = item.part?.carType ?: ""
                    val cModel = item.part?.carModel ?: ""
                    pName.contains(inOrderSearchQuery, ignoreCase = true) ||
                    cType.contains(inOrderSearchQuery, ignoreCase = true) ||
                    cModel.contains(inOrderSearchQuery, ignoreCase = true) ||
                    item.supplierOrder.id.toString().contains(inOrderSearchQuery)
                }
            }
            list.sortedWith { i1, i2 ->
                collator.compare(i1.part?.name ?: "", i2.part?.name ?: "")
            }
        }

        val (statusBg, statusText, statusColor) = when (selectedGroup.status) {
            "cancelled" -> Triple(RoseLight, "ملغية", RoseDanger)
            "completed" -> Triple(EmeraldLight, "مكتملة بالكامل", EmeraldSuccess)
            "partial" -> Triple(AmberLight, "مستلمة جزئياً", AmberSecondary)
            else -> Triple(PrimaryLighter, "مؤكدة للتوريد", PrimaryBlue)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Back button and Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedGroupKey = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "طلبية توريد: ${selectedGroup.orderDate.ifBlank { "غير محدد" }}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(statusBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (selectedGroup.confirmedDate.isNotBlank()) {
                        Text(
                            text = "تاريخ التأكيد: ${selectedGroup.confirmedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { groupToDelete = selectedGroup },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                    modifier = Modifier.height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف الطلبية", fontSize = 11.sp, color = Color.White)
                }
            }

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("عدد الأصناف", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedGroup.items.size} صنف", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("إجمالي المطلوب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedGroup.totalOrdered} قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                        }
                        Column {
                            Text("إجمالي الواصل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedGroup.totalReceived} قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                        }
                        Column {
                            Text("المتبقي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedGroup.totalRemaining} قطعة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (selectedGroup.totalRemaining > 0) RoseDanger else EmeraldSuccess)
                        }
                    }

                    if (selectedGroup.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ملاحظات: ${selectedGroup.notes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Smart Search inside supplier order parts
            OutlinedTextField(
                value = inOrderSearchQuery,
                onValueChange = { inOrderSearchQuery = it },
                placeholder = { Text("بحث ذكي عن صنف أو سيارة داخل هذه الطلبية...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (inOrderSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { inOrderSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد أصناف تطابق البحث", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        val order = item.supplierOrder
                        val part = item.part
                        val partName = part?.name ?: "صنف #${order.partId}"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = partName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        if (part != null) {
                                            Text(
                                                text = "${part.carType} • ${part.carModel}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Part status badge
                                    val (itemBg, itemTxt, itemClr) = when (order.status) {
                                        "completed" -> Triple(EmeraldLight, "مكتمل", EmeraldSuccess)
                                        "partial" -> Triple(AmberLight, "جزئي", AmberSecondary)
                                        "cancelled" -> Triple(RoseLight, "ملغي", RoseDanger)
                                        else -> Triple(PrimaryLighter, "مؤكد", PrimaryBlue)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(itemBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(itemTxt, fontSize = 10.sp, color = itemClr, fontWeight = FontWeight.Bold)
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
                                    Text("المطلوب: ${order.qtyOrdered}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                    Text("الواصل: ${order.qtyReceived}", fontSize = 11.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                    Text("المتبقي: ${order.qtyRemaining}", fontSize = 11.5.sp, color = if (order.qtyRemaining > 0) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (order.status != "completed" && order.status != "cancelled") {
                                        Button(
                                            onClick = { orderToReceive = order },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("استلام دفعة", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { orderToEdit = Pair(order, partName) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("تعديل", fontSize = 10.5.sp)
                                    }

                                    if (order.status != "cancelled") {
                                        OutlinedButton(
                                            onClick = { onCancelSupplierOrder(order.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("إلغاء", fontSize = 10.5.sp)
                                        }
                                    }

                                    if (order.status == "partial" && order.qtyRemaining > 0) {
                                        Button(
                                            onClick = { onReorderRemaining(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("إعادة طلب", fontSize = 10.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Export Bar for this order
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "طلبية: ${selectedGroup.orderDate}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${selectedGroup.items.size} أصناف • ${selectedGroup.totalOrdered} قطعة مطلوبة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportSingleSupplierOrderExcel(
                                    context = context,
                                    orderDate = selectedGroup.orderDate,
                                    confirmedDate = selectedGroup.confirmedDate,
                                    notes = selectedGroup.notes,
                                    items = selectedGroup.items,
                                    appUserName = appUserName
                                )
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Excel", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                exportSingleSupplierOrderPdf(
                                    context = context,
                                    orderDate = selectedGroup.orderDate,
                                    confirmedDate = selectedGroup.confirmedDate,
                                    notes = selectedGroup.notes,
                                    items = selectedGroup.items,
                                    appUserName = appUserName
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

    } else {
        // LIST OF SUPPLIER ORDER CARDS
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header and Export All Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📦 سجل ومتابعة شحنات الموردين",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مجمعة في بطاقات مرتبة حسب التاريخ للأحدث • انقر لعرض التفاصيل",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportSupplierOrdersBoth(context, supplierOrders, appUserName)
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير الكل", fontSize = 11.sp)
                        }
                    }
                }

                // Search Bar for Supplier Orders
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("بحث عن طلبية برقم الطلب، اسم الصنف، التاريخ...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Orders List
                if (orderGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد طلبيات موردين مسجلة")
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { showNewOrderDialog = true }) {
                                Text("➕ إنشاء طلب توريد جديد")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(orderGroups) { group ->
                            val (statusBg, statusText, statusColor) = when (group.status) {
                                "cancelled" -> Triple(RoseLight, "ملغية", RoseDanger)
                                "completed" -> Triple(EmeraldLight, "مكتملة بالكامل", EmeraldSuccess)
                                "partial" -> Triple(AmberLight, "مستلمة جزئياً", AmberSecondary)
                                else -> Triple(PrimaryLighter, "مؤكدة للتوريد", PrimaryBlue)
                            }

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGroupKey = group.groupKey },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "📦 طلبية توريد بتاريخ: ${group.orderDate.ifBlank { "غير محدد" }}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (group.confirmedDate.isNotBlank()) {
                                                Text(
                                                    text = "تاريخ التأكيد: ${group.confirmedDate}",
                                                    fontSize = 11.sp,
                                                    color = PrimaryBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(statusBg, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { groupToDelete = group },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف الطلبية", tint = RoseDanger, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quantities strip
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("الأصناف: ${group.items.size} صنف", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                        Text("المطلوب: ${group.totalOrdered} قطعة", fontSize = 11.5.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                        Text("الواصل: ${group.totalReceived} قطعة", fontSize = 11.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        Text("المتبقي: ${group.totalRemaining} قطعة", fontSize = 11.5.sp, color = if (group.totalRemaining > 0) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                                    }

                                    if (group.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "ملاحظات: ${group.notes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB to add new manual supplier order
            FloatingActionButton(
                onClick = { showNewOrderDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = AmberSecondary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "طلب جديد")
            }
        }
    }

    // Delete supplier order group dialog
    groupToDelete?.let { grp ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
            title = { Text("حذف بطاقة طلبية الموردين", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنت متأكد من حذف هذه الطلبية بكافة أصنافها (${grp.items.size} صنف، بإجمالي ${grp.totalOrdered} قطعة)؟")
                    Text(
                        text = "سيتم إزالة بطاقة الطلبية وسجل متابعة الشحنة من النظام بشكل نهائي.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        grp.items.forEach { onCancelSupplierOrder(it.supplierOrder.id) }
                        grp.items.forEach { onDeleteSupplierOrder(it.supplierOrder.id) }
                        if (selectedGroupKey == grp.groupKey) {
                            selectedGroupKey = null
                        }
                        groupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، احذف الطلبية")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showNewOrderDialog) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        NewSupplierOrderDialog(
            parts = parts,
            onDismiss = { showNewOrderDialog = false },
            onConfirm = { partId, qty, expectedDate, notes ->
                onCreateSupplierOrder(partId, qty, expectedDate, notes, today, today)
                showNewOrderDialog = false
            }
        )
    }

    orderToReceive?.let { order ->
        ReceiveSupplierOrderDialog(
            order = order,
            onDismiss = { orderToReceive = null },
            onConfirm = { qtyNow ->
                onReceiveOrder(order.id, qtyNow)
                orderToReceive = null
            }
        )
    }

    orderToEdit?.let { (order, partName) ->
        EditSupplierOrderDialog(
            order = order,
            partName = partName,
            onDismiss = { orderToEdit = null },
            onConfirm = { updated ->
                onUpdateSupplierOrder(updated)
                orderToEdit = null
            }
        )
    }
}

// ----------------------------------------------------
// Export helpers for Matrix, Customer Orders, and Supplier Orders
// ----------------------------------------------------

fun exportMatrixBoth(context: Context, matrixRows: List<MatrixRow>, customers: List<CustomerEntity>, appUserName: String = "عيسى عبدربه") {
    val headers = listOf("الصنف / السيارة") + customers.map { it.name } + listOf("الإجمالي")
    val rows = matrixRows.map { row ->
        val partDesc = "${row.part.name} (${row.part.carType})"
        val custVals = customers.map { (row.customerQuantities[it.id] ?: 0).toString() }
        listOf(partDesc) + custVals + listOf(row.totalQty.toString())
    }
    val grandTotal = matrixRows.sumOf { it.totalQty }
    val summaries = listOf(
        "إجمالي الأصناف: ${matrixRows.size} صنف",
        "إجمالي القطع المطلوبة للعملاء: $grandTotal قطعة",
        "عدد العملاء: ${customers.size} عميل"
    )

    // Export PDF
    ExportUtils.exportToPdf(
        context = context,
        fileName = "Matrix_Orders_Report",
        title = "جدول الطلبات الجماعي للأصناف والعملاء",
        subtitle = "توزيع كميات الأصناف المطلوبة لكافة العملاء والإجمالي",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )

    // Export Excel CSV
    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Matrix_Orders_Report",
        title = "جدول الطلبات الجماعي للأصناف والعملاء",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportCustomerOrderExcel(
    context: Context,
    customer: CustomerEntity,
    order: OrderEntity?,
    items: List<com.example.data.model.OrderItemWithPart>,
    totalAmount: Double,
    paidAmount: Double,
    remaining: Double,
    appUserName: String = "عيسى عبدربه"
) {
    val headers = listOf("الصنف", "نوع وموديل السيارة", "الكمية المطلوبة", "سعر القطعة", "الإجمالي")
    val rows = items.map { item ->
        val lineTotal = item.item.quantity * item.part.price
        listOf(
            item.part.name,
            "${item.part.carType} ${item.part.carModel}",
            item.item.quantity.toString(),
            "${item.part.price} ر.س",
            "$lineTotal ر.س"
        )
    }
    val summaries = listOf(
        "العميل: ${customer.name} (هاتف: ${customer.phone})",
        "تاريخ الطلب: ${order?.orderDate ?: ""}",
        "إجمالي الفاتورة: $totalAmount ر.س",
        "المبلغ الواصل (المسدد): $paidAmount ر.س",
        "المبلغ المتبقي: $remaining ر.س",
        "حالة السداد: ${order?.paymentStatus ?: "غير مسدد"}"
    )

    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Customer_Order_${customer.name}_#${order?.id ?: 0}",
        title = "فاتورة طلب العميل: ${customer.name}",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportCustomerOrderPdf(
    context: Context,
    customer: CustomerEntity,
    order: OrderEntity?,
    items: List<com.example.data.model.OrderItemWithPart>,
    totalAmount: Double,
    paidAmount: Double,
    remaining: Double,
    appUserName: String = "عيسى عبدربه"
) {
    val headers = listOf("الصنف", "السيارة", "الكمية", "السعر", "الإجمالي")
    val rows = items.map { item ->
        val lineTotal = item.item.quantity * item.part.price
        listOf(
            item.part.name,
            "${item.part.carType} ${item.part.carModel}",
            item.item.quantity.toString(),
            "${item.part.price} ر.س",
            "$lineTotal ر.س"
        )
    }
    val summaries = listOf(
        "العميل: ${customer.name} | الجوال: ${customer.phone}",
        "رقم الطلب: #${order?.id ?: 0} | التاريخ: ${order?.orderDate ?: ""}",
        "إجمالي القيمة: $totalAmount ر.س",
        "المبلغ الواصل (المسدد): $paidAmount ر.س",
        "المبلغ المتبقي على العميل: $remaining ر.س",
        "حالة الطلب والسداد: ${order?.paymentStatus ?: "غير مسدد"}"
    )

    ExportUtils.exportToPdf(
        context = context,
        fileName = "Order_${customer.name}_#${order?.id ?: 0}",
        title = "سند طلب مبيعات للعميل: ${customer.name}",
        subtitle = "نظام قطع غيار Pro - إدارة المخزون والمبيعات",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportSupplierOrdersBoth(context: Context, orders: List<SupplierOrderWithPart>, appUserName: String = "عيسى عبدربه") {
    val headers = listOf("رقم الطلب", "الصنف", "السيارة", "الكمية المطلوبة", "المستلم (الواصل)", "المتبقي", "تاريخ الطلب", "تاريخ التأكيد", "الحالة")
    val rows = orders.map { o ->
        val s = o.supplierOrder
        val p = o.part
        listOf(
            "#${s.id}",
            p?.name ?: "صنف #${s.partId}",
            p?.carType ?: "",
            s.qtyOrdered.toString(),
            s.qtyReceived.toString(),
            s.qtyRemaining.toString(),
            s.orderDate,
            s.confirmedDate,
            s.status
        )
    }
    val summaries = listOf(
        "إجمالي عدد طلبيات التوريد: ${orders.size}",
        "إجمالي القطع المطلوبة من الموردين: ${orders.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
        "إجمالي القطع المستلمة: ${orders.sumOf { it.supplierOrder.qtyReceived }} قطعة",
        "إجمالي القطع المتبقية: ${orders.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
    )

    ExportUtils.exportToPdf(
        context = context,
        fileName = "Supplier_Orders_Summary",
        title = "تقرير طلبيات وشحنات الموردين",
        subtitle = "توثيق شامل للطلبيات والتواريخ والكميات المتبقية",
        headers = headers.take(6),
        rows = rows.map { it.take(6) },
        summaryLines = summaries,
        appUserName = appUserName
    )

    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Supplier_Orders_Summary",
        title = "تقرير طلبيات الموردين الشامل",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportSingleSupplierOrderPdf(
    context: Context,
    orderDate: String,
    confirmedDate: String,
    notes: String,
    items: List<SupplierOrderWithPart>,
    appUserName: String = "عيسى عبدربه"
) {
    val headers = listOf("رقم الطلب", "الصنف", "السيارة", "المطلوب", "الواصل", "المتبقي", "الحالة")
    val rows = items.map { item ->
        val s = item.supplierOrder
        val p = item.part
        listOf(
            "#${s.id}",
            p?.name ?: "صنف #${s.partId}",
            p?.carType ?: "",
            s.qtyOrdered.toString(),
            s.qtyReceived.toString(),
            s.qtyRemaining.toString(),
            when (s.status) {
                "completed" -> "مكتملة"
                "partial" -> "مستلمة جزئياً"
                "cancelled" -> "ملغية"
                else -> "مؤكدة"
            }
        )
    }
    val totalOrd = items.sumOf { it.supplierOrder.qtyOrdered }
    val totalRec = items.sumOf { it.supplierOrder.qtyReceived }
    val totalRem = items.sumOf { it.supplierOrder.qtyRemaining }
    val summaries = listOf(
        "تاريخ الطلب: $orderDate | تاريخ التأكيد: $confirmedDate",
        "إجمالي الأصناف: ${items.size} صنف | إجمالي القطع المطلوبة: $totalOrd قطعة",
        "إجمالي القطع المستلمة: $totalRec قطعة | المتبقي: $totalRem قطعة",
        if (notes.isNotBlank()) "ملاحظات: $notes" else "لا توجد ملاحظات إضافية"
    )

    ExportUtils.exportToPdf(
        context = context,
        fileName = "Supplier_Order_${orderDate.replace('-', '_')}",
        title = "سند طلبية توريد الموردين",
        subtitle = "بيانات تفصيلية لطلبية التوريد والقطع المتبقية",
        headers = headers.take(6),
        rows = rows.map { it.take(6) },
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportSingleSupplierOrderExcel(
    context: Context,
    orderDate: String,
    confirmedDate: String,
    notes: String,
    items: List<SupplierOrderWithPart>,
    appUserName: String = "عيسى عبدربه"
) {
    val headers = listOf("رقم الطلب", "الصنف", "نوع وموديل السيارة", "الكمية المطلوبة", "المستلم (الواصل)", "الكمية المتبقية", "تاريخ الطلب", "تاريخ التأكيد", "الحالة")
    val rows = items.map { item ->
        val s = item.supplierOrder
        val p = item.part
        listOf(
            "#${s.id}",
            p?.name ?: "صنف #${s.partId}",
            "${p?.carType ?: ""} ${p?.carModel ?: ""}".trim(),
            s.qtyOrdered.toString(),
            s.qtyReceived.toString(),
            s.qtyRemaining.toString(),
            s.orderDate,
            s.confirmedDate,
            s.status
        )
    }
    val totalOrd = items.sumOf { it.supplierOrder.qtyOrdered }
    val totalRec = items.sumOf { it.supplierOrder.qtyReceived }
    val totalRem = items.sumOf { it.supplierOrder.qtyRemaining }
    val summaries = listOf(
        "تاريخ الطلب: $orderDate | تاريخ التأكيد: $confirmedDate",
        "إجمالي عدد الأصناف: ${items.size}",
        "إجمالي القطع المطلوبة: $totalOrd قطعة | المستلمة: $totalRec قطعة | المتبقية: $totalRem قطعة",
        if (notes.isNotBlank()) "ملاحظات الطلبية: $notes" else ""
    )

    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Supplier_Order_${orderDate.replace('-', '_')}",
        title = "بيانات طلبية الموردين ($orderDate)",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

@Composable
fun SelectPartForCustomerDialog(
    parts: List<PartEntity>,
    onDismiss: () -> Unit,
    onPartSelected: (partId: Long, qty: Int) -> Unit,
    onAddNewPart: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    val filteredParts = remember(parts, searchQuery) {
        val filtered = if (searchQuery.isBlank()) parts else {
            parts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.carType.contains(searchQuery, ignoreCase = true) ||
                it.carModel.contains(searchQuery, ignoreCase = true)
            }
        }
        filtered.sortedWith { p1, p2 -> collator.compare(p1.name, p2.name) }
    }

    var selectedPartId by remember { mutableStateOf<Long?>(filteredParts.firstOrNull()?.id) }
    var qtyText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة صنف لطلب العميل", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (parts.isEmpty()) {
                    Text("لا توجد أصناف معرفة بالمستودع")
                    Button(onClick = onAddNewPart) {
                        Text("➕ إضافة صنف جديد")
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (selectedPartId !in filteredParts.map { p -> p.id }) {
                                selectedPartId = filteredParts.firstOrNull()?.id
                            }
                        },
                        placeholder = { Text("بحث ذكي عن صنف أو نوع سيارة...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = "اختر الصنف (${filteredParts.size} متطابق، مرتبة أبجدياً):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(filteredParts) { part ->
                            val isSelected = selectedPartId == part.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPartId = part.id }
                                    .background(
                                        if (isSelected) PrimaryLighter else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(part.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${part.carType} ${part.carModel} • ${part.price} ر.س", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Text("✓", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("الكمية المطلوبة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pId = selectedPartId
                    val q = qtyText.toIntOrNull() ?: 1
                    if (pId != null && q > 0) {
                        onPartSelected(pId, q)
                    }
                },
                enabled = selectedPartId != null
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
