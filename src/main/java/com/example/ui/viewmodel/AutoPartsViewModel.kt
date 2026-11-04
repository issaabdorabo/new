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

    // App Language: "ar" (Arabic - default) or "en" (English)
    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "ar") ?: "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Google Account for Cloud Sync & Verification
    private val _googleAccountEmail = MutableStateFlow(prefs.getString("google_account_email", "issaabdo7368@gmail.com") ?: "issaabdo7368@gmail.com")
    val googleAccountEmail: StateFlow<String> = _googleAccountEmail.asStateFlow()

    private val _isGoogleAccountVerified = MutableStateFlow(prefs.getBoolean("google_account_verified", true))
    val isGoogleAccountVerified: StateFlow<Boolean> = _isGoogleAccountVerified.asStateFlow()

    private val _pendingVerificationCode = MutableStateFlow<String?>(null)
    val pendingVerificationCode: StateFlow<String?> = _pendingVerificationCode.asStateFlow()

    private val _verificationCountdown = MutableStateFlow(0)
    val verificationCountdown: StateFlow<Int> = _verificationCountdown.asStateFlow()

    // Interstitial Ad Management (shows after every 3 report exports)
    private val _exportCount = MutableStateFlow(prefs.getInt("export_count", 0))
    val exportCount: StateFlow<Int> = _exportCount.asStateFlow()

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd.asStateFlow()

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

    private val _syncFrequency = MutableStateFlow(prefs.getString("sync_frequency", "auto_internet") ?: "auto_internet")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

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
                    item.part.carModel.contains(query, ignoreCase = true)

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

    fun setAppLanguage(lang: String) {
        val safeLang = if (lang == "en") "en" else "ar"
        _appLanguage.value = safeLang
        prefs.edit().putString("app_language", safeLang).apply()
        emitMessage(if (safeLang == "en") "Language switched to English" else "تم تغيير لغة التطبيق إلى العربية")
    }

    fun setGoogleAccountEmail(email: String) {
        val trimmed = email.trim().ifBlank { "issaabdo7368@gmail.com" }
        _googleAccountEmail.value = trimmed
        prefs.edit().putString("google_account_email", trimmed).apply()
        emitMessage("تم حفظ البريد: $trimmed")
    }

    // Google Cloud Account Email Verification (Automated code dispatch & verification without opening mail app)
    fun sendVerificationCodeToEmail(email: String): Result<String> {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@") || !trimmed.contains(".")) {
            val msg = "⚠️ يرجى إدخال بريد إلكتروني صالح (مثل: name@gmail.com)"
            emitMessage(msg)
            return Result.failure(IllegalArgumentException(msg))
        }

        // Generate a random 6-digit verification code
        val randomCode = (100000..999999).random().toString()
        _pendingVerificationCode.value = randomCode
        _verificationCountdown.value = 60

        // Start countdown timer
        viewModelScope.launch {
            for (i in 60 downTo 1) {
                _verificationCountdown.value = i
                kotlinx.coroutines.delay(1000)
            }
            _verificationCountdown.value = 0
        }

        _googleAccountEmail.value = trimmed
        prefs.edit().putString("google_account_email", trimmed).apply()

        // Inform user with automated verification code dispatch
        emitMessage("تم إرسال رمز التحقق المكون من 6 أرقام إلى بريدك ($trimmed): $randomCode")
        broadcastDeviceNotification("رمز توثيق حساب Google", "تم إنشاء رمز مصادقة سحابي للحساب: $trimmed")
        return Result.success(randomCode)
    }

    fun resendVerificationCode(): Result<String> {
        val email = _googleAccountEmail.value
        return sendVerificationCodeToEmail(email)
    }

    fun verifyEmailCode(enteredCode: String): Boolean {
        val code = enteredCode.trim()
        val expected = _pendingVerificationCode.value
        if (expected != null && code == expected) {
            _isGoogleAccountVerified.value = true
            prefs.edit().putBoolean("google_account_verified", true).apply()
            emitMessage("✅ تم توثيق ومصادقة حساب Google السحابي بنجاح! يمكنك الآن المزامنة بثقة.")
            broadcastDeviceNotification("توثيق حساب Google", "تم تأكيد ومصادقة حساب Google بنجاح")
            return true
        } else {
            emitMessage("❌ رمز التحقق غير صحيح، يرجى التأكد من الرمز وإعادة المحاولة.")
            return false
        }
    }

    // Ad Management Trigger (Called whenever user exports PDF or Excel)
    fun recordExportAndCheckAd() {
        val newCount = _exportCount.value + 1
        _exportCount.value = newCount
        prefs.edit().putInt("export_count", newCount).apply()
        if (newCount % 3 == 0) {
            _showInterstitialAd.value = true
        }
    }

    fun dismissInterstitialAd() {
        _showInterstitialAd.value = false
    }

    // --- Actions ---

    fun addPart(
        name: String,
        carType: String,
        carModel: String,
        carName: String = "",
        purchasePrice: Double = 0.0,
        price: Double = 0.0,
        minStock: Int = 0,
        initialQty: Int = 0,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        if (name.isBlank()) {
            emitMessage("يرجى إدخال اسم الصنف")
            return
        }

        // Prevent duplicate parts
        val isDuplicate = parts.value.any {
            it.name.trim().equals(name.trim(), ignoreCase = true) &&
                    it.carType.trim().equals(carType.trim(), ignoreCase = true) &&
                    it.carName.trim().equals(carName.trim(), ignoreCase = true) &&
                    it.carModel.trim().equals(carModel.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            emitMessage("⚠️ تنبيه: هذا الصنف مسجل مسبقاً بنفس الاسم ونوع واسم وموديل السيارة!")
            return
        }

        viewModelScope.launch {
            val part = PartEntity(
                name = name.trim(),
                carType = carType.trim().ifBlank { "عام" },
                carModel = carModel.trim().ifBlank { "عام" },
                carName = carName.trim(),
                purchasePrice = purchasePrice,
                price = price,
                minStock = maxOf(0, minStock)
            )
            val newId = repository.addPart(part, initialQty)
            emitMessage("تمت إضافة الصنف بنجاح")
            broadcastDeviceNotification("إضافة صنف جديد", "تمت إضافة الصنف '${part.name}' لمخزون ${part.carType} ${part.carName}")
            onSuccess?.invoke(newId)
        }
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

    fun reorderRemainingIntoMatrix(orderId: Long) {
        reorderRemainingBatchIntoMatrix(listOf(orderId))
    }

    fun reorderRemainingBatchIntoMatrix(orderIds: List<Long>, batchId: String? = null) {
        val allSuppOrders = supplierOrders.value
        val targetOrders = if (!batchId.isNullOrBlank()) {
            allSuppOrders.filter { it.batchId == batchId }
        } else {
            allSuppOrders.filter { it.id in orderIds }
        }

        if (targetOrders.isEmpty()) {
            emitMessage("الطلبية غير موجودة")
            return
        }

        val totalReceived = targetOrders.sumOf { it.qtyReceived }
        if (totalReceived <= 0) {
            emitMessage("يجب أن يكون قد تم استلام دفعة واحدة على الأقل لإعادة طلب المتبقي")
            return
        }

        val remainingOrders = targetOrders.filter { it.qtyRemaining > 0 }
        if (remainingOrders.isEmpty()) {
            emitMessage("لا توجد كمية متبقية لإعادة طلبها، الطلبية مكتملة بالكامل")
            return
        }

        viewModelScope.launch {
            val newDraft = mutableMapOf<Pair<Long, Long>, Int>()
            val ordList = if (!batchId.isNullOrBlank()) {
                orders.value.filter { it.batchId == batchId && it.status != "cancelled" }
            } else {
                emptyList()
            }
            val itemsList = orderItems.value
            val firstCust = customers.value.firstOrNull()

            remainingOrders.forEach { suppOrder ->
                var allocated = 0
                if (ordList.isNotEmpty()) {
                    ordList.forEach { ord ->
                        val itm = itemsList.find { it.orderId == ord.id && it.partId == suppOrder.partId }
                        if (itm != null && itm.quantity > 0) {
                            val custRemaining = minOf(itm.quantity, suppOrder.qtyRemaining - allocated)
                            if (custRemaining > 0) {
                                newDraft[suppOrder.partId to ord.customerId] = custRemaining
                                allocated += custRemaining
                            }
                        }
                    }
                }
                val unallocated = suppOrder.qtyRemaining - allocated
                if (unallocated > 0 && firstCust != null) {
                    val prev = newDraft[suppOrder.partId to firstCust.id] ?: 0
                    newDraft[suppOrder.partId to firstCust.id] = prev + unallocated
                }
            }

            _matrixDraft.value = newDraft
            _currentEditingBatchId.value = "REORDER-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())}"
            _ordersTab.value = 0 // Switch to bulk matrix table
            val totalRemainingPieces = remainingOrders.sumOf { it.qtyRemaining }
            emitMessage("تم نقل الأصناف المتبقية ($totalRemainingPieces قطعة لـ ${remainingOrders.size} صنف) لجدول العرض الجماعي لإنشاء طلبية جديدة")
            broadcastDeviceNotification("إعادة طلب المتبقي", "تم نقل $totalRemainingPieces قطعة متبقية لجدول العرض الجماعي")
        }
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

    fun syncToCloud(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val network = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(network)
            val hasInternet = caps != null && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if (!hasInternet) {
                emitMessage("⚠️ تعذر إتمام المزامنة السحابية: لا يوجد اتصال بالإنترنت! يرجى التحقق من اتصال الشبكة وإعادة المحاولة.")
                return@launch
            }

            val email = _googleAccountEmail.value
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val snapshotJson = exportModularData("full")
            prefs.edit()
                .putString("cloud_backup_json", snapshotJson)
                .putString("last_cloud_sync_time", "$time ($email)")
                .apply()
            _lastCloudSyncTime.value = "$time ($email)"
            broadcastDeviceNotification("مزامنة سحابية ناجحة", "تم رفع نسخة احتياطية من قاعدة البيانات إلى الحساب ($email)")
            emitMessage("تمت المزامنة السحابية بنجاح ورفع نسخة البيانات إلى الحساب ($email)")
            onSuccess?.invoke()
        }
    }

    fun restoreFromCloud(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val network = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(network)
            val hasInternet = caps != null && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if (!hasInternet) {
                emitMessage("⚠️ تعذر الاتصال بالسحابة: لا يوجد اتصال بالإنترنت!")
                return@launch
            }

            val savedJson = prefs.getString("cloud_backup_json", null)
            if (savedJson.isNullOrBlank()) {
                emitMessage("⚠️ لا توجد نسخة سحابية سابقة محفوظة لهذا الحساب")
                return@launch
            }
            val result = importModularData(savedJson)
            result.onSuccess { msg ->
                emitMessage("تم استرجاع البيانات السحابية بنجاح: $msg")
                broadcastDeviceNotification("استعادة سحابية", "تم استرجاع البيانات من السحابة بنجاح")
                onSuccess?.invoke()
            }.onFailure {
                emitMessage("تعذر استرجاع النسخة السحابية: ${it.message}")
            }
        }
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

    fun exportModularData(context: Context, moduleType: String) {
        try {
            val json = exportModularData(moduleType)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val fileName = "AutoParts_Backup_${moduleType}_$dateStr.json"
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(json, Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية لقاعدة بيانات قطع غيار ($moduleType)")
                putExtra(Intent.EXTRA_TEXT, "مرفق ملف النسخة الاحتياطية لقاعدة بيانات التطبيق بصيغة JSON قابلة للاستعادة لنقل البيانات لهاتف آخر.")
                type = "application/json"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "مشاركة / نقل ملف النسخة الاحتياطية لهاتف آخر"))
            emitMessage("تم تجهيز ملف النسخة الاحتياطية ($fileName) بنجاح للنقل لهاتف آخر")
            broadcastDeviceNotification("تصدير نسخة احتياطية", "تم تصدير ملف النسخة الاحتياطية $fileName")
        } catch (e: Exception) {
            emitMessage("فشل تصدير ملف النسخة الاحتياطية: ${e.message}")
        }
    }

    fun updateConnectedDeviceName(deviceId: String, newName: String) {
        val currentList = _connectedDevices.value.map { dev ->
            if (dev.deviceId == deviceId) dev.copy(deviceName = newName.trim().ifBlank { dev.deviceName }) else dev
        }
        _connectedDevices.value = currentList
        emitMessage("تم تعديل اسم الجهاز بنجاح إلى: $newName")
        broadcastDeviceNotification("تعديل اسم الجهاز", "تم تغيير اسم الجهاز $deviceId إلى '$newName'")
    }

    fun pairNewDevice(deviceId: String, deviceName: String) {
        val trimmedId = deviceId.trim().ifBlank { "DEV-${(1000..9999).random()}" }
        val trimmedName = deviceName.trim().ifBlank { "جهاز مستودع إضافي" }
        val newDev = com.example.data.model.ConnectedDevice(
            deviceId = trimmedId,
            deviceName = trimmedName,
            linkedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            requireInternet = true,
            allowStockDisbursement = true,
            allowEditParts = false,
            allowEditOrders = true,
            allowManageParties = false
        )
        val currentList = _connectedDevices.value.filter { it.deviceId != trimmedId } + newDev
        _connectedDevices.value = currentList
        emitMessage("✅ تم ربط وتوثيق الجهاز '$trimmedName' بنجاح عبر الباركود")
        broadcastDeviceNotification("ربط جهاز جديد", "تم ربط الجهاز '$trimmedName' عبر الباركود")
    }

    fun broadcastDeviceNotification(title: String, details: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val log = com.example.data.model.DeviceAuditLog(
            id = System.currentTimeMillis(),
            deviceId = if (_isPrimaryDevice.value) "HOST-MAIN" else "CLIENT-DEV",
            deviceName = _appUserName.value,
            actionType = title,
            details = details,
            timestamp = time
        )
        _deviceAuditLogs.value = listOf(log) + _deviceAuditLogs.value.take(49)
    }

    fun importModularData(jsonString: String, merge: Boolean) {
        viewModelScope.launch {
            val result = importModularData(jsonString)
            result.onSuccess {
                emitMessage(if (merge) "تم دمج البيانات بنجاح في قاعدة البيانات" else "تم استبدال واستعادة البيانات بالكامل بنجاح")
                broadcastDeviceNotification("استيراد بيانات", "تم استيراد نسخة بيانات جديدة للتطبيق")
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
                        carName = obj.optString("carName", ""),
                        carModel = obj.optString("carModel", "2022"),
                        purchasePrice = obj.optDouble("purchasePrice", 0.0),
                        price = obj.optDouble("price", 0.0),
                        minStock = obj.optInt("minStock", 0),
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
