package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partId: Long,
    val availableQty: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
