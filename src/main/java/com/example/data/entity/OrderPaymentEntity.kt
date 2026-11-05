package com.example.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "order_payments")
data class OrderPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val amount: Double,
    val paymentDate: String, // yyyy-MM-dd
    val payerName: String, // اسم الشخص الذي أوصل المبلغ
    val note: String = "",
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val notes: String get() = note
}
