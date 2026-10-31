package com.example.ui.screens

import android.content.Context
import android.net.Uri
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.ui.util.AttachmentUtils
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
    onClearMatrixQuantities: () -> Unit = {},
    onLoadSupplierOrderIntoMatrix: (orderId: Long) -> Unit = {},
    onReorderRemainingIntoMatrix: (orderId: Long) -> Unit = {},
    onEditSupplierOrderReceived: (orderId: Long, newReceivedQty: Int, receivingDate: String, receivingNote: String) -> Unit = { _, _, _, _ -> },
    onUpdateSupplierOrderAttachment: (orderId: Long, uri: String?) -> Unit = { _, _ -> },
    onUpdateCustomerOrderAttachment: (orderId: Long, uri: String?) -> Unit = { _, _ -> },
    onCreateNewCustomerOrder: (customerId: Long, orderDate: String, title: String) -> Unit = { _, _, _ -> },
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
                onConfirmBulkSupplierOrders = onConfirmBulkSupplierOrders,
                onClearMatrix = onClearMatrixQuantities
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
                onUpdateAttachment = onUpdateCustomerOrderAttachment,
                onCreateNewOrder = onCreateNewCustomerOrder,
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
                onLoadIntoMatrix = onLoadSupplierOrderIntoMatrix,
                onReorderRemainingIntoMatrix = onReorderRemainingIntoMatrix,
                onEditReceived = onEditSupplierOrderReceived,
                onUpdateAttachment = onUpdateSupplierOrderAttachment,
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
    onConfirmBulkSupplierOrders: (orderDate: String, confirmedDate: String, notes: String) -> Unit,
    onClearMatrix: () -> Unit = {}
) {
    val context = LocalContext.current
    var cellToEdit by remember { mutableStateOf<Triple<PartEntity, CustomerEntity, Int>?>(null) }
    var showBulkConfirmDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

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
                // Clear Matrix Button
                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.ClearAll, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("تصفير الجدول", fontSize = 11.sp, color = RoseDanger, fontWeight = FontWeight.Bold)
                }

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

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = { Icon(Icons.Default.ClearAll, contentDescription = null, tint = RoseDanger) },
            title = { Text("تصفير جدول العرض الجماعي", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في تصفير جميع الكميات في جدول العرض الجماعي لبدء طلبيات جديدة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearMatrix()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، تصفير الجدول")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("إلغاء")
                }
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
    onUpdateAttachment: (orderId: Long, uri: String?) -> Unit = { _, _ -> },
    onCreateNewOrder: (customerId: Long, orderDate: String, title: String) -> Unit = { _, _, _ -> },
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showPartSelectorDialog by remember { mutableStateOf(false) }
    var targetOrderIdForAddPart by remember { mutableStateOf<Long?>(null) }
    var showCreateOrderDialog by remember { mutableStateOf(false) }

    var orderToEditPaid by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToEditDetails by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToExportData by remember { mutableStateOf<Triple<CustomerEntity, OrderEntity, List<com.example.data.model.OrderItemWithPart>>?>(null) }
    var exportIsPdf by remember { mutableStateOf(true) }

    var itemSearchQuery by remember { mutableStateOf("") }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    var pendingAttachmentOrderId by remember { mutableStateOf<Long?>(null) }
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingAttachmentOrderId?.let { orderId ->
                val saved = AttachmentUtils.saveAttachment(context, it, "customer_order_$orderId")
                onUpdateAttachment(orderId, saved)
            }
        }
        pendingAttachmentOrderId = null
    }

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }

    if (selectedCustomerId != null) {
        val selectedCustomerWithOrder = customerOrders.find { it.customer.id == selectedCustomerId }

        BackHandler {
            onSelectCustomer(null)
        }

        if (selectedCustomerWithOrder == null) {
            onSelectCustomer(null)
            return
        }

        val customer = selectedCustomerWithOrder.customer
        val ordersList = remember(selectedCustomerWithOrder) {
            if (selectedCustomerWithOrder.allOrders.isNotEmpty()) {
                selectedCustomerWithOrder.allOrders.sortedByDescending { it.order.orderDate }
            } else if (selectedCustomerWithOrder.activeOrder != null) {
                val act = selectedCustomerWithOrder.activeOrder!!
                val itms = selectedCustomerWithOrder.items
                val q = itms.sumOf { it.item.quantity }
                val amt = itms.sumOf { it.item.quantity * it.part.price }
                listOf(com.example.data.model.CustomerOrderDetail(act, itms, q, amt))
            } else {
                emptyList()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header with Back button and Create Order button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = { onSelectCustomer(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text(
                            text = "طلبات العميل: ${customer.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = "الجوال: ${customer.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = { showCreateOrderDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طلبية جديدة", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search in customer parts
            OutlinedTextField(
                value = itemSearchQuery,
                onValueChange = { itemSearchQuery = it },
                placeholder = { Text("بحث ذكي عن صنف في طلبات العميل...", fontSize = 12.sp) },
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

            Spacer(modifier = Modifier.height(8.dp))

            if (ordersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد أي بطاقات طلبية لهذا العميل حالياً",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { showCreateOrderDialog = true }) {
                            Text("➕ إنشاء أول طلبية للعميل")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordersList) { orderDetail ->
                        val order = orderDetail.order
                        val rawItems = orderDetail.items
                        val filteredItems = remember(rawItems, itemSearchQuery) {
                            val list = if (itemSearchQuery.isBlank()) rawItems else {
                                rawItems.filter {
                                    it.part.name.contains(itemSearchQuery, ignoreCase = true) ||
                                    it.part.carType.contains(itemSearchQuery, ignoreCase = true) ||
                                    it.part.carModel.contains(itemSearchQuery, ignoreCase = true)
                                }
                            }
                            list.sortedWith { i1, i2 -> collator.compare(i1.part.name, i2.part.name) }
                        }

                        val totalPieces = orderDetail.totalQty
                        val calculatedTotal = orderDetail.totalAmount
                        val paidAmount = order.paidAmount
                        val discount = order.discount
                        val remaining = maxOf(0.0, (calculatedTotal - discount) - paidAmount)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Order Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "📦 طلبية: ${order.orderDate.ifBlank { "غير محدد" }}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = PrimaryBlue
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
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
                                        if (order.notes.isNotBlank()) {
                                            Text(
                                                text = order.notes,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Attachment preview/add button
                                    if (!order.attachmentUri.isNullOrBlank()) {
                                        val displayName = AttachmentUtils.getDisplayName(context, order.attachmentUri)
                                        Box(
                                            modifier = Modifier
                                                .background(PrimaryLighter, RoundedCornerShape(8.dp))
                                                .clickable { AttachmentUtils.openAttachment(context, order.attachmentUri) }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = displayName.take(12),
                                                    fontSize = 10.sp,
                                                    color = PrimaryBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "إزالة المرفق",
                                                    tint = RoseDanger,
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clickable { onUpdateAttachment(order.id, null) }
                                                )
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                pendingAttachmentOrderId = order.id
                                                attachmentPicker.launch("*/*")
                                            },
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("إرفاق", fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Financial bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الفاتورة: $calculatedTotal ر.س", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    Text("الواصل: $paidAmount ر.س", fontSize = 11.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                    Text("المتبقي: $remaining ر.س", fontSize = 11.5.sp, color = if (remaining > 0) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick actions for this order card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { orderToEditPaid = order },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("الواصل", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { orderToEditDetails = order },
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("تعديل", fontSize = 10.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            targetOrderIdForAddPart = order.id
                                            showPartSelectorDialog = true
                                        },
                                        modifier = Modifier.weight(1.2f).height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("إضافة صنف", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = { orderToDelete = order },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                                        modifier = Modifier.weight(0.9f).height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("حذف", fontSize = 10.sp, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Items Table/List
                                if (filteredItems.isEmpty()) {
                                    Text("لا توجد أصناف في هذه الطلبية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        filteredItems.forEachIndexed { index, itemWithPart ->
                                            val item = itemWithPart.item
                                            val part = itemWithPart.part
                                            val lineTotal = item.quantity * part.price

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.width(22.dp)
                                                    )
                                                    Column {
                                                        Text(part.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("${part.carType} • ${part.carModel} (${part.price} ر.س)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = { onUpdateQty(item.id, item.quantity - 1) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    }
                                                    Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    IconButton(
                                                        onClick = { onUpdateQty(item.id, item.quantity + 1) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    }

                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("$lineTotal ر.س", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberSecondary)

                                                    IconButton(
                                                        onClick = { onDeleteOrderItem(item) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Export Buttons for this specific order card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            exportIsPdf = false
                                            orderToExportData = Triple(customer, order, rawItems)
                                        },
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تصدير Excel", fontSize = 10.5.sp)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Button(
                                        onClick = {
                                            exportIsPdf = true
                                            orderToExportData = Triple(customer, order, rawItems)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تصدير PDF", fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete order card dialog without returning stock (explicit user rule)
        orderToDelete?.let { ord ->
            AlertDialog(
                onDismissRequest = { orderToDelete = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
                title = { Text("حذف بطاقة الطلبية", fontWeight = FontWeight.Bold) },
                text = {
                    Text("هل أنت متأكد من رغبتك في حذف بطاقة الطلبية بتاريخ (${ord.orderDate}) نهائياً؟ سيتم الحذف دون إرجاع الكميات للمخزون.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteOrderCard(ord.id, false)
                            orderToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Text("نعم، حذف الطلبية")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { orderToDelete = null }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Edit paid amount dialog
        orderToEditPaid?.let { ord ->
            EditPaidAmountDialog(
                order = ord,
                customerName = customer.name,
                onDismiss = { orderToEditPaid = null },
                onConfirm = { paid, disc ->
                    onUpdatePayment(ord.id, paid, disc)
                    orderToEditPaid = null
                }
            )
        }

        // Edit order details dialog
        orderToEditDetails?.let { ord ->
            EditOrderDetailsDialog(
                order = ord,
                customerName = customer.name,
                onDismiss = { orderToEditDetails = null },
                onConfirm = { newTitle, newNotes, newDate ->
                    onUpdateOrderDetails(ord.id, newTitle, newNotes, newDate)
                    orderToEditDetails = null
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
                            text = "انقر على أي عميل لعرض كافة طلبياته ومرفقاتها والمدفوعات",
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
                val ordersCount = custWithOrder.allOrders.size
                val pieces = custWithOrder.totalQty
                val totalCost = custWithOrder.allOrders.sumOf { it.totalAmount }
                val paid = custWithOrder.allOrders.sumOf { it.order.paidAmount }
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
                                        text = if (ordersCount > 0) "$ordersCount طلبيات مسجلة" else "لا توجد طلبيات",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
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

                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { customerToDelete = customer },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف العميل", tint = RoseDanger, modifier = Modifier.size(18.dp))
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

        // Delete Customer confirmation
        customerToDelete?.let { cust ->
            AlertDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text("حذف العميل", fontWeight = FontWeight.Bold) },
                text = { Text("هل أنت متأكد من حذف العميل (${cust.name}) نهائياً؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteCustomer(cust)
                            customerToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerToDelete = null }) { Text("إلغاء") }
                }
            )
        }
    }

    // Dialog for creating a new customer order
    if (showCreateOrderDialog && selectedCustomerId != null) {
        CreateNewCustomerOrderDialog(
            onDismiss = { showCreateOrderDialog = false },
            onConfirm = { date, title ->
                onCreateNewOrder(selectedCustomerId, date, title)
                showCreateOrderDialog = false
            }
        )
    }

    // Dialog for adding a part to a specific customer order
    if (showPartSelectorDialog && selectedCustomerId != null) {
        SelectPartForCustomerDialog(
            parts = parts,
            onDismiss = {
                showPartSelectorDialog = false
                targetOrderIdForAddPart = null
            },
            onPartSelected = { partId, qty ->
                onAddPartToOrder(selectedCustomerId, partId, qty)
                showPartSelectorDialog = false
                targetOrderIdForAddPart = null
            },
            onAddNewPart = onAddNewPart
        )
    }

    // Export Customization Dialog for Customer Order
    orderToExportData?.let { (cust, ord, rawItems) ->
        CustomerOrderExportCustomizationDialog(
            customer = cust,
            order = ord,
            items = rawItems,
            isPdf = exportIsPdf,
            onDismiss = { orderToExportData = null },
            appUserName = appUserName
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
    onLoadIntoMatrix: (orderId: Long) -> Unit = {},
    onReorderRemainingIntoMatrix: (orderId: Long) -> Unit = {},
    onEditReceived: (orderId: Long, newReceivedQty: Int, receivingDate: String, receivingNote: String) -> Unit = { _, _, _, _ -> },
    onUpdateAttachment: (orderId: Long, uri: String?) -> Unit = { _, _ -> },
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var orderToReceive by remember { mutableStateOf<SupplierOrderEntity?>(null) }
    var orderToEdit by remember { mutableStateOf<Pair<SupplierOrderEntity, String>?>(null) }
    var orderToEditReceived by remember { mutableStateOf<SupplierOrderEntity?>(null) }
    var selectedGroupKey by remember { mutableStateOf<String?>(null) }
    var groupToDelete by remember { mutableStateOf<SupplierOrderGroup?>(null) }
    var inOrderSearchQuery by remember { mutableStateOf("") }

    var singleOrderToExport by remember { mutableStateOf<SupplierOrderGroup?>(null) }
    var singleOrderExportIsPdf by remember { mutableStateOf(true) }

    var pendingSupplierAttachmentOrderId by remember { mutableStateOf<Long?>(null) }
    val supplierAttachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingSupplierAttachmentOrderId?.let { orderId ->
                val saved = AttachmentUtils.saveAttachment(context, it, "supplier_order_$orderId")
                onUpdateAttachment(orderId, saved)
            }
        }
        pendingSupplierAttachmentOrderId = null
    }

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
            }

            // Top Actions: Load into Matrix, Reorder Remaining, Attachment, Delete
            val firstOrd = selectedGroup.items.first().supplierOrder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Load into Matrix to edit (enabled ONLY if totalReceived == 0)
                OutlinedButton(
                    onClick = {
                        onLoadIntoMatrix(firstOrd.id)
                        Toast.makeText(context, "تم تحميل الطلبية في العرض الجماعي للتعديل عليها", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedGroup.totalReceived == 0,
                    modifier = Modifier.weight(1.1f).height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("تعديل بالجماعي", fontSize = 10.sp)
                }

                // 2. Reorder remaining into Matrix (enabled ONLY if totalReceived > 0 && totalRemaining > 0)
                Button(
                    onClick = {
                        onReorderRemainingIntoMatrix(firstOrd.id)
                        Toast.makeText(context, "تم نقل الأصناف المتبقية للعرض الجماعي لإنشاء طلبية منفصلة", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedGroup.totalReceived > 0 && selectedGroup.totalRemaining > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                    modifier = Modifier.weight(1.1f).height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("إعادة طلب المتبقي", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // 3. Attachment button / preview
                if (!firstOrd.attachmentUri.isNullOrBlank()) {
                    val disp = AttachmentUtils.getDisplayName(context, firstOrd.attachmentUri)
                    Box(
                        modifier = Modifier
                            .background(PrimaryLighter, RoundedCornerShape(8.dp))
                            .clickable { AttachmentUtils.openAttachment(context, firstOrd.attachmentUri) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(disp.take(7), fontSize = 10.sp, color = PrimaryBlue)
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                tint = RoseDanger,
                                modifier = Modifier.size(11.dp).clickable { onUpdateAttachment(firstOrd.id, null) }
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            pendingSupplierAttachmentOrderId = firstOrd.id
                            supplierAttachmentPicker.launch("*/*")
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("مرفق", fontSize = 10.sp)
                    }
                }

                // 4. Delete group button
                Button(
                    onClick = { groupToDelete = selectedGroup },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                    modifier = Modifier.height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("حذف", fontSize = 10.sp, color = Color.White)
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
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (order.status != "completed" && order.status != "cancelled") {
                                        Button(
                                            onClick = { orderToReceive = order },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            modifier = Modifier.weight(1.1f).height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("استلام دفعة", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Edit received quantity & date tracking
                                    OutlinedButton(
                                        onClick = { orderToEditReceived = order },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSuccess),
                                        modifier = Modifier.weight(1.1f).height(32.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("تعديل الواصل", fontSize = 10.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { orderToEdit = Pair(order, partName) },
                                        modifier = Modifier.weight(0.9f).height(32.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("تعديل", fontSize = 10.sp)
                                    }

                                    if (order.status != "cancelled") {
                                        OutlinedButton(
                                            onClick = { onCancelSupplierOrder(order.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                                            modifier = Modifier.weight(0.9f).height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("إلغاء", fontSize = 10.sp)
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
                                singleOrderExportIsPdf = false
                                singleOrderToExport = selectedGroup
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Excel", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                singleOrderExportIsPdf = true
                                singleOrderToExport = selectedGroup
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
                                exportAllSupplierOrdersExcel(context, supplierOrders, appUserName)
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Excel", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                exportAllSupplierOrdersPdf(context, supplierOrders, appUserName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("PDF", fontSize = 11.sp)
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

    orderToEditReceived?.let { ord ->
        EditReceivedDialog(
            order = ord,
            onDismiss = { orderToEditReceived = null },
            onConfirm = { newRec, date, note ->
                onEditReceived(ord.id, newRec, date, note)
                orderToEditReceived = null
            }
        )
    }

    singleOrderToExport?.let { grp ->
        SingleSupplierOrderExportCustomizationDialog(
            group = grp,
            isPdf = singleOrderExportIsPdf,
            onDismiss = { singleOrderToExport = null },
            appUserName = appUserName
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

fun exportAllSupplierOrdersPdf(context: Context, orders: List<SupplierOrderWithPart>, appUserName: String = "عيسى عبدربه") {
    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
        strength = java.text.Collator.PRIMARY
    }
    val sortedOrders = orders.sortedWith { o1, o2 ->
        collator.compare(o1.part?.name ?: "", o2.part?.name ?: "")
    }
    val headers = listOf("م", "الصنف", "السيارة", "المطلوب", "الواصل", "المتبقي", "تاريخ الطلب", "الحالة")
    val rows = sortedOrders.mapIndexed { index, o ->
        val s = o.supplierOrder
        val p = o.part
        listOf(
            (index + 1).toString(),
            p?.name ?: "صنف #${s.partId}",
            p?.carType ?: "",
            s.qtyOrdered.toString(),
            s.qtyReceived.toString(),
            s.qtyRemaining.toString(),
            s.orderDate,
            when (s.status) {
                "completed" -> "مكتمل"
                "partial" -> "مستلم جزئياً"
                "cancelled" -> "ملغي"
                else -> "مؤكد"
            }
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
        subtitle = "توثيق شامل للطلبيات مرتبة أبجدياً بالتسلسل التلقائي",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

fun exportAllSupplierOrdersExcel(context: Context, orders: List<SupplierOrderWithPart>, appUserName: String = "عيسى عبدربه") {
    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
        strength = java.text.Collator.PRIMARY
    }
    val sortedOrders = orders.sortedWith { o1, o2 ->
        collator.compare(o1.part?.name ?: "", o2.part?.name ?: "")
    }
    val headers = listOf("م", "الصنف", "نوع وموديل السيارة", "الكمية المطلوبة", "المستلم (الواصل)", "الكمية المتبقية", "تاريخ الطلب", "تاريخ التأكيد", "الحالة")
    val rows = sortedOrders.mapIndexed { index, o ->
        val s = o.supplierOrder
        val p = o.part
        listOf(
            (index + 1).toString(),
            p?.name ?: "صنف #${s.partId}",
            "${p?.carType ?: ""} ${p?.carModel ?: ""}".trim(),
            s.qtyOrdered.toString(),
            s.qtyReceived.toString(),
            s.qtyRemaining.toString(),
            s.orderDate,
            s.confirmedDate,
            when (s.status) {
                "completed" -> "مكتمل"
                "partial" -> "مستلم جزئياً"
                "cancelled" -> "ملغي"
                else -> "مؤكد"
            }
        )
    }
    val summaries = listOf(
        "إجمالي عدد طلبيات التوريد: ${orders.size}",
        "إجمالي القطع المطلوبة من الموردين: ${orders.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
        "إجمالي القطع المستلمة: ${orders.sumOf { it.supplierOrder.qtyReceived }} قطعة",
        "إجمالي القطع المتبقية: ${orders.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
    )

    ExportUtils.exportToExcelCsv(
        context = context,
        fileName = "Supplier_Orders_Summary",
        title = "تقرير طلبيات الموردين الشامل (مرتب أبجدياً)",
        headers = headers,
        rows = rows,
        summaryLines = summaries,
        appUserName = appUserName
    )
}

@Composable
fun CreateNewCustomerOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, title: String) -> Unit
) {
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var orderDate by remember { mutableStateOf(today) }
    var orderTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء بطاقة طلبية جديدة للعميل", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = orderDate,
                    onValueChange = { orderDate = it },
                    label = { Text("تاريخ الطلبية (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = orderTitle,
                    onValueChange = { orderTitle = it },
                    label = { Text("عنوان / وصف الطلبية (اختياري)") },
                    placeholder = { Text("مثال: طلبية قطع محرك وتبريد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(orderDate.ifBlank { today }, orderTitle.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("إنشاء الطلبية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun CustomerOrderExportCustomizationDialog(
    customer: CustomerEntity,
    order: OrderEntity,
    items: List<com.example.data.model.OrderItemWithPart>,
    isPdf: Boolean,
    onDismiss: () -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showPartCol by remember { mutableStateOf(true) }
    var showCarCol by remember { mutableStateOf(true) }
    var showQtyCol by remember { mutableStateOf(true) }
    var showPriceCol by remember { mutableStateOf(true) }
    var showTotalCol by remember { mutableStateOf(true) }

    var sumTotalCost by remember { mutableStateOf(true) }
    var sumPaid by remember { mutableStateOf(true) }
    var sumRemaining by remember { mutableStateOf(true) }
    var sumCustomerPhone by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تخصيص تصدير طلب العميل (${if (isPdf) "PDF" else "Excel"})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("الأعمدة المراد تضمينها (التسلسل التلقائي 'م' مضمن أولاً):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showPartCol, onCheckedChange = { showPartCol = it })
                    Text("اسم الصنف", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showCarCol, onCheckedChange = { showCarCol = it })
                    Text("نوع وموديل السيارة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showQtyCol, onCheckedChange = { showQtyCol = it })
                    Text("الكمية المطلوبة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showPriceCol, onCheckedChange = { showPriceCol = it })
                    Text("سعر الحبة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showTotalCol, onCheckedChange = { showTotalCol = it })
                    Text("إجمالي البند", fontSize = 12.sp)
                }

                HorizontalDivider()
                Text("ملخص الفاتورة والسداد في التذييل:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumTotalCost, onCheckedChange = { sumTotalCost = it })
                    Text("إجمالي قيمة الطلب", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumPaid, onCheckedChange = { sumPaid = it })
                    Text("المبلغ الواصل (المسدد)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumRemaining, onCheckedChange = { sumRemaining = it })
                    Text("المبلغ المتبقي", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumCustomerPhone, onCheckedChange = { sumCustomerPhone = it })
                    Text("بيانات التواصل بالعميل", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
                        strength = java.text.Collator.PRIMARY
                    }
                    val sortedItems = items.sortedWith { i1, i2 ->
                        collator.compare(i1.part.name, i2.part.name)
                    }

                    val headers = mutableListOf<String>()
                    headers.add("م")
                    if (showPartCol) headers.add("الصنف")
                    if (showCarCol) headers.add("السيارة")
                    if (showQtyCol) headers.add("الكمية")
                    if (showPriceCol) headers.add("السعر")
                    if (showTotalCol) headers.add("الإجمالي")

                    val rows = sortedItems.mapIndexed { index, item ->
                        val lineTotal = item.item.quantity * item.part.price
                        val r = mutableListOf<String>()
                        r.add((index + 1).toString())
                        if (showPartCol) r.add(item.part.name)
                        if (showCarCol) r.add("${item.part.carType} ${item.part.carModel}".trim())
                        if (showQtyCol) r.add(item.item.quantity.toString())
                        if (showPriceCol) r.add("${item.part.price} ر.س")
                        if (showTotalCol) r.add("$lineTotal ر.س")
                        r
                    }

                    val totalAmount = items.sumOf { it.item.quantity * it.part.price }
                    val remaining = (totalAmount - order.paidAmount).coerceAtLeast(0.0)

                    val summaries = mutableListOf<String>()
                    if (sumCustomerPhone) summaries.add("العميل: ${customer.name} | الجوال: ${customer.phone}")
                    summaries.add("رقم الطلبية: #${order.id} | التاريخ: ${order.orderDate}")
                    if (sumTotalCost) summaries.add("إجمالي الفاتورة: $totalAmount ر.س")
                    if (sumPaid) summaries.add("المبلغ الواصل: ${order.paidAmount} ر.س")
                    if (sumRemaining) summaries.add("المبلغ المتبقي: $remaining ر.س")

                    val fileBase = "Order_${customer.name}_#${order.id}"
                    if (isPdf) {
                        ExportUtils.exportToPdf(
                            context = context,
                            fileName = fileBase,
                            title = "سند طلب مبيعات: ${customer.name}",
                            subtitle = "نظام قطع غيار Pro - إدارة المخزون والمبيعات",
                            headers = headers,
                            rows = rows,
                            summaryLines = summaries,
                            appUserName = appUserName
                        )
                    } else {
                        ExportUtils.exportToExcelCsv(
                            context = context,
                            fileName = fileBase,
                            title = "طلب العميل ${customer.name}",
                            headers = headers,
                            rows = rows,
                            summaryLines = summaries,
                            appUserName = appUserName
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تصدير الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun EditReceivedDialog(
    order: SupplierOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (newReceivedQty: Int, receivingDate: String, receivingNote: String) -> Unit
) {
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var receivedQtyText by remember { mutableStateOf(order.qtyReceived.toString()) }
    var receivingDate by remember { mutableStateOf(today) }
    var receivingNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الكمية الواردة (الواصل)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الكمية المطلوبة في الطلب: ${order.qtyOrdered} قطعة",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = receivedQtyText,
                    onValueChange = { receivedQtyText = it },
                    label = { Text("إجمالي الكمية المستلمة حتى الآن") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = receivingDate,
                    onValueChange = { receivingDate = it },
                    label = { Text("تاريخ استلام الدفعة (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = receivingNote,
                    onValueChange = { receivingNote = it },
                    label = { Text("ملاحظات الاستلام (اختياري)") },
                    placeholder = { Text("مثال: استلام الدفعة الثانية من الفرع الرئيسي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = receivedQtyText.toIntOrNull() ?: order.qtyReceived
                    onConfirm(q, receivingDate.ifBlank { today }, receivingNote.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("حفظ التعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SingleSupplierOrderExportCustomizationDialog(
    group: SupplierOrderGroup,
    isPdf: Boolean,
    onDismiss: () -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var showPartCol by remember { mutableStateOf(true) }
    var showCarCol by remember { mutableStateOf(true) }
    var showOrderedCol by remember { mutableStateOf(true) }
    var showReceivedCol by remember { mutableStateOf(true) }
    var showRemainingCol by remember { mutableStateOf(true) }
    var showStatusCol by remember { mutableStateOf(true) }

    var sumDates by remember { mutableStateOf(true) }
    var sumItemCount by remember { mutableStateOf(true) }
    var sumTotalOrdered by remember { mutableStateOf(true) }
    var sumTotalReceived by remember { mutableStateOf(true) }
    var sumTotalRemaining by remember { mutableStateOf(true) }
    var sumNotes by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تخصيص تصدير طلبية التوريد (${if (isPdf) "PDF" else "Excel"})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("الأعمدة المراد تضمينها (م مرتبة أبجدياً):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showPartCol, onCheckedChange = { showPartCol = it })
                    Text("اسم الصنف", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showCarCol, onCheckedChange = { showCarCol = it })
                    Text("نوع السيارة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showOrderedCol, onCheckedChange = { showOrderedCol = it })
                    Text("الكمية المطلوبة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showReceivedCol,
                        onCheckedChange = {
                            showReceivedCol = it
                            if (!it) {
                                // If received is unchecked, remaining is unchecked and disabled
                                showRemainingCol = false
                            }
                        }
                    )
                    Text("الكمية الواصلة (المستلمة)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showRemainingCol,
                        onCheckedChange = { showRemainingCol = it },
                        enabled = showReceivedCol
                    )
                    Text(
                        text = "الكمية المتبقية" + if (!showReceivedCol) " (معطل عند إلغاء الواصل)" else "",
                        fontSize = 12.sp,
                        color = if (showReceivedCol) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showStatusCol, onCheckedChange = { showStatusCol = it })
                    Text("حالة البند", fontSize = 12.sp)
                }

                HorizontalDivider()
                Text("ملخص الطلبية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumDates, onCheckedChange = { sumDates = it })
                    Text("تواريخ الطلب والتأكيد", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumItemCount, onCheckedChange = { sumItemCount = it })
                    Text("عدد الأصناف", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumTotalOrdered, onCheckedChange = { sumTotalOrdered = it })
                    Text("إجمالي الكميات المطلوبة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumTotalReceived, onCheckedChange = { sumTotalReceived = it })
                    Text("إجمالي الكميات الواصلة", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sumTotalRemaining, onCheckedChange = { sumTotalRemaining = it })
                    Text("إجمالي الكميات المتبقية", fontSize = 12.sp)
                }
                if (group.notes.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = sumNotes, onCheckedChange = { sumNotes = it })
                        Text("ملاحظات الطلبية", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val collator = java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
                        strength = java.text.Collator.PRIMARY
                    }
                    val sortedItems = group.items.sortedWith { i1, i2 ->
                        collator.compare(i1.part?.name ?: "", i2.part?.name ?: "")
                    }

                    val headers = mutableListOf<String>()
                    headers.add("م")
                    if (showPartCol) headers.add("الصنف")
                    if (showCarCol) headers.add("السيارة")
                    if (showOrderedCol) headers.add("المطلوب")
                    if (showReceivedCol) headers.add("الواصل")
                    if (showRemainingCol) headers.add("المتبقي")
                    if (showStatusCol) headers.add("الحالة")

                    val rows = sortedItems.mapIndexed { index, item ->
                        val s = item.supplierOrder
                        val p = item.part
                        val r = mutableListOf<String>()
                        r.add((index + 1).toString())
                        if (showPartCol) r.add(p?.name ?: "صنف #${s.partId}")
                        if (showCarCol) r.add(p?.carType ?: "")
                        if (showOrderedCol) r.add(s.qtyOrdered.toString())
                        if (showReceivedCol) r.add(s.qtyReceived.toString())
                        if (showRemainingCol) r.add(s.qtyRemaining.toString())
                        if (showStatusCol) {
                            r.add(
                                when (s.status) {
                                    "completed" -> "مكتمل"
                                    "partial" -> "مستلم جزئياً"
                                    "cancelled" -> "ملغي"
                                    else -> "مؤكد"
                                }
                            )
                        }
                        r
                    }

                    val summaries = mutableListOf<String>()
                    if (sumDates) summaries.add("تاريخ الطلب: ${group.orderDate} | تاريخ التأكيد: ${group.confirmedDate}")
                    if (sumItemCount) summaries.add("إجمالي الأصناف: ${group.items.size} صنف")
                    if (sumTotalOrdered) summaries.add("إجمالي المطلوب: ${group.totalOrdered} قطعة")
                    if (sumTotalReceived) summaries.add("إجمالي الواصل: ${group.totalReceived} قطعة")
                    if (sumTotalRemaining) summaries.add("إجمالي المتبقي: ${group.totalRemaining} قطعة")
                    if (sumNotes && group.notes.isNotBlank()) summaries.add("ملاحظات: ${group.notes}")

                    val fileBase = "Supplier_Order_${group.orderDate.replace('-', '_')}"
                    if (isPdf) {
                        ExportUtils.exportToPdf(
                            context = context,
                            fileName = fileBase,
                            title = "سند طلبية توريد الموردين",
                            subtitle = "توثيق رسمي للطلبية مرتبة أبجدياً بالتسلسل التلقائي",
                            headers = headers,
                            rows = rows,
                            summaryLines = summaries,
                            appUserName = appUserName
                        )
                    } else {
                        ExportUtils.exportToExcelCsv(
                            context = context,
                            fileName = fileBase,
                            title = "طلبية توريد بتاريخ ${group.orderDate}",
                            headers = headers,
                            rows = rows,
                            summaryLines = summaries,
                            appUserName = appUserName
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تصدير الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
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
