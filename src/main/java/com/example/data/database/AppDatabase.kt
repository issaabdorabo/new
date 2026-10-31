package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AutoPartsDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.InventoryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.StockIssueEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.TransactionEntity

@Database(
    entities = [
        PartEntity::class,
        CustomerEntity::class,
        PartyEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        InventoryEntity::class,
        SupplierOrderEntity::class,
        StockIssueEntity::class,
        TransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun autoPartsDao(): AutoPartsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auto_parts_pro.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
