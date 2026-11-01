package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts")
data class PartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val carType: String,
    val carModel: String,
    val purchasePrice: Double = 0.0,
    val price: Double = 0.0,
    val minStock: Int = 5,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
