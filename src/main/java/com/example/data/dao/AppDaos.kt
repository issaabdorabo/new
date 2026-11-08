package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BackupRecord
import com.example.data.model.Beneficiary
import com.example.data.model.Item
import com.example.data.model.Voucher
import com.example.data.model.VoucherItem
import com.example.data.model.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses ORDER BY name ASC")
    fun getAllWarehouses(): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses ORDER BY name ASC")
    suspend fun getAllWarehousesList(): List<Warehouse>

    @Query("SELECT * FROM warehouses WHERE id = :id LIMIT 1")
    suspend fun getWarehouseById(id: Long): Warehouse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse): Long

    @Update
    suspend fun updateWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)

    @Query("SELECT COUNT(*) FROM warehouses")
    suspend fun getCount(): Int
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name COLLATE NOCASE ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllItemsList(): List<Item>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): Item?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int
}

@Dao
interface BeneficiaryDao {
    @Query("SELECT * FROM beneficiaries ORDER BY name COLLATE NOCASE ASC")
    fun getAllBeneficiaries(): Flow<List<Beneficiary>>

    @Query("SELECT * FROM beneficiaries ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllBeneficiariesList(): List<Beneficiary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeneficiary(beneficiary: Beneficiary): Long

    @Update
    suspend fun updateBeneficiary(beneficiary: Beneficiary)

    @Delete
    suspend fun deleteBeneficiary(beneficiary: Beneficiary)
}

data class VoucherWithItems(
    @androidx.room.Embedded val voucher: Voucher,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "voucherId"
    )
    val items: List<VoucherItem>
)

@Dao
interface VoucherDao {
    @Transaction
    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    fun getAllVouchersWithItems(): Flow<List<VoucherWithItems>>

    @Transaction
    @Query("SELECT * FROM vouchers WHERE voucherType = :type ORDER BY date DESC, id DESC")
    fun getVouchersByTypeWithItems(type: String): Flow<List<VoucherWithItems>>

    @Transaction
    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    suspend fun getAllVouchersWithItemsList(): List<VoucherWithItems>

    @Transaction
    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getVoucherById(id: Long): VoucherWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: Voucher): Long

    @Update
    suspend fun updateVoucher(voucher: Voucher)

    @Delete
    suspend fun deleteVoucher(voucher: Voucher)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucherItems(items: List<VoucherItem>)

    @Query("DELETE FROM voucher_items WHERE voucherId = :voucherId")
    suspend fun deleteVoucherItemsByVoucherId(voucherId: Long)

    @Query("SELECT COUNT(*) FROM voucher_items WHERE itemId = :itemId")
    suspend fun getVoucherItemsCountByItemId(itemId: Long): Int

    @Query("SELECT * FROM voucher_items")
    suspend fun getAllVoucherItems(): List<VoucherItem>

    @Query("SELECT * FROM vouchers")
    suspend fun getAllVouchers(): List<Voucher>

    @Query("UPDATE vouchers SET attachmentUri = NULL, attachmentType = NULL WHERE id = :voucherId")
    suspend fun removeAttachment(voucherId: Long)
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecord): Long

    @Delete
    suspend fun deleteBackup(backup: BackupRecord)

    @Query("DELETE FROM backups WHERE id = :id")
    suspend fun deleteBackupById(id: Long)
}
