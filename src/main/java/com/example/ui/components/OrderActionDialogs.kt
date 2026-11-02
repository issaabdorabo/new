package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OrderEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.model.MatrixRow
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RoseDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog to cancel customer order with user choice for inventory stock management.
 */
@Composable
fun CancelOrderDialog(
    order: OrderEntity,
    customerName: String,
    onDismiss: () -> Unit,
    onConfirmCancel: (returnStock: Boolean) -> Unit
) {
    var returnStockToInventory by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = RoseDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إلغاء طلب العميل: $customerName", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أنت على وشك إلغاء الطلب (#${order.id}). كيف ترغب في التعامل مع كميات قطع الغيار في المخزن؟",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = returnStockToInventory,
                                onClick = { returnStockToInventory = true }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("إعادة الكميات إلى المخزون (موصى به)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("تحديث رصيد المخزن وزيادته بالكميات التي كانت محجوزة للطلب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = !returnStockToInventory,
                                onClick = { returnStockToInventory = false }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("إبقاء المخزون دون تعديل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("تسجيل إلغاء الطلب فقط دون تغيير رصيد القطع بالمخزن", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(returnStockToInventory) },
                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
            ) {
                Text("تأكيد الإلغاء", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("تراجع")
            }
        }
    )
}

/**
 * Dialog to edit paid amount (تعديل الواصل والخصم) with live preview of remaining debt.
 */
@Composable
fun EditPaidAmountDialog(
    order: OrderEntity,
    customerName: String,
    onDismiss: () -> Unit,
    onConfirm: (paidAmount: Double, discount: Double) -> Unit
) {
    var paidText by remember { mutableStateOf(if (order.paidAmount > 0) order.paidAmount.toString() else "") }
    var discountText by remember { mutableStateOf(if (order.discount > 0) order.discount.toString() else "") }

    val currentTotal = order.totalAmount
    val enteredPaid = paidText.toDoubleOrNull() ?: 0.0
    val enteredDiscount = discountText.toDoubleOrNull() ?: 0.0
    val netTotal = maxOf(0.0, currentTotal - enteredDiscount)
    val remaining = maxOf(0.0, netTotal - enteredPaid)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldSuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل الواصل والمدفوعات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("العميل: $customerName (طلب #${order.id})", fontSize = 13.sp, fontWeight = FontWeight.Medium)

                // Current totals preview card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي قيمة الأصناف:", fontSize = 12.sp)
                            Text("$currentTotal ر.س", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الصافي بعد الخصم:", fontSize = 12.sp)
                            Text("$netTotal ر.س", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المبلغ المتبقي على العميل:", fontSize = 12.sp)
                            Text("$remaining ر.س", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (remaining > 0) RoseDanger else EmeraldSuccess)
                        }
                    }
                }

                OutlinedTextField(
                    value = paidText,
                    onValueChange = { paidText = it },
                    label = { Text("المبلغ الواصل (المدفوع)") },
                    placeholder = { Text("أدخل المبلغ المسدد ر.س") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("الخصم الممنوح (اختياري)") },
                    placeholder = { Text("0.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(enteredPaid, enteredDiscount) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Dialog to edit order details (title, orderDate, notes).
 */
@Composable
fun EditOrderDetailsDialog(
    order: OrderEntity,
    customerName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, orderDate: String) -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var title by remember { mutableStateOf(order.title.ifBlank { "طلب $customerName" }) }
    var orderDate by remember { mutableStateOf(order.orderDate.ifBlank { today }) }
    var notes by remember { mutableStateOf(order.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل بيانات الطلب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان / مسمى الطلب") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = orderDate,
                    onValueChange = { orderDate = it },
                    label = { Text("تاريخ الطلب (YYYY-MM-DD)") },
                    placeholder = { Text(today) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الطلب") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, notes, orderDate) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تحديث الطلب", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Dialog to confirm bulk order to suppliers from the matrix view, with confirmation date.
 */
@Composable
fun ConfirmBulkSupplierOrderDialog(
    matrixRows: List<MatrixRow>,
    onDismiss: () -> Unit,
    onConfirm: (orderDate: String, confirmedDate: String, notes: String) -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var orderDate by remember { mutableStateOf(today) }
    var confirmedDate by remember { mutableStateOf(today) }
    var notes by remember { mutableStateOf("طلبية توريد مجمعة من جدول الطلبات") }

    val neededItems = matrixRows.filter { it.totalQty > 0 }
    val totalPieces = neededItems.sumOf { it.totalQty }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = AmberSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تأكيد طلب التوريد للموردين", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "سيتم توليد طلبيات توريد للموردين لكافة الأصناف المطلوبة في جدول العرض الجماعي.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ملخص الطلبية الجماعية:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• عدد الأصناف المطلوبة: ${neededItems.size} أصناف",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• إجمالي الكميات: $totalPieces قطعة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                    }
                }

                OutlinedTextField(
                    value = confirmedDate,
                    onValueChange = { confirmedDate = it },
                    label = { Text("تاريخ تأكيد الطلبية (للتوثيق والتعديل)") },
                    placeholder = { Text(today) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = orderDate,
                    onValueChange = { orderDate = it },
                    label = { Text("تاريخ إنشاء الطلب") },
                    placeholder = { Text(today) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات وشروط التوريد") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(orderDate, confirmedDate, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
            ) {
                Text("تأكيد وإرسال للموردين", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Dialog to edit an existing supplier order.
 */
@Composable
fun EditSupplierOrderDialog(
    order: SupplierOrderEntity,
    partName: String,
    onDismiss: () -> Unit,
    onConfirm: (updatedOrder: SupplierOrderEntity) -> Unit
) {
    var qtyOrderedText by remember { mutableStateOf(order.qtyOrdered.toString()) }
    var orderDate by remember { mutableStateOf(order.orderDate) }
    var confirmedDate by remember { mutableStateOf(order.confirmedDate.ifBlank { order.orderDate }) }
    var expectedDate by remember { mutableStateOf(order.expectedDate) }
    var notes by remember { mutableStateOf(order.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل طلبية المورد (#${order.id})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الصنف: $partName", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(
                    value = qtyOrderedText,
                    onValueChange = { qtyOrderedText = it },
                    label = { Text("الكمية المطلوبة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmedDate,
                    onValueChange = { confirmedDate = it },
                    label = { Text("تاريخ تأكيد الطلبية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = orderDate,
                    onValueChange = { orderDate = it },
                    label = { Text("تاريخ الطلب") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = expectedDate,
                    onValueChange = { expectedDate = it },
                    label = { Text("تاريخ الوصول المتوقع (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newQty = qtyOrderedText.toIntOrNull() ?: order.qtyOrdered
                    val newRemaining = maxOf(0, newQty - order.qtyReceived)
                    val newStatus = if (newRemaining == 0) "completed" else if (order.qtyReceived > 0) "partial" else "confirmed"
                    onConfirm(
                        order.copy(
                            qtyOrdered = newQty,
                            qtyRemaining = newRemaining,
                            status = newStatus,
                            orderDate = orderDate,
                            confirmedDate = confirmedDate,
                            expectedDate = expectedDate,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
