package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplier_orders")
data class SupplierOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partId: Long,
    val qtyOrdered: Int,
    val qtyReceived: Int = 0,
    val qtyRemaining: Int = qtyOrdered,
    val status: String = "pending", // pending, confirmed, partial, completed, cancelled
    val orderDate: String = "",
    val confirmedDate: String = "", // تاريخ تأكيد الطلبية
    val expectedDate: String = "",
    val notes: String = ""
)
