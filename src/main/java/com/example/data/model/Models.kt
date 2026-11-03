package com.example.data.model

import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.OrderPaymentEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.StockIssueEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.SupplierReceiptEntity

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
    val totalAmount: Double,
    val payments: List<OrderPaymentEntity> = emptyList()
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

data class CustomerPartRequirement(
    val customerId: Long,
    val customerName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val paidAmount: Double,
    val remainingAmount: Double
) {
    val customerPhone: String = ""
    val quantityRequested: Int get() = quantity
    val totalAmount: Double get() = totalPrice
}

data class SupplierOrderWithPart(
    val supplierOrder: SupplierOrderEntity,
    val part: PartEntity?,
    val receipts: List<SupplierReceiptEntity> = emptyList(),
    val customersWhoOrdered: List<CustomerPartRequirement> = emptyList()
)

data class StockIssueWithDetails(
    val issue: StockIssueEntity,
    val partName: String,
    val carInfo: String,
    val partyName: String,
    val partyType: String
)

data class ConnectedDevice(
    val deviceId: String,
    val deviceName: String,
    val linkedAt: String,
    val requireInternet: Boolean = true, // 1- لا يجري أي عملية إلا متصل بالانترنت
    val allowStockDisbursement: Boolean = true, // 2- عمليات صرف المخزون
    val allowEditParts: Boolean = false, // 3- حذف أو تعديل أصناف أو مخزون
    val allowEditOrders: Boolean = true, // 4- إضافة طلبية أو تعديلها أو حذفها
    val allowManageParties: Boolean = false, // 5- إدارة العملاء أو جهات الصرف
    val isBlocked: Boolean = false
) {
    val status: String get() = if (isBlocked) "محظور" else "متصل ومصرح"
    val allowDisbursement: Boolean get() = allowStockDisbursement
    val allowEditDeleteParts: Boolean get() = allowEditParts
    val allowManageOrders: Boolean get() = allowEditOrders
    val allowManageCustomersParties: Boolean get() = allowManageParties
}

data class DeviceAuditLog(
    val id: Long = 0,
    val deviceId: String,
    val deviceName: String,
    val actionType: String,
    val details: String,
    val timestamp: String,
    val wasOfflineConflict: Boolean = false
)

data class OfflineConflictLog(
    val id: Long = 0,
    val deviceName: String,
    val partName: String,
    val requestedQty: Int,
    val availableQtyAtSync: Int,
    val timestamp: String,
    val reason: String
) {
    val entityType: String get() = "صرف مخزون ($partName)"
    val localChange: String get() = "$deviceName: طلب صرف $requestedQty قطعة"
    val remoteChange: String get() = "المتوفر الفعلي: $availableQtyAtSync قطعة ($reason)"
}

data class PartDisbursementItem(
    val issueId: Long,
    val issueDate: String,
    val quantity: Int,
    val partyName: String,
    val partyType: String,
    val personName: String,
    val note: String
)

data class PartDisbursementHistory(
    val part: PartEntity,
    val availableQty: Int,
    val disbursements: List<PartDisbursementItem>
)

data class DailyMovementItem(
    val id: String,
    val timestamp: Long,
    val dateTimeStr: String,
    val movementType: String, // "وارد توريد", "منصرف مخزون", "طلبية مبيعات", "سند قبض", "تسوية جردية"
    val partName: String,
    val carType: String,
    val barcode: String = "",
    val quantityChange: Int = 0,
    val entityName: String = "", // Party or Customer
    val personName: String = "", // Recipient or Payer
    val amount: Double = 0.0,
    val note: String = "",
    val carModel: String = "",
    val batchOrInvoice: String = ""
) {
    val date: String get() = if (dateTimeStr.length >= 10) dateTimeStr.take(10) else dateTimeStr
    val type: String get() = movementType
    val quantity: Int get() = kotlin.math.abs(quantityChange)
    val unitPrice: Double get() = if (quantity > 0) amount / quantity else amount
    val totalPrice: Double get() = amount
    val notes: String get() = note
}

data class AppNotification(
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // part, order, payment, supplier, issue, device, sync
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (60 * 1000)
        return when {
            minutes < 1 -> "الآن"
            minutes < 60 -> "منذ $minutes دقيقة"
            minutes < 1440 -> "منذ ${minutes / 60} ساعة"
            else -> "منذ ${minutes / 1440} يوم"
        }
    }
}

data class CloudBackupRecord(
    val id: String,
    val timestamp: Long,
    val dateTimeStr: String,
    val email: String,
    val partsCount: Int,
    val ordersCount: Int,
    val issuesCount: Int,
    val jsonData: String
)

