package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String = "",
    val managerName: String = "",
    val phone: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String = "",
    val unit: String = "حبة",
    val category: String = "عام",
    val minStockLevel: Double = 5.0,
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "beneficiaries")
data class Beneficiary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val department: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class VoucherType(val titleAr: String) {
    INWARD("توريد وارد"),
    TRANSFER("صرف وتحويل لمخزن"),
    DISBURSEMENT("صرف لمستفيد")
}

@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNumber: String,
    val voucherType: VoucherType,
    val date: Long = System.currentTimeMillis(),
    val sourceWarehouseId: Long? = null,
    val destinationWarehouseId: Long? = null,
    val supplierName: String = "",
    val receiverName: String = "",
    val beneficiaryId: Long? = null,
    val beneficiaryName: String = "",
    val disburserName: String = "",
    val notes: String = "",
    val attachmentUri: String? = null,
    val attachmentType: String? = null, // "IMAGE" or "PDF"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "voucher_items",
    indices = [Index(value = ["voucherId"]), Index(value = ["itemId"])]
)
data class VoucherItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherId: Long,
    val itemId: Long,
    val itemName: String,
    val itemUnit: String = "حبة",
    val quantity: Double,
    val unitPrice: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "backups")
data class BackupRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val warehouseCount: Int = 0,
    val itemCount: Int = 0,
    val voucherCount: Int = 0,
    val sizeBytes: Long = 0,
    val backupJson: String = ""
)

data class WarehouseStockSummary(
    val warehouseId: Long,
    val warehouseName: String,
    val totalItemsCount: Int,
    val totalQuantity: Double,
    val totalValue: Double
)

data class ItemStockInfo(
    val item: Item,
    val currentStock: Double,
    val totalValue: Double,
    val isLowStock: Boolean
)

data class WarehouseItemStock(
    val warehouseId: Long,
    val warehouseName: String,
    val itemId: Long,
    val itemName: String,
    val itemUnit: String,
    val currentStock: Double,
    val minStockLevel: Double,
    val purchasePrice: Double
)
