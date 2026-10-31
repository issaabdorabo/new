package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_issues")
data class StockIssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partId: Long,
    val quantity: Int,
    val partyId: Long, // FK to parties
    val personName: String, // اسم الشخص المستلم
    val issueDate: String, // yyyy-MM-dd
    val note: String = "",
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
