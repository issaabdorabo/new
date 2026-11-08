package com.example

import com.example.data.model.Item
import com.example.data.model.VoucherType
import com.example.data.model.Warehouse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTestSuite {

    @Test
    fun testWarehouseModel() {
        val warehouse = Warehouse(
            id = 1L,
            name = "المستودع الرئيسي",
            location = "شارع الستين",
            managerName = "عيسى عبدربه",
            phone = "771438740"
        )
        assertEquals(1L, warehouse.id)
        assertEquals("المستودع الرئيسي", warehouse.name)
        assertEquals("عيسى عبدربه", warehouse.managerName)
    }

    @Test
    fun testItemModel() {
        val item = Item(
            id = 10L,
            name = "حاسوب محمول",
            barcode = "62910001",
            unit = "جهاز",
            minStockLevel = 4.0,
            purchasePrice = 500.0
        )
        assertEquals(10L, item.id)
        assertEquals("حاسوب محمول", item.name)
        assertEquals(4.0, item.minStockLevel, 0.001)
    }

    @Test
    fun testVoucherTypeTitles() {
        assertEquals("توريد وارد", VoucherType.INWARD.titleAr)
        assertEquals("صرف وتحويل لمخزن", VoucherType.TRANSFER.titleAr)
        assertEquals("صرف لمستفيد", VoucherType.DISBURSEMENT.titleAr)
    }

    @Test
    fun testBackupJsonSerialization() {
        val json = JSONObject()
        json.put("appName", "مخزني الذكي")
        json.put("developer", "عيسى عبدربه 771438740")
        json.put("version", "1.0.0")

        val serialized = json.toString()
        assertTrue(serialized.contains("مخزني الذكي"))
        assertTrue(serialized.contains("771438740"))

        val parsed = JSONObject(serialized)
        assertEquals("مخزني الذكي", parsed.getString("appName"))
        assertEquals("1.0.0", parsed.getString("version"))
    }
}
