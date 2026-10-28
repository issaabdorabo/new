package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.model.CustomerWithOrder
import com.example.data.model.MatrixRow
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.components.EditMatrixCellDialog
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
    onCreateSupplierOrder: (partId: Long, qty: Int, date: String, notes: String) -> Unit,
    onReceiveSupplierOrder: (orderId: Long, qtyReceivedNow: Int) -> Unit,
    onReorderRemaining: (orderId: Long) -> Unit,
    onAddNewPartRequested: () -> Unit
) {
    val context = LocalContext.current
    val tabs = listOf("العرض الجماعي", "العرض الفردي", "طلبات الموردين")

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = currentSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryBlue
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
                            fontWeight = if (currentSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
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
                onShareMatrix = { shareMatrixReport(context, matrixRows, customers) },
                onAddPart = onAddNewPartRequested
            )
            1 -> IndividualOrdersView(
                customerOrders = customerOrdersDetail,
                parts = parts,
                selectedCustomerId = selectedCustomerIdForDetail,
                onSelectCustomer = onSelectCustomerForDetail,
                onUpdateQty = onUpdateOrderItemQty,
                onDeleteOrderItem = onDeleteOrderItem,
                onAddPartToOrder = onAddPartToCustomerOrder,
                onAddNewPart = onAddNewPartRequested
            )
            2 -> SupplierOrdersView(
                supplierOrders = supplierOrdersWithPart,
                parts = parts,
                onCreateSupplierOrder = onCreateSupplierOrder,
                onReceiveOrder = onReceiveSupplierOrder,
                onReorderRemaining = onReorderRemaining
            )
        }
    }
}

