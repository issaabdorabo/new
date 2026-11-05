package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
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
import com.example.ui.components.AttachmentViewerDialog
import com.example.ui.components.NavigationItems
import com.example.ui.components.NotificationsCenterDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StockIssueScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.AdMobBanner
import com.example.ui.util.AdMobManager
import com.example.ui.util.AttachmentUtils
import com.example.ui.viewmodel.AutoPartsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize AdMob SDK and preload App Open Ad
        AdMobManager.initialize(this)

        setContent {
            val viewModel: AutoPartsViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

            MyApplicationTheme (themeMode = themeMode) {
                // Layout direction according to appLanguage (RTL for Arabic, LTR for English)
                val layoutDirection = if (appLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onTimeout = {
                            showSplash = false
                        })
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
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
    val isGoogleAccountVerified by viewModel.isGoogleAccountVerified.collectAsStateWithLifecycle()
    val lastCloudSyncTime by viewModel.lastCloudSyncTime.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val cloudBackups by viewModel.cloudBackups.collectAsStateWithLifecycle()
    val syncFrequencyIndex by viewModel.syncFrequencyIndex.collectAsStateWithLifecycle()
    val syncCustomDurationText by viewModel.syncCustomDurationText.collectAsStateWithLifecycle()

    // Recipient names & Daily movements
    val recipientNames by viewModel.recipientNames.collectAsStateWithLifecycle()
    val dailyMovements by viewModel.dailyMovements.collectAsStateWithLifecycle()

    // Notifications state
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Batch & Multi-device states
    val isEditingBatch by viewModel.isEditingBatch.collectAsStateWithLifecycle()
    val editingBatchId by viewModel.editingBatchId.collectAsStateWithLifecycle()
    val isPrimaryDevice by viewModel.isPrimaryDevice.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedDevices.collectAsStateWithLifecycle()
    val deviceAuditLogs by viewModel.deviceAuditLogs.collectAsStateWithLifecycle()
    val offlineConflictLogs by viewModel.offlineConflictLogs.collectAsStateWithLifecycle()

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

    // Request notification permission for Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { /* granted */ }
        LaunchedEffect(Unit) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val activePreviewPath by AttachmentUtils.activePreviewPath.collectAsStateWithLifecycle()
    val lowStockCount = partsWithInventory.count { it.isLowStock }
    val currentTitle = NavigationItems.getOrNull(currentNavIndex)?.title ?: "قطع غيارpro"

    Scaffold(
        topBar = {
            AppTopBar(
                title = "قطع غيارpro - $currentTitle",
                isOnline = isOnline,
                lowStockCount = lowStockCount,
                unreadNotificationsCount = unreadNotificationsCount,
                onNotificationClick = { showNotificationsDialog = true }
            )
        },
        bottomBar = {
            androidx.compose.foundation.layout.Column {
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                )
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
            }
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
                    onApplyReorderDistribution = { allocations -> viewModel.loadCustomReorderDistributionIntoMatrix(allocations) },
                    onEditSupplierOrderReceived = { orderId, qty, date, note -> viewModel.editSupplierOrderReceived(orderId, qty, date, note) },
                    onUpdateSupplierOrderAttachment = { orderId, uri -> viewModel.updateSupplierOrderAttachment(orderId, uri) },
                    onUpdateCustomerOrderAttachment = { orderId, uri -> viewModel.updateCustomerOrderAttachment(orderId, uri) },
                    onCreateNewCustomerOrder = { custId, date, title -> viewModel.createNewCustomerOrder(custId, date, title) },
                    onAddNewPartRequested = { showAddPartSheet = true },
                    onAddCustomer = { name, phone, notes -> viewModel.addCustomer(name, phone, notes) },
                    onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) },
                    supplierSearchQuery = supplierSearchQuery,
                    onSupplierSearchQueryChange = { q -> viewModel.setSupplierSearchQuery(q) },
                    isEditingBatch = isEditingBatch,
                    editingBatchId = editingBatchId,
                    onSaveMatrixAsCustomerOrders = { orderDate -> viewModel.saveMatrixAsCustomerOrders(orderDate) },
                    onLoadBatchIntoMatrix = { batchId -> viewModel.loadBatchIntoMatrix(batchId) },
                    onAddOrderPayment = { orderId, amount, date, payer, notes, uri -> viewModel.addOrderPayment(orderId, amount, date, payer, notes, uri) },
                    onUpdateOrderPaymentRecord = { payment -> viewModel.updateOrderPaymentRecord(payment) },
                    onDeleteOrderPaymentRecord = { paymentId, orderId -> viewModel.deleteOrderPaymentRecord(paymentId, orderId) },
                    onDeletePaymentAttachmentRecord = { paymentId -> viewModel.deletePaymentAttachmentRecord(paymentId) },
                    onAddSupplierReceiptRecord = { supplierOrderId, qty, date, recName, notes, uri -> viewModel.addSupplierReceiptRecord(supplierOrderId, qty, date, recName, notes, uri) },
                    onUpdateSupplierReceiptRecord = { receipt -> viewModel.updateSupplierReceiptRecord(receipt) },
                    onDeleteSupplierReceiptRecord = { receiptId, supplierOrderId -> viewModel.deleteSupplierReceiptRecord(receiptId, supplierOrderId) },
                    onDeleteReceiptAttachmentRecord = { receiptId -> viewModel.deleteReceiptAttachmentRecord(receiptId) },
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
                    onAddNewPart = { showAddPartSheet = true },
                    onGetPartHistory = { partId -> viewModel.getPartDisbursementHistory(partId) }
                )
                3 -> StockIssueScreen(
                    preselectedPart = preselectedPartForIssue,
                    allPartsWithInventory = partsWithInventory,
                    parties = parties,
                    stockIssuesWithDetails = stockIssuesWithDetails,
                    recipientNames = recipientNames,
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
                    dailyMovements = dailyMovements,
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
                    appLanguage = appLanguage,
                    onAppLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                    googleAccountEmail = googleAccountEmail,
                    onSetGoogleAccountEmail = { email -> viewModel.setGoogleAccountEmail(email) },
                    isGoogleAccountVerified = isGoogleAccountVerified,
                    onSendVerificationCode = { email, onSuccess -> viewModel.sendGoogleAccountVerificationCode(email, context, onSuccess) },
                    onConfirmVerificationCode = { code, onSuccess, onError -> viewModel.confirmGoogleAccountVerificationCode(code, onSuccess, onError) },
                    onAuthenticateWithGoogle = { email -> viewModel.authenticateWithGoogleAccount(email) },
                    onUnverifyGoogleAccount = { viewModel.unverifyGoogleAccount() },
                    lastCloudSyncTime = lastCloudSyncTime,
                    onSyncToCloud = { viewModel.syncToCloud() },
                    onRestoreFromCloud = { viewModel.restoreFromCloud() },
                    cloudBackups = cloudBackups,
                    onRestoreCloudBackup = { backup -> viewModel.restoreCloudBackup(backup) },
                    onDeleteCloudBackup = { backupId -> viewModel.deleteCloudBackup(backupId) },
                    syncFrequencyIndex = syncFrequencyIndex,
                    syncCustomDurationText = syncCustomDurationText,
                    onSyncFrequencyChange = { idx, custom -> viewModel.setSyncFrequency(idx, custom) },
                    onExportModular = { type -> viewModel.exportModularData(context, type) },
                    onExportCustomData = { modules, asExcel -> viewModel.exportCustomData(context, modules, asExcel) },
                    onImportModular = { json, merge -> viewModel.importModularData(json, merge) },
                    onExportEnteredDataToExcel = { type -> viewModel.exportEnteredDataToExcel(context, type) },
                    isPrimaryDevice = isPrimaryDevice,
                    connectedDevices = connectedDevices,
                    deviceAuditLogs = deviceAuditLogs,
                    offlineConflictLogs = offlineConflictLogs,
                    onToggleDevicePermission = { devId, pNum, en -> viewModel.setDevicePermission(devId, pNum, en) },
                    onDisconnectDevice = { devId -> viewModel.disconnectDevice(devId) },
                    onUpdateConnectedDeviceName = { devId, newName -> viewModel.updateConnectedDeviceName(devId, newName) },
                    onPairDeviceFromQr = { qr -> viewModel.pairDeviceFromQrCode(qr) },
                    onResolveConflict = { cId, keepLocal -> viewModel.resolveConflict(cId, keepLocal) },
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

    // Modal Notifications Center Dialog
    if (showNotificationsDialog) {
        NotificationsCenterDialog(
            notifications = notifications,
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
            onClearAll = { viewModel.clearNotifications() },
            onDismiss = { showNotificationsDialog = false }
        )
    }

    // Modal Add Part Bottom Sheet
    if (showAddPartSheet) {
        val existingCarTypes = parts.map { it.carType }.filter { it.isNotBlank() }.distinct()
        val existingCarNames = parts.map { it.carName }.filter { it.isNotBlank() }.distinct()
        AddPartBottomSheet(
            existingCarTypes = existingCarTypes,
            existingCarNames = existingCarNames,
            onDismiss = { showAddPartSheet = false },
            onSave = { name, carType, carName, carModel, purchasePrice, price, minStock, initialQty, barcode ->
                viewModel.addPart(name, carType, carName, carModel, purchasePrice, price, minStock, initialQty, barcode)
                showAddPartSheet = false
            }
        )
    }

    // Universal In-App Document & Image Viewer Dialog
    activePreviewPath?.let { path ->
        AttachmentViewerDialog(
            filePath = path,
            onDismiss = { AttachmentUtils.activePreviewPath.value = null }
        )
    }
}
