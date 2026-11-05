package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // ورشة / معرض / شركة / فرد / أخرى
    val phone: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
