package com.example.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "supplier_receipts")
data class SupplierReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierOrderId: Long,
    val partId: Long,
    val quantity: Int,
    val receiptDate: String, // yyyy-MM-dd
    val receiverName: String, // اسم المستلم
    val note: String = "",
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val qtyReceived: Int get() = quantity
    @get:Ignore
    val receivingDate: String get() = receiptDate
    @get:Ignore
    val notes: String get() = note
}
