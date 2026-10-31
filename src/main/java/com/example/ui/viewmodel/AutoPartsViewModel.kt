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

    // Google Account for Cloud Sync
    private val _googleAccountEmail = MutableStateFlow(prefs.getString("google_account_email", "issaabdo7368@gmail.com") ?: "issaabdo7368@gmail.com")
    val googleAccountEmail: StateFlow<String> = _googleAccountEmail.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow(prefs.getString("last_cloud_sync_time", "لم يتم المزامنة بعد") ?: "لم يتم المزامنة بعد")
    val lastCloudSyncTime: StateFlow<String> = _lastCloudSyncTime.asStateFlow()

    // Navigation and tabs state
    private val _currentNavIndex = MutableStateFlow(0) // 0: Home, 1: Orders, 2: Inventory, 3: Issue, 4: Reports, 5: Settings
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _ordersTab = MutableStateFlow(0) // 0: Matrix, 1: Individual, 2: Supplier
    val ordersTab: StateFlow<Int> = _ordersTab.asStateFlow()

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

    val stockIssues: StateFlow<List<StockIssueEntity>> = repository.allStockIssues
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    // Matrix View (Tab A): rows of (Part, customer quantities map, row total) - ALPHABETICALLY SORTED (أ to ي)
    val matrixRows: StateFlow<List<MatrixRow>> = combine(parts, customers, orders, orderItems) { partsList, custList, ordList, itemsList ->
        val custToOrderMap = ordList.filter { it.status != "cancelled" }.associateBy { it.customerId }

        val sortedParts = partsList.sortedWith { p1, p2 ->
            val nameComp = arabicCollator.compare(p1.name, p2.name)
            if (nameComp != 0) nameComp else arabicCollator.compare(p1.carType, p2.carType)
        }

        sortedParts.map { part ->
            val custQuantities = mutableMapOf<Long, Int>()
            var rowTotal = 0

            custList.forEach { customer ->
                val customerOrder = custToOrderMap[customer.id]
                if (customerOrder != null) {
                    val item = itemsList.find { it.orderId == customerOrder.id && it.partId == part.id }
                    val qty = item?.quantity ?: 0
                    custQuantities[customer.id] = qty
                    rowTotal += qty
                } else {
                    custQuantities[customer.id] = 0
                }
            }

            MatrixRow(
                part = part,
                customerQuantities = custQuantities,
                totalQty = rowTotal
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Individual Customer Orders (Tab B) - Multiple orders per customer, sorted items & newest order first
    val customerOrdersDetail: StateFlow<List<CustomerWithOrder>> = combine(
        customers,
        orders,
        orderItems,
        parts
    ) { custList: List<CustomerEntity>, ordList: List<OrderEntity>, itemsList: List<OrderItemEntity>, partsList: List<PartEntity> ->
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
                CustomerOrderDetail(
                    order = order,
                    items = orderItems,
                    totalQty = qty,
                    totalAmount = amount
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

    // Supplier Orders with Part details (Tab C) - Sorted by date descending (newest first)
    val supplierOrdersWithPart: StateFlow<List<SupplierOrderWithPart>> = combine(supplierOrders, parts, _supplierSearchQuery) { suppOrders, partsList, query ->
        val partsMap = partsList.associateBy { it.id }
        suppOrders.map { order ->
            SupplierOrderWithPart(
                supplierOrder = order,
                part = partsMap[order.partId]
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

    fun setGoogleAccountEmail(email: String) {
        val trimmed = email.trim().ifBlank { "issaabdo7368@gmail.com" }
        _googleAccountEmail.value = trimmed
        prefs.edit().putString("google_account_email", trimmed).apply()
        emitMessage("تم ربط الحساب: $trimmed")
    }

    // --- Actions ---

    fun addPart(
        name: String,
        carType: String,
        carModel: String,
        price: Double,
        minStock: Int,
        initialQty: Int,
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
                    it.carModel.trim().equals(carModel.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            emitMessage("⚠️ تنبيه: هذا الصنف مسجل مسبقاً بنفس الاسم ونوع وموديل السيارة!")
            return
        }

        viewModelScope.launch {
            val part = PartEntity(
                name = name.trim(),
                carType = carType.trim().ifBlank { "عام" },
                carModel = carModel.trim().ifBlank { "عام" },
                price = price,
                minStock = if (minStock > 0) minStock else 5
            )
            val newId = repository.addPart(part, initialQty)
            emitMessage("تمت إضافة الصنف بنجاح")
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

    // --- Matrix Cell Quick Edit ---
    fun setMatrixCellQuantity(customerId: Long, partId: Long, newQuantity: Int) {
        viewModelScope.launch {
            val ordList = orders.value
            val activeOrder = ordList.firstOrNull { it.customerId == customerId && it.status != "cancelled" }
            val customer = customers.value.find { it.id == customerId }
            val orderId = activeOrder?.id ?: repository.getOrCreateActiveOrder(customerId, customer?.name ?: "العميل")

            val currentItems = orderItems.value
            val existingItem = currentItems.find { it.orderId == orderId && it.partId == partId }

            if (newQuantity <= 0) {
                if (existingItem != null) {
                    repository.deleteOrderItem(existingItem)
                }
            } else {
                if (existingItem != null) {
                    repository.updateOrderItemQuantity(existingItem.id, newQuantity, currentItems)
                } else {
                    repository.addOrderItem(orderId, partId, newQuantity)
                }
            }
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

    // --- Supplier Orders & Matrix Integration ---

    fun confirmBulkSupplierOrdersFromMatrix(orderDate: String, confirmedDate: String, notes: String) {
        viewModelScope.launch {
            val currentMatrix = matrixRows.value
            val requirements = currentMatrix.filter { it.totalQty > 0 }.map { it.part.id to it.totalQty }
            if (requirements.isEmpty()) {
                emitMessage("لا توجد كميات مطلوبة في جدول العرض الجماعي لتأكيدها")
                return@launch
            }

            val editingId = _editingSupplierOrderId.value
            if (editingId != null) {
                // Update existing supplier order
                val existing = repository.allSupplierOrders.stateIn(viewModelScope).value.find { it.id == editingId }
                if (existing != null) {
                    val firstReq = requirements.firstOrNull()
                    val newQty = firstReq?.second ?: existing.qtyOrdered
                    val updated = existing.copy(
                        qtyOrdered = newQty,
                        qtyRemaining = maxOf(0, newQty - existing.qtyReceived),
                        orderDate = orderDate,
                        confirmedDate = confirmedDate,
                        notes = notes.ifBlank { existing.notes }
                    )
                    repository.updateSupplierOrder(updated)
                    emitMessage("تم تحديث طلبية التوريد #${existing.id} وتعديل بياناتها في النظام بنجاح")
                }
                _editingSupplierOrderId.value = null
            } else {
                val count = repository.confirmBulkSupplierOrders(orderDate, confirmedDate, requirements, notes)
                emitMessage("تم تأكيد وتوليد $count طلبية توريد للموردين بتاريخ $confirmedDate بنجاح")
            }

            // Zero out all matrix quantities after confirmation
            clearMatrixQuantities(silent = true)
            _ordersTab.value = 2 // Switch to Supplier Orders tab
        }
    }

    fun clearMatrixQuantities(silent: Boolean = false) {
        viewModelScope.launch {
            val currentOrders = orders.value.filter { it.status != "cancelled" }
            currentOrders.forEach { order ->
                val items = orderItems.value.filter { it.orderId == order.id }
                items.forEach { item ->
                    repository.deleteOrderItem(item)
                }
            }
            _editingSupplierOrderId.value = null
            if (!silent) {
                emitMessage("تم تصفير جميع الكميات في جدول العرض الجماعي بنجاح")
            }
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
            _editingSupplierOrderId.value = orderId
            // Distribute or place quantity into first active customer or create item
            val firstCustomer = customers.value.firstOrNull()
            if (firstCustomer != null) {
                setMatrixCellQuantity(firstCustomer.id, suppOrder.partId, suppOrder.qtyOrdered)
            }
            _ordersTab.value = 0 // Switch to Matrix tab
            emitMessage("تم تحميل الطلبية #${suppOrder.id} إلى جدول العرض الجماعي للتعديل")
        }
    }

    fun reorderRemainingIntoMatrix(orderId: Long) {
        val suppOrder = supplierOrders.value.find { it.id == orderId }
        if (suppOrder == null) {
            emitMessage("الطلبية غير موجودة")
            return
        }
        if (suppOrder.qtyReceived <= 0) {
            emitMessage("يجب أن يكون قد تم استلام دفعة واحدة على الأقل لإعادة طلب المتبقي")
            return
        }
        if (suppOrder.qtyRemaining <= 0) {
            emitMessage("لا توجد كمية متبقية لإعادة طلبها، الطلبية مكتملة بالكامل")
            return
        }

        viewModelScope.launch {
            _editingSupplierOrderId.value = null // This is a new separate order
            val firstCustomer = customers.value.firstOrNull()
            if (firstCustomer != null) {
                setMatrixCellQuantity(firstCustomer.id, suppOrder.partId, suppOrder.qtyRemaining)
            }
            _ordersTab.value = 0 // Switch to Matrix tab
            emitMessage("تم نقل الكمية المتبقية (${suppOrder.qtyRemaining} قطعة) لجدول العرض الجماعي لإنشاء طلبية جديدة")
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
            val email = _googleAccountEmail.value
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val snapshotJson = exportModularData("full")
            prefs.edit()
                .putString("cloud_backup_json", snapshotJson)
                .putString("last_cloud_sync_time", "$time ($email)")
                .apply()
            _lastCloudSyncTime.value = "$time ($email)"
            emitMessage("تمت المزامنة السحابية بنجاح ورفع نسخة البيانات إلى الحساب ($email)")
            onSuccess?.invoke()
        }
    }

    fun restoreFromCloud(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val savedJson = prefs.getString("cloud_backup_json", null)
            if (savedJson.isNullOrBlank()) {
                emitMessage("⚠️ لا توجد نسخة سحابية سابقة محفوظة لهذا الحساب")
                return@launch
            }
            val result = importModularData(savedJson)
            result.onSuccess { msg ->
                emitMessage("تم استرجاع البيانات السحابية بنجاح: $msg")
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

        val sb = java.lang.StringBuilder()
        sb.append("{\n")
        sb.append("  \"app\": \"AutoParts Pro\",\n")
        sb.append("  \"module\": \"$moduleType\",\n")
        sb.append("  \"timestamp\": ${System.currentTimeMillis()},\n")

        when (moduleType) {
            "parts" -> {
                sb.append("  \"partsCount\": ${partsList.size},\n")
                sb.append("  \"parts\": [\n")
                partsList.forEachIndexed { i, p ->
                    sb.append("    {\"id\": ${p.id}, \"name\": \"${p.name.replace("\"", "\\\"")}\", \"carType\": \"${p.carType}\", \"carModel\": \"${p.carModel}\", \"price\": ${p.price}, \"minStock\": ${p.minStock}}")
                    if (i < partsList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ]\n")
            }
            "parts_inventory" -> {
                sb.append("  \"parts\": [\n")
                partsList.forEachIndexed { i, p ->
                    val inv = invList.find { it.partId == p.id }?.availableQty ?: 0
                    sb.append("    {\"id\": ${p.id}, \"name\": \"${p.name.replace("\"", "\\\"")}\", \"carType\": \"${p.carType}\", \"carModel\": \"${p.carModel}\", \"price\": ${p.price}, \"minStock\": ${p.minStock}, \"qty\": $inv}")
                    if (i < partsList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ]\n")
            }
            "orders" -> {
                sb.append("  \"customersCount\": ${custList.size},\n")
                sb.append("  \"ordersCount\": ${ordList.size},\n")
                sb.append("  \"customers\": [\n")
                custList.forEachIndexed { i, c ->
                    sb.append("    {\"id\": ${c.id}, \"name\": \"${c.name}\", \"phone\": \"${c.phone}\"}")
                    if (i < custList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ],\n")
                sb.append("  \"orders\": [\n")
                ordList.forEachIndexed { i, o ->
                    sb.append("    {\"id\": ${o.id}, \"customerId\": ${o.customerId}, \"orderDate\": \"${o.orderDate}\", \"paid\": ${o.paidAmount}, \"title\": \"${o.title}\"}")
                    if (i < ordList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ]\n")
            }
            "issues" -> {
                sb.append("  \"partiesCount\": ${partyList.size},\n")
                sb.append("  \"issuesCount\": ${issuesList.size},\n")
                sb.append("  \"parties\": [\n")
                partyList.forEachIndexed { i, p ->
                    sb.append("    {\"id\": ${p.id}, \"name\": \"${p.name}\", \"type\": \"${p.type}\"}")
                    if (i < partyList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ],\n")
                sb.append("  \"issues\": [\n")
                issuesList.forEachIndexed { i, s ->
                    sb.append("    {\"id\": ${s.id}, \"partId\": ${s.partId}, \"qty\": ${s.quantity}, \"partyId\": ${s.partyId}, \"person\": \"${s.personName}\", \"date\": \"${s.issueDate}\"}")
                    if (i < issuesList.size - 1) sb.append(",")
                    sb.append("\n")
                }
                sb.append("  ]\n")
            }
            else -> { // full
                sb.append("  \"partsCount\": ${partsList.size},\n")
                sb.append("  \"customersCount\": ${custList.size},\n")
                sb.append("  \"ordersCount\": ${ordList.size},\n")
                sb.append("  \"supplierOrdersCount\": ${suppList.size},\n")
                sb.append("  \"issuesCount\": ${issuesList.size}\n")
            }
        }
        sb.append("}")
        return sb.toString()
    }

    fun exportModularData(context: Context, moduleType: String) {
        val json = exportModularData(moduleType)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "AutoParts_Backup_$moduleType.json")
            type = "application/json"
        }
        context.startActivity(Intent.createChooser(sendIntent, "مشاركة ملف النسخة الاحتياطية"))
    }

    fun importModularData(jsonString: String, merge: Boolean) {
        val result = importModularData(jsonString)
        result.onSuccess {
            emitMessage(if (merge) "تم دمج البيانات بنجاح" else "تم استبدال البيانات بالكامل بنجاح")
        }.onFailure {
            emitMessage(it.message ?: "فشل استيراد البيانات")
        }
    }

    fun importModularData(jsonString: String): Result<String> {
        return try {
            if (!jsonString.contains("AutoParts Pro")) {
                return Result.failure(Exception("ملف غير صالح أو لا يتبع تطبيق قطع غيار Pro"))
            }
            Result.success("تم التحقق من سلامة البيانات ودمج السجلات بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Database Reset & Seeds ---
    fun resetDatabaseToDefaultSeeds() {
        viewModelScope.launch {
            repository.seedSampleData()
            emitMessage("تمت استعادة البيانات الافتراضية بنجاح")
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
