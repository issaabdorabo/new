package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface AutoPartsDao {

    // --- Parts ---
    @Query("SELECT * FROM parts ORDER BY id DESC")
    fun getAllParts(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE id = :id")
    suspend fun getPartById(id: Long): PartEntity?

    @Query("SELECT * FROM parts WHERE carType = :carType")
    suspend fun getPartsByCarType(carType: String): List<PartEntity>

    @Query("SELECT COUNT(*) FROM parts")
    suspend fun getPartsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: PartEntity): Long

    @Update
    suspend fun updatePart(part: PartEntity)

    @Delete
    suspend fun deletePart(part: PartEntity)

    @Query("DELETE FROM parts WHERE id = :partId")
    suspend fun deletePartById(partId: Long)

    @Query("DELETE FROM parts WHERE carType = :carType")
    suspend fun deletePartsByCarType(carType: String)

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY id ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // --- Parties ---
    @Query("SELECT * FROM parties ORDER BY id ASC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Delete
    suspend fun deleteParty(party: PartyEntity)

    // --- Inventory ---
    @Query("SELECT * FROM inventory")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE partId = :partId LIMIT 1")
    suspend fun getInventoryByPartId(partId: Long): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: InventoryEntity): Long

    @Update
    suspend fun updateInventory(inventory: InventoryEntity)

    @Query("DELETE FROM inventory WHERE partId = :partId")
    suspend fun deleteInventoryByPartId(partId: Long)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY updatedAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY updatedAt DESC")
    fun getOrdersByCustomerId(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId")
    suspend fun getOrdersListByCustomerId(customerId: Long): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE customerId = :customerId")
    suspend fun deleteOrdersByCustomerId(customerId: Long)

    // --- Order Items ---
    @Query("SELECT * FROM order_items")
    fun getAllOrderItems(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsByOrderId(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsByOrderIdDirect(orderId: Long): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND partId = :partId LIMIT 1")
    suspend fun getOrderItem(orderId: Long, partId: Long): OrderItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItemEntity): Long

    @Update
    suspend fun updateOrderItem(item: OrderItemEntity)

    @Delete
    suspend fun deleteOrderItem(item: OrderItemEntity)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)

    @Query("DELETE FROM order_items WHERE orderId = :orderId AND partId = :partId")
    suspend fun deleteOrderItemByPart(orderId: Long, partId: Long)

    @Query("DELETE FROM order_items WHERE partId = :partId")
    suspend fun deleteOrderItemsByPartId(partId: Long)

    // --- Supplier Orders (Sorted newest first) ---
    @Query("SELECT * FROM supplier_orders ORDER BY orderDate DESC, id DESC")
    fun getAllSupplierOrders(): Flow<List<SupplierOrderEntity>>

    @Query("SELECT * FROM supplier_orders WHERE id = :id")
    suspend fun getSupplierOrderById(id: Long): SupplierOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierOrder(supplierOrder: SupplierOrderEntity): Long

    @Update
    suspend fun updateSupplierOrder(supplierOrder: SupplierOrderEntity)

    @Delete
    suspend fun deleteSupplierOrder(supplierOrder: SupplierOrderEntity)

    @Query("DELETE FROM supplier_orders WHERE id = :id")
    suspend fun deleteSupplierOrderById(id: Long)

    // --- Stock Issues ---
    @Query("SELECT * FROM stock_issues ORDER BY createdAt DESC")
    fun getAllStockIssues(): Flow<List<StockIssueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockIssue(stockIssue: StockIssueEntity): Long

    // --- Transactions (Audit Log) ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    // --- Wipe Database for Clean Reset ---
    @Query("DELETE FROM parts")
    suspend fun clearParts()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM parties")
    suspend fun clearParties()

    @Query("DELETE FROM inventory")
    suspend fun clearInventory()

    @Query("DELETE FROM orders")
    suspend fun clearOrders()

    @Query("DELETE FROM order_items")
    suspend fun clearOrderItems()

    @Query("DELETE FROM supplier_orders")
    suspend fun clearSupplierOrders()

    @Query("DELETE FROM stock_issues")
    suspend fun clearStockIssues()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}
