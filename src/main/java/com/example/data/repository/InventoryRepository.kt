package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.dao.VoucherWithItems
import com.example.data.database.AppDatabase
import com.example.data.model.BackupRecord
import com.example.data.model.Beneficiary
import com.example.data.model.Item
import com.example.data.model.ItemStockInfo
import com.example.data.model.Voucher
import com.example.data.model.VoucherItem
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import com.example.data.model.WarehouseItemStock
import com.example.data.model.WarehouseStockSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class InventoryRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val warehouseDao = database.warehouseDao()
    private val itemDao = database.itemDao()
    private val beneficiaryDao = database.beneficiaryDao()
    private val voucherDao = database.voucherDao()
    private val backupDao = database.backupDao()

    val allWarehouses: Flow<List<Warehouse>> = warehouseDao.getAllWarehouses()
    val allItems: Flow<List<Item>> = itemDao.getAllItems()
    val allBeneficiaries: Flow<List<Beneficiary>> = beneficiaryDao.getAllBeneficiaries()
    val allVouchers: Flow<List<VoucherWithItems>> = voucherDao.getAllVouchersWithItems()
    val allBackups: Flow<List<BackupRecord>> = backupDao.getAllBackups()

    // 1. Warehouse Operations
    suspend fun insertWarehouse(warehouse: Warehouse): Long = withContext(Dispatchers.IO) {
        warehouseDao.insertWarehouse(warehouse)
    }

    suspend fun updateWarehouse(warehouse: Warehouse) = withContext(Dispatchers.IO) {
        warehouseDao.updateWarehouse(warehouse)
    }

    suspend fun deleteWarehouse(warehouse: Warehouse) = withContext(Dispatchers.IO) {
        warehouseDao.deleteWarehouse(warehouse)
    }

    suspend fun warehouseHasStock(warehouseId: Long): Boolean = withContext(Dispatchers.IO) {
        val vouchers = voucherDao.getAllVouchersWithItemsList()
        var netStock = 0.0

        for (v in vouchers) {
            val type = v.voucher.voucherType
            val src = v.voucher.sourceWarehouseId
            val dest = v.voucher.destinationWarehouseId

            when (type) {
                VoucherType.INWARD -> {
                    if (dest == warehouseId) {
                        netStock += v.items.sumOf { it.quantity }
                    }
                }
                VoucherType.TRANSFER -> {
                    if (dest == warehouseId) {
                        netStock += v.items.sumOf { it.quantity }
                    }
                    if (src == warehouseId) {
                        netStock -= v.items.sumOf { it.quantity }
                    }
                }
                VoucherType.DISBURSEMENT -> {
                    if (src == warehouseId) {
                        netStock -= v.items.sumOf { it.quantity }
                    }
                }
            }
        }
        netStock > 0.0001
    }

    // 2. Item Operations
    suspend fun insertItem(item: Item): Long = withContext(Dispatchers.IO) {
        itemDao.insertItem(item)
    }

    suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.deleteItem(item)
    }

    suspend fun itemHasTransactions(itemId: Long): Boolean = withContext(Dispatchers.IO) {
        voucherDao.getVoucherItemsCountByItemId(itemId) > 0
    }

    // 3. Beneficiary Operations
    suspend fun insertBeneficiary(beneficiary: Beneficiary): Long = withContext(Dispatchers.IO) {
        beneficiaryDao.insertBeneficiary(beneficiary)
    }

    suspend fun updateBeneficiary(beneficiary: Beneficiary) = withContext(Dispatchers.IO) {
        beneficiaryDao.updateBeneficiary(beneficiary)
    }

    suspend fun deleteBeneficiary(beneficiary: Beneficiary) = withContext(Dispatchers.IO) {
        beneficiaryDao.deleteBeneficiary(beneficiary)
    }

    // 4. Voucher Operations (Inward, Transfer, Disbursement)
    suspend fun saveVoucherWithItems(
        voucher: Voucher,
        items: List<VoucherItem>
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val voucherId = if (voucher.id == 0L) {
                voucherDao.insertVoucher(voucher)
            } else {
                voucherDao.updateVoucher(voucher)
                voucherDao.deleteVoucherItemsByVoucherId(voucher.id)
                voucher.id
            }

            val itemsWithVoucherId = items.map { it.copy(voucherId = voucherId) }
            voucherDao.insertVoucherItems(itemsWithVoucherId)
            voucherId
        }
    }

    suspend fun deleteVoucher(voucherId: Long) = withContext(Dispatchers.IO) {
        database.withTransaction {
            voucherDao.deleteVoucherItemsByVoucherId(voucherId)
            voucherDao.deleteVoucherById(voucherId)
        }
    }

    suspend fun removeVoucherAttachment(voucherId: Long) = withContext(Dispatchers.IO) {
        voucherDao.removeAttachment(voucherId)
    }

    // 5. Stock Calculations & Analytics
    fun getCombinedStockStream(): Flow<StockSnapshot> {
        return combine(
            allWarehouses,
            allItems,
            allVouchers
        ) { warehouses, items, vouchers ->
            calculateStockSnapshot(warehouses, items, vouchers)
        }
    }

    private fun calculateStockSnapshot(
        warehouses: List<Warehouse>,
        items: List<Item>,
        vouchers: List<VoucherWithItems>
    ): StockSnapshot {
        // Map: Pair(warehouseId, itemId) -> stockQuantity
        val warehouseItemStockMap = mutableMapOf<Pair<Long, Long>, Double>()
        // Map: itemId -> totalQuantityAcrossAllWarehouses
        val itemTotalStockMap = mutableMapOf<Long, Double>()

        for (vWithItems in vouchers) {
            val v = vWithItems.voucher
            val type = v.voucherType
            val src = v.sourceWarehouseId
            val dest = v.destinationWarehouseId

            for (vi in vWithItems.items) {
                val qty = vi.quantity
                val itemId = vi.itemId

                when (type) {
                    VoucherType.INWARD -> {
                        if (dest != null) {
                            val key = Pair(dest, itemId)
                            warehouseItemStockMap[key] = (warehouseItemStockMap[key] ?: 0.0) + qty
                            itemTotalStockMap[itemId] = (itemTotalStockMap[itemId] ?: 0.0) + qty
                        }
                    }
                    VoucherType.TRANSFER -> {
                        if (src != null) {
                            val keySrc = Pair(src, itemId)
                            warehouseItemStockMap[keySrc] = (warehouseItemStockMap[keySrc] ?: 0.0) - qty
                        }
                        if (dest != null) {
                            val keyDest = Pair(dest, itemId)
                            warehouseItemStockMap[keyDest] = (warehouseItemStockMap[keyDest] ?: 0.0) + qty
                        }
                        // Total stock across all warehouses unchanged in transfer
                    }
                    VoucherType.DISBURSEMENT -> {
                        if (src != null) {
                            val keySrc = Pair(src, itemId)
                            warehouseItemStockMap[keySrc] = (warehouseItemStockMap[keySrc] ?: 0.0) - qty
                            itemTotalStockMap[itemId] = (itemTotalStockMap[itemId] ?: 0.0) - qty
                        }
                    }
                }
            }
        }

        // Build list of WarehouseStockSummary
        val warehouseSummaries = warehouses.map { w ->
            var count = 0
            var totalQty = 0.0
            var totalVal = 0.0
            items.forEach { item ->
                val stock = warehouseItemStockMap[Pair(w.id, item.id)] ?: 0.0
                if (stock > 0.0001) {
                    count++
                    totalQty += stock
                    totalVal += stock * item.purchasePrice
                }
            }
            WarehouseStockSummary(
                warehouseId = w.id,
                warehouseName = w.name,
                totalItemsCount = count,
                totalQuantity = totalQty,
                totalValue = totalVal
            )
        }

        // Build list of ItemStockInfo
        val itemStockInfos = items.map { item ->
            val stock = itemTotalStockMap[item.id] ?: 0.0
            val isLow = stock <= item.minStockLevel
            ItemStockInfo(
                item = item,
                currentStock = stock,
                totalValue = stock * item.purchasePrice,
                isLowStock = isLow
            )
        }.sortedBy { it.item.name }

        // Build list of WarehouseItemStock
        val warehouseItemStocks = mutableListOf<WarehouseItemStock>()
        warehouses.forEach { w ->
            items.forEach { item ->
                val stock = warehouseItemStockMap[Pair(w.id, item.id)] ?: 0.0
                warehouseItemStocks.add(
                    WarehouseItemStock(
                        warehouseId = w.id,
                        warehouseName = w.name,
                        itemId = item.id,
                        itemName = item.name,
                        itemUnit = item.unit,
                        currentStock = stock,
                        minStockLevel = item.minStockLevel,
                        purchasePrice = item.purchasePrice
                    )
                )
            }
        }

        val lowStockItems = itemStockInfos.filter { it.isLowStock }

        return StockSnapshot(
            warehouses = warehouses,
            items = items,
            vouchers = vouchers,
            warehouseSummaries = warehouseSummaries,
            itemStockInfos = itemStockInfos,
            warehouseItemStocks = warehouseItemStocks,
            lowStockItems = lowStockItems,
            totalInventoryValue = itemStockInfos.sumOf { it.totalValue },
            totalInventoryQuantity = itemStockInfos.sumOf { it.currentStock }
        )
    }

    // 6. Multi-Backup & Restore Engine
    suspend fun createBackup(name: String): BackupRecord = withContext(Dispatchers.IO) {
        val warehouses = warehouseDao.getAllWarehousesList()
        val items = itemDao.getAllItemsList()
        val beneficiaries = beneficiaryDao.getAllBeneficiariesList()
        val vouchers = voucherDao.getAllVouchers()
        val voucherItems = voucherDao.getAllVoucherItems()

        val json = JSONObject()
        json.put("version", 1)
        json.put("timestamp", System.currentTimeMillis())
        json.put("backupName", name)

        val wArr = JSONArray()
        warehouses.forEach { w ->
            wArr.put(JSONObject().apply {
                put("id", w.id)
                put("name", w.name)
                put("location", w.location)
                put("managerName", w.managerName)
                put("phone", w.phone)
                put("notes", w.notes)
                put("createdAt", w.createdAt)
            })
        }
        json.put("warehouses", wArr)

        val iArr = JSONArray()
        items.forEach { i ->
            iArr.put(JSONObject().apply {
                put("id", i.id)
                put("name", i.name)
                put("barcode", i.barcode)
                put("unit", i.unit)
                put("category", i.category)
                put("minStockLevel", i.minStockLevel)
                put("purchasePrice", i.purchasePrice)
                put("salePrice", i.salePrice)
                put("notes", i.notes)
                put("createdAt", i.createdAt)
            })
        }
        json.put("items", iArr)

        val bArr = JSONArray()
        beneficiaries.forEach { b ->
            bArr.put(JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("phone", b.phone)
                put("department", b.department)
                put("notes", b.notes)
                put("createdAt", b.createdAt)
            })
        }
        json.put("beneficiaries", bArr)

        val vArr = JSONArray()
        vouchers.forEach { v ->
            vArr.put(JSONObject().apply {
                put("id", v.id)
                put("voucherNumber", v.voucherNumber)
                put("voucherType", v.voucherType.name)
                put("date", v.date)
                put("sourceWarehouseId", v.sourceWarehouseId ?: JSONObject.NULL)
                put("destinationWarehouseId", v.destinationWarehouseId ?: JSONObject.NULL)
                put("supplierName", v.supplierName)
                put("receiverName", v.receiverName)
                put("beneficiaryId", v.beneficiaryId ?: JSONObject.NULL)
                put("beneficiaryName", v.beneficiaryName)
                put("disburserName", v.disburserName)
                put("notes", v.notes)
                put("attachmentUri", v.attachmentUri ?: JSONObject.NULL)
                put("attachmentType", v.attachmentType ?: JSONObject.NULL)
                put("createdAt", v.createdAt)
            })
        }
        json.put("vouchers", vArr)

        val viArr = JSONArray()
        voucherItems.forEach { vi ->
            viArr.put(JSONObject().apply {
                put("id", vi.id)
                put("voucherId", vi.voucherId)
                put("itemId", vi.itemId)
                put("itemName", vi.itemName)
                put("itemUnit", vi.itemUnit)
                put("quantity", vi.quantity)
                put("unitPrice", vi.unitPrice)
                put("notes", vi.notes)
            })
        }
        json.put("voucherItems", viArr)

        val jsonString = json.toString(2)
        val bytes = jsonString.toByteArray(Charsets.UTF_8)

        val record = BackupRecord(
            name = name,
            timestamp = System.currentTimeMillis(),
            warehouseCount = warehouses.size,
            itemCount = items.size,
            voucherCount = vouchers.size,
            sizeBytes = bytes.size.toLong(),
            backupJson = jsonString
        )
        val id = backupDao.insertBackup(record)
        record.copy(id = id)
    }

    suspend fun restoreBackup(backupJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(backupJson)
            database.withTransaction {
                // Parse Warehouses
                val wArr = json.optJSONArray("warehouses")
                if (wArr != null) {
                    for (i in 0 until wArr.length()) {
                        val obj = wArr.getJSONObject(i)
                        val w = Warehouse(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            location = obj.optString("location", ""),
                            managerName = obj.optString("managerName", ""),
                            phone = obj.optString("phone", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        warehouseDao.insertWarehouse(w)
                    }
                }

                // Parse Items
                val iArr = json.optJSONArray("items")
                if (iArr != null) {
                    for (i in 0 until iArr.length()) {
                        val obj = iArr.getJSONObject(i)
                        val item = Item(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            barcode = obj.optString("barcode", ""),
                            unit = obj.optString("unit", "حبة"),
                            category = obj.optString("category", "عام"),
                            minStockLevel = obj.optDouble("minStockLevel", 5.0),
                            purchasePrice = obj.optDouble("purchasePrice", 0.0),
                            salePrice = obj.optDouble("salePrice", 0.0),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        itemDao.insertItem(item)
                    }
                }

                // Parse Beneficiaries
                val bArr = json.optJSONArray("beneficiaries")
                if (bArr != null) {
                    for (i in 0 until bArr.length()) {
                        val obj = bArr.getJSONObject(i)
                        val b = Beneficiary(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            department = obj.optString("department", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        beneficiaryDao.insertBeneficiary(b)
                    }
                }

                // Parse Vouchers
                val vArr = json.optJSONArray("vouchers")
                if (vArr != null) {
                    for (i in 0 until vArr.length()) {
                        val obj = vArr.getJSONObject(i)
                        val vType = try {
                            VoucherType.valueOf(obj.getString("voucherType"))
                        } catch (e: Exception) {
                            VoucherType.INWARD
                        }
                        val v = Voucher(
                            id = obj.optLong("id", 0L),
                            voucherNumber = obj.optString("voucherNumber", ""),
                            voucherType = vType,
                            date = obj.optLong("date", System.currentTimeMillis()),
                            sourceWarehouseId = if (obj.isNull("sourceWarehouseId")) null else obj.optLong("sourceWarehouseId"),
                            destinationWarehouseId = if (obj.isNull("destinationWarehouseId")) null else obj.optLong("destinationWarehouseId"),
                            supplierName = obj.optString("supplierName", ""),
                            receiverName = obj.optString("receiverName", ""),
                            beneficiaryId = if (obj.isNull("beneficiaryId")) null else obj.optLong("beneficiaryId"),
                            beneficiaryName = obj.optString("beneficiaryName", ""),
                            disburserName = obj.optString("disburserName", ""),
                            notes = obj.optString("notes", ""),
                            attachmentUri = if (obj.isNull("attachmentUri")) null else obj.optString("attachmentUri"),
                            attachmentType = if (obj.isNull("attachmentType")) null else obj.optString("attachmentType"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        voucherDao.insertVoucher(v)
                    }
                }

                // Parse Voucher Items
                val viArr = json.optJSONArray("voucherItems")
                if (viArr != null) {
                    val viList = mutableListOf<VoucherItem>()
                    for (i in 0 until viArr.length()) {
                        val obj = viArr.getJSONObject(i)
                        viList.add(
                            VoucherItem(
                                id = obj.optLong("id", 0L),
                                voucherId = obj.getLong("voucherId"),
                                itemId = obj.getLong("itemId"),
                                itemName = obj.getString("itemName"),
                                itemUnit = obj.optString("itemUnit", "حبة"),
                                quantity = obj.getDouble("quantity"),
                                unitPrice = obj.optDouble("unitPrice", 0.0),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                    if (viList.isNotEmpty()) {
                        voucherDao.insertVoucherItems(viList)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteBackup(backup: BackupRecord) = withContext(Dispatchers.IO) {
        backupDao.deleteBackup(backup)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (warehouseDao.getCount() == 0 && itemDao.getCount() == 0) {
            // Seed sample warehouses
            val wMainId = warehouseDao.insertWarehouse(
                Warehouse(
                    name = "المستودع الرئيسي (صنعاء)",
                    location = "المنطقة المركزية - شارع الستين",
                    managerName = "عيسى عبدربه",
                    phone = "771438740",
                    notes = "المخزن العام لكافة البضائع والمستلزمات"
                )
            )
            val wBranchId = warehouseDao.insertWarehouse(
                Warehouse(
                    name = "مخزن الفرع الثاني (عدن)",
                    location = "المنطقة الصناعية",
                    managerName = "أحمد محمد",
                    phone = "770000001",
                    notes = "مخزن التوزيع الإقليمي"
                )
            )

            // Seed sample items
            val i1 = itemDao.insertItem(
                Item(
                    name = "حاسوب محمول Dell Latitude",
                    barcode = "62910001",
                    unit = "جهاز",
                    category = "أجهزة إلكترونية",
                    minStockLevel = 4.0,
                    purchasePrice = 450.0,
                    salePrice = 520.0
                )
            )
            val i2 = itemDao.insertItem(
                Item(
                    name = "شاشة سامسونج 27 بوصة IPS",
                    barcode = "62910002",
                    unit = "شاشة",
                    category = "أجهزة إلكترونية",
                    minStockLevel = 5.0,
                    purchasePrice = 180.0,
                    salePrice = 220.0
                )
            )
            val i3 = itemDao.insertItem(
                Item(
                    name = "ورق طباعة A4 دبل إيه",
                    barcode = "62910003",
                    unit = "كرتون",
                    category = "قرطاسية ومكتبيات",
                    minStockLevel = 10.0,
                    purchasePrice = 25.0,
                    salePrice = 32.0
                )
            )
            val i4 = itemDao.insertItem(
                Item(
                    name = "طابعة HP Laser Pro متعددة المهام",
                    barcode = "62910004",
                    unit = "طابعة",
                    category = "أجهزة مكتبية",
                    minStockLevel = 3.0,
                    purchasePrice = 310.0,
                    salePrice = 370.0
                )
            )
            val i5 = itemDao.insertItem(
                Item(
                    name = "كابل شبكة Cat6 أصلي 305 متر",
                    barcode = "62910005",
                    unit = "بكرة",
                    category = "شبكات وأسلاك",
                    minStockLevel = 5.0,
                    purchasePrice = 90.0,
                    salePrice = 115.0
                )
            )

            // Seed sample beneficiaries
            val b1 = beneficiaryDao.insertBeneficiary(
                Beneficiary(
                    name = "قسم تقنية المعلومات والبرمجة",
                    department = "الإدارة العامة",
                    phone = "771112233",
                    notes = "مستفيد دائم"
                )
            )
            val b2 = beneficiaryDao.insertBeneficiary(
                Beneficiary(
                    name = "إدارة الموارد البشرية والتدريب",
                    department = "المبنى الإداري",
                    phone = "772223344",
                    notes = "مستلزمات مكتبية"
                )
            )

            // Seed initial inward voucher
            val vInwardId = voucherDao.insertVoucher(
                Voucher(
                    voucherNumber = "IN-2026-001",
                    voucherType = VoucherType.INWARD,
                    date = System.currentTimeMillis() - 86400000L * 3,
                    destinationWarehouseId = wMainId,
                    supplierName = "شركة التكنولوجيا الحديثة للتوريدات",
                    receiverName = "عيسى عبدربه",
                    disburserName = "",
                    notes = "توريد الدفعة الربع سنوية المعتمدة"
                )
            )
            voucherDao.insertVoucherItems(
                listOf(
                    VoucherItem(
                        voucherId = vInwardId,
                        itemId = i1,
                        itemName = "حاسوب محمول Dell Latitude",
                        itemUnit = "جهاز",
                        quantity = 15.0,
                        unitPrice = 450.0
                    ),
                    VoucherItem(
                        voucherId = vInwardId,
                        itemId = i2,
                        itemName = "شاشة سامسونج 27 بوصة IPS",
                        itemUnit = "شاشة",
                        quantity = 20.0,
                        unitPrice = 180.0
                    ),
                    VoucherItem(
                        voucherId = vInwardId,
                        itemId = i3,
                        itemName = "ورق طباعة A4 دبل إيه",
                        itemUnit = "كرتون",
                        quantity = 8.0, // Will be low stock since min is 10
                        unitPrice = 25.0
                    ),
                    VoucherItem(
                        voucherId = vInwardId,
                        itemId = i5,
                        itemName = "كابل شبكة Cat6 أصلي 305 متر",
                        itemUnit = "بكرة",
                        quantity = 12.0,
                        unitPrice = 90.0
                    )
                )
            )

            // Seed a transfer voucher
            val vTransferId = voucherDao.insertVoucher(
                Voucher(
                    voucherNumber = "TR-2026-001",
                    voucherType = VoucherType.TRANSFER,
                    date = System.currentTimeMillis() - 86400000L * 2,
                    sourceWarehouseId = wMainId,
                    destinationWarehouseId = wBranchId,
                    receiverName = "أحمد محمد",
                    disburserName = "عيسى عبدربه",
                    notes = "تحويل تعزيزي لمخزن عدن"
                )
            )
            voucherDao.insertVoucherItems(
                listOf(
                    VoucherItem(
                        voucherId = vTransferId,
                        itemId = i1,
                        itemName = "حاسوب محمول Dell Latitude",
                        itemUnit = "جهاز",
                        quantity = 3.0,
                        unitPrice = 450.0
                    ),
                    VoucherItem(
                        voucherId = vTransferId,
                        itemId = i2,
                        itemName = "شاشة سامسونج 27 بوصة IPS",
                        itemUnit = "شاشة",
                        quantity = 4.0,
                        unitPrice = 180.0
                    )
                )
            )

            // Seed a disbursement voucher to beneficiary
            val vOutId = voucherDao.insertVoucher(
                Voucher(
                    voucherNumber = "OUT-2026-001",
                    voucherType = VoucherType.DISBURSEMENT,
                    date = System.currentTimeMillis() - 86400000L * 1,
                    sourceWarehouseId = wMainId,
                    beneficiaryId = b1,
                    beneficiaryName = "قسم تقنية المعلومات والبرمجة",
                    disburserName = "عيسى عبدربه",
                    receiverName = "م. خالد المهدي",
                    notes = "صرف أجهزة لمهندسي النظم الجدد"
                )
            )
            voucherDao.insertVoucherItems(
                listOf(
                    VoucherItem(
                        voucherId = vOutId,
                        itemId = i1,
                        itemName = "حاسوب محمول Dell Latitude",
                        itemUnit = "جهاز",
                        quantity = 2.0,
                        unitPrice = 450.0
                    ),
                    VoucherItem(
                        voucherId = vOutId,
                        itemId = i2,
                        itemName = "شاشة سامسونج 27 بوصة IPS",
                        itemUnit = "شاشة",
                        quantity = 2.0,
                        unitPrice = 180.0
                    )
                )
            )
        }
    }
}

data class StockSnapshot(
    val warehouses: List<Warehouse>,
    val items: List<Item>,
    val vouchers: List<VoucherWithItems>,
    val warehouseSummaries: List<WarehouseStockSummary>,
    val itemStockInfos: List<ItemStockInfo>,
    val warehouseItemStocks: List<WarehouseItemStock>,
    val lowStockItems: List<ItemStockInfo>,
    val totalInventoryValue: Double,
    val totalInventoryQuantity: Double
)
