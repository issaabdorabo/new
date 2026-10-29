package com.example.ui.viewmodel

import android.app.Application
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
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.TransactionEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoPartsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AutoPartsRepository(db.autoPartsDao())

    // UI Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Navigation and tabs state
    private val _currentNavIndex = MutableStateFlow(0) // 0: Home, 1: Orders, 2: Inventory, 3: Issue, 4: Reports, 5: Settings
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _ordersTab = MutableStateFlow(0) // 0: Matrix, 1: Individual, 2: Supplier
    val ordersTab: StateFlow<Int> = _ordersTab.asStateFlow()

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

    // Base flows from repository - Using Eagerly sharing so that collections never pause or delay on screen switches
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

    // Combined: Parts with real-time Inventory
    val partsWithInventory: StateFlow<List<PartWithInventory>> = combine(parts, inventory) { partsList, invList ->
        val invMap = invList.associateBy { it.partId }
        partsList.map { part ->
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

    // Matrix View (Tab A): rows of (Part, customer quantities map, row total)
    val matrixRows: StateFlow<List<MatrixRow>> = combine(parts, customers, orders, orderItems) { partsList, custList, ordList, itemsList ->
        val custToOrderMap = ordList.filter { it.status != "cancelled" }.associateBy { it.customerId }

        partsList.map { part ->
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

    // Individual Customer Orders (Tab B)
    val customerOrdersDetail: StateFlow<List<CustomerWithOrder>> = combine(customers, orders, orderItems, parts) { custList, ordList, itemsList, partsList ->
        val partsMap = partsList.associateBy { it.id }

        custList.map { customer ->
            val activeOrder = ordList.firstOrNull { it.customerId == customer.id && it.status != "cancelled" }
            val itemsForOrder = if (activeOrder != null) {
                itemsList.filter { it.orderId == activeOrder.id }.mapNotNull { item ->
                    val part = partsMap[item.partId]
                    if (part != null) OrderItemWithPart(item, part) else null
                }
            } else {
                emptyList()
            }
            val totalQty = itemsForOrder.sumOf { it.item.quantity }

            CustomerWithOrder(
                customer = customer,
                activeOrder = activeOrder,
                items = itemsForOrder,
                totalQty = totalQty
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

    // App Security PIN state
    private val _isPinProtected = MutableStateFlow(false)
    val isPinProtected: StateFlow<Boolean> = _isPinProtected.asStateFlow()

    private val _appPin = MutableStateFlow("")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Pre-selected part for stock issue navigation
    private val _preselectedPartForIssue = MutableStateFlow<PartWithInventory?>(null)
    val preselectedPartForIssue: StateFlow<PartWithInventory?> = _preselectedPartForIssue.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
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

    // --- Customer Order Operations (Cancel, Edit, Paid) ---

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

    // --- Supplier Orders ---

    fun confirmBulkSupplierOrdersFromMatrix(orderDate: String, confirmedDate: String, notes: String) {
        viewModelScope.launch {
            val currentMatrix = matrixRows.value
            val requirements = currentMatrix.filter { it.totalQty > 0 }.map { it.part.id to it.totalQty }
            if (requirements.isEmpty()) {
                emitMessage("لا توجد كميات مطلوبة في جدول العرض الجماعي لتأكيدها")
                return@launch
            }
            val count = repository.confirmBulkSupplierOrders(orderDate, confirmedDate, requirements, notes)
            emitMessage("تم تأكيد وتوليد $count طلبية توريد للموردين بتاريخ $confirmedDate بنجاح")
            // Switch tab to Supplier Orders
            _ordersTab.value = 2
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

    // --- Stock Issue ---
    fun issueStock(
        partId: Long,
        quantity: Int,
        partyId: Long,
        personName: String,
        issueDate: String,
        note: String,
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
                note = note.trim()
            )
            result.onSuccess { remaining ->
                emitMessage("تم الصرف بنجاح - المتبقي في المخزون: $remaining قطعة")
                onSuccess()
            }.onFailure { err ->
                emitMessage(err.message ?: "فشلت عملية الصرف")
            }
        }
    }

    // --- Database Reset & Seeds ---
    fun resetDatabaseToDefaultSeeds() {
        viewModelScope.launch {
            repository.seedSampleData()
            emitMessage("تمت استعادة البيانات الافتراضية بنجاح")
        }
    }

    // --- App PIN Lock ---
    fun setAppPin(pin: String) {
        if (pin.length in 4..6) {
            _appPin.value = pin
            _isPinProtected.value = true
            emitMessage("تم تعيين رمز الحماية (PIN) بنجاح")
        } else {
            emitMessage("رمز PIN يجب أن يتكون من 4 إلى 6 أرقام")
        }
    }

    fun disablePinProtection() {
        _isPinProtected.value = false
        _appPin.value = ""
        _isAppLocked.value = false
        emitMessage("تم إلغاء قفل الحماية")
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _appPin.value) {
            _isAppLocked.value = false
            return true
        } else {
            emitMessage("رمز PIN غير صحيح!")
            return false
        }
    }

    fun lockApp() {
        if (_isPinProtected.value) {
            _isAppLocked.value = true
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
