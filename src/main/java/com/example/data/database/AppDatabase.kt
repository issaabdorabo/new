package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.BackupDao
import com.example.data.dao.BeneficiaryDao
import com.example.data.dao.ItemDao
import com.example.data.dao.VoucherDao
import com.example.data.dao.WarehouseDao
import com.example.data.model.BackupRecord
import com.example.data.model.Beneficiary
import com.example.data.model.Item
import com.example.data.model.Voucher
import com.example.data.model.VoucherItem
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse

class DatabaseConverters {
    @TypeConverter
    fun fromVoucherType(type: VoucherType?): String? = type?.name

    @TypeConverter
    fun toVoucherType(value: String?): VoucherType? {
        return value?.let {
            try {
                VoucherType.valueOf(it)
            } catch (e: Exception) {
                VoucherType.INWARD
            }
        }
    }
}

@Database(
    entities = [
        Warehouse::class,
        Item::class,
        Beneficiary::class,
        Voucher::class,
        VoucherItem::class,
        BackupRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun warehouseDao(): WarehouseDao
    abstract fun itemDao(): ItemDao
    abstract fun beneficiaryDao(): BeneficiaryDao
    abstract fun voucherDao(): VoucherDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_inventory_database.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
