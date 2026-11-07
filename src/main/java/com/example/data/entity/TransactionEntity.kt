package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // in, out, return, adjust
    val refId: Long = 0,
    val partId: Long,
    val qty: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
