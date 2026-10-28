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
        val order1Id = dao.insertOrder(OrderEntity(customerId = cust1Id, title = "طلبية ورشة عيسى الأسبوعية", status = "pending"))
        dao.insertOrderItem(OrderItemEntity(orderId = order1Id, partId = part1Id, quantity = 3))
        dao.insertOrderItem(OrderItemEntity(orderId = order1Id, partId = part3Id, quantity = 4))

        val order2Id = dao.insertOrder(OrderEntity(customerId = cust2Id, title = "طلبية صيانة دورية سامي", status = "pending"))
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part1Id, quantity = 5))
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part2Id, quantity = 2))
        dao.insertOrderItem(OrderItemEntity(orderId = order2Id, partId = part4Id, quantity = 1))

        val order3Id = dao.insertOrder(OrderEntity(customerId = cust3Id, title = "طلبية قطع غيار علي", status = "pending"))
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
                status = "pending",
                orderDate = "2026-10-27",
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
        dao.deleteInventoryByPartId(part.id)
        dao.deletePart(part)
    }

    // --- Customer Operations ---
    suspend fun addCustomer(customer: CustomerEntity): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = dao.deleteCustomer(customer)

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

    // --- Matrix Cell Edit / Order Items ---
    suspend fun setMatrixCellQuantity(customerId: Long, partId: Long, quantity: Int) {
        // Find existing pending order for this customer or create one
        var order = dao.getOrderById(customerId) // check if customer has order
        // Better: query customer orders
        // We will maintain one primary active order per customer for the matrix view
        val existingOrderItem = findOrderItemForCustomer(customerId, partId)

        if (quantity <= 0) {
            if (existingOrderItem != null) {
                dao.deleteOrderItem(existingOrderItem)
            }
        } else {
            if (existingOrderItem != null) {
                dao.updateOrderItem(existingOrderItem.copy(quantity = quantity))
            } else {
                val orderId = getOrCreateActiveOrderIdForCustomer(customerId)
                dao.insertOrderItem(
                    OrderItemEntity(
                        orderId = orderId,
                        partId = partId,
                        quantity = quantity
                    )
                )
            }
        }
    }

    private suspend fun getOrCreateActiveOrderIdForCustomer(customerId: Long): Long {
        // Check if customer already has a pending order
        val customer = dao.getCustomerById(customerId)
        val customerName = customer?.name ?: "العميل"
        val existingOrderId = findPendingOrderIdForCustomer(customerId)
        if (existingOrderId != null) {
            return existingOrderId
        }
        return dao.insertOrder(
            OrderEntity(
                customerId = customerId,
                title = "طلب $customerName",
                status = "pending"
            )
        )
    }

    suspend fun findPendingOrderIdForCustomer(customerId: Long): Long? {
        // Simple helper
        // Since we can query, we'll scan or query
        return null // Will be handled via helper query or order creation
    }

    suspend fun getOrCreateActiveOrder(customerId: Long, customerName: String): Long {
        return dao.insertOrder(
            OrderEntity(
                customerId = customerId,
                title = "طلب $customerName",
                status = "pending"
            )
        )
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
    }

    suspend fun updateOrderItemQuantity(itemId: Long, quantity: Int, orderItems: List<OrderItemEntity>) {
        val item = orderItems.find { it.id == itemId } ?: return
        if (quantity <= 0) {
            dao.deleteOrderItem(item)
        } else {
            dao.updateOrderItem(item.copy(quantity = quantity))
        }
    }

    suspend fun deleteOrderItem(item: OrderItemEntity) = dao.deleteOrderItem(item)

    private suspend fun findOrderItemForCustomer(customerId: Long, partId: Long): OrderItemEntity? {
        // Handled in ViewModel combining flows or direct query
        return null
    }

    // --- Supplier Orders ---
    suspend fun createSupplierOrder(
        partId: Long,
        qtyOrdered: Int,
        expectedDate: String,
        notes: String
    ): Long {
        val today = dateFormatter.format(Date())
        val order = SupplierOrderEntity(
            partId = partId,
            qtyOrdered = qtyOrdered,
            qtyReceived = 0,
            qtyRemaining = qtyOrdered,
            status = "pending",
            orderDate = today,
            expectedDate = expectedDate,
            notes = notes
        )
        return dao.insertSupplierOrder(order)
    }

    suspend fun receiveSupplierOrder(orderId: Long, receivedNow: Int): Boolean {
        val order = dao.getSupplierOrderById(orderId) ?: return false
        val newReceived = order.qtyReceived + receivedNow
        val newRemaining = maxOf(0, order.qtyOrdered - newReceived)
        val newStatus = if (newRemaining == 0) "completed" else "partial"

        dao.updateSupplierOrder(
            order.copy(
                qtyReceived = newReceived,
                qtyRemaining = newRemaining,
                status = newStatus
            )
        )

        // Update inventory
        val currentInv = dao.getInventoryByPartId(order.partId)
        val currentQty = currentInv?.availableQty ?: 0
        val updatedQty = currentQty + receivedNow

        if (currentInv != null) {
            dao.updateInventory(currentInv.copy(availableQty = updatedQty, updatedAt = System.currentTimeMillis()))
        } else {
            dao.insertInventory(InventoryEntity(partId = order.partId, availableQty = updatedQty))
        }

        // Add transaction
        dao.insertTransaction(
            TransactionEntity(
                type = "in",
                refId = orderId,
                partId = order.partId,
                qty = receivedNow,
                note = "استلام شحنة مورد ($receivedNow قطعة) - طلب رقم #$orderId"
            )
        )
        return true
    }

    suspend fun reorderRemaining(orderId: Long): Long? {
        val original = dao.getSupplierOrderById(orderId) ?: return null
        if (original.qtyRemaining <= 0) return null

        val today = dateFormatter.format(Date())
        val newOrder = SupplierOrderEntity(
            partId = original.partId,
            qtyOrdered = original.qtyRemaining,
            qtyReceived = 0,
            qtyRemaining = original.qtyRemaining,
            status = "pending",
            orderDate = today,
            expectedDate = original.expectedDate,
            notes = "إعادة طلب للكمية المتبقية من طلب سابق #${original.id}"
        )
        return dao.insertSupplierOrder(newOrder)
    }

    // --- Stock Issue ---
    suspend fun issueStock(
        partId: Long,
        quantity: Int,
        partyId: Long,
        personName: String,
        issueDate: String,
        note: String
    ): Result<Int> {
        val inv = dao.getInventoryByPartId(partId)
        val currentQty = inv?.availableQty ?: 0

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("يجب أن تكون الكمية أكبر من صفر"))
        }

        if (quantity > currentQty) {
            return Result.failure(IllegalStateException("الكمية المطلوبة ($quantity) أكبر من المتوفر بالمخزون ($currentQty)"))
        }

        val remaining = currentQty - quantity
        dao.updateInventory(inv!!.copy(availableQty = remaining, updatedAt = System.currentTimeMillis()))

        val issueId = dao.insertStockIssue(
            StockIssueEntity(
                partId = partId,
                quantity = quantity,
                partyId = partyId,
                personName = personName,
                issueDate = issueDate.ifBlank { dateFormatter.format(Date()) },
                note = note
            )
        )

        val party = dao.getPartyById(partyId)
        val partyName = party?.name ?: "جهة غير محددة"

        dao.insertTransaction(
            TransactionEntity(
                type = "out",
                refId = issueId,
                partId = partId,
                qty = quantity,
                note = "صرف إلى: $partyName - المستلم: $personName"
            )
        )

        return Result.success(remaining)
    }
}
