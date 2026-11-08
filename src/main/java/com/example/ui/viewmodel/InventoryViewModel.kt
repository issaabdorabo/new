package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.InventoryApp
import com.example.data.dao.VoucherWithItems
import com.example.data.model.BackupRecord
import com.example.data.model.Beneficiary
import com.example.data.model.Item
import com.example.data.model.Voucher
import com.example.data.model.VoucherItem
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import com.example.data.repository.StockSnapshot
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as InventoryApp
    private val repository = app.repository
    private val preferences = app.preferencesManager

    // Preferences & Theme
    val theme: StateFlow<AppTheme> = preferences.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.LIGHT)

    val userName: StateFlow<String> = preferences.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "عيسى عبدربه")

    val orgName: StateFlow<String> = preferences.orgNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "المؤسسة العامة للتوريد والمخازن")

    val googleAccount: StateFlow<String> = preferences.googleAccountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "issanew2026@gmail.com")

    val syncEnabled: StateFlow<Boolean> = preferences.syncEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lastSyncTime: StateFlow<Long> = preferences.lastSyncFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Stock & Inventory Data
    val stockSnapshot: StateFlow<StockSnapshot?> = repository.getCombinedStockStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val warehouses: StateFlow<List<Warehouse>> = repository.allWarehouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<Item>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val beneficiaries: StateFlow<List<Beneficiary>> = repository.allBeneficiaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vouchers: StateFlow<List<VoucherWithItems>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backups: StateFlow<List<BackupRecord>> = repository.allBackups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Messages
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // 1. Warehouse Operations
    fun saveWarehouse(warehouse: Warehouse, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (warehouse.id == 0L) {
                repository.insertWarehouse(warehouse)
                _userMessage.emit("تمت إضافة المخزن بنجاح")
            } else {
                repository.updateWarehouse(warehouse)
                _userMessage.emit("تم تحديث بيانات المخزن بنجاح")
            }
            onDone()
        }
    }

    suspend fun checkWarehouseHasStock(warehouseId: Long): Boolean {
        return repository.warehouseHasStock(warehouseId)
    }

    fun deleteWarehouse(warehouse: Warehouse) {
        viewModelScope.launch {
            repository.deleteWarehouse(warehouse)
            _userMessage.emit("تم حذف المخزن نهائياً")
        }
    }

    // 2. Item Operations
    fun saveItem(item: Item, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
                _userMessage.emit("تمت إضافة الصنف بنجاح")
            } else {
                repository.updateItem(item)
                _userMessage.emit("تم تحديث بيانات الصنف بنجاح")
            }
            onDone()
        }
    }

    suspend fun checkItemHasTransactions(itemId: Long): Boolean {
        return repository.itemHasTransactions(itemId)
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _userMessage.emit("تم حذف الصنف نهائياً")
        }
    }

    // 3. Beneficiary Operations
    fun saveBeneficiary(beneficiary: Beneficiary, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (beneficiary.id == 0L) {
                val newId = repository.insertBeneficiary(beneficiary)
                _userMessage.emit("تمت إضافة المستفيد بنجاح")
                newId
            } else {
                repository.updateBeneficiary(beneficiary)
                _userMessage.emit("تم تحديث بيانات المستفيد")
                beneficiary.id
            }
            onDone(id)
        }
    }

    fun deleteBeneficiary(beneficiary: Beneficiary) {
        viewModelScope.launch {
            repository.deleteBeneficiary(beneficiary)
            _userMessage.emit("تم حذف المستفيد")
        }
    }

    // 4. Voucher Operations (Inward, Transfer, Disbursement)
    fun saveVoucher(
        voucher: Voucher,
        items: List<VoucherItem>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveVoucherWithItems(voucher, items)
            val actionName = when (voucher.voucherType) {
                VoucherType.INWARD -> "التوريد"
                VoucherType.TRANSFER -> "التحويل المخزني"
                VoucherType.DISBURSEMENT -> "أمر الصرف"
            }
            _userMessage.emit("تم حفظ $actionName بنجاح")
            onDone()
        }
    }

    fun deleteVoucher(voucherId: Long) {
        viewModelScope.launch {
            repository.deleteVoucher(voucherId)
            _userMessage.emit("تم حذف السند بنجاح")
        }
    }

    fun removeAttachment(voucherId: Long) {
        viewModelScope.launch {
            repository.removeVoucherAttachment(voucherId)
            _userMessage.emit("تم حذف المرفق بنجاح")
        }
    }

    // 5. Backups & Sync Operations
    fun createBackup(name: String) {
        viewModelScope.launch {
            try {
                repository.createBackup(name)
                _userMessage.emit("تم إنشاء النسخة الاحتياطية بنجاح")
            } catch (e: Exception) {
                _userMessage.emit("تعذر إنشاء النسخة: ${e.localizedMessage}")
            }
        }
    }

    fun restoreBackup(record: BackupRecord) {
        viewModelScope.launch {
            try {
                val ok = repository.restoreBackup(record.backupJson)
                if (ok) {
                    _userMessage.emit("تمت استعادة البيانات بنجاح من النسخة")
                } else {
                    _userMessage.emit("فشلت استعادة البيانات من النسخة")
                }
            } catch (e: Exception) {
                _userMessage.emit("خطأ أثناء الاستعادة: ${e.localizedMessage}")
            }
        }
    }

    fun deleteBackup(record: BackupRecord) {
        viewModelScope.launch {
            repository.deleteBackup(record)
            _userMessage.emit("تم حذف النسخة الاحتياطية")
        }
    }

    fun performManualSync() {
        viewModelScope.launch {
            preferences.updateLastSync()
            _userMessage.emit("تمت المزامنة بنجاح مع حساب جوجل (${googleAccount.value})")
        }
    }

    // 6. Settings Actions
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { preferences.setTheme(theme) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { preferences.setUserName(name) }
    }

    fun setOrgName(name: String) {
        viewModelScope.launch { preferences.setOrgName(name) }
    }

    fun setGoogleAccount(account: String) {
        viewModelScope.launch { preferences.setGoogleAccount(account) }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSyncEnabled(enabled) }
    }
}

class InventoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
