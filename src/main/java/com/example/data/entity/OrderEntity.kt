package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val title: String = "",
    val status: String = "pending", // draft, pending, completed, cancelled
    val notes: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0, // الواصل
    val discount: Double = 0.0,
    val orderDate: String = "",
    val isConfirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = maxOf(0.0, (totalAmount - discount) - paidAmount)

    val paymentStatus: String
        get() = when {
            status == "cancelled" -> "ملغي"
            paidAmount <= 0.0 -> "غير مسدد"
            paidAmount >= (totalAmount - discount) && (totalAmount - discount) > 0 -> "مدفوع بالكامل"
            else -> "مسدد جزئياً"
        }
}
