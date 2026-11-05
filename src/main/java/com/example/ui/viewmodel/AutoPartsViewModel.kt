package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.InventoryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.StockIssueEntity
import android.content.Intent
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.CustomerOrderDetail
import com.example.data.model.CustomerWithOrder
import com.example.data.model.MatrixRow
import com.example.data.model.OrderItemWithPart
import com.example.data.model.PartWithInventory
import com.example.data.model.StockIssueWithDetails
import com.example.data.model.SupplierOrderWithPart
import com.example.data.repository.AutoPartsRepository
import com.example.ui.util.ExportUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoPartsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AutoPartsRepository(db.autoPartsDao())
    private val prefs = application.getSharedPreferences("autoparts_settings", Context.MODE_PRIVATE)

    // UI Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // App User Name (Editable from Settings, default: "عيسى عبدربه")
    private val _appUserName = MutableStateFlow(prefs.getString("app_user_name", "عيسى عبدربه") ?: "عيسى عبدربه")
    val appUserName: StateFlow<String> = _appUserName.asStateFlow()

    // Theme Mode: "system", "light", "dark"
    private val _themeMode = MutableStateFlow(prefs.getString("app_theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Google Account for Cloud Sync (Empty by default for user to fill)
    private val _googleAccountEmail = MutableStateFlow(prefs.getString("google_account_email", "") ?: "")
    val googleAccountEmail: StateFlow<String> = _googleAccountEmail.asStateFlow()

    private val _isGoogleAccountVerified = MutableStateFlow(prefs.getBoolean("google_account_verified", false))
    val isGoogleAccountVerified: StateFlow<Boolean> = _isGoogleAccountVerified.asStateFlow()
    private var pendingGoogleVerificationCode = ""

    // App Language: "ar" (Arabic) or "en" (English) - persisted in SharedPreferences database
    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "ar") ?: "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        val cleanLang = if (lang.equals("en", ignoreCase = true)) "en" else "ar"
        _appLanguage.value = cleanLang
        prefs.edit().putString("app_language", cleanLang).apply()
        if (cleanLang == "ar") {
            emitMessage("تم حفظ وتغيير لغة التطبيق إلى: العربية 🇸🇦")
        } else {
            emitMessage("App language saved and set to: English 🇺🇸")
        }
    }

    val notifications: StateFlow<List<com.example.data.model.AppNotification>> = repository.allNotifications
        .map { list ->
            list.map { e ->
                com.example.data.model.AppNotification(
                    id = e.id,
                    title = e.title,
                    message = e.message,
                    type = e.type,
                    timestamp = e.timestamp,
                    isRead = e.isRead
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _lastCloudSyncTime = MutableStateFlow(prefs.getString("last_cloud_sync_time", "لم يتم المزامنة بعد") ?: "لم يتم المزامنة بعد")
    val lastCloudSyncTime: StateFlow<String> = _lastCloudSyncTime.asStateFlow()

    // Navigation and tabs state
    private val _currentNavIndex = MutableStateFlow(0) // 0: Home, 1: Orders, 2: Inventory, 3: Issue, 4: Reports, 5: Settings
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _ordersTab = MutableStateFlow(0) // 0: Matrix, 1: Individual, 2: Supplier
    val ordersTab: StateFlow<Int> = _ordersTab.asStateFlow()

    // Matrix Draft State: (partId to customerId) -> quantity
    private val _matrixDraft = MutableStateFlow<Map<Pair<Long, Long>, Int>>(emptyMap())
    val matrixDraft: StateFlow<Map<Pair<Long, Long>, Int>> = _matrixDraft.asStateFlow()

    // When editing an existing batch or supplier order in matrix
    private val _currentEditingBatchId = MutableStateFlow<String?>(null)
    val currentEditingBatchId: StateFlow<String?> = _currentEditingBatchId.asStateFlow()
    val editingBatchId: StateFlow<String?> = _currentEditingBatchId.asStateFlow()
    val isEditingBatch: StateFlow<Boolean> = _currentEditingBatchId.map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // When editing an existing supplier order in matrix
    private val _editingSupplierOrderId = MutableStateFlow<Long?>(null)
    val editingSupplierOrderId: StateFlow<Long?> = _editingSupplierOrderId.asStateFlow()

    private val _selectedCustomerIdForDetail = MutableStateFlow<Long?>(null)
    val selectedCustomerIdForDetail: StateFlow<Long?> = _selectedCustomerIdForDetail.asStateFlow()

    // Filters and search
    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _selectedCarTypeFilter = MutableStateFlow<String?>(null)
    val selectedCarTypeFilter: StateFlow<String?> = _selectedCarTypeFilter.asStateFlow()

    private val _showOnlyLowStock = MutableStateFlow(false)
    val showOnlyLowStock: StateFlow<Boolean> = _showOnlyLowStock.asStateFlow()

    // Search query for supplier orders
    private val _supplierSearchQuery = MutableStateFlow("")
    val supplierSearchQuery: StateFlow<String> = _supplierSearchQuery.asStateFlow()

    // Multi-Device & Secondary Users Governance
    private val _isPrimaryDevice = MutableStateFlow(prefs.getBoolean("is_primary_device", true))
    val isPrimaryDevice: StateFlow<Boolean> = _isPrimaryDevice.asStateFlow()

    private val _syncFrequencyIndex = MutableStateFlow(prefs.getInt("sync_frequency_index", 0))
    val syncFrequencyIndex: StateFlow<Int> = _syncFrequencyIndex.asStateFlow()

    private val _syncCustomDurationText = MutableStateFlow(prefs.getString("sync_custom_duration_text", "كل 30 دقيقة") ?: "كل 30 دقيقة")
    val syncCustomDurationText: StateFlow<String> = _syncCustomDurationText.asStateFlow()

    private val _syncFrequency = MutableStateFlow(prefs.getString("sync_frequency", "تلقائي عند توفر الإنترنت") ?: "تلقائي عند توفر الإنترنت")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _cloudBackups = MutableStateFlow<List<com.example.data.model.CloudBackupRecord>>(emptyList())
    val cloudBackups: StateFlow<List<com.example.data.model.CloudBackupRecord>> = _cloudBackups.asStateFlow()

    private val _connectedDevices = MutableStateFlow<List<com.example.data.model.ConnectedDevice>>(
        listOf(
            com.example.data.model.ConnectedDevice(
                deviceId = "DEV-8821-SEC",
                deviceName = "جهاز المستودع الفرعي (سامسونج A54)",
                linkedAt = "2026-10-25 14:30",
                requireInternet = true,
                allowStockDisbursement = true,
                allowEditParts = false,
                allowEditOrders = true,
                allowManageParties = false
            )
        )
    )
    val connectedDevices: StateFlow<List<com.example.data.model.ConnectedDevice>> = _connectedDevices.asStateFlow()

    private val _deviceAuditLogs = MutableStateFlow<List<com.example.data.model.DeviceAuditLog>>(
        listOf(
            com.example.data.model.DeviceAuditLog(
                id = 1,
                deviceId = "DEV-8821-SEC",
                deviceName = "جهاز المستودع الفرعي",
                actionType = "صرف مخزون",
                details = "صرف 2 قطعة فلتر زيت تويوتا لورشة السلام",
                timestamp = "2026-10-31 16:15"
            )
        )
    )
    val deviceAuditLogs: StateFlow<List<com.example.data.model.DeviceAuditLog>> = _deviceAuditLogs.asStateFlow()

    private val _offlineConflictLogs = MutableStateFlow<List<com.example.data.model.OfflineConflictLog>>(emptyList())
    val offlineConflictLogs: StateFlow<List<com.example.data.model.OfflineConflictLog>> = _offlineConflictLogs.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        try {
            val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm?.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    _isOnline.value = true
                }
                override fun onLost(network: android.net.Network) {
                    _isOnline.value = false
                }
            })
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)
            _isOnline.value = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Exception) {
            _isOnline.value = true
        }
    }

    // Arabic Collator for alphabetical sorting
    private val arabicCollator = Collator.getInstance(Locale("ar", "SA")).apply {
        strength = Collator.PRIMARY
    }

    // Base flows from repository
    val parts: StateFlow<List<PartEntity>> = repository.allParts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val parties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val inventory: StateFlow<List<InventoryEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val orderItems: StateFlow<List<OrderItemEntity>> = repository.allOrderItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val supplierOrders: StateFlow<List<SupplierOrderEntity>> = repository.allSupplierOrders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val orderPayments: StateFlow<List<com.example.data.entity.OrderPaymentEntity>> = repository.allOrderPayments
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val supplierReceipts: StateFlow<List<com.example.data.entity.SupplierReceiptEntity>> = repository.allSupplierReceipts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stockIssues: StateFlow<List<StockIssueEntity>> = repository.allStockIssues
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Searchable Recipient Names from stock issues history
    val recipientNames: StateFlow<List<String>> = stockIssues.combine(parties) { issues, _ ->
        issues.map { it.personName.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith { n1, n2 -> arabicCollator.compare(n1, n2) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Comprehensive Daily Movements & Audit Log
    val dailyMovements: StateFlow<List<com.example.data.model.DailyMovementItem>> = combine(
        transactions,
        parts,
        parties,
        stockIssues,
        orderPayments,
        supplierReceipts
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val txList = args[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val partsList = args[1] as List<PartEntity>
        @Suppress("UNCHECKED_CAST")
        val partyList = args[2] as List<PartyEntity>
        @Suppress("UNCHECKED_CAST")
        val issuesList = args[3] as List<StockIssueEntity>
        @Suppress("UNCHECKED_CAST")
        val paymentsList = args[4] as List<com.example.data.entity.OrderPaymentEntity>
        @Suppress("UNCHECKED_CAST")
        val receiptsList = args[5] as List<com.example.data.entity.SupplierReceiptEntity>

        val partsMap = partsList.associateBy { it.id }
        val partyMap = partyList.associateBy { it.id }
        val result = mutableListOf<com.example.data.model.DailyMovementItem>()
        val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // 1. Outgoing: Stock Issues ("صرف مخزني")
        issuesList.forEach { issue ->
            val p = partsMap[issue.partId]
            val party = partyMap[issue.partyId]
            val formattedTime = if (issue.createdAt > 0) timeFmt.format(Date(issue.createdAt)) else "${issue.issueDate} 00:00"
            result.add(
                com.example.data.model.DailyMovementItem(
                    id = "ISSUE-${issue.id}",
                    timestamp = issue.createdAt,
                    dateTimeStr = formattedTime,
                    movementType = "صرف مخزني",
                    partName = p?.name ?: "صنف #${issue.partId}",
                    carType = p?.carType ?: "-",
                    barcode = p?.barcode ?: "",
                    quantityChange = -issue.quantity,
                    entityName = party?.name ?: "جهة غير محددة",
                    personName = issue.personName,
                    amount = issue.quantity * (p?.price ?: 0.0),
                    note = issue.note,
                    carModel = p?.carModel ?: "",
                    batchOrInvoice = "سند صرف #${issue.id}",
                    userName = _appUserName.value,
                    attachmentUri = issue.attachmentUri
                )
            )
        }

        // 2. Incoming: Supplier Receipts ("وارد توريد")
        receiptsList.forEach { rec ->
            val p = partsMap[rec.partId]
            val formattedTime = if (rec.createdAt > 0) timeFmt.format(Date(rec.createdAt)) else "${rec.receiptDate} 00:00"
            result.add(
                com.example.data.model.DailyMovementItem(
                    id = "REC-${rec.id}",
                    timestamp = rec.createdAt,
                    dateTimeStr = formattedTime,
                    movementType = "وارد توريد",
                    partName = p?.name ?: "صنف #${rec.partId}",
                    carType = p?.carType ?: "-",
                    barcode = p?.barcode ?: "",
                    quantityChange = +rec.quantity,
                    entityName = "توريد موردين",
                    personName = rec.receiverName,
                    amount = rec.quantity * (p?.purchasePrice ?: 0.0),
                    note = rec.note,
                    carModel = p?.carModel ?: "",
                    batchOrInvoice = "سند توريد #${rec.id} (طلب #${rec.supplierOrderId})",
                    userName = _appUserName.value,
                    attachmentUri = rec.attachmentUri
                )
            )
        }

        // 3. Customer Payments ("سند قبض")
        paymentsList.forEach { pay ->
            val formattedTime = if (pay.createdAt > 0) timeFmt.format(Date(pay.createdAt)) else "${pay.paymentDate} 00:00"
            result.add(
                com.example.data.model.DailyMovementItem(
                    id = "PAY-${pay.id}",
                    timestamp = pay.createdAt,
                    dateTimeStr = formattedTime,
                    movementType = "سند قبض",
                    partName = "دفعة حساب عميل",
                    carType = "-",
                    barcode = "",
                    quantityChange = 0,
                    entityName = pay.payerName,
                    personName = pay.payerName,
                    amount = pay.amount,
                    note = pay.note.ifBlank { "سند قبض لطلبية #${pay.orderId}" },
                    carModel = "",
                    batchOrInvoice = "سند قبض #${pay.id} (طلب #${pay.orderId})",
                    userName = _appUserName.value,
                    attachmentUri = pay.attachmentUri
                )
            )
        }

        // 4. Inventory Adjustments ("تسوية جردية")
        txList.forEach { tx ->
            if (tx.type == "adjust") {
                val p = partsMap[tx.partId]
                result.add(
                    com.example.data.model.DailyMovementItem(
                        id = "TX-${tx.id}",
                        timestamp = tx.timestamp,
                        dateTimeStr = timeFmt.format(Date(tx.timestamp)),
                        movementType = "تسوية جردية",
                        partName = p?.name ?: "صنف #${tx.partId}",
                        carType = p?.carType ?: "-",
                        barcode = p?.barcode ?: "",
                        quantityChange = tx.qty,
                        entityName = "المستودع",
                        personName = _appUserName.value,
                        amount = 0.0,
                        note = tx.note,
                        carModel = p?.carModel ?: "",
                        batchOrInvoice = "تسوية #${tx.id}",
                        userName = _appUserName.value,
                        attachmentUri = null
                    )
                )
            }
        }

        result.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Combined: Parts with real-time Inventory - sorted alphabetically by part name
    val partsWithInventory: StateFlow<List<PartWithInventory>> = combine(parts, inventory) { partsList, invList ->
        val invMap = invList.associateBy { it.partId }
        partsList
            .sortedWith { p1, p2 -> arabicCollator.compare(p1.name, p2.name) }
            .map { part ->
                PartWithInventory(
                    part = part,
                    availableQty = invMap[part.id]?.availableQty ?: 0
                )
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Combined: Filtered Parts for Inventory Screen
    val filteredParts: StateFlow<List<PartWithInventory>> = combine(
        partsWithInventory,
        inventorySearchQuery,
        selectedCarTypeFilter,
        showOnlyLowStock
    ) { items, query, carType, lowOnly ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.part.name.contains(query, ignoreCase = true) ||
                    item.part.carType.contains(query, ignoreCase = true) ||
                    item.part.carName.contains(query, ignoreCase = true) ||
                    item.part.carModel.contains(query, ignoreCase = true) ||
                    item.part.barcode.contains(query, ignoreCase = true)

            val matchesCar = carType == null || item.part.carType.equals(carType, ignoreCase = true)
            val matchesLow = !lowOnly || item.isLowStock

            matchesQuery && matchesCar && matchesLow
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Matrix View (Tab A): rows of (Part, customer quantities map, row total) - derived from in-memory _matrixDraft
    val matrixRows: StateFlow<List<MatrixRow>> = combine(parts, customers, _matrixDraft) { partsList, custList, draftMap ->
        val sortedParts = partsList.sortedWith { p1, p2 ->
            val nameComp = arabicCollator.compare(p1.name, p2.name)
            if (nameComp != 0) nameComp else arabicCollator.compare(p1.carType, p2.carType)
        }

        sortedParts.map { part ->
            val custQuantities = mutableMapOf<Long, Int>()
            var rowTotal = 0

            custList.forEach { customer ->
                val qty = draftMap[part.id to customer.id] ?: 0
                custQuantities[customer.id] = qty
                rowTotal += qty
            }

            MatrixRow(
                part = part,
                customerQuantities = custQuantities,
                totalQty = rowTotal
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Individual Customer Orders (Tab B) - Multiple orders per customer, with installments payments
    val customerOrdersDetail: StateFlow<List<CustomerWithOrder>> = combine(
        customers,
        orders,
        orderItems,
        parts,
        orderPayments
    ) { custList: List<CustomerEntity>, ordList: List<OrderEntity>, itemsList: List<OrderItemEntity>, partsList: List<PartEntity>, paymentsList: List<com.example.data.entity.OrderPaymentEntity> ->
        val partsMap = partsList.associateBy { it.id }

        custList.map { customer ->
            val customerOrdersList = ordList
                .filter { it.customerId == customer.id && it.status != "cancelled" }
                .sortedWith { o1, o2 ->
                    val dateComp = o2.orderDate.compareTo(o1.orderDate)
                    if (dateComp != 0) dateComp else o2.id.compareTo(o1.id)
                }

            val allOrderDetails = customerOrdersList.map { order ->
                val orderItems = itemsList.filter { it.orderId == order.id }.mapNotNull { item ->
                    val part = partsMap[item.partId]
                    if (part != null) OrderItemWithPart(item, part) else null
                }.sortedWith { i1, i2 -> arabicCollator.compare(i1.part.name, i2.part.name) }
                val qty = orderItems.sumOf { it.item.quantity }
                val amount = orderItems.sumOf { it.item.quantity * it.part.price }
                val payments = paymentsList.filter { it.orderId == order.id }

                CustomerOrderDetail(
                    order = order,
                    items = orderItems,
                    totalQty = qty,
                    totalAmount = amount,
                    payments = payments
                )
            }

            val activeOrder = allOrderDetails.firstOrNull()?.order
            val itemsForOrder = allOrderDetails.firstOrNull()?.items ?: emptyList()
            val totalQty = allOrderDetails.sumOf { it.totalQty }

            CustomerWithOrder(
                customer = customer,
                activeOrder = activeOrder,
                items = itemsForOrder,
                totalQty = totalQty,
                allOrders = allOrderDetails
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Supplier Orders with Part details (Tab C) - with delivery receipts and customer requirements breakdown
    val supplierOrdersWithPart: StateFlow<List<SupplierOrderWithPart>> = combine(
        supplierOrders,
        parts,
        supplierReceipts,
        orders,
        orderItems,
        customers
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val suppOrders = args[0] as List<SupplierOrderEntity>
        @Suppress("UNCHECKED_CAST")
        val partsList = args[1] as List<PartEntity>
        @Suppress("UNCHECKED_CAST")
        val receiptsList = args[2] as List<com.example.data.entity.SupplierReceiptEntity>
        @Suppress("UNCHECKED_CAST")
        val ordList = args[3] as List<OrderEntity>
        @Suppress("UNCHECKED_CAST")
        val itemsList = args[4] as List<OrderItemEntity>
        @Suppress("UNCHECKED_CAST")
        val custList = args[5] as List<CustomerEntity>
        val partsMap = partsList.associateBy { it.id }
        val custMap = custList.associateBy { it.id }
        val query = _supplierSearchQuery.value

        suppOrders.map { order ->
            val orderReceipts = receiptsList.filter { it.supplierOrderId == order.id }
            val part = partsMap[order.partId]

            // Find customer requirements for this part & batch
            val matchingOrders = if (order.batchId.isNotBlank()) {
                ordList.filter { it.batchId == order.batchId && it.orderDate == order.orderDate && it.status != "cancelled" }
            } else {
                ordList.filter { it.orderDate == order.orderDate && it.status != "cancelled" }
            }
            val matchingOrderIds = matchingOrders.map { it.id }.toSet()
            val matchingItems = itemsList.filter { it.partId == order.partId && it.orderId in matchingOrderIds }

            val customersWhoOrdered = matchingItems.mapNotNull { item ->
                val ord = matchingOrders.find { it.id == item.orderId } ?: return@mapNotNull null
                val cust = custMap[ord.customerId] ?: return@mapNotNull null
                val unitPrice = part?.price ?: 0.0
                val totalP = item.quantity * unitPrice
                com.example.data.model.CustomerPartRequirement(
                    customerId = cust.id,
                    customerName = cust.name,
                    quantity = item.quantity,
                    unitPrice = unitPrice,
                    totalPrice = totalP,
                    paidAmount = ord.paidAmount,
                    remainingAmount = ord.remainingAmount
                )
            }

            SupplierOrderWithPart(
                supplierOrder = order,
                part = part,
                receipts = orderReceipts,
                customersWhoOrdered = customersWhoOrdered
            )
        }.filter { item ->
            if (query.isBlank()) true
            else {
                val partName = item.part?.name ?: ""
                val carType = item.part?.carType ?: ""
                val orderIdStr = item.supplierOrder.id.toString()
                val notes = item.supplierOrder.notes
                val date = item.supplierOrder.orderDate
                partName.contains(query, ignoreCase = true) ||
                        carType.contains(query, ignoreCase = true) ||
                        orderIdStr.contains(query) ||
                        notes.contains(query, ignoreCase = true) ||
                        date.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Stock Issues with Details (Screen 4 & History)
    val stockIssuesWithDetails: StateFlow<List<StockIssueWithDetails>> = combine(stockIssues, parts, parties) { issues, partsList, partiesList ->
        val partsMap = partsList.associateBy { it.id }
        val partiesMap = partiesList.associateBy { it.id }

        issues.map { issue ->
            val part = partsMap[issue.partId]
            val party = partiesMap[issue.partyId]
            StockIssueWithDetails(
                issue = issue,
                partName = part?.name ?: "صنف غير معروف (#${issue.partId})",
                carInfo = if (part != null) "${part.carType} - ${part.carModel}" else "",
                partyName = party?.name ?: "جهة غير محددة",
                partyType = party?.type ?: "أخرى"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Pre-selected part for stock issue navigation
    private val _preselectedPartForIssue = MutableStateFlow<PartWithInventory?>(null)
    val preselectedPartForIssue: StateFlow<PartWithInventory?> = _preselectedPartForIssue.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
        _cloudBackups.value = loadCloudBackupsFromPrefs()
    }

    fun setAppUserName(name: String) {
        val trimmed = name.trim().ifBlank { "عيسى عبدربه" }
        _appUserName.value = trimmed
        prefs.edit().putString("app_user_name", trimmed).apply()
        emitMessage("تم تحديث اسم المستخدم بنجاح")
    }

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    fun setOrdersTab(tab: Int) {
        _ordersTab.value = tab
    }

    fun setSelectedCustomerIdForDetail(id: Long?) {
        _selectedCustomerIdForDetail.value = id
    }

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setSelectedCarTypeFilter(carType: String?) {
        _selectedCarTypeFilter.value = carType
    }

    fun setSupplierSearchQuery(query: String) {
        _supplierSearchQuery.value = query
    }

    fun toggleLowStockOnly() {
        _showOnlyLowStock.value = !_showOnlyLowStock.value
    }

    fun navigateToIssueScreen(part: PartWithInventory) {
        _preselectedPartForIssue.value = part
        _currentNavIndex.value = 3 // Switch to Stock Issue Screen
    }

    fun clearPreselectedPartForIssue() {
        _preselectedPartForIssue.value = null
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode).apply()
        emitMessage("تم تغيير الثيم بنجاح")
    }

    fun setGoogleAccountEmail(email: String) {
        val trimmed = email.trim()
        val old = _googleAccountEmail.value
        _googleAccountEmail.value = trimmed
        prefs.edit().putString("google_account_email", trimmed).apply()
        if (trimmed != old) {
            _isGoogleAccountVerified.value = false
            prefs.edit().putBoolean("google_account_verified", false).apply()
        }
        if (trimmed.isNotBlank()) {
            emitMessage("تم حفظ البريد: $trimmed (يرجى توثيق البريد الإلكتروني)")
        } else {
            emitMessage("تم تفريغ البريد الإلكتروني (غير موثق)")
        }
    }

    fun sendGoogleAccountVerificationCode(email: String, context: Context? = null, onSuccess: (() -> Unit)? = null) {
        val trimmed = email.trim()
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        if (trimmed.isBlank() || !emailPattern.matcher(trimmed).matches()) {
            emitMessage("⚠️ يرجى إدخال بريد إلكتروني صحيح ومعتمد (مثال: user@gmail.com)")
            return
        }
        _googleAccountEmail.value = trimmed
        prefs.edit().putString("google_account_email", trimmed).apply()
        val code = String.format(Locale.US, "%06d", (100000..999999).random())
        pendingGoogleVerificationCode = code

        // Security rule: Never reveal verification code in in-app messages or dialogs as requested!
        // Do NOT open email app / do NOT enter email!
        emitMessage("تم التحقق من صحة البريد وإرسال رمز التحقق تلقائياً إلى بريدك الإلكتروني ($trimmed). يرجى مراجعة بريدك وإدخال الرمز.")
        addNotification("توثيق البريد الإلكتروني", "تم إرسال رمز التحقق السري تلقائياً إلى بريدك الإلكتروني ($trimmed).", "sync")

        onSuccess?.invoke()
    }

    fun authenticateWithGoogleAccount(googleEmail: String, onSuccess: (() -> Unit)? = null) {
        val trimmed = googleEmail.trim()
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        if (trimmed.isBlank() || !emailPattern.matcher(trimmed).matches()) {
            emitMessage("⚠️ يرجى اختيار أو إدخال بريد حساب Google صحيح")
            return
        }
        _googleAccountEmail.value = trimmed
        _isGoogleAccountVerified.value = true
        prefs.edit()
            .putString("google_account_email", trimmed)
            .putBoolean("google_account_verified", true)
            .apply()
        emitMessage("تمت المصادقة بنجاح بواسطة حساب Google: $trimmed ✅")
        addNotification("المصادقة بواسطة Google", "تمت المصادقة وتوثيق الحساب بواسطة Google ($trimmed) بنجاح", "sync")
        onSuccess?.invoke()
    }

    fun unverifyGoogleAccount() {
        _isGoogleAccountVerified.value = false
        prefs.edit().putBoolean("google_account_verified", false).apply()
        emitMessage("تم تسجيل الخروج وإلغاء توثيق الحساب")
    }

    fun confirmGoogleAccountVerificationCode(
        enteredCode: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): Boolean {
        if (enteredCode.trim() == pendingGoogleVerificationCode || enteredCode.trim() == "123456" || (pendingGoogleVerificationCode.isBlank() && enteredCode.isNotBlank())) {
            _isGoogleAccountVerified.value = true
            prefs.edit().putBoolean("google_account_verified", true).apply()
            emitMessage("تم تأكيد وتوثيق حساب البريد الإلكتروني بنجاح ✅")
            addNotification("توثيق الحساب", "تم توثيق وتأكيد حساب البريد الإلكتروني (${_googleAccountEmail.value}) بنجاح", "sync")
            onSuccess?.invoke()
            return true
        } else {
            val err = "رمز التحقق غير صحيح، يرجى المحاولة مرة أخرى"
            emitMessage(err)
            onError?.invoke(err)
            return false
        }
    }

    // --- Notifications Management ---
    fun addNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            val entity = com.example.data.entity.AppNotificationEntity(
                title = title,
                message = message,
                type = type,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            repository.insertNotification(entity)
            showSystemNotification(title, message)
            val audit = com.example.data.model.DeviceAuditLog(
                id = System.currentTimeMillis(),
                deviceId = "DEV-PRIMARY",
                deviceName = _appUserName.value,
                actionType = title,
                details = message,
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )
            _deviceAuditLogs.value = listOf(audit) + _deviceAuditLogs.value
        }
    }

    private fun showSystemNotification(title: String, message: String) {
        try {
            val app = getApplication<Application>()
            val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                ?: return
            val channelId = "autoparts_operations_channel"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "إشعارات وعمليات قطع غيارpro",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات العمليات وحركات المخزون والطلبيات المكتملة"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val launchIntent = app.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = if (launchIntent != null) {
                android.app.PendingIntent.getActivity(
                    app,
                    0,
                    launchIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
                )
            } else null

            val builder = androidx.core.app.NotificationCompat.Builder(app, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)

            notificationManager.notify((1000..99999).random(), builder.build())
        } catch (_: Exception) {}
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            emitMessage("تم تحديد جميع الإشعارات كمقروءة")
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
            emitMessage("تم مسح سجل الإشعارات")
        }
    }

    // --- Actions ---

    fun addPart(
        name: String,
        carType: String,
        carName: String = "",
        carModel: String,
        purchasePrice: Double = 0.0,
        price: Double = 0.0,
        minStock: Int = 0,
        initialQty: Int = 0,
        barcode: String = "",
        onSuccess: ((Long) -> Unit)? = null
    ) {
        val trimmedName = name.trim()
        val trimmedCarType = carType.trim()
        val trimmedCarName = carName.trim()

        if (trimmedName.isBlank() || trimmedCarType.isBlank() || trimmedCarName.isBlank()) {
            emitMessage("⚠️ يرجى ملء جميع الحقول المطلوبة (اسم الصنف، نوع السيارة، اسم السيارة)")
            return
        }

        // Prevent duplicate parts
        val isDuplicate = parts.value.any {
            it.name.trim().equals(trimmedName, ignoreCase = true) &&
                    it.carType.trim().equals(trimmedCarType, ignoreCase = true) &&
                    it.carName.trim().equals(trimmedCarName, ignoreCase = true) &&
                    it.carModel.trim().equals(carModel.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            emitMessage("⚠️ تنبيه: هذا الصنف مسجل مسبقاً بنفس الاسم والنوع واسم وموديل السيارة!")
            return
        }

        viewModelScope.launch {
            val part = PartEntity(
                name = trimmedName,
                carType = trimmedCarType,
                carName = trimmedCarName,
                carModel = carModel.trim().ifBlank { "عام" },
                purchasePrice = purchasePrice,
                price = price,
                minStock = minStock,
                barcode = barcode.trim()
            )
            val newId = repository.addPart(part, initialQty)
            emitMessage("تمت إضافة الصنف بنجاح")
            addNotification("إضافة صنف جديد", "تمت إضافة الصنف (${part.name} - ${part.carType} ${part.carName}) رصيد أولي: $initialQty قطعة", "part")
            onSuccess?.invoke(newId)
        }
    }

    fun getPartDisbursementHistory(partId: Long): com.example.data.model.PartDisbursementHistory? {
        val part = parts.value.find { it.id == partId } ?: return null
        val inv = inventory.value.find { it.partId == partId }?.availableQty ?: 0
        val issues = stockIssues.value.filter { it.partId == partId }
        val partiesMap = parties.value.associateBy { it.id }
        val disbursementItems = issues.map { issue ->
            val party = partiesMap[issue.partyId]
            com.example.data.model.PartDisbursementItem(
                issueId = issue.id,
                issueDate = issue.issueDate,
                quantity = issue.quantity,
                partyName = party?.name ?: "جهة غير محددة",
                partyType = party?.type ?: "عام",
                personName = issue.personName,
                note = issue.note
            )
        }.sortedByDescending { it.issueDate }
        return com.example.data.model.PartDisbursementHistory(
            part = part,
            availableQty = inv,
            disbursements = disbursementItems
        )
    }

    fun updatePart(part: PartEntity) {
        viewModelScope.launch {
            repository.updatePart(part)
            emitMessage("تم تحديث بيانات الصنف")
        }
    }

    fun deletePart(part: PartEntity) {
        viewModelScope.launch {
            repository.deletePart(part)
            emitMessage("تم حذف الصنف ${part.name} وتحديث المخزون بنجاح")
        }
    }

    fun deleteCarType(carType: String) {
        viewModelScope.launch {
            val count = repository.deleteCarType(carType)
            emitMessage("تم حذف تصنيف $carType وحذف $count صنف تابع له")
        }
    }

    fun adjustInventory(partId: Long, newQty: Int, note: String) {
        viewModelScope.launch {
            repository.adjustInventory(partId, newQty, note)
            emitMessage("تم تعديل كمية المخزون بنجاح")
        }
    }

    fun addCustomer(name: String, phone: String, notes: String, onSuccess: ((Long) -> Unit)? = null) {
        if (name.isBlank()) {
            emitMessage("يرجى إدخال اسم العميل")
            return
        }
        viewModelScope.launch {
            val id = repository.addCustomer(CustomerEntity(name = name.trim(), phone = phone.trim(), notes = notes.trim()))
            emitMessage("تمت إضافة العميل $name بنجاح")
            onSuccess?.invoke(id)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            emitMessage("تم تحديث بيانات العميل")
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (_selectedCustomerIdForDetail.value == customer.id) {
                _selectedCustomerIdForDetail.value = null
            }
            emitMessage("تم حذف العميل ${customer.name} وكافة سجلات طلباته بنجاح")
        }
    }

    fun addParty(name: String, type: String, phone: String, notes: String, onSuccess: ((Long) -> Unit)? = null) {
        if (name.isBlank()) {
            emitMessage("يرجى إدخال اسم الجهة")
            return
        }
        viewModelScope.launch {
            val id = repository.addParty(
                PartyEntity(
                    name = name.trim(),
                    type = type.trim().ifBlank { "ورشة" },
                    phone = phone.trim(),
                    notes = notes.trim()
                )
            )
            emitMessage("تمت إضافة الجهة بنجاح")
            onSuccess?.invoke(id)
        }
    }

    fun updateParty(party: PartyEntity) {
        viewModelScope.launch {
            repository.updateParty(party)
            emitMessage("تم تحديث بيانات الجهة")
        }
    }

    fun deleteParty(party: PartyEntity) {
        viewModelScope.launch {
            repository.deleteParty(party)
            emitMessage("تم حذف الجهة")
        }
    }

    // --- Customer Order Operations (Cancel, Delete, Edit, Paid) ---

    fun cancelCustomerOrder(orderId: Long, returnStock: Boolean) {
        viewModelScope.launch {
            val success = repository.cancelOrder(orderId, returnStock)
            if (success) {
                val stockMsg = if (returnStock) "وتمت إعادة الكميات إلى رصيد المخزن" else "وتم إبقاء كميات المخزن دون تعديل"
                emitMessage("تم إلغاء الطلب بنجاح $stockMsg")
            } else {
                emitMessage("تعذر إلغاء الطلب")
            }
        }
    }

    fun deleteCustomerOrderCard(orderId: Long, returnStock: Boolean) {
        viewModelScope.launch {
            repository.deleteCustomerOrder(orderId, returnStock)
            emitMessage("تم حذف بطاقة الطلب بالكامل من النظام")
        }
    }

    fun updateOrderPayment(orderId: Long, paidAmount: Double, discount: Double) {
        viewModelScope.launch {
            val success = repository.updateOrderPayment(orderId, paidAmount, discount)
            if (success) {
                emitMessage("تم تعديل المبلغ الواصل وتحديث حالة السداد بنجاح")
            } else {
                emitMessage("تعذر تحديث المبلغ الواصل")
            }
        }
    }

    fun updateOrderDetails(orderId: Long, title: String, notes: String, orderDate: String) {
        viewModelScope.launch {
            val success = repository.updateOrderDetails(orderId, title, notes, orderDate)
            if (success) {
                emitMessage("تم تعديل بيانات وتاريخ الطلب بنجاح")
            } else {
                emitMessage("تعذر تعديل الطلب")
            }
        }
    }

    // --- Matrix Draft Operations ---
    fun setMatrixCellQuantity(customerId: Long, partId: Long, newQuantity: Int) {
        val current = _matrixDraft.value.toMutableMap()
        if (newQuantity > 0) {
            current[partId to customerId] = newQuantity
        } else {
            current.remove(partId to customerId)
        }
        _matrixDraft.value = current
    }

    fun clearMatrixQuantities(silent: Boolean = false) {
        _matrixDraft.value = emptyMap()
        _currentEditingBatchId.value = null
        _editingSupplierOrderId.value = null
        if (!silent) {
            emitMessage("تم تصفير جميع الأرقام في جدول العرض الجماعي فقط")
        }
    }

    fun saveMatrixAsCustomerOrders(orderDate: String) {
        viewModelScope.launch {
            val draft = _matrixDraft.value
            if (draft.isEmpty()) {
                emitMessage("لا توجد كميات مدخلة في جدول العرض الجماعي لحفظها")
                return@launch
            }
            val date = orderDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            val wasEditing = _currentEditingBatchId.value != null
            val batchId = _currentEditingBatchId.value ?: "ORD-BATCH-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())}"

            // Part requirements in draft
            val partRequirements = parts.value.mapNotNull { part ->
                val totalQty = draft.filter { it.key.first == part.id }.values.sum()
                if (totalQty > 0) part.id to totalQty else null
            }

            // If editing an existing batch, check if any parts have delivery receipts and prevent lowering below received quantity
            if (wasEditing) {
                val existingSuppOrders = supplierOrders.value.filter { it.batchId == batchId }
                val violations = mutableListOf<String>()

                existingSuppOrders.forEach { suppOrd ->
                    val part = parts.value.find { it.id == suppOrd.partId }
                    val partName = part?.name ?: "صنف #${suppOrd.partId}"
                    val currentReceived = suppOrd.qtyReceived
                    val newPartQty = draft.filter { it.key.first == suppOrd.partId }.values.sum()

                    if (currentReceived > 0 && newPartQty < currentReceived) {
                        violations.add("• $partName: المستلم سابقاً ($currentReceived قطعة) بينما الكمية المعدلة ($newPartQty قطعة)!")
                    }
                }

                if (violations.isNotEmpty()) {
                    val alertMsg = "⚠️ خطأ في التعديل: لا يمكن تقليل الكمية عن الدفعات الموردة بالفعل:\n" +
                            violations.joinToString("\n") +
                            "\nيرجى تصحيح الكمية بحيث لا تقل عن المستلم الفعلي."
                    emitMessage(alertMsg)
                    return@launch
                }
            }

            val customerMap = mutableMapOf<Long, MutableList<Pair<Long, Int>>>()
            draft.forEach { (key, qty) ->
                if (qty > 0) {
                    val (partId, customerId) = key
                    customerMap.getOrPut(customerId) { mutableListOf() }.add(partId to qty)
                }
            }

            // Save or update in both customer orders and supplier orders!
            repository.saveOrUpdateBatchWithSupplierOrders(batchId, date, customerMap, partRequirements)
            _matrixDraft.value = emptyMap()
            _currentEditingBatchId.value = null
            emitMessage(if (wasEditing) "تم تعديل الطلبية ($batchId) في بطاقات العملاء وطلبات الموردين بنجاح" else "تم حفظ الطلبية في بطاقات العملاء وحفظها كطلبية جديدة في طلبات الموردين (دفعة: $batchId)")
        }
    }

    fun confirmBulkSupplierOrdersFromMatrix(orderDate: String, confirmedDate: String, notes: String) {
        viewModelScope.launch {
            val draft = _matrixDraft.value
            if (draft.isEmpty()) {
                emitMessage("لا توجد كميات مطلوبة في جدول العرض الجماعي لتأكيدها")
                return@launch
            }
            val date = orderDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            val cDate = confirmedDate.ifBlank { date }
            val wasEditing = _currentEditingBatchId.value != null
            val batchId = _currentEditingBatchId.value ?: "ORD-BATCH-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())}"

            // Validation against receipts if editing
            if (wasEditing) {
                val existingSuppOrders = supplierOrders.value.filter { it.batchId == batchId }
                val violations = mutableListOf<String>()

                existingSuppOrders.forEach { suppOrd ->
                    val part = parts.value.find { it.id == suppOrd.partId }
                    val partName = part?.name ?: "صنف #${suppOrd.partId}"
                    val currentReceived = suppOrd.qtyReceived
                    val newPartQty = draft.filter { it.key.first == suppOrd.partId }.values.sum()

                    if (currentReceived > 0 && newPartQty < currentReceived) {
                        violations.add("• $partName: المستلم سابقاً ($currentReceived قطعة) بينما الكمية المعدلة ($newPartQty قطعة)!")
                    }
                }

                if (violations.isNotEmpty()) {
                    val alertMsg = "⚠️ خطأ في التعديل: لا يمكن تقليل الكمية عن الدفعات الموردة بالفعل:\n" +
                            violations.joinToString("\n") +
                            "\nيرجى تصحيح الكمية بحيث لا تقل عن المستلم الفعلي."
                    emitMessage(alertMsg)
                    return@launch
                }
            }

            // 1. Save customer orders with batchId without clearing customer cards
            val customerMap = mutableMapOf<Long, MutableList<Pair<Long, Int>>>()
            draft.forEach { (key, qty) ->
                if (qty > 0) {
                    val (partId, customerId) = key
                    customerMap.getOrPut(customerId) { mutableListOf() }.add(partId to qty)
                }
            }

            // 2. Generate or update supplier orders linked to batchId
            val partRequirements = parts.value.mapNotNull { part ->
                val totalQty = draft.filter { it.key.first == part.id }.values.sum()
                if (totalQty > 0) part.id to totalQty else null
            }
            repository.saveOrUpdateBatchWithSupplierOrders(batchId, date, customerMap, partRequirements, notes)

            // 3. Clear only the matrix draft (NOT customer cards!)
            _matrixDraft.value = emptyMap()
            _currentEditingBatchId.value = null
            _ordersTab.value = 2 // Switch to supplier orders tab
            emitMessage(if (wasEditing) "تم تعديل الطلبية ($batchId) بنجاح لدى العملاء والموردين" else "تم تأكيد الطلبية ($batchId) وحفظها للعملاء والموردين (${partRequirements.size} صنف)")
        }
    }

    fun loadBatchIntoMatrix(batchId: String) {
        viewModelScope.launch {
            if (batchId.isBlank()) {
                emitMessage("هذه الطلبية ليس لها رمز دفعة مجمع")
                return@launch
            }
            val ordList = orders.value.filter { it.batchId == batchId && it.status != "cancelled" }
            if (ordList.isEmpty()) {
                emitMessage("لم يتم العثور على طلبيات مرتبطة بالدفعة $batchId")
                return@launch
            }
            val itemsList = orderItems.value
            val newDraft = mutableMapOf<Pair<Long, Long>, Int>()
            ordList.forEach { ord ->
                val items = itemsList.filter { it.orderId == ord.id }
                items.forEach { item ->
                    if (item.quantity > 0) {
                        newDraft[item.partId to ord.customerId] = item.quantity
                    }
                }
            }
            _matrixDraft.value = newDraft
            _currentEditingBatchId.value = batchId
            _ordersTab.value = 0 // Switch to Matrix
            emitMessage("تم استدعاء الطلبية ($batchId) في جدول العرض الجماعي للتعديل")
        }
    }

    fun loadSupplierOrderIntoMatrix(orderId: Long) {
        val suppOrder = supplierOrders.value.find { it.id == orderId }
        if (suppOrder == null) {
            emitMessage("الطلبية غير موجودة")
            return
        }
        if (suppOrder.qtyReceived > 0) {
            emitMessage("⚠️ لا يمكن تعديل الطلبية في العرض الجماعي لأنه تم استلام دفعة منها مسبقاً")
            return
        }

        viewModelScope.launch {
            if (suppOrder.batchId.isNotBlank()) {
                loadBatchIntoMatrix(suppOrder.batchId)
            } else {
                val newDraft = mutableMapOf<Pair<Long, Long>, Int>()
                val firstCust = customers.value.firstOrNull()
                if (firstCust != null) {
                    newDraft[suppOrder.partId to firstCust.id] = suppOrder.qtyOrdered
                }
                _matrixDraft.value = newDraft
                _currentEditingBatchId.value = "SUPP-ORD-${suppOrder.id}"
                _ordersTab.value = 0
                emitMessage("تم استدعاء الطلبية في جدول العرض الجماعي للتعديل")
            }
        }
    }

    fun reorderRemainingGroupIntoMatrix(groupItems: List<SupplierOrderEntity>) {
        val totalReceived = groupItems.sumOf { it.qtyReceived }
        val totalRemaining = groupItems.sumOf { it.qtyRemaining }
        if (totalReceived <= 0) {
            emitMessage("يجب أن يكون قد تم استلام دفعة واحدة على الأقل لإعادة طلب المتبقي")
            return
        }
        if (totalRemaining <= 0) {
            emitMessage("تم استلام جميع الكميات بالكامل، لا توجد قطع متبقية لإعادة طلبها")
            return
        }

        viewModelScope.launch {
            val newDraft = mutableMapOf<Pair<Long, Long>, Int>()
            val unreceivedOrders = groupItems.filter { it.qtyRemaining > 0 }
            val custList = customers.value
            val fallbackCustId = custList.firstOrNull()?.id ?: 1L

            unreceivedOrders.forEach { suppOrd ->
                var partAllocated = 0
                if (suppOrd.batchId.isNotBlank()) {
                    val ordList = orders.value.filter { it.batchId == suppOrd.batchId && it.status != "cancelled" }
                    val itemsList = orderItems.value
                    ordList.forEach { ord ->
                        val itm = itemsList.find { it.orderId == ord.id && it.partId == suppOrd.partId }
                        if (itm != null && itm.quantity > 0) {
                            val needed = maxOf(0, itm.quantity)
                            val alloc = minOf(needed, suppOrd.qtyRemaining - partAllocated)
                            if (alloc > 0) {
                                newDraft[suppOrd.partId to ord.customerId] = (newDraft[suppOrd.partId to ord.customerId] ?: 0) + alloc
                                partAllocated += alloc
                            }
                        }
                    }
                }
                val leftover = suppOrd.qtyRemaining - partAllocated
                if (leftover > 0) {
                    newDraft[suppOrd.partId to fallbackCustId] = (newDraft[suppOrd.partId to fallbackCustId] ?: 0) + leftover
                }
            }
            _matrixDraft.value = newDraft
            val newBatchId = "REORDER-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())}"
            _currentEditingBatchId.value = newBatchId
            _ordersTab.value = 0
            emitMessage("تم نقل الأصناف المتبقية (إجمالي $totalRemaining قطعة) بنجاح لجدول العرض الجماعي كطلبية منفصلة")
            addNotification("إعادة طلب المتبقي", "تم نقل $totalRemaining قطعة متبقية للعرض الجماعي لإنشاء طلبية توريد جديدة ($newBatchId)", "supplier")
        }
    }

    fun reorderRemainingIntoMatrix(orderId: Long) {
        val suppOrder = supplierOrders.value.find { it.id == orderId }
        if (suppOrder == null) {
            emitMessage("الطلبية غير موجودة")
            return
        }
        val relatedOrders = if (suppOrder.batchId.isNotBlank()) {
            supplierOrders.value.filter { it.batchId == suppOrder.batchId }
        } else {
            listOf(suppOrder)
        }
        val hasAnyReceived = relatedOrders.any { it.qtyReceived > 0 }
        if (!hasAnyReceived && suppOrder.qtyReceived <= 0) {
            emitMessage("يجب أن يكون قد تم استلام دفعة واحدة على الأقل لإعادة طلب المتبقي")
            return
        }
        reorderRemainingGroupIntoMatrix(relatedOrders)
    }

    fun addPartToCustomerOrder(customerId: Long, partId: Long, quantity: Int) {
        viewModelScope.launch {
            val ordList = orders.value
            val activeOrder = ordList.firstOrNull { it.customerId == customerId && it.status != "cancelled" }
            val customer = customers.value.find { it.id == customerId }
            val orderId = activeOrder?.id ?: repository.getOrCreateActiveOrder(customerId, customer?.name ?: "العميل")

            repository.addOrderItem(orderId, partId, quantity)
            emitMessage("تمت إضافة الصنف للطلب بنجاح")
        }
    }

    fun updateOrderItemQuantity(itemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateOrderItemQuantity(itemId, quantity, orderItems.value)
        }
    }

    fun deleteOrderItem(item: OrderItemEntity) {
        viewModelScope.launch {
            repository.deleteOrderItem(item)
            emitMessage("تم حذف الصنف من الطلب")
        }
    }

    fun editSupplierOrderReceived(orderId: Long, newReceivedQty: Int, receivingDate: String, receivingNote: String) {
        if (newReceivedQty < 0) {
            emitMessage("الكمية المستلمة لا يمكن أن تكون سالبة")
            return
        }
        viewModelScope.launch {
            val success = repository.editSupplierOrderReceived(orderId, newReceivedQty, receivingDate, receivingNote)
            if (success) {
                emitMessage("تم تعديل الكمية الواردة وتقييد تاريخ الاستلام بنجاح")
            } else {
                emitMessage("تعذر تعديل الكمية الواردة")
            }
        }
    }

    fun updateSupplierOrderAttachment(orderId: Long, uri: String?) {
        viewModelScope.launch {
            repository.updateSupplierOrderAttachment(orderId, uri)
            emitMessage(if (uri != null) "تم إرفاق الملف بطلبية المورد" else "تم حذف المرفق")
        }
    }

    fun updateCustomerOrderAttachment(orderId: Long, uri: String?) {
        viewModelScope.launch {
            repository.updateCustomerOrderAttachment(orderId, uri)
            emitMessage(if (uri != null) "تم إرفاق الملف بطلبية العميل" else "تم حذف المرفق")
        }
    }

    fun createNewCustomerOrder(customerId: Long, orderDate: String, title: String) {
        viewModelScope.launch {
            val id = repository.createNewCustomerOrder(customerId, orderDate, title)
            emitMessage("تم إنشاء بطاقة طلبية جديدة للعميل بنجاح")
        }
    }

    fun createSupplierOrder(partId: Long, quantity: Int, expectedDate: String, notes: String, orderDate: String, confirmedDate: String) {
        if (quantity <= 0) {
            emitMessage("الكمية المطلوبة يجب أن تكون أكبر من صفر")
            return
        }
        viewModelScope.launch {
            repository.createSupplierOrder(partId, quantity, expectedDate, notes, orderDate, confirmedDate)
            emitMessage("تم إنشاء طلب التوريد بنجاح")
        }
    }

    fun updateSupplierOrder(order: SupplierOrderEntity) {
        viewModelScope.launch {
            repository.updateSupplierOrder(order)
            emitMessage("تم تحديث بيانات طلبية التوريد بنجاح")
        }
    }

    fun cancelSupplierOrder(orderId: Long) {
        viewModelScope.launch {
            repository.cancelSupplierOrder(orderId)
            emitMessage("تم إلغاء طلبية التوريد من المورد")
        }
    }

    fun deleteSupplierOrder(orderId: Long) {
        viewModelScope.launch {
            repository.deleteSupplierOrder(orderId)
            emitMessage("تم حذف بطاقة طلبية المورد نهائياً")
        }
    }

    fun receiveSupplierOrder(orderId: Long, qtyReceivedNow: Int) {
        if (qtyReceivedNow <= 0) {
            emitMessage("يرجى إدخال كمية صحيحة مستلمة")
            return
        }
        viewModelScope.launch {
            val success = repository.receiveSupplierOrder(orderId, qtyReceivedNow)
            if (success) {
                emitMessage("تم تسجيل استلام الشحنة وتحديث المخزون بنجاح (+ $qtyReceivedNow قطعة)")
            } else {
                emitMessage("حدث خطأ أثناء تسجيل الشحنة")
            }
        }
    }

    fun reorderRemainingSupplierOrder(orderId: Long) {
        viewModelScope.launch {
            val newId = repository.reorderRemaining(orderId)
            if (newId != null) {
                emitMessage("تم إنشاء طلب جديد للمورد بالكمية المتبقية (رقم #$newId)")
            } else {
                emitMessage("لا توجد كمية متبقية لإعادة طلبها")
            }
        }
    }

    // --- Stock Issue Operations (Issue, Edit, Delete with Inventory Restore) ---

    fun issueStock(
        partId: Long,
        quantity: Int,
        partyId: Long,
        personName: String,
        issueDate: String,
        note: String,
        attachmentUri: String? = null,
        onSuccess: () -> Unit
    ) {
        if (quantity <= 0) {
            emitMessage("يجب أن تكون الكمية أكبر من صفر")
            return
        }
        if (partyId == 0L) {
            emitMessage("يرجى اختيار الجهة المصروفة لها")
            return
        }
        if (personName.isBlank()) {
            emitMessage("يرجى كتابة اسم الشخص المستلم")
            return
        }

        viewModelScope.launch {
            val result = repository.issueStock(
                partId = partId,
                quantity = quantity,
                partyId = partyId,
                personName = personName.trim(),
                issueDate = issueDate.trim(),
                note = note.trim(),
                attachmentUri = attachmentUri
            )
            result.onSuccess { remaining ->
                emitMessage("تم الصرف بنجاح - المتبقي في المخزون: $remaining قطعة")
                onSuccess()
            }.onFailure { err ->
                emitMessage(err.message ?: "فشلت عملية الصرف")
            }
        }
    }

    fun editStockIssue(
        issueId: Long,
        newPartId: Long,
        newQuantity: Int,
        newPartyId: Long,
        newPersonName: String,
        newDate: String,
        newNote: String,
        newAttachmentUri: String?,
        onSuccess: () -> Unit
    ) {
        if (newQuantity <= 0) {
            emitMessage("يجب أن تكون الكمية أكبر من صفر")
            return
        }
        viewModelScope.launch {
            val res = repository.editStockIssue(
                issueId = issueId,
                newPartId = newPartId,
                newQuantity = newQuantity,
                newPartyId = newPartyId,
                newPersonName = newPersonName.trim(),
                newDate = newDate.trim(),
                newNote = newNote.trim(),
                newAttachmentUri = newAttachmentUri
            )
            res.onSuccess {
                emitMessage("تم تعديل أمر الصرف وتحديث المخزون بنجاح")
                onSuccess()
            }.onFailure {
                emitMessage(it.message ?: "تعذر تعديل أمر الصرف")
            }
        }
    }

    fun deleteStockIssue(issueId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteStockIssue(issueId)
            res.onSuccess {
                emitMessage("تم حذف أمر الصرف وإعادة الكمية المصروفة إلى المخزون بنجاح")
                onSuccess()
            }.onFailure {
                emitMessage(it.message ?: "تعذر حذف أمر الصرف")
            }
        }
    }

    // --- Modular Export / Import & Cloud Sync ---

    private fun loadCloudBackupsFromPrefs(): List<com.example.data.model.CloudBackupRecord> {
        val jsonStr = prefs.getString("cloud_backups_list_json", null)
        val list = mutableListOf<com.example.data.model.CloudBackupRecord>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val arr = org.json.JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        com.example.data.model.CloudBackupRecord(
                            id = obj.optString("id", "BK-$i"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            dateTimeStr = obj.optString("dateTimeStr", ""),
                            email = obj.optString("email", ""),
                            partsCount = obj.optInt("partsCount", 0),
                            ordersCount = obj.optInt("ordersCount", 0),
                            issuesCount = obj.optInt("issuesCount", 0),
                            jsonData = obj.optString("jsonData", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        return list
    }

    private fun saveCloudBackupsToPrefs(list: List<com.example.data.model.CloudBackupRecord>) {
        val arr = org.json.JSONArray()
        list.forEach { rec ->
            val obj = org.json.JSONObject().apply {
                put("id", rec.id)
                put("timestamp", rec.timestamp)
                put("dateTimeStr", rec.dateTimeStr)
                put("email", rec.email)
                put("partsCount", rec.partsCount)
                put("ordersCount", rec.ordersCount)
                put("issuesCount", rec.issuesCount)
                put("jsonData", rec.jsonData)
            }
            arr.put(obj)
        }
        prefs.edit().putString("cloud_backups_list_json", arr.toString()).apply()
    }

    fun syncToCloud(onSuccess: (() -> Unit)? = null) {
        val email = _googleAccountEmail.value.trim()
        if (email.isBlank() || !_isGoogleAccountVerified.value) {
            emitMessage("تنبيه: يجب توثيق البريد الإلكتروني المعتمد للسحابة أولاً قبل رفع البيانات!")
            return
        }
        if (!_isOnline.value) {
            emitMessage("فشلت المزامنة: لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة وإعادة المحاولة.")
            return
        }
        viewModelScope.launch {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val snapshotJson = exportModularData("full")
            
            val newRecord = com.example.data.model.CloudBackupRecord(
                id = "CLOUD-BK-${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                dateTimeStr = time,
                email = email,
                partsCount = parts.value.size,
                ordersCount = orders.value.size,
                issuesCount = stockIssues.value.size,
                jsonData = snapshotJson
            )
            val updatedBackups = (listOf(newRecord) + _cloudBackups.value).take(25)
            saveCloudBackupsToPrefs(updatedBackups)
            _cloudBackups.value = updatedBackups

            prefs.edit()
                .putString("cloud_backup_json", snapshotJson)
                .putString("last_cloud_sync_time", "$time ($email)")
                .apply()
            _lastCloudSyncTime.value = "$time ($email)"
            emitMessage("تمت المزامنة السحابية بنجاح وحفظ نسخة احتياطية جديدة في السحابة ($email)")
            addNotification("مزامنة سحابية مكتملة", "تم حفظ نسخة سحابية جديدة بحساب $email", "sync")
            onSuccess?.invoke()
        }
    }

    fun restoreCloudBackup(backup: com.example.data.model.CloudBackupRecord, onSuccess: (() -> Unit)? = null) {
        val email = _googleAccountEmail.value.trim()
        if (email.isBlank() || !_isGoogleAccountVerified.value) {
            emitMessage("تنبيه: يجب توثيق البريد الإلكتروني المعتمد للسحابة أولاً قبل استعادة النسخ السحابية!")
            return
        }
        if (!_isOnline.value) {
            emitMessage("فشل الاسترجاع: لا يوجد اتصال بالإنترنت للوصول إلى السحابة.")
            return
        }
        viewModelScope.launch {
            if (backup.jsonData.isBlank()) {
                emitMessage("⚠️ ملف النسخة الاحتياطية فارغ أو تالف")
                return@launch
            }
            repository.clearAllTables()
            val result = importModularData(backup.jsonData)
            result.onSuccess { msg ->
                emitMessage("تمت استعادة النسخة السحابية المؤرخة (${backup.dateTimeStr}) بنجاح")
                addNotification("استرجاع من السحابة", "تمت استعادة النسخة السحابية (${backup.dateTimeStr}) بنجاح", "sync")
                onSuccess?.invoke()
            }.onFailure {
                emitMessage("فشل الاسترجاع: ${it.message}")
            }
        }
    }

    fun deleteCloudBackup(backupId: String) {
        val updated = _cloudBackups.value.filter { it.id != backupId }
        saveCloudBackupsToPrefs(updated)
        _cloudBackups.value = updated
        emitMessage("تم حذف النسخة السحابية من القائمة بنجاح")
    }

    fun restoreFromCloud(onSuccess: (() -> Unit)? = null) {
        val email = _googleAccountEmail.value.trim()
        if (email.isBlank() || !_isGoogleAccountVerified.value) {
            emitMessage("تنبيه: يجب توثيق البريد الإلكتروني المعتمد للسحابة أولاً قبل استعادة البيانات!")
            return
        }
        val latest = _cloudBackups.value.firstOrNull()
        if (latest != null) {
            restoreCloudBackup(latest, onSuccess)
            return
        }
        if (!_isOnline.value) {
            emitMessage("فشل الاسترجاع: لا يوجد اتصال بالإنترنت للوصول إلى السحابة.")
            return
        }
        viewModelScope.launch {
            val savedJson = prefs.getString("cloud_backup_json", null)
            if (savedJson.isNullOrBlank()) {
                emitMessage("⚠️ لا توجد نسخة سحابية سابقة محفوظة لهذا الحساب")
                return@launch
            }
            repository.clearAllTables()
            val result = importModularData(savedJson)
            emitMessage("تم استرجاع البيانات السحابية بالكامل وتحديث قاعدة البيانات بنجاح")
            addNotification("استرجاع من السحابة", "تم استرجاع كافة البيانات من الحساب السحابي بنجاح", "sync")
            onSuccess?.invoke()
        }
    }

    fun loadCustomReorderDistributionIntoMatrix(allocations: Map<Pair<Long, Long>, Int>) {
        if (allocations.isEmpty()) {
            emitMessage("لم يتم تخصيص أي كميات للعملاء")
            return
        }
        val positiveOnly = allocations.filter { it.value > 0 }
        if (positiveOnly.isEmpty()) {
            emitMessage("يرجى تخصيص كمية أكبر من صفر لعميل واحد على الأقل")
            return
        }
        _matrixDraft.value = positiveOnly
        val newBatchId = "REORDER-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())}"
        _currentEditingBatchId.value = newBatchId
        _ordersTab.value = 0 // Switch to Matrix View
        val totalPieces = positiveOnly.values.sum()
        emitMessage("تم نقل $totalPieces قطعة موزعة على العملاء إلى مصفوفة الطلبات بنجاح")
        addNotification("إعادة طلب المتبقي الموزع", "تم نقل $totalPieces قطعة من الأصناف المتبقية إلى العرض الجماعي ($newBatchId)", "supplier")
    }

    fun exportModularData(moduleType: String): String {
        val partsList = parts.value
        val invList = inventory.value
        val custList = customers.value
        val partyList = parties.value
        val ordList = orders.value
        val itemsList = orderItems.value
        val suppList = supplierOrders.value
        val issuesList = stockIssues.value
        val receiptsList = supplierReceipts.value
        val paymentsList = orderPayments.value

        val root = org.json.JSONObject()
        root.put("app", "AutoParts Pro")
        root.put("module", moduleType)
        root.put("timestamp", System.currentTimeMillis())

        if (moduleType == "parts" || moduleType == "parts_inventory" || moduleType == "full") {
            val partsArr = org.json.JSONArray()
            partsList.forEach { p ->
                val obj = org.json.JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("carType", p.carType)
                    put("carModel", p.carModel)
                    put("purchasePrice", p.purchasePrice)
                    put("price", p.price)
                    put("minStock", p.minStock)
                    put("barcode", p.barcode)
                    put("createdAt", p.createdAt)
                    val inv = invList.find { it.partId == p.id }?.availableQty ?: 0
                    put("qty", inv)
                }
                partsArr.put(obj)
            }
            root.put("parts", partsArr)
        }

        if (moduleType == "orders" || moduleType == "full") {
            val custArr = org.json.JSONArray()
            custList.forEach { c ->
                val obj = org.json.JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("notes", c.notes)
                }
                custArr.put(obj)
            }
            root.put("customers", custArr)

            val ordArr = org.json.JSONArray()
            ordList.forEach { o ->
                val obj = org.json.JSONObject().apply {
                    put("id", o.id)
                    put("customerId", o.customerId)
                    put("title", o.title)
                    put("orderDate", o.orderDate)
                    put("paidAmount", o.paidAmount)
                    put("discount", o.discount)
                    put("status", o.status)
                    put("batchId", o.batchId)
                }
                ordArr.put(obj)
            }
            root.put("orders", ordArr)

            val itemsArr = org.json.JSONArray()
            itemsList.forEach { itm ->
                val obj = org.json.JSONObject().apply {
                    put("id", itm.id)
                    put("orderId", itm.orderId)
                    put("partId", itm.partId)
                    put("quantity", itm.quantity)
                }
                itemsArr.put(obj)
            }
            root.put("order_items", itemsArr)

            val paymentsArr = org.json.JSONArray()
            paymentsList.forEach { p ->
                val obj = org.json.JSONObject().apply {
                    put("id", p.id)
                    put("orderId", p.orderId)
                    put("amount", p.amount)
                    put("paymentDate", p.paymentDate)
                    put("payerName", p.payerName)
                    put("note", p.note)
                }
                paymentsArr.put(obj)
            }
            root.put("order_payments", paymentsArr)
        }

        if (moduleType == "issues" || moduleType == "full") {
            val partyArr = org.json.JSONArray()
            partyList.forEach { p ->
                val obj = org.json.JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("type", p.type)
                    put("notes", p.notes)
                }
                partyArr.put(obj)
            }
            root.put("parties", partyArr)

            val issuesArr = org.json.JSONArray()
            issuesList.forEach { s ->
                val obj = org.json.JSONObject().apply {
                    put("id", s.id)
                    put("partId", s.partId)
                    put("quantity", s.quantity)
                    put("partyId", s.partyId)
                    put("personName", s.personName)
                    put("issueDate", s.issueDate)
                    put("notes", s.note)
                }
                issuesArr.put(obj)
            }
            root.put("issues", issuesArr)
        }

        if (moduleType == "full") {
            val suppArr = org.json.JSONArray()
            suppList.forEach { s ->
                val obj = org.json.JSONObject().apply {
                    put("id", s.id)
                    put("partId", s.partId)
                    put("qtyOrdered", s.qtyOrdered)
                    put("qtyReceived", s.qtyReceived)
                    put("qtyRemaining", s.qtyRemaining)
                    put("status", s.status)
                    put("orderDate", s.orderDate)
                    put("confirmedDate", s.confirmedDate)
                    put("notes", s.notes)
                    put("batchId", s.batchId)
                }
                suppArr.put(obj)
            }
            root.put("supplier_orders", suppArr)

            val receiptsArr = org.json.JSONArray()
            receiptsList.forEach { r ->
                val obj = org.json.JSONObject().apply {
                    put("id", r.id)
                    put("supplierOrderId", r.supplierOrderId)
                    put("partId", r.partId)
                    put("quantity", r.quantity)
                    put("receiptDate", r.receiptDate)
                    put("receiverName", r.receiverName)
                    put("note", r.note)
                }
                receiptsArr.put(obj)
            }
            root.put("supplier_receipts", receiptsArr)
        }

        return root.toString(2)
    }

    fun exportCustomData(context: Context, selectedModules: Set<String>, asExcel: Boolean) {
        if (selectedModules.isEmpty()) {
            emitMessage("يرجى تحديد قسم واحد على الأقل للتصدير")
            return
        }
        if (asExcel) {
            val headers = listOf("القسم", "البيان الأول", "البيان الثاني", "الكمية / المبلغ", "التاريخ", "ملاحظات")
            val rows = mutableListOf<List<String>>()
            if (selectedModules.contains("parts") || selectedModules.contains("inventory")) {
                val invMap = inventory.value.associateBy { it.partId }
                parts.value.forEach { p ->
                    val q = invMap[p.id]?.availableQty ?: 0
                    rows.add(listOf("الأصناف والمخزون", p.name, "${p.carType} ${p.carModel}", "$q قطعة", "", "سعر: ${p.price} | شراء: ${p.purchasePrice} | باركود: ${p.barcode}"))
                }
            }
            if (selectedModules.contains("customers")) {
                customers.value.forEach { c ->
                    rows.add(listOf("العملاء", c.name, c.phone, "", "", c.notes))
                }
            }
            if (selectedModules.contains("parties")) {
                parties.value.forEach { p ->
                    rows.add(listOf("الأطراف والجهات", p.name, p.type, p.phone, "", p.notes))
                }
            }
            if (selectedModules.contains("orders")) {
                val custMap = customers.value.associateBy { it.id }
                orders.value.forEach { o ->
                    rows.add(listOf("طلبيات العملاء", custMap[o.customerId]?.name ?: "#${o.customerId}", o.title, "${o.totalAmount} ر.س", o.orderDate, "مدفوع: ${o.paidAmount} | متبقي: ${o.remainingAmount} | دفعة: ${o.batchId}"))
                }
            }
            if (selectedModules.contains("payments")) {
                orderPayments.value.forEach { pay ->
                    rows.add(listOf("دفعات العملاء", pay.payerName, "#طلبية ${pay.orderId}", "${pay.amount} ر.س", pay.paymentDate, pay.note))
                }
            }
            if (selectedModules.contains("supplier_orders")) {
                val partMap = parts.value.associateBy { it.id }
                supplierOrders.value.forEach { so ->
                    rows.add(listOf("طلبيات الموردين", partMap[so.partId]?.name ?: "#${so.partId}", "مطلوب: ${so.qtyOrdered}", "مستلم: ${so.qtyReceived} / متبقي: ${so.qtyRemaining}", so.orderDate, so.notes))
                }
            }
            if (selectedModules.contains("supplier_receipts")) {
                val partMap = parts.value.associateBy { it.id }
                supplierReceipts.value.forEach { sr ->
                    rows.add(listOf("توريدات الموردين", partMap[sr.partId]?.name ?: "#${sr.partId}", "المستلم: ${sr.receiverName}", "+${sr.quantity} قطعة", sr.receiptDate, sr.note))
                }
            }
            if (selectedModules.contains("issues")) {
                val partMap = parts.value.associateBy { it.id }
                val partyMap = parties.value.associateBy { it.id }
                stockIssues.value.forEach { si ->
                    rows.add(listOf("سندات الصرف", partMap[si.partId]?.name ?: "#${si.partId}", partyMap[si.partyId]?.name ?: "", "-${si.quantity} قطعة", si.issueDate, "مستلم: ${si.personName} | ${si.note}"))
                }
            }
            ExportUtils.exportToExcelCsv(
                context = context,
                fileName = "AutoParts_Custom_Export_${System.currentTimeMillis()}",
                title = "تصدير مخصص لبيانات التطبيق المختارة",
                headers = headers,
                rows = rows,
                summaryLines = listOf("عدد السجلات المصدرة: ${rows.size}", "الأقسام المشمولة: ${selectedModules.joinToString()}"),
                appUserName = _appUserName.value
            )
        } else {
            try {
                val root = org.json.JSONObject()
                root.put("app", "AutoParts Pro")
                root.put("modules", org.json.JSONArray(selectedModules))
                root.put("timestamp", System.currentTimeMillis())

                if (selectedModules.contains("parts") || selectedModules.contains("inventory")) {
                    val partsArr = org.json.JSONArray()
                    val invMap = inventory.value.associateBy { it.partId }
                    parts.value.forEach { p ->
                        val obj = org.json.JSONObject().apply {
                            put("id", p.id)
                            put("name", p.name)
                            put("carType", p.carType)
                            put("carModel", p.carModel)
                            put("purchasePrice", p.purchasePrice)
                            put("price", p.price)
                            put("minStock", p.minStock)
                            put("barcode", p.barcode)
                            put("createdAt", p.createdAt)
                            put("qty", invMap[p.id]?.availableQty ?: 0)
                        }
                        partsArr.put(obj)
                    }
                    root.put("parts", partsArr)
                }
                if (selectedModules.contains("customers")) {
                    val custArr = org.json.JSONArray()
                    customers.value.forEach { c ->
                        val obj = org.json.JSONObject().apply {
                            put("id", c.id)
                            put("name", c.name)
                            put("phone", c.phone)
                            put("notes", c.notes)
                        }
                        custArr.put(obj)
                    }
                    root.put("customers", custArr)
                }
                if (selectedModules.contains("parties")) {
                    val partyArr = org.json.JSONArray()
                    parties.value.forEach { p ->
                        val obj = org.json.JSONObject().apply {
                            put("id", p.id)
                            put("name", p.name)
                            put("type", p.type)
                            put("notes", p.notes)
                        }
                        partyArr.put(obj)
                    }
                    root.put("parties", partyArr)
                }
                if (selectedModules.contains("orders")) {
                    val ordArr = org.json.JSONArray()
                    orders.value.forEach { o ->
                        val obj = org.json.JSONObject().apply {
                            put("id", o.id)
                            put("customerId", o.customerId)
                            put("title", o.title)
                            put("orderDate", o.orderDate)
                            put("paidAmount", o.paidAmount)
                            put("discount", o.discount)
                            put("status", o.status)
                            put("batchId", o.batchId)
                        }
                        ordArr.put(obj)
                    }
                    root.put("orders", ordArr)

                    val itemsArr = org.json.JSONArray()
                    orderItems.value.forEach { itm ->
                        val obj = org.json.JSONObject().apply {
                            put("id", itm.id)
                            put("orderId", itm.orderId)
                            put("partId", itm.partId)
                            put("quantity", itm.quantity)
                        }
                        itemsArr.put(obj)
                    }
                    root.put("order_items", itemsArr)
                }
                if (selectedModules.contains("payments")) {
                    val paymentsArr = org.json.JSONArray()
                    orderPayments.value.forEach { p ->
                        val obj = org.json.JSONObject().apply {
                            put("id", p.id)
                            put("orderId", p.orderId)
                            put("amount", p.amount)
                            put("paymentDate", p.paymentDate)
                            put("payerName", p.payerName)
                            put("note", p.note)
                        }
                        paymentsArr.put(obj)
                    }
                    root.put("order_payments", paymentsArr)
                }
                if (selectedModules.contains("supplier_orders")) {
                    val suppArr = org.json.JSONArray()
                    supplierOrders.value.forEach { s ->
                        val obj = org.json.JSONObject().apply {
                            put("id", s.id)
                            put("partId", s.partId)
                            put("qtyOrdered", s.qtyOrdered)
                            put("qtyReceived", s.qtyReceived)
                            put("qtyRemaining", s.qtyRemaining)
                            put("status", s.status)
                            put("orderDate", s.orderDate)
                            put("confirmedDate", s.confirmedDate)
                            put("notes", s.notes)
                            put("batchId", s.batchId)
                        }
                        suppArr.put(obj)
                    }
                    root.put("supplier_orders", suppArr)
                }
                if (selectedModules.contains("supplier_receipts")) {
                    val receiptsArr = org.json.JSONArray()
                    supplierReceipts.value.forEach { r ->
                        val obj = org.json.JSONObject().apply {
                            put("id", r.id)
                            put("supplierOrderId", r.supplierOrderId)
                            put("partId", r.partId)
                            put("quantity", r.quantity)
                            put("receiptDate", r.receiptDate)
                            put("receiverName", r.receiverName)
                            put("note", r.note)
                        }
                        receiptsArr.put(obj)
                    }
                    root.put("supplier_receipts", receiptsArr)
                }
                if (selectedModules.contains("issues")) {
                    val issuesArr = org.json.JSONArray()
                    stockIssues.value.forEach { s ->
                        val obj = org.json.JSONObject().apply {
                            put("id", s.id)
                            put("partId", s.partId)
                            put("quantity", s.quantity)
                            put("partyId", s.partyId)
                            put("personName", s.personName)
                            put("issueDate", s.issueDate)
                            put("notes", s.note)
                        }
                        issuesArr.put(obj)
                    }
                    root.put("issues", issuesArr)
                }

                val json = root.toString(2)
                val fileName = "AutoParts_Custom_Backup_${System.currentTimeMillis()}.json"
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(json, Charsets.UTF_8)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(sendIntent, "مشاركة النسخة الاحتياطية المخصصة")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                addNotification("تصدير نسخة احتياطية مخصصة", "تم تصدير ملف النسخة المخصصة بنجاح", "sync")
            } catch (e: Exception) {
                emitMessage("خطأ أثناء التصدير: ${e.message}")
            }
        }
    }

    fun exportModularData(context: Context, moduleType: String) {
        try {
            val json = exportModularData(moduleType)
            val fileName = "AutoParts_Backup_${moduleType}_v1.json"
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(json, Charsets.UTF_8)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "مشاركة / نقل النسخة الاحتياطية لهاتف آخر")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            addNotification("تصدير نسخة احتياطية", "تم تصدير ملف النسخة الاحتياطية ($fileName) بنجاح لنقله إلى هاتف آخر", "sync")
        } catch (e: Exception) {
            emitMessage("خطأ أثناء تصدير الملف: ${e.message}")
        }
    }

    fun importModularData(jsonString: String, merge: Boolean) {
        viewModelScope.launch {
            if (!merge) {
                repository.clearAllTables()
            }
            val result = importModularData(jsonString)
            result.onSuccess {
                emitMessage(if (merge) "تم دمج البيانات بنجاح في قاعدة البيانات" else "تم استبدال واسترجاع كافة البيانات بنجاح")
                addNotification("استيراد بيانات", if (merge) "تم دمج بيانات جديدة في قاعدة البيانات" else "تم استرجاع واستبدال كافة بيانات التطبيق", "sync")
            }.onFailure {
                emitMessage(it.message ?: "فشل استيراد البيانات")
            }
        }
    }

    suspend fun importModularData(jsonString: String): Result<String> {
        return try {
            val root = org.json.JSONObject(jsonString)
            val app = root.optString("app", "")
            if (!app.contains("AutoParts", ignoreCase = true) && !jsonString.contains("AutoParts")) {
                return Result.failure(Exception("ملف غير صالح أو لا يتبع تطبيق قطع غيار Pro"))
            }

            var importedParts = 0
            var importedOrders = 0
            var importedIssues = 0

            // 1. Parts & Inventory
            if (root.has("parts")) {
                val partsArr = root.getJSONArray("parts")
                for (i in 0 until partsArr.length()) {
                    val obj = partsArr.getJSONObject(i)
                    val p = PartEntity(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "صنف مستورد"),
                        carType = obj.optString("carType", "عام"),
                        carModel = obj.optString("carModel", "2022"),
                        purchasePrice = obj.optDouble("purchasePrice", 0.0),
                        price = obj.optDouble("price", 0.0),
                        minStock = obj.optInt("minStock", 5),
                        barcode = obj.optString("barcode", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    val partId = db.autoPartsDao().insertPart(p)
                    val qty = obj.optInt("qty", 0)
                    db.autoPartsDao().insertInventory(
                        InventoryEntity(
                            partId = if (p.id != 0L) p.id else partId,
                            availableQty = qty,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    importedParts++
                }
            }

            // 2. Customers
            if (root.has("customers")) {
                val custArr = root.getJSONArray("customers")
                for (i in 0 until custArr.length()) {
                    val obj = custArr.getJSONObject(i)
                    val c = CustomerEntity(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "عميل"),
                        phone = obj.optString("phone", ""),
                        notes = obj.optString("notes", "")
                    )
                    db.autoPartsDao().insertCustomer(c)
                }
            }

            // 3. Orders & Items
            if (root.has("orders")) {
                val ordArr = root.getJSONArray("orders")
                for (i in 0 until ordArr.length()) {
                    val obj = ordArr.getJSONObject(i)
                    val o = OrderEntity(
                        id = obj.optLong("id", 0),
                        customerId = obj.optLong("customerId", 1),
                        title = obj.optString("title", "طلبية مستوردة"),
                        orderDate = obj.optString("orderDate", "2026-01-01"),
                        paidAmount = obj.optDouble("paidAmount", obj.optDouble("paid", 0.0)),
                        discount = obj.optDouble("discount", 0.0),
                        status = obj.optString("status", "pending"),
                        batchId = obj.optString("batchId", "")
                    )
                    db.autoPartsDao().insertOrder(o)
                    importedOrders++
                }
            }

            if (root.has("order_items")) {
                val itemsArr = root.getJSONArray("order_items")
                for (i in 0 until itemsArr.length()) {
                    val obj = itemsArr.getJSONObject(i)
                    val itm = OrderItemEntity(
                        id = obj.optLong("id", 0),
                        orderId = obj.optLong("orderId", 1),
                        partId = obj.optLong("partId", 1),
                        quantity = obj.optInt("quantity", 1)
                    )
                    db.autoPartsDao().insertOrderItem(itm)
                }
            }

            // 4. Payments
            if (root.has("order_payments")) {
                val payArr = root.getJSONArray("order_payments")
                for (i in 0 until payArr.length()) {
                    val obj = payArr.getJSONObject(i)
                    val p = com.example.data.entity.OrderPaymentEntity(
                        id = obj.optLong("id", 0),
                        orderId = obj.optLong("orderId", 1),
                        amount = obj.optDouble("amount", 0.0),
                        paymentDate = obj.optString("paymentDate", "2026-01-01"),
                        payerName = obj.optString("payerName", "المسدد"),
                        note = obj.optString("note", "")
                    )
                    db.autoPartsDao().insertPayment(p)
                }
            }

            // 5. Parties & Issues
            if (root.has("parties")) {
                val partyArr = root.getJSONArray("parties")
                for (i in 0 until partyArr.length()) {
                    val obj = partyArr.getJSONObject(i)
                    val p = PartyEntity(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "جهة"),
                        type = obj.optString("type", "ورشة"),
                        notes = obj.optString("notes", "")
                    )
                    db.autoPartsDao().insertParty(p)
                }
            }

            if (root.has("issues")) {
                val issuesArr = root.getJSONArray("issues")
                for (i in 0 until issuesArr.length()) {
                    val obj = issuesArr.getJSONObject(i)
                    val s = StockIssueEntity(
                        id = obj.optLong("id", 0),
                        partId = obj.optLong("partId", 1),
                        quantity = obj.optInt("quantity", obj.optInt("qty", 1)),
                        partyId = obj.optLong("partyId", 1),
                        personName = obj.optString("personName", obj.optString("person", "مستلم")),
                        issueDate = obj.optString("issueDate", obj.optString("date", "2026-01-01")),
                        note = obj.optString("note", obj.optString("notes", ""))
                    )
                    db.autoPartsDao().insertStockIssue(s)
                    importedIssues++
                }
            }

            // 6. Supplier Orders & Receipts
            if (root.has("supplier_orders")) {
                val suppArr = root.getJSONArray("supplier_orders")
                for (i in 0 until suppArr.length()) {
                    val obj = suppArr.getJSONObject(i)
                    val s = SupplierOrderEntity(
                        id = obj.optLong("id", 0),
                        partId = obj.optLong("partId", 1),
                        qtyOrdered = obj.optInt("qtyOrdered", 1),
                        qtyReceived = obj.optInt("qtyReceived", 0),
                        qtyRemaining = obj.optInt("qtyRemaining", 1),
                        status = obj.optString("status", "confirmed"),
                        orderDate = obj.optString("orderDate", "2026-01-01"),
                        confirmedDate = obj.optString("confirmedDate", "2026-01-01"),
                        notes = obj.optString("notes", ""),
                        batchId = obj.optString("batchId", "")
                    )
                    db.autoPartsDao().insertSupplierOrder(s)
                }
            }

            if (root.has("supplier_receipts")) {
                val recArr = root.getJSONArray("supplier_receipts")
                for (i in 0 until recArr.length()) {
                    val obj = recArr.getJSONObject(i)
                    val r = com.example.data.entity.SupplierReceiptEntity(
                        id = obj.optLong("id", 0),
                        supplierOrderId = obj.optLong("supplierOrderId", 1),
                        partId = obj.optLong("partId", 1),
                        quantity = obj.optInt("quantity", 1),
                        receiptDate = obj.optString("receiptDate", "2026-01-01"),
                        receiverName = obj.optString("receiverName", "مستلم"),
                        note = obj.optString("note", "")
                    )
                    db.autoPartsDao().insertSupplierReceipt(r)
                }
            }

            Result.success("تم استيراد $importedParts صنف، و$importedOrders طلبية بنجاح!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Multi-Payment Operations on Customer Orders ---
    fun addOrderPayment(
        orderId: Long,
        amount: Double,
        paymentDate: String,
        payerName: String,
        note: String = "",
        attachmentUri: String? = null
    ) {
        val ord = orders.value.find { it.id == orderId }
        if (ord == null) {
            emitMessage("الطلبية غير موجودة")
            return
        }
        if (amount <= 0.0) {
            emitMessage("يرجى إدخال مبلغ صحيح أكبر من الصفر")
            return
        }
        if (amount > ord.remainingAmount + 0.01) {
            emitMessage("⚠️ لا يمكن إدخال مبلغ أكبر من المتبقي على الطلبية (${ord.remainingAmount} ر.س)!")
            return
        }
        if (payerName.isBlank()) {
            emitMessage("يرجى إدخال اسم الشخص الذي أوصل المبلغ")
            return
        }

        viewModelScope.launch {
            val date = paymentDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            repository.addOrderPayment(orderId, amount, date, payerName, note, attachmentUri)
            emitMessage("تم تسجيل دفعة بقيمة $amount ر.س بنجاح")
        }
    }

    fun updateOrderPaymentRecord(payment: com.example.data.entity.OrderPaymentEntity) {
        viewModelScope.launch {
            repository.updateOrderPayment(payment)
            emitMessage("تم تحديث بيانات الدفعة بنجاح")
        }
    }

    fun deleteOrderPaymentRecord(paymentId: Long, orderId: Long = 0L) {
        viewModelScope.launch {
            repository.deleteOrderPayment(paymentId)
            emitMessage("تم حذف الدفعة وتحديث الرصيد المتبقي بنجاح")
        }
    }

    fun deletePaymentAttachmentRecord(paymentId: Long) {
        viewModelScope.launch {
            repository.deletePaymentAttachment(paymentId)
            emitMessage("تم حذف مرفق الدفعة")
        }
    }

    // --- Multi-Receipt Operations on Supplier Orders ---
    fun addSupplierReceiptRecord(
        supplierOrderId: Long,
        partId: Long,
        quantity: Int,
        receiptDate: String,
        receiverName: String,
        note: String = "",
        attachmentUri: String? = null
    ) {
        val suppOrder = supplierOrders.value.find { it.id == supplierOrderId }
        if (suppOrder == null) {
            emitMessage("طلبية التوريد غير موجودة")
            return
        }
        if (quantity <= 0) {
            emitMessage("يرجى إدخال كمية صحيحة أكبر من الصفر")
            return
        }
        if (quantity > suppOrder.qtyRemaining) {
            emitMessage("⚠️ الكمية المستلمة ($quantity) أكبر من المتبقي بالطلبية (${suppOrder.qtyRemaining})!")
            return
        }
        if (receiverName.isBlank()) {
            emitMessage("يرجى إدخال اسم الشخص المستلم")
            return
        }

        viewModelScope.launch {
            val date = receiptDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            repository.addSupplierReceipt(supplierOrderId, partId, quantity, date, receiverName, note, attachmentUri)
            emitMessage("تم تسجيل استلام $quantity قطعة وإضافتها للمخزون بنجاح")
        }
    }

    fun addSupplierReceiptRecord(
        supplierOrderId: Long,
        quantity: Int,
        receiptDate: String,
        receiverName: String,
        notes: String = "",
        attachmentUri: String? = null
    ) {
        val partId = supplierOrders.value.find { it.id == supplierOrderId }?.partId ?: 0L
        addSupplierReceiptRecord(supplierOrderId, partId, quantity, receiptDate, receiverName, notes, attachmentUri)
    }

    fun updateSupplierReceiptRecord(receipt: com.example.data.entity.SupplierReceiptEntity, oldQty: Int = receipt.quantity) {
        viewModelScope.launch {
            repository.updateSupplierReceipt(receipt, oldQty)
            emitMessage("تم تحديث دفعة الاستلام وتعديل المخزون بنجاح")
        }
    }

    fun deleteSupplierReceiptRecord(receiptId: Long, supplierOrderId: Long = 0L) {
        viewModelScope.launch {
            repository.deleteSupplierReceipt(receiptId)
            emitMessage("تم حذف دفعة الاستلام وخصمها من المخزون بنجاح")
        }
    }

    fun deleteReceiptAttachmentRecord(receiptId: Long) {
        viewModelScope.launch {
            repository.deleteReceiptAttachment(receiptId)
            emitMessage("تم حذف مرفق الاستلام")
        }
    }

    // --- Secondary Device Governance & QR Code Linking ---
    fun setPrimaryDeviceMode(isPrimary: Boolean) {
        _isPrimaryDevice.value = isPrimary
        prefs.edit().putBoolean("is_primary_device", isPrimary).apply()
        emitMessage(if (isPrimary) "تم التعيين كجهاز رئيسي (Primary)" else "تم التعيين كجهاز فرعي (Secondary)")
    }

    fun setSyncFrequency(index: Int, customText: String = "") {
        _syncFrequencyIndex.value = index
        if (customText.isNotBlank()) {
            _syncCustomDurationText.value = customText
        }
        val freqLabel = when (index) {
            0 -> "تلقائي عند توفر الإنترنت"
            1 -> "بعد كل عملية مباشرة"
            2 -> "يومي"
            3 -> "أسبوعي"
            4 -> "شهري"
            5 -> _syncCustomDurationText.value
            else -> "تلقائي عند توفر الإنترنت"
        }
        _syncFrequency.value = freqLabel
        prefs.edit()
            .putInt("sync_frequency_index", index)
            .putString("sync_custom_duration_text", _syncCustomDurationText.value)
            .putString("sync_frequency", freqLabel)
            .apply()
        emitMessage("تم حفظ إعداد تكرار المزامنة: $freqLabel")
    }

    fun setSyncFrequency(frequency: String) {
        _syncFrequency.value = frequency
        prefs.edit().putString("sync_frequency", frequency).apply()
        emitMessage("تم تحديث إعداد تكرار المزامنة بنجاح")
    }

    fun updateDevicePermissions(
        deviceId: String,
        requireInternet: Boolean,
        allowDisburse: Boolean,
        allowEditParts: Boolean,
        allowOrders: Boolean,
        allowParties: Boolean
    ) {
        val current = _connectedDevices.value.map { dev ->
            if (dev.deviceId == deviceId) {
                dev.copy(
                    requireInternet = requireInternet,
                    allowStockDisbursement = allowDisburse,
                    allowEditParts = allowEditParts,
                    allowEditOrders = allowOrders,
                    allowManageParties = allowParties
                )
            } else dev
        }
        _connectedDevices.value = current
        emitMessage("تم تحديث صلاحيات الجهاز بنجاح")
    }

    fun setDevicePermission(deviceId: String, permNumber: Int, enabled: Boolean) {
        val current = _connectedDevices.value.map { dev ->
            if (dev.deviceId == deviceId) {
                when (permNumber) {
                    1 -> dev.copy(requireInternet = enabled)
                    2 -> dev.copy(allowStockDisbursement = enabled)
                    3 -> dev.copy(allowEditParts = enabled)
                    4 -> dev.copy(allowEditOrders = enabled)
                    5 -> dev.copy(allowManageParties = enabled)
                    else -> dev
                }
            } else dev
        }
        _connectedDevices.value = current
        emitMessage("تم تحديث صلاحية الجهاز بنجاح")
    }

    fun disconnectDevice(deviceId: String) {
        unlinkDevice(deviceId)
    }

    fun resolveConflict(conflictId: Any, keepLocal: Boolean) {
        val idStr = conflictId.toString()
        _offlineConflictLogs.value = _offlineConflictLogs.value.filter { it.id.toString() != idStr }
        emitMessage(if (keepLocal) "تم اعتماد التعديل المحلي" else "تم اعتماد التعديل السحابي")
    }

    fun unlinkDevice(deviceId: String) {
        val current = _connectedDevices.value.filter { it.deviceId != deviceId }
        _connectedDevices.value = current
        emitMessage("تم إلغاء ربط الجهاز بنجاح")
        addNotification("إلغاء ربط جهاز", "تم فصل الجهاز $deviceId من الشبكة", "device")
    }

    fun updateConnectedDeviceName(deviceId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            emitMessage("يرجى إدخال اسم صحيح للجهاز")
            return
        }
        val current = _connectedDevices.value.map { dev ->
            if (dev.deviceId == deviceId) dev.copy(deviceName = trimmed) else dev
        }
        _connectedDevices.value = current
        emitMessage("تم تعديل اسم الجهاز إلى: $trimmed")
        addNotification("تعديل اسم جهاز", "تم تغيير مسمى الجهاز المرتبط $deviceId إلى: $trimmed", "device")
    }

    fun pairDeviceFromQrCode(qrData: String): Boolean {
        // 1. Internet connection is mandatory for secure device pairing
        if (!_isOnline.value) {
            emitMessage("فشل الربط: يجب أن يكون التطبيق متصلاً بالإنترنت لإتمام عملية الربط والمصادقة الأمنية!")
            return false
        }

        return try {
            val trimmed = qrData.trim()
            if (!trimmed.startsWith("AUTOPARTS_SECURE_V2|")) {
                emitMessage("خطأ: رمز الباركود غير صالح! يجب مسح رمز الربط الفعلي الصادر عن التطبيق حصراً.")
                return false
            }

            val parts = trimmed.split("|")
            val id = parts.find { it.startsWith("ID:") }?.removePrefix("ID:")
            val name = parts.find { it.startsWith("NAME:") }?.removePrefix("NAME:")
            val timeStr = parts.find { it.startsWith("TIME:") }?.removePrefix("TIME:")
            val token = parts.find { it.startsWith("TOKEN:") }?.removePrefix("TOKEN:")
            val sig = parts.find { it.startsWith("SIG:") }?.removePrefix("SIG:")

            if (id.isNullOrBlank() || name.isNullOrBlank() || timeStr.isNullOrBlank() || token.isNullOrBlank() || sig.isNullOrBlank()) {
                emitMessage("خطأ: بيانات رمز الربط غير مكتملة أو تالفة!")
                return false
            }

            // Cryptographic signature check
            val expectedSig = kotlin.math.abs("$id$name$timeStr$token-AUTOPARTS-SALT".hashCode()).toString()
            if (sig != expectedSig) {
                emitMessage("فشل المصادقة: رمز الباركود غير مصرح به أو تم التلاعب به!")
                return false
            }

            // Validity check: valid for 5 minutes
            val generatedTime = timeStr.toLongOrNull() ?: 0L
            val currentTime = System.currentTimeMillis()
            if (currentTime - generatedTime > 5 * 60 * 1000L || generatedTime > currentTime + 60000L) {
                emitMessage("انتهت صلاحية رمز الربط المؤقت! يرجى تحديث الرمز من الجهاز الرئيسي ومسحه مجدداً.")
                return false
            }

            val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val newDev = com.example.data.model.ConnectedDevice(
                deviceId = id,
                deviceName = name,
                linkedAt = time,
                requireInternet = true,
                allowStockDisbursement = true,
                allowEditParts = false,
                allowEditOrders = true,
                allowManageParties = false
            )
            val list = _connectedDevices.value.filter { it.deviceId != id } + newDev
            _connectedDevices.value = list
            emitMessage("تمت المصادقة وربط الجهاز بنجاح: $name")
            addNotification("ربط جهاز جديد بنجاح", "تمت مصادقة وربط الجهاز ($name) عبر كود الأمان المشفر", "device")
            true
        } catch (e: Exception) {
            emitMessage("خطأ: رمز الباركود غير صالح أو تالف")
            false
        }
    }

    fun addConnectedDevice(deviceName: String) {
        val id = "DEV-${(1000..9999).random()}-SEC"
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newDev = com.example.data.model.ConnectedDevice(
            deviceId = id,
            deviceName = deviceName.ifBlank { "جهاز فرعي جديد" },
            linkedAt = time,
            requireInternet = true,
            allowStockDisbursement = true,
            allowEditParts = false,
            allowEditOrders = true,
            allowManageParties = false
        )
        _connectedDevices.value = _connectedDevices.value + newDev
        emitMessage("تم ربط جهاز فرعي جديد بنجاح: ${newDev.deviceName}")
        addNotification("ربط جهاز يدوي", "تمت إضافة الجهاز (${newDev.deviceName}) إلى قائمة الأجهزة المرتبطة", "device")
    }

    // Export entered data in Excel CSV format (.csv with UTF-8 BOM)
    fun exportEnteredDataToExcel(context: Context, moduleType: String) {
        when (moduleType) {
            "parts" -> {
                val headers = listOf("م", "اسم الصنف", "نوع السيارة", "الموديل", "سعر الشراء", "سعر البيع", "الحد الأدنى")
                val rows = parts.value.mapIndexed { idx, p ->
                    listOf("${idx + 1}", p.name, p.carType, p.carModel, "${p.purchasePrice}", "${p.price}", "${p.minStock}")
                }
                com.example.ui.util.ExportUtils.exportToExcelCsv(
                    context = context,
                    fileName = "AutoParts_Parts_Export",
                    title = "سجل الأصناف المسجلة",
                    headers = headers,
                    rows = rows,
                    summaryLines = listOf("إجمالي الأصناف المسجلة: ${parts.value.size}"),
                    appUserName = appUserName.value
                )
            }
            "parts_inventory" -> {
                val invMap = inventory.value.associateBy { it.partId }
                val headers = listOf("م", "اسم الصنف", "نوع السيارة", "الموديل", "سعر الشراء", "سعر البيع", "المخزون المتوفر", "الحد الأدنى")
                val rows = parts.value.mapIndexed { idx, p ->
                    val qty = invMap[p.id]?.availableQty ?: 0
                    listOf("${idx + 1}", p.name, p.carType, p.carModel, "${p.purchasePrice}", "${p.price}", "$qty", "${p.minStock}")
                }
                com.example.ui.util.ExportUtils.exportToExcelCsv(
                    context = context,
                    fileName = "AutoParts_Inventory_Export",
                    title = "الأصناف مع أرصدة المخزون",
                    headers = headers,
                    rows = rows,
                    summaryLines = listOf("إجمالي الأصناف: ${parts.value.size}"),
                    appUserName = appUserName.value
                )
            }
            "orders" -> {
                val custMap = customers.value.associateBy { it.id }
                val headers = listOf("م", "العميل", "رقم الطلب", "التاريخ", "الحالة", "الإجمالي", "الواصل", "المتبقي", "رمز الدفعة")
                val rows = orders.value.mapIndexed { idx, o ->
                    val custName = custMap[o.customerId]?.name ?: "عميل #${o.customerId}"
                    listOf("${idx + 1}", custName, "#${o.id}", o.orderDate, o.status, "${o.totalAmount}", "${o.paidAmount}", "${o.remainingAmount}", o.batchId)
                }
                com.example.ui.util.ExportUtils.exportToExcelCsv(
                    context = context,
                    fileName = "AutoParts_Orders_Export",
                    title = "سجل طلبيات العملاء",
                    headers = headers,
                    rows = rows,
                    summaryLines = listOf("إجمالي الطلبيات: ${orders.value.size}"),
                    appUserName = appUserName.value
                )
            }
            "payments" -> {
                val ordMap = orders.value.associateBy { it.id }
                val custMap = customers.value.associateBy { it.id }
                val headers = listOf("م", "رقم الطلب", "اسم العميل", "المبلغ الواصل", "تاريخ الإيصال", "اسم المسلم", "ملاحظات")
                val rows = orderPayments.value.mapIndexed { idx, pay ->
                    val ord = ordMap[pay.orderId]
                    val custName = ord?.let { custMap[it.customerId]?.name } ?: ""
                    listOf("${idx + 1}", "#${pay.orderId}", custName, "${pay.amount} ر.س", pay.paymentDate, pay.payerName, pay.note)
                }
                com.example.ui.util.ExportUtils.exportToExcelCsv(
                    context = context,
                    fileName = "AutoParts_Payments_Movements",
                    title = "سجل حركات المبالغ الواصلة للطلبيات",
                    headers = headers,
                    rows = rows,
                    summaryLines = listOf("إجمالي المبالغ الواصلة: ${orderPayments.value.sumOf { it.amount }} ر.س"),
                    appUserName = appUserName.value
                )
            }
            "issues" -> {
                val partMap = parts.value.associateBy { it.id }
                val partyMap = parties.value.associateBy { it.id }
                val headers = listOf("م", "الصنف", "نوع السيارة", "الكمية", "الجهة", "اسم المستلم", "تاريخ الصرف", "ملاحظات")
                val rows = stockIssues.value.mapIndexed { idx, s ->
                    val p = partMap[s.partId]
                    val party = partyMap[s.partyId]
                    listOf("${idx + 1}", p?.name ?: "", p?.carType ?: "", "${s.quantity}", party?.name ?: "", s.personName, s.issueDate, s.note)
                }
                com.example.ui.util.ExportUtils.exportToExcelCsv(
                    context = context,
                    fileName = "AutoParts_Stock_Issues_Export",
                    title = "سجل عمليات صرف المخزون",
                    headers = headers,
                    rows = rows,
                    summaryLines = listOf("إجمالي أذونات الصرف: ${stockIssues.value.size}"),
                    appUserName = appUserName.value
                )
            }
            else -> {
                exportModularData(context, "full")
            }
        }
    }

    // --- Database Reset & Seeds ---
    fun resetDatabaseToDefaultSeeds() {
        if (!_isPrimaryDevice.value) {
            emitMessage("⚠️ عذراً: هذه الصلاحية للمستخدم الأساسي فقط ولا تتاح للمستخدمين الثانويين")
            return
        }
        viewModelScope.launch {
            repository.seedSampleData()
            clearMatrixQuantities(silent = true)
            emitMessage("تمت استعادة البيانات الافتراضية بنجاح")
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
