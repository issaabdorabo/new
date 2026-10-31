package com.example.data.model

import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.StockIssueEntity
import com.example.data.entity.SupplierOrderEntity

data class PartWithInventory(
    val part: PartEntity,
    val availableQty: Int
) {
    val isLowStock: Boolean
        get() = availableQty <= part.minStock

    val stockStatus: StockStatus
        get() = when {
            availableQty <= 0 -> StockStatus.OUT_OF_STOCK
            availableQty <= part.minStock -> StockStatus.LOW_STOCK
            availableQty <= part.minStock * 2 -> StockStatus.MEDIUM_STOCK
            else -> StockStatus.GOOD
        }
}

enum class StockStatus {
    OUT_OF_STOCK,
    LOW_STOCK,
    MEDIUM_STOCK,
    GOOD
}

data class OrderItemWithPart(
    val item: OrderItemEntity,
    val part: PartEntity
)

data class CustomerOrderDetail(
    val order: OrderEntity,
    val items: List<OrderItemWithPart>,
    val totalQty: Int,
    val totalAmount: Double
)

data class CustomerWithOrder(
    val customer: CustomerEntity,
    val activeOrder: OrderEntity?,
    val items: List<OrderItemWithPart>,
    val totalQty: Int,
    val allOrders: List<CustomerOrderDetail> = emptyList()
)

data class MatrixRow(
    val part: PartEntity,
    val customerQuantities: Map<Long, Int>, // customerId -> quantity
    val totalQty: Int
)

data class SupplierOrderWithPart(
    val supplierOrder: SupplierOrderEntity,
    val part: PartEntity?
)

data class StockIssueWithDetails(
    val issue: StockIssueEntity,
    val partName: String,
    val carInfo: String,
    val partyName: String,
    val partyType: String
)
