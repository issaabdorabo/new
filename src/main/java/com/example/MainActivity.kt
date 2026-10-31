package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddPartBottomSheet
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.NavigationItems
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StockIssueScreen
import com.example.ui.theme.AutoPartsProTheme
import com.example.ui.viewmodel.AutoPartsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AutoPartsViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            AutoPartsProTheme(themeMode = themeMode) {
                // Ensure RTL layout across the entire application
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onTimeout = { showSplash = false })
                    } else {
                        AutoPartsApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AutoPartsApp(viewModel: AutoPartsViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect base UI states
    val currentNavIndex by viewModel.currentNavIndex.collectAsStateWithLifecycle()
    val ordersTab by viewModel.ordersTab.collectAsStateWithLifecycle()
    val selectedCustomerIdForDetail by viewModel.selectedCustomerIdForDetail.collectAsStateWithLifecycle()

    val inventorySearchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
    val selectedCarTypeFilter by viewModel.selectedCarTypeFilter.collectAsStateWithLifecycle()
    val showOnlyLowStock by viewModel.showOnlyLowStock.collectAsStateWithLifecycle()
    val supplierSearchQuery by viewModel.supplierSearchQuery.collectAsStateWithLifecycle()

    // Base database flows
    val parts by viewModel.parts.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val parties by viewModel.parties.collectAsStateWithLifecycle()
    val partsWithInventory by viewModel.partsWithInventory.collectAsStateWithLifecycle()
    val filteredParts by viewModel.filteredParts.collectAsStateWithLifecycle()
    val matrixRows by viewModel.matrixRows.collectAsStateWithLifecycle()
    val customerOrdersDetail by viewModel.customerOrdersDetail.collectAsStateWithLifecycle()
    val supplierOrdersWithPart by viewModel.supplierOrdersWithPart.collectAsStateWithLifecycle()
    val stockIssuesWithDetails by viewModel.stockIssuesWithDetails.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    // App User Name & Settings states
    val appUserName by viewModel.appUserName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
    val lastCloudSyncTime by viewModel.lastCloudSyncTime.collectAsStateWithLifecycle()

    // Issue pre-selection
    val preselectedPartForIssue by viewModel.preselectedPartForIssue.collectAsStateWithLifecycle()

    // Dialog state for Add Part Sheet
    var showAddPartSheet by remember { mutableStateOf(false) }

    // Listen to ViewModel snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back button for secondary screens
    if (currentNavIndex != 0) {
        BackHandler {
            if (currentNavIndex == 1 && selectedCustomerIdForDetail != null) {
                viewModel.setSelectedCustomerIdForDetail(null)
            } else {
                viewModel.setNavIndex(0)
            }
        }
    }

    val lowStockCount = partsWithInventory.count { it.isLowStock }
    val currentTitle = NavigationItems.getOrNull(currentNavIndex)?.title ?: "قطع غيار Pro"

    Scaffold(
        topBar = {
            AppTopBar(
                title = "قطع غيار Pro - $currentTitle",
                lowStockCount = lowStockCount
            )
        },
        bottomBar = {
            AppBottomNavBar(
                selectedIndex = currentNavIndex,
                lowStockCount = lowStockCount,
                onSelectIndex = { index ->
                    viewModel.setNavIndex(index)
                    if (index != 1) {
                        viewModel.setSelectedCustomerIdForDetail(null)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavIndex) {
                0 -> DashboardScreen(
                    partsWithInventory = partsWithInventory,
                    pendingSupplierOrdersCount = supplierOrdersWithPart.count { it.supplierOrder.status == "pending" || it.supplierOrder.status == "confirmed" },
                    activeCustomersCount = customers.size,
                    activePartiesCount = parties.size,
                    recentTransactions = transactions,
                    onNavigateToOrders = {
                        viewModel.setNavIndex(1)
                        viewModel.setOrdersTab(2) // Jump to supplier orders tab
                    },
                    onNavigateToInventory = { viewModel.setNavIndex(2) },
                    onNavigateToIssue = { viewModel.setNavIndex(3) },
                    onQuickAddPart = { showAddPartSheet = true },
                    onRestockPart = { item ->
                        viewModel.setNavIndex(1)
                        viewModel.setOrdersTab(2)
                    }
                )
                1 -> OrdersScreen(
                    currentSubTab = ordersTab,
                    onTabSelected = { tab -> viewModel.setOrdersTab(tab) },
                    matrixRows = matrixRows,
                    customers = customers,
                    parts = parts,
                    customerOrdersDetail = customerOrdersDetail,
                    supplierOrdersWithPart = supplierOrdersWithPart,
                    selectedCustomerIdForDetail = selectedCustomerIdForDetail,
                    onSelectCustomerForDetail = { id -> viewModel.setSelectedCustomerIdForDetail(id) },
                    onSetMatrixCellQty = { custId, partId, qty -> viewModel.setMatrixCellQuantity(custId, partId, qty) },
                    onAddPartToCustomerOrder = { custId, partId, qty -> viewModel.addPartToCustomerOrder(custId, partId, qty) },
                    onUpdateOrderItemQty = { itemId, qty -> viewModel.updateOrderItemQuantity(itemId, qty) },
                    onDeleteOrderItem = { item -> viewModel.deleteOrderItem(item) },
                    onCancelCustomerOrder = { orderId, returnStock -> viewModel.cancelCustomerOrder(orderId, returnStock) },
                    onDeleteCustomerOrderCard = { orderId, returnStock -> viewModel.deleteCustomerOrderCard(orderId, returnStock) },
                    onUpdateOrderPayment = { orderId, paid, disc -> viewModel.updateOrderPayment(orderId, paid, disc) },
                    onUpdateOrderDetails = { orderId, title, notes, date -> viewModel.updateOrderDetails(orderId, title, notes, date) },
                    onConfirmBulkSupplierOrders = { orderDate, confirmedDate, notes -> viewModel.confirmBulkSupplierOrdersFromMatrix(orderDate, confirmedDate, notes) },
                    onCreateSupplierOrder = { partId, qty, date, notes, orderDate, confirmedDate ->
                        viewModel.createSupplierOrder(partId, qty, date, notes, orderDate, confirmedDate)
                    },
                    onUpdateSupplierOrder = { order -> viewModel.updateSupplierOrder(order) },
                    onCancelSupplierOrder = { orderId -> viewModel.cancelSupplierOrder(orderId) },
                    onDeleteSupplierOrder = { orderId -> viewModel.deleteSupplierOrder(orderId) },
                    onReceiveSupplierOrder = { orderId, qty -> viewModel.receiveSupplierOrder(orderId, qty) },
                    onReorderRemaining = { orderId -> viewModel.reorderRemainingSupplierOrder(orderId) },
                    onClearMatrixQuantities = { viewModel.clearMatrixQuantities() },
                    onLoadSupplierOrderIntoMatrix = { orderId -> viewModel.loadSupplierOrderIntoMatrix(orderId) },
                    onReorderRemainingIntoMatrix = { orderId -> viewModel.reorderRemainingIntoMatrix(orderId) },
                    onEditSupplierOrderReceived = { orderId, qty, date, note -> viewModel.editSupplierOrderReceived(orderId, qty, date, note) },
                    onUpdateSupplierOrderAttachment = { orderId, uri -> viewModel.updateSupplierOrderAttachment(orderId, uri) },
                    onUpdateCustomerOrderAttachment = { orderId, uri -> viewModel.updateCustomerOrderAttachment(orderId, uri) },
                    onCreateNewCustomerOrder = { custId, date, title -> viewModel.createNewCustomerOrder(custId, date, title) },
                    onAddNewPartRequested = { showAddPartSheet = true },
                    onAddCustomer = { name, phone, notes -> viewModel.addCustomer(name, phone, notes) },
                    onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) },
                    supplierSearchQuery = supplierSearchQuery,
                    onSupplierSearchQueryChange = { q -> viewModel.setSupplierSearchQuery(q) },
                    appUserName = appUserName
                )
                2 -> InventoryScreen(
                    partsWithInventory = filteredParts,
                    searchQuery = inventorySearchQuery,
                    onSearchQueryChange = { q -> viewModel.setInventorySearchQuery(q) },
                    selectedCarFilter = selectedCarTypeFilter,
                    onCarFilterSelected = { c -> viewModel.setSelectedCarTypeFilter(c) },
                    showLowStockOnly = showOnlyLowStock,
                    onToggleLowStockOnly = { viewModel.toggleLowStockOnly() },
                    onIssuePart = { partWithInv ->
                        viewModel.navigateToIssueScreen(partWithInv)
                    },
                    onAdjustStock = { partId, newQty, note -> viewModel.adjustInventory(partId, newQty, note) },
                    onUpdatePart = { part -> viewModel.updatePart(part) },
                    onDeletePart = { part -> viewModel.deletePart(part) },
                    onDeleteCarType = { carType -> viewModel.deleteCarType(carType) },
                    onAddNewPart = { showAddPartSheet = true }
                )
                3 -> StockIssueScreen(
                    preselectedPart = preselectedPartForIssue,
                    allPartsWithInventory = partsWithInventory,
                    parties = parties,
                    stockIssuesWithDetails = stockIssuesWithDetails,
                    onClearPreselectedPart = { viewModel.clearPreselectedPartForIssue() },
                    onIssueStock = { partId, qty, partyId, person, date, note, attachment, onSuccess ->
                        viewModel.issueStock(partId, qty, partyId, person, date, note, attachment, onSuccess)
                    },
                    onEditStockIssue = { issueId, newPartId, newQty, newPartyId, newPerson, newDate, newNote, newAttachment, onSuccess ->
                        viewModel.editStockIssue(issueId, newPartId, newQty, newPartyId, newPerson, newDate, newNote, newAttachment, onSuccess)
                    },
                    onDeleteStockIssue = { issueId, onSuccess ->
                        viewModel.deleteStockIssue(issueId, onSuccess)
                    },
                    onAddNewParty = { name, type, phone, notes, onSuccess ->
                        viewModel.addParty(name, type, phone, notes, onSuccess)
                    }
                )
                4 -> ReportsScreen(
                    partsWithInventory = partsWithInventory,
                    customerOrders = customerOrdersDetail,
                    stockIssuesWithDetails = stockIssuesWithDetails,
                    supplierOrdersWithPart = supplierOrdersWithPart,
                    appUserName = appUserName
                )
                5 -> SettingsScreen(
                    customers = customers,
                    parties = parties,
                    parts = parts,
                    appUserName = appUserName,
                    onSetAppUserName = { name -> viewModel.setAppUserName(name) },
                    themeMode = themeMode,
                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                    googleAccountEmail = googleAccountEmail,
                    lastCloudSyncTime = lastCloudSyncTime,
                    onSyncToCloud = { viewModel.syncToCloud() },
                    onRestoreFromCloud = { viewModel.restoreFromCloud() },
                    onExportModular = { type -> viewModel.exportModularData(context, type) },
                    onImportModular = { json, merge -> viewModel.importModularData(json, merge) },
                    onAddCustomer = { name, phone, notes -> viewModel.addCustomer(name, phone, notes) },
                    onUpdateCustomer = { cust -> viewModel.updateCustomer(cust) },
                    onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) },
                    onAddParty = { name, type, phone, notes -> viewModel.addParty(name, type, phone, notes) },
                    onUpdateParty = { party -> viewModel.updateParty(party) },
                    onDeleteParty = { party -> viewModel.deleteParty(party) },
                    onResetDatabase = { viewModel.resetDatabaseToDefaultSeeds() }
                )
            }
        }
    }

    // Modal Add Part Bottom Sheet
    if (showAddPartSheet) {
        AddPartBottomSheet(
            onDismiss = { showAddPartSheet = false },
            onSave = { name, carType, carModel, price, minStock, initialQty ->
                viewModel.addPart(name, carType, carModel, price, minStock, initialQty)
                showAddPartSheet = false
            }
        )
    }
}
