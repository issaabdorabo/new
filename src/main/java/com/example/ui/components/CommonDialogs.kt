package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PartEntity
import com.example.data.entity.PartyEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RoseDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartBottomSheet(
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String, carType: String, carModel: String, price: Double, minStock: Int, initialQty: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var carType by remember { mutableStateOf("تويوتا") }
    var carModel by remember { mutableStateOf("2022") }
    var priceText by remember { mutableStateOf("100") }
    var minStockText by remember { mutableStateOf("5") }
    var initialQtyText by remember { mutableStateOf("10") }

    val commonCarTypes = listOf("تويوتا", "هونداي", "نيسان", "كيا", "فورد", "شفروليه", "عام")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "➕ إضافة صنف جديد للمخزون",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم الصنف (مثال: فلتر زيت، تيل فرامل...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("نوع السيارة:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonCarTypes.take(4).forEach { type ->
                    FilterChip(
                        selected = carType == type,
                        onClick = { carType = type },
                        label = { Text(type) }
                    )
                }
            }

            OutlinedTextField(
                value = carType,
                onValueChange = { carType = it },
                label = { Text("نوع السيارة المخصص") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = carModel,
                onValueChange = { carModel = it },
                label = { Text("الموديل أو السنوات (مثال: 2020-2024)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("السعر (ر.س)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it },
                    label = { Text("الحد الأدنى") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = initialQtyText,
                onValueChange = { initialQtyText = it },
                label = { Text("الكمية الافتتاحية الحالية بالمخزون") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        val minStock = minStockText.toIntOrNull() ?: 5
                        val initialQty = initialQtyText.toIntOrNull() ?: 0
                        onSave(name, carType, carModel, price, minStock, initialQty)
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("حفظ وإضافة", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AddPartyDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ورشة") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val partyTypes = listOf("ورشة", "معرض", "شركة", "فرد", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "➕ إضافة جهة صرف جديدة",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الجهة (مثال: ورشة النجم، شركة الأفق...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("نوع الجهة:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    partyTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف / الجوال") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedType, phone, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ الجهة", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ إضافة عميل جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الجوال") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
            Button(onClick = { onSave(name, phone, notes) }) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun EditMatrixCellDialog(
    part: PartEntity,
    customer: CustomerEntity,
    currentQty: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(currentQty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("تعديل كمية الطلب", fontWeight = FontWeight.Bold)
                Text(
                    "الصنف: ${part.name} - ${part.carType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "العميل: ${customer.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 0) quantity-- },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "تقليل")
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { quantity += 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("+5", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { quantity += 10 },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("+10", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = { quantity = 0 }) {
                        Text("مسح (0)", color = RoseDanger)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ التعديل", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ReceiveSupplierOrderDialog(
    order: SupplierOrderEntity,
    part: PartEntity?,
    onDismiss: () -> Unit,
    onConfirm: (receivedNow: Int) -> Unit
) {
    var receivedInput by remember { mutableStateOf(order.qtyRemaining.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📦 تسجيل وصول شحنة مورد",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الصنف: ${part?.name ?: "صنف #${order.partId}"}", fontWeight = FontWeight.Bold)
                Text("السيارة: ${part?.carType ?: ""} - ${part?.carModel ?: ""}")
                Text("الكمية المطلوبة: ${order.qtyOrdered} قطعة")
                Text("الواصل سابقاً: ${order.qtyReceived} قطعة")
                Text("المتبقي حالياً: ${order.qtyRemaining} قطعة", color = AmberSecondary, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = receivedInput,
                    onValueChange = { receivedInput = it },
                    label = { Text("الكمية الواصلة الآن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "سيتم إضافة هذه الكمية تلقائياً إلى رصيد المخزون الحالي.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = receivedInput.toIntOrNull() ?: 0
                    onConfirm(qty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("تأكيد الاستلام وتحديث المخزون", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun NewSupplierOrderDialog(
    parts: List<PartEntity>,
    onDismiss: () -> Unit,
    onConfirm: (partId: Long, quantity: Int, expectedDate: String, notes: String) -> Unit
) {
    var selectedPartId by remember { mutableStateOf(parts.firstOrNull()?.id ?: 0L) }
    var quantityText by remember { mutableStateOf("10") }
    var expectedDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🚚 طلب توريد جديد من المورد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("اختر الصنف المطلوب:", style = MaterialTheme.typography.labelMedium)
                parts.forEach { part ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPartId = part.id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPartId == part.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(part.name, fontWeight = FontWeight.Medium)
                            Text("${part.carType} - ${part.carModel}", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("الكمية المطلوبة للتوريد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expectedDate,
                    onValueChange = { expectedDate = it },
                    label = { Text("تاريخ الوصول المتوقع (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 0
                    onConfirm(selectedPartId, qty, expectedDate, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تأكيد إنشاء الطلب", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
