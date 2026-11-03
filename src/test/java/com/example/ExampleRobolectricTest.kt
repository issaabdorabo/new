package com.example

import com.example.data.model.DailyMovementItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `test daily movement item calculations`() {
    val item = DailyMovementItem(
      id = "1",
      timestamp = 1000L,
      dateTimeStr = "2026-11-02 12:00",
      movementType = "صرف مخزني",
      partName = "فحمات فرامل",
      carType = "تويوتا",
      quantityChange = -5,
      amount = 500.0,
      userName = "عيسى عبدربه"
    )
    assertEquals("2026-11-02", item.date)
    assertEquals(5, item.quantity)
    assertEquals(100.0, item.unitPrice, 0.01)
    assertEquals(500.0, item.totalPrice, 0.01)
    assertEquals("عيسى عبدربه", item.userName)
  }
}