// ----------------------------------------------------
// Tab A: "العرض الجماعي" (Matrix View with frozen first column)
// ----------------------------------------------------
@Composable
fun MatrixGroupView(
    matrixRows: List<MatrixRow>,
    customers: List<CustomerEntity>,
    onCellClick: (customerId: Long, partId: Long, newQty: Int) -> Unit,
    onShareMatrix: () -> Unit,
    onAddPart: () -> Unit
) {
    var cellToEdit by remember { mutableStateOf<Triple<PartEntity, CustomerEntity, Int>?>(null) }

    val customerGradients = listOf(
        listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
        listOf(Color(0xFF047857), Color(0xFF10B981)),
        listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)),
        listOf(Color(0xFFBE123C), Color(0xFFF43F5E))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                    text = "انقر على أي خلية لتعديل الكمية مباشرة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onShareMatrix,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصدير", fontSize = 12.sp)
                }
                Button(
                    onClick = onAddPart,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صنف", fontSize = 12.sp)
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
                        .width(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    // Header for frozen column
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(PrimaryBlue)
                            .padding(8.dp),
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

                    // Rows for frozen column
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(matrixRows) { row ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        text = row.part.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${row.part.carType} - ${row.part.carModel}",
                                        fontSize = 9.sp,
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
}

// ----------------------------------------------------
// Tab B: "العرض الفردي" (Individual Orders)
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
    onAddNewPart: () -> Unit
) {
    val context = LocalContext.current
    var showPartSelectorDialog by remember { mutableStateOf(false) }

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
        val items = selectedCustomerWithOrder.items
        val totalPieces = items.sumOf { it.item.quantity }
        val totalAmount = items.sumOf { it.item.quantity * it.part.price }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectCustomer(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "طلب العميل: ${customer.name}",
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
                Button(
                    onClick = { showPartSelectorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة صنف", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("لا توجد أصناف في طلب هذا العميل حالياً")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showPartSelectorDialog = true }) {
                            Text("➕ أضف أول صنف للطلب")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { itemWithPart ->
                        val item = itemWithPart.item
                        val part = itemWithPart.part
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = part.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "${part.carType} - ${part.carModel}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "السعر: ${part.price} ر.س | الإجمالي: ${part.price * item.quantity} ر.س",
                                        fontSize = 11.sp,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        fontSize = 16.sp
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

            // Bottom Summary Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        Text(
                            text = "إجمالي القطع: $totalPieces قطعة",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "القيمة الإجمالية: $totalAmount ر.س",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = {
                            shareCustomerOrderReport(context, customer, items)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة الطلب")
                    }
                }
            }
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
                Text(
                    text = "👥 بطاقات طلبات العملاء الفردية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختر عميلاً لعرض الأصناف وإدارتها بشكل تفصيلي",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(customerOrders) { custWithOrder ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCustomer(custWithOrder.customer.id) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(PrimaryLighter, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = custWithOrder.customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = custWithOrder.customer.notes.ifBlank { "عميل معتمد" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { onSelectCustomer(custWithOrder.customer.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("فتح الطلب", fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الأصناف المطلوبة: ${custWithOrder.items.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "إجمالي القطع: ${custWithOrder.totalQty}",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // Part Selector Dialog inside Order
    if (showPartSelectorDialog && selectedCustomerId != null) {
        SelectPartDialog(
            parts = parts,
            onDismiss = { showPartSelectorDialog = false },
            onSelectPart = { part, qty ->
                onAddPartToOrder(selectedCustomerId, part.id, qty)
                showPartSelectorDialog = false
            },
            onAddNewPartRequested = {
                showPartSelectorDialog = false
                onAddNewPart()
            }
        )
    }
}

// ----------------------------------------------------
// Tab C: "طلبات الموردين" (Supplier Orders)
// ----------------------------------------------------
@Composable
fun SupplierOrdersView(
    supplierOrders: List<SupplierOrderWithPart>,
    parts: List<PartEntity>,
    onCreateSupplierOrder: (partId: Long, qty: Int, date: String, notes: String) -> Unit,
    onReceiveOrder: (orderId: Long, qtyReceivedNow: Int) -> Unit,
    onReorderRemaining: (orderId: Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") } // all, pending, partial, completed
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var orderToReceive by remember { mutableStateOf<SupplierOrderWithPart?>(null) }

    val filteredOrders = supplierOrders.filter { item ->
        when (selectedFilter) {
            "pending" -> item.supplierOrder.status == "pending"
            "partial" -> item.supplierOrder.status == "partial"
            "completed" -> item.supplierOrder.status == "completed"
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("all", "الكل (${supplierOrders.size})"),
                    Pair("pending", "معلق"),
                    Pair("partial", "جزئي"),
                    Pair("completed", "مكتمل")
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
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
                        Text("لا توجد طلبات موردين في هذا القسم")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders) { item ->
                        val order = item.supplierOrder
                        val part = item.part

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = part?.name ?: "صنف #${order.partId}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (part != null) {
                                            Text(
                                                text = "${part.carType} - ${part.carModel}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Status Badge
                                    val (badgeBg, badgeText, badgeColor) = when (order.status) {
                                        "completed" -> Triple(EmeraldLight, "مكتمل", EmeraldSuccess)
                                        "partial" -> Triple(AmberLight, "واصل جزئي", AmberSecondary)
                                        else -> Triple(RoseLight, "معلق", RoseDanger)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("المطلوب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${order.qtyOrdered}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الواصل", fontSize = 11.sp, color = EmeraldSuccess)
                                        Text("${order.qtyReceived}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("المتبقي", fontSize = 11.sp, color = RoseDanger)
                                        Text("${order.qtyRemaining}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoseDanger)
                                    }
                                }

                                if (order.notes.isNotBlank()) {
                                    Text(
                                        text = "ملاحظات: ${order.notes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (order.status != "completed") {
                                        Button(
                                            onClick = { orderToReceive = item },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("تسجيل وصول", fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                    if (order.qtyRemaining > 0 && order.status == "partial") {
                                        OutlinedButton(
                                            onClick = { onReorderRemaining(order.id) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("رفع طلب مجدد", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showNewOrderDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_new_supplier_order"),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "طلب توريد جديد")
        }
    }

    if (showNewOrderDialog) {
        NewSupplierOrderDialog(
            parts = parts,
            onDismiss = { showNewOrderDialog = false },
            onConfirm = { partId, qty, date, notes ->
                onCreateSupplierOrder(partId, qty, date, notes)
                showNewOrderDialog = false
            }
        )
    }

    orderToReceive?.let { item ->
        ReceiveSupplierOrderDialog(
            order = item.supplierOrder,
            part = item.part,
            onDismiss = { orderToReceive = null },
            onConfirm = { receivedNow ->
                onReceiveOrder(item.supplierOrder.id, receivedNow)
                orderToReceive = null
            }
        )
    }
}

// ----------------------------------------------------
// Smart Part Selector Dialog inside Order
// ----------------------------------------------------
@Composable
fun SelectPartDialog(
    parts: List<PartEntity>,
    onDismiss: () -> Unit,
    onSelectPart: (PartEntity, Int) -> Unit,
    onAddNewPartRequested: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedQty by remember { mutableIntStateOf(1) }

    val filtered = parts.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.carType.contains(searchQuery, ignoreCase = true) ||
                it.carModel.contains(searchQuery, ignoreCase = true)
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("➕ اختيار صنف للطلب", fontWeight = FontWeight.Bold, color = PrimaryBlue)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث عن صنف...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("لا توجد نتائج مطابقة لـ \"$searchQuery\"")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddNewPartRequested,
                                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
                            ) {
                                Text("➕ إضافة هذا الصنف الآن", color = Color.Black)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { part ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPart(part, selectedQty) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(part.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${part.carType} - ${part.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${part.price} ر.س", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

// ----------------------------------------------------
// Share / Export Reports helper functions
// ----------------------------------------------------
fun shareMatrixReport(context: Context, matrixRows: List<MatrixRow>, customers: List<CustomerEntity>) {
    val sb = StringBuilder()
    sb.appendLine("📊 تقرير الطلبات الجماعي - قطع غيار Pro")
    sb.appendLine("==========================================")
    val header = "الصنف | " + customers.joinToString(" | ") { it.name } + " | الإجمالي"
    sb.appendLine(header)
    sb.appendLine("------------------------------------------")

    matrixRows.forEach { row ->
        val line = "${row.part.name} (${row.part.carType}) | " +
                customers.joinToString(" | ") { cust -> "${row.customerQuantities[cust.id] ?: 0}" } +
                " | ${row.totalQty}"
        sb.appendLine(line)
    }
    sb.appendLine("------------------------------------------")
    val totalLine = "الإجمالي | " +
            customers.joinToString(" | ") { cust -> "${matrixRows.sumOf { it.customerQuantities[cust.id] ?: 0 }}" } +
            " | ${matrixRows.sumOf { it.totalQty }}"
    sb.appendLine(totalLine)

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة تقرير الطلبات الجماعي"))
}

fun shareCustomerOrderReport(
    context: Context,
    customer: CustomerEntity,
    items: List<com.example.data.model.OrderItemWithPart>
) {
    val sb = StringBuilder()
    sb.appendLine("📋 تفاصيل طلب العميل: ${customer.name}")
    sb.appendLine("الهاتف: ${customer.phone}")
    sb.appendLine("==========================================")
    items.forEachIndexed { i, itemWithPart ->
        val part = itemWithPart.part
        val qty = itemWithPart.item.quantity
        sb.appendLine("${i + 1}) ${part.name} (${part.carType} - ${part.carModel})")
        sb.appendLine("   الكمية: $qty | السعر: ${part.price} ر.س | المجموع: ${qty * part.price} ر.س")
    }
    sb.appendLine("==========================================")
    val totalQty = items.sumOf { it.item.quantity }
    val totalVal = items.sumOf { it.item.quantity * it.part.price }
    sb.appendLine("إجمالي القطع: $totalQty قطعة")
    sb.appendLine("القيمة الإجمالية: $totalVal ر.س")
    sb.appendLine("تطبيق: قطع غيار Pro")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة تفاصيل طلب ${customer.name}"))
}
