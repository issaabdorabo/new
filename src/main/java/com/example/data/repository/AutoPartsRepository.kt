package com.example.data.repository

import com.example.data.dao.AutoPartsDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.InventoryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.StockIssueEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoPartsRepository(private val dao: AutoPartsDao) {

    val allParts: Flow<List<PartEntity>> = dao.getAllParts()
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val allParties: Flow<List<PartyEntity>> = dao.getAllParties()
    val allInventory: Flow<List<InventoryEntity>> = dao.getAllInventory()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allOrderItems: Flow<List<OrderItemEntity>> = dao.getAllOrderItems()
    val allSupplierOrders: Flow<List<SupplierOrderEntity>> = dao.getAllSupplierOrders()
    val allStockIssues: Flow<List<StockIssueEntity>> = dao.getAllStockIssues()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun checkAndSeedDatabase() {
        if (dao.getPartsCount() == 0) {
            seedSampleData()
        }
    }

    suspend fun seedSampleData() {
        // Clear everything first
        dao.clearOrderItems()
        dao.clearOrders()
        dao.clearStockIssues()
        dao.clearSupplierOrders()
        dao.clearTransactions()
        dao.clearInventory()
        dao.clearParts()
        dao.clearCustomers()
        dao.clearParties()

        val today = dateFormatter.format(Date())

        // 1. Seed Parts
        val part1Id = dao.insertPart(
            PartEntity(name = "فلتر زيت محرك أصلي", carType = "تويوتا", carModel = "لاندكروزر / كامري 2020-2024", price = 45.0, minStock = 10)
        )
        val part2Id = dao.insertPart(
            PartEntity(name = "تيل فرامل أمامي سيراميك", carType = "هونداي", carModel = "إلنترا / سوناتا 2022", price = 180.0, minStock = 6)
        )
        val part3Id = dao.insertPart(
            PartEntity(name = "بواجي إيريديوم ليزر (طقم)", carType = "نيسان", carModel = "صني / التيما 2021", price = 120.0, minStock = 8)
        )
        val part4Id = dao.insertPart(
            PartEntity(name = "فلتر هواء محرك عالي التدفق", carType = "كيا", carModel = "سبورتاج / سيراتو 2023", price = 65.0, minStock = 5)
        )
        val part5Id = dao.insertPart(
            PartEntity(name = "سير ماكينة دينمو أصلي", carType = "فورد", carModel = "تورس / إكسبلورر 2019", price = 95.0, minStock = 4)
        )

        // 2. Seed Inventory
        dao.insertInventory(InventoryEntity(partId = part1Id, availableQty = 25))
        dao.insertInventory(InventoryEntity(partId = part2Id, availableQty = 4)) // Low stock!
        dao.insertInventory(InventoryEntity(partId = part3Id, availableQty = 18))
        dao.insertInventory(InventoryEntity(partId = part4Id, availableQty = 2)) // Critical low stock!
        dao.insertInventory(InventoryEntity(partId = part5Id, availableQty = 12))

        // 3. Seed Customers
        val cust1Id = dao.insertCustomer(CustomerEntity(name = "عيسى", phone = "0501234567", notes = "عميل مميز - ورشة المحركات"))
        val cust2Id = dao.insertCustomer(CustomerEntity(name = "سامي", phone = "0559876543", notes = "مركز صيانة السيارات المتكامل"))
        val cust3Id = dao.insertCustomer(CustomerEntity(name = "علي", phone = "0541122334", notes = "معرض سيارات الساحل"))

        // 4. Seed Parties (الجهات المصروفة لها)
        val party1Id = dao.insertParty(PartyEntity(name = "ورشة النجم الذهبي", type = "ورشة", phone = "0533344556", notes = "الرياض - الصناعية القديمة"))
        val party2Id = dao.insertParty(PartyEntity(name = "شركة الأفق للنقل", type = "شركة", phone = "0567788990", notes = "إدارة أسطول الحافلات"))
        val party3Id = dao.insertParty(PartyEntity(name = "معرض الرواد الحديث", type = "معرض", phone = "0509988776", notes = "جدة - طريق المدينة"))

        // 5. Seed Orders for the customers
        val order1Id = dao.insertOrder(
            OrderEntity(
                customerId = cust1Id,
                title = "طلبية ورشة عيسى الأسبوعية",
                status = "pending",
                totalAmount = 615.0,
                paidAmount = 300.0,
                orderDate = today,
                isConfirmed = true
            )
        )
        dao.insertOrderItem(OrderItemEntity(orderId = order1Id, partId = part1Id, quantity = 3))
        dao.insertOrderItem(OrderItemEntity(orderId = order1Id, partId = part3Id, quantity = 4))

        val order2Id = dao.insertOrder(
            OrderEntity(
                customerId = cust2Id,
                title = "طلبية صيانة دورية سامي",
                status = "pending",
                totalAmount = 650.0,
                paidAmount = 650.0,
                orderDate = today,
                isConfirmed = true
            )
        )
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part1Id, quantity = 5))
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part2Id, quantity = 2))
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part4Id, quantity = 1))

        val order3Id = dao.insertOrder(
            OrderEntity(
                customerId = cust3Id,
                title = "طلبية قطع غيار علي",
                status = "pending",
                totalAmount = 465.0,
                paidAmount = 0.0,
                orderDate = today,
                isConfirmed = true
            )
        )
        dao.insertOrderItem(OrderItemEntity(orderId = order3Id, partId = part2Id, quantity = 1))
        dao.insertOrderItem(OrderItemEntity(orderId = order3Id, partId = part5Id, quantity = 3))

        // 6. Seed Supplier Orders
        dao.insertSupplierOrder(
            SupplierOrderEntity(
                partId = part2Id,
                qtyOrdered = 20,
                qtyReceived = 5,
                qtyRemaining = 15,
                status = "partial",
                orderDate = "2026-10-25",
                confirmedDate = "2026-10-25",
                expectedDate = "2026-10-30",
                notes = "توريد شحنة تيل فرامل هونداي عاجلة"
            )
        )
        dao.insertSupplierOrder(
            SupplierOrderEntity(
                partId = part4Id,
                qtyOrdered = 15,
                qtyReceived = 0,
                qtyRemaining = 15,
                status = "confirmed",
                orderDate = "2026-10-27",
                confirmedDate = "2026-10-27",
                expectedDate = "2026-11-02",
                notes = "طلب فلتر هواء كيا لتعويض النقص"
            )
        )

        // 7. Seed Stock Issue & Transaction
        dao.insertStockIssue(
            StockIssueEntity(
                partId = part1Id,
                quantity = 2,
                partyId = party1Id,
                personName = "أحمد محمد",
                issueDate = today,
                note = "صيانة عاجلة لسيارة لاندكروزر"
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                type = "out",
                partId = part1Id,
                qty = 2,
                timestamp = System.currentTimeMillis() - 3600000,
                note = "صرف إلى ورشة النجم الذهبي (أحمد محمد)"
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                type = "in",
                partId = part2Id,
                qty = 5,
                timestamp = System.currentTimeMillis() - 7200000,
                note = "وصول دفعة جزئية من المورد"
            )
        )
    }

    // --- Part Operations ---
    suspend fun addPart(part: PartEntity, initialQty: Int = 0): Long {
        val partId = dao.insertPart(part)
        dao.insertInventory(InventoryEntity(partId = partId, availableQty = initialQty))
        if (initialQty > 0) {
            dao.insertTransaction(
                TransactionEntity(
                    type = "in",
                    partId = partId,
                    qty = initialQty,
                    note = "مخزون افتتاحي للصنف: ${part.name}"
                )
            )
        }
        return partId
    }

    suspend fun updatePart(part: PartEntity) = dao.updatePart(part)

    suspend fun deletePart(part: PartEntity) {
        dao.deleteOrderItemsByPartId(part.id)
        dao.deleteInventoryByPartId(part.id)
        dao.deletePart(part)
        dao.insertTransaction(
            TransactionEntity(
                type = "delete",
                partId = part.id,
                qty = 0,
                note = "تم حذف الصنف: ${part.name}"
            )
        )
    }

    suspend fun deleteCarType(carType: String): Int {
        val partsToDelete = dao.getPartsByCarType(carType)
        partsToDelete.forEach { part ->
            dao.deleteOrderItemsByPartId(part.id)
            dao.deleteInventoryByPartId(part.id)
            dao.deletePart(part)
        }
        return partsToDelete.size
    }

    // --- Customer Operations ---
    suspend fun addCustomer(customer: CustomerEntity): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) {
        val customerOrders = dao.getOrdersListByCustomerId(customer.id)
        customerOrders.forEach { order ->
            dao.deleteOrderItemsByOrderId(order.id)
        }
        dao.deleteOrdersByCustomerId(customer.id)
        dao.deleteCustomer(customer)
    }

    // --- Party Operations ---
    suspend fun addParty(party: PartyEntity): Long = dao.insertParty(party)
    suspend fun updateParty(party: PartyEntity) = dao.updateParty(party)
    suspend fun deleteParty(party: PartyEntity) = dao.deleteParty(party)

    // --- Inventory Operations ---
    suspend fun adjustInventory(partId: Long, newQty: Int, note: String) {
        val current = dao.getInventoryByPartId(partId)
        val oldQty = current?.availableQty ?: 0
        if (current != null) {
            dao.updateInventory(current.copy(availableQty = newQty, updatedAt = System.currentTimeMillis()))
        } else {
            dao.insertInventory(InventoryEntity(partId = partId, availableQty = newQty))
        }
        val diff = newQty - oldQty
        dao.insertTransaction(
            TransactionEntity(
                type = "adjust",
                partId = partId,
                qty = diff,
                note = note.ifBlank { "تعديل يدوي للمخزون من $oldQty إلى $newQty" }
            )
        )
    }

    // --- Orders Management (Cancel, Edit, Paid) ---

    suspend fun getOrCreateActiveOrder(customerId: Long, customerName: String): Long {
        val existingOrders = dao.getOrdersListByCustomerId(customerId)
        val active = existingOrders.firstOrNull { it.status != "cancelled" && it.status != "completed" }
        if (active != null) {
            return active.id
        }
        val today = dateFormatter.format(Date())
        return dao.insertOrder(
            OrderEntity(
                customerId = customerId,
                title = "طلب $customerName",
                status = "pending",
                orderDate = today,
                isConfirmed = true
            )
        )
    }

    suspend fun cancelOrder(orderId: Long, returnStock: Boolean): Boolean {
        val order = dao.getOrderById(orderId) ?: return false
        val items = dao.getOrderItemsByOrderIdDirect(orderId)

        if (returnStock) {
            // Return quantities back to inventory
            items.forEach { item ->
                val inv = dao.getInventoryByPartId(item.partId)
                if (inv != null) {
                    val updatedQty = inv.availableQty + item.quantity
                    dao.updateInventory(inv.copy(availableQty = updatedQty, updatedAt = System.currentTimeMillis()))
                    dao.insertTransaction(
                        TransactionEntity(
                            type = "in",
                            partId = item.partId,
                            qty = item.quantity,
                            note = "إعادة مخزون بعد إلغاء الطلب #${order.id}"
                        )
                    )
                }
            }
        }

        dao.updateOrder(
            order.copy(
                status = "cancelled",
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun updateOrderPayment(orderId: Long, paidAmount: Double, discount: Double): Boolean {
        val order = dao.getOrderById(orderId) ?: return false
        dao.updateOrder(
            order.copy(
                paidAmount = paidAmount,
                discount = discount,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                type = "payment",
                partId = 0,
                qty = 0,
                note = "تعديل الواصل للطلب #${order.id}: الواصل $paidAmount ر.س، الخصم $discount ر.س"
            )
        )
        return true
    }

    suspend fun updateOrderDetails(orderId: Long, title: String, notes: String, orderDate: String): Boolean {
        val order = dao.getOrderById(orderId) ?: return false
        dao.updateOrder(
            order.copy(
                title = title.trim(),
                notes = notes.trim(),
                orderDate = orderDate.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun addOrderItem(orderId: Long, partId: Long, quantity: Int) {
        val existing = dao.getOrderItem(orderId, partId)
        if (existing != null) {
            dao.updateOrderItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            dao.insertOrderItem(
                OrderItemEntity(
                    orderId = orderId,
                    partId = partId,
                    quantity = quantity
                )
            )
        }
        recalculateOrderTotal(orderId)
    }

    suspend fun updateOrderItemQuantity(itemId: Long, newQuantity: Int, currentItems: List<OrderItemEntity>) {
        val target = currentItems.find { it.id == itemId } ?: return
        if (newQuantity <= 0) {
            dao.deleteOrderItem(target)
        } else {
            dao.updateOrderItem(target.copy(quantity = newQuantity))
        }
        recalculateOrderTotal(target.orderId)
    }

    suspend fun deleteOrderItem(item: OrderItemEntity) {
        dao.deleteOrderItem(item)
        recalculateOrderTotal(item.orderId)
    }

    suspend fun recalculateOrderTotal(orderId: Long) {
        val order = dao.getOrderById(orderId) ?: return
        val items = dao.getOrderItemsByOrderIdDirect(orderId)
        var total = 0.0
        for (item in items) {
            val part = dao.getPartById(item.partId)
            if (part != null) {
                total += part.price * item.quantity
            }
        }
        dao.updateOrder(order.copy(totalAmount = total, updatedAt = System.currentTimeMillis()))
    }

    // --- Supplier Orders Operations ---

    suspend fun createSupplierOrder(
        partId: Long,
        quantity: Int,
        expectedDate: String,
        notes: String,
        orderDate: String = dateFormatter.format(Date()),
        confirmedDate: String = dateFormatter.format(Date())
    ): Long {
        return dao.insertSupplierOrder(
            SupplierOrderEntity(
                partId = partId,
                qtyOrdered = quantity,
                qtyReceived = 0,
                qtyRemaining = quantity,
                status = "confirmed",
                orderDate = orderDate,
                confirmedDate = confirmedDate,
                expectedDate = expectedDate,
                notes = notes
            )
        )
    }

    suspend fun confirmBulkSupplierOrders(
        orderDate: String,
        confirmedDate: String,
        partRequirements: List<Pair<Long, Int>>,
        notes: String
    ): Int {
        var createdCount = 0
        partRequirements.forEach { (partId, totalNeeded) ->
            if (totalNeeded > 0) {
                dao.insertSupplierOrder(
                    SupplierOrderEntity(
                        partId = partId,
                        qtyOrdered = totalNeeded,
                        qtyReceived = 0,
                        qtyRemaining = totalNeeded,
                        status = "confirmed",
                        orderDate = orderDate.ifBlank { dateFormatter.format(Date()) },
                        confirmedDate = confirmedDate.ifBlank { dateFormatter.format(Date()) },
                        expectedDate = "",
                        notes = notes.ifBlank { "طلبية توريد جماعية مؤكدة من جدول الطلبات" }
                    )
                )
                createdCount++
            }
        }
        return createdCount
    }

    suspend fun updateSupplierOrder(order: SupplierOrderEntity) {
        dao.updateSupplierOrder(order)
    }

    suspend fun cancelSupplierOrder(orderId: Long) {
        val order = dao.getSupplierOrderById(orderId) ?: return
        dao.updateSupplierOrder(order.copy(status = "cancelled"))
    }

    suspend fun receiveSupplierOrder(orderId: Long, qtyReceivedNow: Int): Boolean {
        val order = dao.getSupplierOrderById(orderId) ?: return false
        val newReceived = order.qtyReceived + qtyReceivedNow
        val newRemaining = maxOf(0, order.qtyOrdered - newReceived)
        val newStatus = if (newRemaining == 0) "completed" else "partial"

        dao.updateSupplierOrder(
            order.copy(
                qtyReceived = newReceived,
                qtyRemaining = newRemaining,
                status = newStatus
            )
        )

        // Increment inventory
        val currentInv = dao.getInventoryByPartId(order.partId)
        if (currentInv != null) {
            dao.updateInventory(
                currentInv.copy(
                    availableQty = currentInv.availableQty + qtyReceivedNow,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertInventory(
                InventoryEntity(
                    partId = order.partId,
                    availableQty = qtyReceivedNow
                )
            )
        }

        // Audit transaction
        val part = dao.getPartById(order.partId)
        dao.insertTransaction(
            TransactionEntity(
                type = "in",
                partId = order.partId,
                qty = qtyReceivedNow,
                note = "استلام شحنة مورد للصنف ${part?.name ?: ""}: +$qtyReceivedNow قطعة (متبقي: $newRemaining)"
            )
        )
        return true
    }

    suspend fun reorderRemaining(orderId: Long): Long? {
        val order = dao.getSupplierOrderById(orderId) ?: return null
        if (order.qtyRemaining <= 0) return null

        val today = dateFormatter.format(Date())
        val newOrderId = dao.insertSupplierOrder(
            SupplierOrderEntity(
                partId = order.partId,
                qtyOrdered = order.qtyRemaining,
                qtyReceived = 0,
                qtyRemaining = order.qtyRemaining,
                status = "confirmed",
                orderDate = today,
                confirmedDate = today,
                notes = "إعادة طلب الكمية المتبقية من طلب #${order.id}"
            )
        )

        dao.updateSupplierOrder(order.copy(status = "completed", qtyRemaining = 0))
        return newOrderId
    }

    suspend fun deleteSupplierOrder(orderId: Long) {
        dao.deleteSupplierOrderById(orderId)
    }

    suspend fun deleteCustomerOrder(orderId: Long, returnStock: Boolean) {
        if (returnStock) {
            val items = dao.getOrderItemsByOrderIdDirect(orderId)
            items.forEach { item ->
                val inv = dao.getInventoryByPartId(item.partId)
                if (inv != null) {
                    val updatedQty = inv.availableQty + item.quantity
                    dao.updateInventory(inv.copy(availableQty = updatedQty, updatedAt = System.currentTimeMillis()))
                    dao.insertTransaction(
                        TransactionEntity(
                            type = "in",
                            partId = item.partId,
                            qty = item.quantity,
                            note = "إعادة مخزون بعد حذف الطلب #$orderId"
                        )
                    )
                }
            }
        }
        dao.deleteOrderItemsByOrderId(orderId)
        val order = dao.getOrderById(orderId)
        if (order != null) {
            dao.deleteOrder(order)
        }
    }

    // --- Stock Issue Operations ---

    suspend fun issueStock(
        partId: Long,
        quantity: Int,
        partyId: Long,
        personName: String,
        issueDate: String,
        note: String,
        attachmentUri: String? = null
    ): Result<Int> {
        val inventory = dao.getInventoryByPartId(partId)
        val available = inventory?.availableQty ?: 0

        if (available < quantity) {
            return Result.failure(Exception("الكمية المطلوبة ($quantity) أكبر من المتوفر في المخزون ($available)"))
        }

        val newQty = available - quantity
        dao.updateInventory(
            inventory!!.copy(
                availableQty = newQty,
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertStockIssue(
            StockIssueEntity(
                partId = partId,
                quantity = quantity,
                partyId = partyId,
                personName = personName,
                issueDate = issueDate,
                note = note,
                attachmentUri = attachmentUri
            )
        )

        val party = dao.getPartyById(partyId)
        val part = dao.getPartById(partId)
        dao.insertTransaction(
            TransactionEntity(
                type = "out",
                partId = partId,
                qty = quantity,
                note = "صرف ${part?.name ?: ""}: $quantity قطعة إلى ${party?.name ?: ""} (المستلم: $personName)"
            )
        )

        return Result.success(newQty)
    }

    suspend fun editStockIssue(
        issueId: Long,
        newPartId: Long,
        newQuantity: Int,
        newPartyId: Long,
        newPersonName: String,
        newDate: String,
        newNote: String,
        newAttachmentUri: String?
    ): Result<Boolean> {
        val issue = dao.getStockIssueById(issueId) ?: return Result.failure(Exception("أمر الصرف غير موجود"))

        // Revert old inventory
        val oldInv = dao.getInventoryByPartId(issue.partId)
        if (oldInv != null) {
            dao.updateInventory(oldInv.copy(availableQty = oldInv.availableQty + issue.quantity, updatedAt = System.currentTimeMillis()))
        }

        // Check new inventory availability
        val currentTargetInv = dao.getInventoryByPartId(newPartId)
        val available = currentTargetInv?.availableQty ?: 0
        if (available < newQuantity) {
            // Rollback old inventory restoration
            if (oldInv != null) {
                dao.updateInventory(oldInv.copy(availableQty = oldInv.availableQty, updatedAt = System.currentTimeMillis()))
            }
            return Result.failure(Exception("الكمية الجديدة ($newQuantity) غير متوفرة في رصيد الصنف الحالي ($available)"))
        }

        // Deduct new inventory
        dao.updateInventory(currentTargetInv!!.copy(availableQty = available - newQuantity, updatedAt = System.currentTimeMillis()))

        dao.updateStockIssue(
            issue.copy(
                partId = newPartId,
                quantity = newQuantity,
                partyId = newPartyId,
                personName = newPersonName,
                issueDate = newDate,
                note = newNote,
                attachmentUri = newAttachmentUri ?: issue.attachmentUri
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                type = "adjust",
                partId = newPartId,
                qty = newQuantity,
                note = "تعديل أمر صرف #$issueId (الكمية الجديدة: $newQuantity)"
            )
        )

        return Result.success(true)
    }

    suspend fun deleteStockIssue(issueId: Long): Result<Boolean> {
        val issue = dao.getStockIssueById(issueId) ?: return Result.failure(Exception("أمر الصرف غير موجود"))

        // Restore quantity to inventory
        val inv = dao.getInventoryByPartId(issue.partId)
        if (inv != null) {
            dao.updateInventory(inv.copy(availableQty = inv.availableQty + issue.quantity, updatedAt = System.currentTimeMillis()))
        }

        val part = dao.getPartById(issue.partId)
        dao.deleteStockIssue(issue)

        dao.insertTransaction(
            TransactionEntity(
                type = "in",
                partId = issue.partId,
                qty = issue.quantity,
                note = "إلغاء واسترجاع أمر صرف #$issueId للصنف ${part?.name ?: ""}: +${issue.quantity} للمخزون"
            )
        )

        return Result.success(true)
    }

    // --- Supplier Delivery Management & Attachments ---

    suspend fun editSupplierOrderReceived(
        orderId: Long,
        newReceivedQty: Int,
        receivingDate: String,
        receivingNote: String
    ): Boolean {
        val order = dao.getSupplierOrderById(orderId) ?: return false
        val diff = newReceivedQty - order.qtyReceived

        val inventory = dao.getInventoryByPartId(order.partId)
        if (inventory != null && diff != 0) {
            val updatedQty = maxOf(0, inventory.availableQty + diff)
            dao.updateInventory(inventory.copy(availableQty = updatedQty, updatedAt = System.currentTimeMillis()))
        }

        val newRemaining = maxOf(0, order.qtyOrdered - newReceivedQty)
        val newStatus = when {
            newRemaining == 0 && newReceivedQty > 0 -> "completed"
            newReceivedQty > 0 -> "partial"
            else -> "confirmed"
        }

        val historyEntry = "$receivingDate|$newReceivedQty|$receivingNote"
        val updatedHistory = if (order.receivingHistory.isBlank()) historyEntry else "${order.receivingHistory};$historyEntry"

        dao.updateSupplierOrder(
            order.copy(
                qtyReceived = newReceivedQty,
                qtyRemaining = newRemaining,
                status = newStatus,
                receivingHistory = updatedHistory
            )
        )

        val part = dao.getPartById(order.partId)
        dao.insertTransaction(
            TransactionEntity(
                type = if (diff >= 0) "in" else "out",
                partId = order.partId,
                qty = kotlin.math.abs(diff),
                note = "تعديل وارد طلبية مورد #${order.id} للصنف ${part?.name ?: ""}: الإجمالي الواصل $newReceivedQty (المتبقي: $newRemaining)"
            )
        )
        return true
    }

    suspend fun updateSupplierOrderAttachment(orderId: Long, uri: String?) {
        val order = dao.getSupplierOrderById(orderId) ?: return
        dao.updateSupplierOrder(order.copy(attachmentUri = uri))
    }

    suspend fun updateCustomerOrderAttachment(orderId: Long, uri: String?) {
        val order = dao.getOrderById(orderId) ?: return
        dao.updateOrder(order.copy(attachmentUri = uri))
    }

    suspend fun createNewCustomerOrder(customerId: Long, orderDate: String, title: String): Long {
        return dao.insertOrder(
            OrderEntity(
                customerId = customerId,
                title = title.ifBlank { "طلبية بتاريخ $orderDate" },
                orderDate = orderDate,
                status = "pending",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearMatrixAllItems() {
        val allOrders = dao.getOrdersListByCustomerId(0) // or all orders
        // Clear order items belonging to unconfirmed / current matrix orders
        val orders = dao.getOrderById(0) // placeholder
    }
}
