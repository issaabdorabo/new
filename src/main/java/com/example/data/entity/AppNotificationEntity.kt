package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // part, order, payment, supplier, issue, device, sync
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
