package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.util.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderPaymentEntity
import com.example.data.entity.SupplierOrderEntity
import com.example.data.entity.SupplierReceiptEntity
import com.example.data.model.CustomerOrderDetail
import com.example.data.model.CustomerPartRequirement
import com.example.data.model.CustomerWithOrder
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.util.AttachmentUtils
import com.example.ui.util.ExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConfirmDeleteAttachmentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
        title = { Text("تأكيد حذف المرفق", fontWeight = FontWeight.Bold) },
        text = { Text("هل أنت متأكد من رغبتك في حذف هذا المرفق نهائياً؟") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
            ) {
                Text("نعم، حذف المرفق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SaveMatrixOrderDialog(
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (orderDate: String) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var orderDate by remember { mutableStateOf(today) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "تعديل الطلبية في بطاقات العملاء" else "حفظ الطلبية في بطاقات العملاء",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEditing) {
                        "سيتم تحديث هذه الطلبية في بطاقات العملاء المحددة وفي شاشة طلبات الموردين وتصفير جدول العرض الجماعي."
                    } else {
                        "سيتم حفظ الأصناف المحددة كطلبية جديدة في بطاقات العملاء برمز دفعة فريد (Batch ID) وتصفير الجدول دون المساس بأي طلبيات سابقة."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = orderDate,
                    onValueChange = { orderDate = it },
                    label = { Text("تاريخ الطلبية (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(orderDate.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) AmberSecondary else PrimaryBlue)
            ) {
                Text(if (isEditing) "تأكيد التعديل وتصفير الجدول" else "حفظ الطلبية وتصفير الجدول")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun MultiPaymentDialog(
    order: OrderEntity,
    customerName: String,
    totalOrderAmount: Double,
    payments: List<OrderPaymentEntity>,
    onDismiss: () -> Unit,
    onAddPayment: (amount: Double, date: String, payerName: String, notes: String, attachmentUri: String?) -> Unit,
    onUpdatePayment: (OrderPaymentEntity) -> Unit,
    onDeletePayment: (paymentId: Long) -> Unit,
    onDeleteAttachment: (paymentId: Long) -> Unit,
    itemCount: Int = 0,
    totalPieces: Int = 0,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val localPayments = remember(payments) { mutableStateListOf<OrderPaymentEntity>().apply { addAll(payments) } }

    val totalPaid = localPayments.sumOf { it.amount }
    val remaining = maxOf(0.0, totalOrderAmount - order.discount - totalPaid)

    var amountInput by remember { mutableStateOf("") }
    var payerNameInput by remember { mutableStateOf(customerName) }
    var paymentDateInput by remember { mutableStateOf(today) }
    var notesInput by remember { mutableStateOf("") }
    var newAttachmentUri by remember { mutableStateOf<String?>(null) }
    var showAddSection by remember { mutableStateOf(false) }

    var paymentToDelete by remember { mutableStateOf<OrderPaymentEntity?>(null) }
    var paymentToEdit by remember { mutableStateOf<OrderPaymentEntity?>(null) }
    var attachmentToDeletePaymentId by remember { mutableStateOf<Long?>(null) }

    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val saved = AttachmentUtils.saveAttachment(context, it, "payment_${System.currentTimeMillis()}")
            newAttachmentUri = saved
        }
    }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountValid = parsedAmount > 0.0 && parsedAmount <= (remaining + 0.001)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("سجل دفعات العميل (الواصل)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("العميل: $customerName • طلبية: ${order.orderDate}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Financial overview box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي الفاتورة: $totalOrderAmount ر.س", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            Text("الخصم: ${order.discount} ر.س", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي الواصل: $totalPaid ر.س", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            Text("المتبقي: $remaining ر.س", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (remaining > 0) RoseDanger else EmeraldSuccess)
                        }
                    }
                }

                // Existing payments list
                Text("الدفعات المسجلة (${localPayments.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (localPayments.isEmpty()) {
                    Text("لا توجد دفعات مسجلة لهذه الطلبية حتى الآن.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        localPayments.sortedBy { it.paymentDate }.forEachIndexed { idx, p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("م ${idx + 1}:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryBlue)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${p.amount} ر.س", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(${p.paymentDate})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { paymentToEdit = p }, modifier = Modifier.size(26.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(13.dp))
                                            }
                                            IconButton(onClick = { paymentToDelete = p }, modifier = Modifier.size(26.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    if (p.payerName.isNotBlank()) {
                                        Text("اسم المسدد: ${p.payerName}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (p.note.isNotBlank()) {
                                        Text("ملاحظات: ${p.note}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Attachment
                                    if (!p.attachmentUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val disp = AttachmentUtils.getDisplayName(context, p.attachmentUri)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryLighter, RoundedCornerShape(6.dp))
                                                    .clickable { AttachmentUtils.openAttachment(context, p.attachmentUri) }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(disp.take(12), fontSize = 9.5.sp, color = PrimaryBlue)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "حذف المرفق",
                                                tint = RoseDanger,
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clickable { attachmentToDeletePaymentId = p.id }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add installment form
                if (remaining > 0) {
                    HorizontalDivider()
                    Text("➕ إضافة دفعة جديدة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("المبلغ المدفوع (أقصى حد: $remaining ر.س)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = parsedAmount > remaining
                    )
                    if (parsedAmount > remaining) {
                        Text("⚠️ المبلغ المدفوع لا يمكن أن يتجاوز المتبقي ($remaining ر.س)!", color = RoseDanger, fontSize = 10.sp)
                    }

                    OutlinedTextField(
                        value = payerNameInput,
                        onValueChange = { payerNameInput = it },
                        label = { Text("اسم الشخص المسدد") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = paymentDateInput,
                        onValueChange = { paymentDateInput = it },
                        label = { Text("تاريخ السداد (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات السند / الحوالة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Attachment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مرفق السند (صورة أو PDF):", fontSize = 11.sp)
                        if (!newAttachmentUri.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(AttachmentUtils.getDisplayName(context, newAttachmentUri).take(10), fontSize = 10.sp, color = PrimaryBlue)
                                IconButton(onClick = { newAttachmentUri = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(13.dp))
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { attachmentPicker.launch("*/*") },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("إرفاق سند", fontSize = 10.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isAmountValid) {
                                val newP = OrderPaymentEntity(
                                    id = System.currentTimeMillis(),
                                    orderId = order.id,
                                    amount = parsedAmount,
                                    paymentDate = paymentDateInput.trim(),
                                    payerName = payerNameInput.trim(),
                                    note = notesInput.trim(),
                                    attachmentUri = newAttachmentUri
                                )
                                localPayments.add(newP)
                                onAddPayment(parsedAmount, paymentDateInput.trim(), payerNameInput.trim(), notesInput.trim(), newAttachmentUri)
                                amountInput = ""
                                notesInput = ""
                                newAttachmentUri = null
                            }
                        },
                        enabled = isAmountValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("تسجيل هذه الدفعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Dual Export for this single order
                HorizontalDivider()
                Text("تصدير كشف حركات السداد لهذه الطلبية:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportSingleOrderPayments(context, customerName, order, totalOrderAmount, localPayments, false, itemCount, totalPieces, appUserName)
                        },
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel", fontSize = 10.5.sp)
                    }

                    Button(
                        onClick = {
                            exportSingleOrderPayments(context, customerName, order, totalOrderAmount, localPayments, true, itemCount, totalPieces, appUserName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 10.5.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )

    // Delete payment confirmation dialog
    paymentToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
            title = { Text("تأكيد حذف الدفعة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف هذه الدفعة بقيمة ${p.amount} ر.س المسددة بتاريخ ${p.paymentDate}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        localPayments.removeAll { it.id == p.id }
                        onDeletePayment(p.id)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، حذف الدفعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Delete attachment confirmation dialog
    attachmentToDeletePaymentId?.let { pId ->
        ConfirmDeleteAttachmentDialog(
            onDismiss = { attachmentToDeletePaymentId = null },
            onConfirm = {
                onDeleteAttachment(pId)
                attachmentToDeletePaymentId = null
            }
        )
    }

    // Edit payment dialog
    paymentToEdit?.let { p ->
        var editAmount by remember { mutableStateOf(p.amount.toString()) }
        var editPayer by remember { mutableStateOf(p.payerName) }
        var editDate by remember { mutableStateOf(p.paymentDate) }
        var editNotes by remember { mutableStateOf(p.note) }

        AlertDialog(
            onDismissRequest = { paymentToEdit = null },
            title = { Text("تعديل بيانات الدفعة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("المبلغ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPayer,
                        onValueChange = { editPayer = it },
                        label = { Text("اسم المسدد") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("التاريخ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val a = editAmount.toDoubleOrNull() ?: p.amount
                        val updated = p.copy(amount = a, payerName = editPayer.trim(), paymentDate = editDate.trim(), note = editNotes.trim())
                        val idx = localPayments.indexOfFirst { it.id == p.id }
                        if (idx >= 0) {
                            localPayments[idx] = updated
                        }
                        onUpdatePayment(updated)
                        paymentToEdit = null
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToEdit = null }) { Text("إلغاء") }
            }
        )
    }
}

private fun exportSingleOrderPayments(
    context: Context,
    customerName: String,
    order: OrderEntity,
    totalOrderAmount: Double,
    payments: List<OrderPaymentEntity>,
    isPdf: Boolean,
    itemCount: Int = 0,
    totalPieces: Int = 0,
    appUserName: String
) {
    val headers = listOf("م", "تاريخ السداد", "اسم المسدد", "المبلغ الواصل", "الملاحظات")
    val sorted = payments.sortedBy { it.paymentDate }
    val rows = sorted.mapIndexed { idx, p ->
        listOf(
            "${idx + 1}",
            p.paymentDate,
            p.payerName.ifBlank { customerName },
            "${p.amount} ر.س",
            p.note.ifBlank { "-" }
        )
    }
    val totalPaid = payments.sumOf { it.amount }
    val rem = maxOf(0.0, totalOrderAmount - order.discount - totalPaid)
    val summaries = listOf(
        "رقم / رمز الدفعة: ${if (order.batchId.isNotBlank()) order.batchId else "مفردة"}",
        "عدد الأصناف المطلوبة: $itemCount صنف",
        "إجمالي عدد القطع: $totalPieces قطعة",
        "إجمالي قيمة الطلبية: $totalOrderAmount ر.س",
        "الخصم: ${order.discount} ر.س",
        "إجمالي المبالغ الواصلة: $totalPaid ر.س",
        "المتبقي النهائي: $rem ر.س"
    )

    if (isPdf) {
        ExportUtils.exportToPdf(
            context = context,
            fileName = "كشف_سداد_${customerName}_${order.orderDate}",
            title = "كشف حركات سداد طلبية العميل: $customerName",
            subtitle = "تاريخ الطلبية: ${order.orderDate} • رمز الدفعة: ${order.batchId.ifBlank { "مفردة" }}",
            headers = headers,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    } else {
        ExportUtils.exportToExcelCsv(
            context = context,
            fileName = "كشف_سداد_${customerName}_${order.orderDate}",
            title = "كشف حركات سداد طلبية العميل: $customerName",
            headers = headers,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    }
}

@Composable
fun CustomerPartRequirementsDialog(
    item: SupplierOrderWithPart,
    onDismiss: () -> Unit
) {
    CustomerPartRequirementsDialog(
        partName = item.part?.name ?: "صنف #${item.supplierOrder.partId}",
        carType = item.part?.carType ?: "",
        carModel = item.part?.carModel ?: "",
        requirements = item.customersWhoOrdered,
        onDismiss = onDismiss
    )
}

@Composable
fun CustomerPartRequirementsDialog(
    partName: String,
    carType: String,
    carModel: String,
    requirements: List<CustomerPartRequirement>,
    onDismiss: () -> Unit
) {
    val totalRequiredPieces = requirements.sumOf { it.quantity }
    val totalCustomerAmount = requirements.sumOf { it.totalPrice }
    val totalPaid = requirements.sumOf { it.paidAmount }
    val totalRemaining = requirements.sumOf { it.remainingAmount }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("معلومات طلبات العملاء للصنف", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("$partName ($carType • $carModel)", fontSize = 12.sp, color = PrimaryBlue)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("العدد المطلوب من العملاء:", fontSize = 11.5.sp)
                            Text("$totalRequiredPieces قطعة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي القيمة المطلوبة:", fontSize = 11.5.sp)
                            Text("$totalCustomerAmount ر.س", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الواصل من العملاء: $totalPaid ر.س", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            Text("المتبقي عليهم: $totalRemaining ر.س", fontSize = 11.sp, color = RoseDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("قائمة العملاء الطالبين لهذا الصنف (${requirements.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                if (requirements.isEmpty()) {
                    Text("لا توجد طلبات عملاء محددة مسجلة لهذا الصنف في هذه الطلبية.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        requirements.forEachIndexed { idx, req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${idx + 1}. ${req.customerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${req.quantity} قطعة", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("سعر البيع: ${req.unitPrice} ر.س", fontSize = 10.5.sp)
                                        Text("الإجمالي: ${req.totalPrice} ر.س", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("الواصل: ${req.paidAmount} ر.س", fontSize = 10.5.sp, color = EmeraldSuccess)
                                        Text("المتبقي: ${req.remainingAmount} ر.س", fontSize = 10.5.sp, color = if (req.remainingAmount > 0) RoseDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun MultiReceiptDialog(
    supplierOrderWithPart: SupplierOrderWithPart? = null,
    item: SupplierOrderWithPart? = supplierOrderWithPart,
    onDismiss: () -> Unit,
    onAddReceipt: (qty: Int, date: String, receiverName: String, notes: String, attachmentUri: String?) -> Unit,
    onUpdateReceipt: (SupplierReceiptEntity) -> Unit,
    onDeleteReceipt: (receiptId: Long) -> Unit,
    onDeleteAttachment: (receiptId: Long) -> Unit = {},
    onDeleteReceiptAttachment: (receiptId: Long) -> Unit = onDeleteAttachment,
    appUserName: String = "عيسى عبدربه"
) {
    val actualItem = item ?: supplierOrderWithPart ?: return
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val order = actualItem.supplierOrder
    val part = actualItem.part
    val receipts = actualItem.receipts
    val localReceipts = remember(receipts) { mutableStateListOf<SupplierReceiptEntity>().apply { addAll(receipts) } }

    val totalOrdered = order.qtyOrdered
    val totalReceived = localReceipts.sumOf { it.quantity }
    val remaining = maxOf(0, totalOrdered - totalReceived)

    var qtyInput by remember { mutableStateOf("") }
    var receiverNameInput by remember { mutableStateOf(appUserName) }
    var receiptDateInput by remember { mutableStateOf(today) }
    var notesInput by remember { mutableStateOf("") }
    var newAttachmentUri by remember { mutableStateOf<String?>(null) }

    var receiptToDelete by remember { mutableStateOf<SupplierReceiptEntity?>(null) }
    var receiptToEdit by remember { mutableStateOf<SupplierReceiptEntity?>(null) }
    var attachmentToDeleteReceiptId by remember { mutableStateOf<Long?>(null) }

    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val saved = AttachmentUtils.saveAttachment(context, it, "receipt_${System.currentTimeMillis()}")
            newAttachmentUri = saved
        }
    }

    val parsedQty = qtyInput.toIntOrNull() ?: 0
    val isQtyValid = parsedQty in 1..remaining

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("استلام دفعات التوريد وتحديث المخزون", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("الصنف: ${part?.name ?: "صنف #${order.partId}"} (${part?.carType} • ${part?.carModel})", fontSize = 11.5.sp, color = PrimaryBlue)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الكمية المطلوبة: $totalOrdered قطعة", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            Text("المستلم حتى الآن: $totalReceived قطعة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المتبقي للتوريد:", fontSize = 11.5.sp)
                            Text("$remaining قطعة", fontWeight = FontWeight.Bold, color = if (remaining > 0) RoseDanger else EmeraldSuccess, fontSize = 12.sp)
                        }
                    }
                }

                // Existing receipts list
                Text("الدفعات المستلمة سابقاً (${localReceipts.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (localReceipts.isEmpty()) {
                    Text("لم يتم استلام أي دفعات توريد لهذا الصنف بعد.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        localReceipts.sortedBy { it.receiptDate }.forEachIndexed { idx, r ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("دفعة ${idx + 1}:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryBlue)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+${r.quantity} قطعة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(${r.receiptDate})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { receiptToEdit = r }, modifier = Modifier.size(26.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(13.dp))
                                            }
                                            IconButton(onClick = { receiptToDelete = r }, modifier = Modifier.size(26.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    if (r.receiverName.isNotBlank()) {
                                        Text("المستلم: ${r.receiverName}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (r.note.isNotBlank()) {
                                        Text("ملاحظات: ${r.note}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Attachment
                                    if (!r.attachmentUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val disp = AttachmentUtils.getDisplayName(context, r.attachmentUri)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryLighter, RoundedCornerShape(6.dp))
                                                    .clickable { AttachmentUtils.openAttachment(context, r.attachmentUri) }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(disp.take(12), fontSize = 9.5.sp, color = PrimaryBlue)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "حذف المرفق",
                                                tint = RoseDanger,
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clickable { attachmentToDeleteReceiptId = r.id }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add receipt form
                if (remaining > 0) {
                    HorizontalDivider()
                    Text("➕ تسجيل استلام دفعة جديدة الآن:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("الكمية المستلمة (أقصى حد: $remaining قطعة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = parsedQty > remaining
                    )
                    if (parsedQty > remaining) {
                        Text("⚠️ الكمية المستلمة لا يمكن أن تتجاوز المتبقي ($remaining قطعة)!", color = RoseDanger, fontSize = 10.sp)
                    }

                    OutlinedTextField(
                        value = receiverNameInput,
                        onValueChange = { receiverNameInput = it },
                        label = { Text("اسم الشخص المستلم بالمستودع") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = receiptDateInput,
                        onValueChange = { receiptDateInput = it },
                        label = { Text("تاريخ الاستلام (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الفحص والاستلام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Attachment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مرفق سند التوريد (صورة أو PDF):", fontSize = 11.sp)
                        if (!newAttachmentUri.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(AttachmentUtils.getDisplayName(context, newAttachmentUri).take(10), fontSize = 10.sp, color = PrimaryBlue)
                                IconButton(onClick = { newAttachmentUri = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "حذف", tint = RoseDanger, modifier = Modifier.size(13.dp))
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { attachmentPicker.launch("*/*") },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("إرفاق سند", fontSize = 10.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isQtyValid) {
                                val newR = SupplierReceiptEntity(
                                    id = System.currentTimeMillis(),
                                    supplierOrderId = order.id,
                                    partId = order.partId,
                                    quantity = parsedQty,
                                    receiptDate = receiptDateInput.trim(),
                                    receiverName = receiverNameInput.trim(),
                                    note = notesInput.trim(),
                                    attachmentUri = newAttachmentUri
                                )
                                localReceipts.add(newR)
                                onAddReceipt(parsedQty, receiptDateInput.trim(), receiverNameInput.trim(), notesInput.trim(), newAttachmentUri)
                                qtyInput = ""
                                notesInput = ""
                                newAttachmentUri = null
                            }
                        },
                        enabled = isQtyValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("تأكيد الاستلام وإضافتها للمخزون", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )

    // Delete receipt confirmation
    receiptToDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { receiptToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseDanger) },
            title = { Text("تأكيد حذف الدفعة المستلمة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه الدفعة (${r.quantity} قطعة) المستلمة بتاريخ ${r.receiptDate}؟ سيتم خصم هذه الكمية تلقائياً من رصيد المخزون.") },
            confirmButton = {
                Button(
                    onClick = {
                        localReceipts.removeAll { it.id == r.id }
                        onDeleteReceipt(r.id)
                        receiptToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("نعم، حذف الدفعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Delete attachment confirmation
    attachmentToDeleteReceiptId?.let { rId ->
        ConfirmDeleteAttachmentDialog(
            onDismiss = { attachmentToDeleteReceiptId = null },
            onConfirm = {
                onDeleteReceiptAttachment(rId)
                attachmentToDeleteReceiptId = null
            }
        )
    }

    // Edit receipt dialog
    receiptToEdit?.let { r ->
        var editQty by remember { mutableStateOf(r.quantity.toString()) }
        var editReceiver by remember { mutableStateOf(r.receiverName) }
        var editDate by remember { mutableStateOf(r.receiptDate) }
        var editNotes by remember { mutableStateOf(r.note) }

        AlertDialog(
            onDismissRequest = { receiptToEdit = null },
            title = { Text("تعديل الدفعة المستلمة سابقاً", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editQty,
                        onValueChange = { editQty = it },
                        label = { Text("الكمية المستلمة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editReceiver,
                        onValueChange = { editReceiver = it },
                        label = { Text("اسم المستلم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("تاريخ الاستلام") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = editQty.toIntOrNull() ?: r.quantity
                        val updated = r.copy(quantity = q, receiverName = editReceiver.trim(), receiptDate = editDate.trim(), note = editNotes.trim())
                        val idx = localReceipts.indexOfFirst { it.id == r.id }
                        if (idx >= 0) {
                            localReceipts[idx] = updated
                        }
                        onUpdateReceipt(updated)
                        receiptToEdit = null
                    }
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToEdit = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun SupplierOrderTopExportDialog(
    supplierOrders: List<SupplierOrderWithPart> = emptyList(),
    allOrders: List<SupplierOrderWithPart> = supplierOrders,
    onDismiss: () -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val effectiveOrders = if (supplierOrders.isNotEmpty()) supplierOrders else allOrders
    val context = LocalContext.current
    val columns = listOf(
        "اسم الصنف",
        "نوع السيارة",
        "موديل السيارة",
        "الكمية المطلوبة",
        "الكمية الواصلة",
        "الكمية المتبقية",
        "حالة الطلب",
        "تاريخ الطلب",
        "تاريخ التأكيد",
        "الملاحظات"
    )
    val selectedCols = remember { mutableStateListOf(*List(columns.size) { true }.toTypedArray()) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    val allPartNames = remember(effectiveOrders) {
        effectiveOrders.mapNotNull { it.part?.name }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val allCustomerNames = remember(effectiveOrders) {
        effectiveOrders.flatMap { it.customersWhoOrdered }.map { it.customerName }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPartNames by remember { mutableStateOf(setOf<String>()) }
    var selectedCustomerNames by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تخصيص وتصدير طلبات الموردين", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("الأعمدة المراد تصديرها:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                columns.forEachIndexed { idx, col ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCols[idx] = !selectedCols[idx] }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = selectedCols[idx], onCheckedChange = { selectedCols[idx] = it })
                        Text(col, fontSize = 11.5.sp)
                    }
                }

                HorizontalDivider()
                Text("تصفية وفلترة البيانات:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("من تاريخ (yyyy-MM-dd)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("إلى تاريخ (yyyy-MM-dd)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                MultiSelectSelector(
                    label = "الأصناف",
                    allOptions = allPartNames,
                    selectedOptions = selectedPartNames,
                    onSelectionChanged = { selectedPartNames = it }
                )

                MultiSelectSelector(
                    label = "العملاء",
                    allOptions = allCustomerNames,
                    selectedOptions = selectedCustomerNames,
                    onSelectionChanged = { selectedCustomerNames = it }
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        exportFilteredSupplierOrders(context, effectiveOrders, selectedCols, columns, fromDate, toDate, selectedPartNames, selectedCustomerNames, false, appUserName)
                        onDismiss()
                    }
                ) {
                    Text("Excel")
                }
                Button(
                    onClick = {
                        exportFilteredSupplierOrders(context, effectiveOrders, selectedCols, columns, fromDate, toDate, selectedPartNames, selectedCustomerNames, true, appUserName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

private fun exportFilteredSupplierOrders(
    context: Context,
    allOrders: List<SupplierOrderWithPart>,
    selectedCols: List<Boolean>,
    columns: List<String>,
    fromDate: String,
    toDate: String,
    selectedPartNames: Set<String>,
    selectedCustomerNames: Set<String>,
    isPdf: Boolean,
    appUserName: String
) {
    val filtered = allOrders.filter { item ->
        val ord = item.supplierOrder
        val part = item.part
        val dateMatch = (fromDate.isBlank() || ord.orderDate >= fromDate.trim()) &&
                (toDate.isBlank() || ord.orderDate <= toDate.trim())
        val partMatch = selectedPartNames.isEmpty() || (part?.name != null && selectedPartNames.contains(part.name))
        val customerMatch = selectedCustomerNames.isEmpty() || item.customersWhoOrdered.any { selectedCustomerNames.contains(it.customerName) }
        dateMatch && partMatch && customerMatch
    }

    // Sort items alphabetically with Arabic collator
    val collator = java.text.Collator.getInstance(Locale("ar"))
    val sorted = filtered.sortedWith(compareBy(collator) { it.part?.name ?: "" })

    val activeHeaders = listOf("م") + columns.filterIndexed { index, _ -> selectedCols.getOrElse(index) { true } }
    val rows = sorted.mapIndexed { idx, item ->
        val ord = item.supplierOrder
        val part = item.part
        val fullRow = listOf(
            part?.name ?: "صنف #${ord.partId}",
            part?.carType ?: "-",
            part?.carModel ?: "-",
            "${ord.qtyOrdered}",
            "${ord.qtyReceived}",
            "${ord.qtyRemaining}",
            when (ord.status) {
                "completed" -> "مكتمل"
                "partial" -> "مستلم جزئياً"
                "cancelled" -> "ملغي"
                else -> "قيد التوريد"
            },
            ord.orderDate,
            ord.confirmedDate,
            ord.notes
        )
        listOf("${idx + 1}") + fullRow.filterIndexed { index, _ -> selectedCols.getOrElse(index) { true } }
    }

    val summaries = listOf(
        "إجمالي عدد أصناف التوريد: ${sorted.size}",
        "إجمالي الكميات المطلوبة: ${sorted.sumOf { it.supplierOrder.qtyOrdered }} قطعة",
        "إجمالي الكميات الواصلة: ${sorted.sumOf { it.supplierOrder.qtyReceived }} قطعة",
        "إجمالي الكميات المتبقية: ${sorted.sumOf { it.supplierOrder.qtyRemaining }} قطعة"
    )

    if (isPdf) {
        ExportUtils.exportToPdf(
            context = context,
            fileName = "تقرير_طلبات_الموردين_${System.currentTimeMillis()}",
            title = "تقرير طلبيات التوريد من الموردين",
            subtitle = "الفترة: ${fromDate.ifBlank { "البداية" }} إلى ${toDate.ifBlank { "الآن" }}",
            headers = activeHeaders,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    } else {
        ExportUtils.exportToExcelCsv(
            context = context,
            fileName = "تقرير_طلبات_الموردين_${System.currentTimeMillis()}",
            title = "تقرير طلبيات التوريد من الموردين",
            headers = activeHeaders,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    }
}

@Composable
fun AllCustomerPaymentsExportDialog(
    allCustomerOrders: List<CustomerWithOrder>,
    onDismiss: () -> Unit,
    appUserName: String = "عيسى عبدربه"
) {
    val context = LocalContext.current
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    val allPartNames = remember(allCustomerOrders) {
        allCustomerOrders.flatMap { it.allOrders }.flatMap { it.items }.map { it.part.name }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val allCustomerNames = remember(allCustomerOrders) {
        allCustomerOrders.map { it.customer.name }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val allPayerNames = remember(allCustomerOrders) {
        allCustomerOrders.flatMap { it.allOrders }.flatMap { it.payments }.map { it.payerName.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPartNames by remember { mutableStateOf(setOf<String>()) }
    var selectedCustomerNames by remember { mutableStateOf(setOf<String>()) }
    var selectedPayerNames by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تصدير حركات السداد لكافة طلبات العملاء", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("تحديد نطاق التاريخ والفلترة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("من تاريخ (yyyy-MM-dd)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("إلى تاريخ (yyyy-MM-dd)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                MultiSelectSelector(
                    label = "الأصناف",
                    allOptions = allPartNames,
                    selectedOptions = selectedPartNames,
                    onSelectionChanged = { selectedPartNames = it }
                )

                MultiSelectSelector(
                    label = "العملاء",
                    allOptions = allCustomerNames,
                    selectedOptions = selectedCustomerNames,
                    onSelectionChanged = { selectedCustomerNames = it }
                )

                MultiSelectSelector(
                    label = "الأشخاص الذين قاموا بالسداد",
                    allOptions = allPayerNames,
                    selectedOptions = selectedPayerNames,
                    onSelectionChanged = { selectedPayerNames = it }
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        exportAllCustomerPayments(context, allCustomerOrders, fromDate, toDate, selectedPartNames, selectedCustomerNames, selectedPayerNames, false, appUserName)
                        onDismiss()
                    }
                ) {
                    Text("Excel")
                }
                Button(
                    onClick = {
                        exportAllCustomerPayments(context, allCustomerOrders, fromDate, toDate, selectedPartNames, selectedCustomerNames, selectedPayerNames, true, appUserName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

private fun exportAllCustomerPayments(
    context: Context,
    customerOrders: List<CustomerWithOrder>,
    fromDate: String,
    toDate: String,
    selectedPartNames: Set<String>,
    selectedCustomerNames: Set<String>,
    selectedPayerNames: Set<String>,
    isPdf: Boolean,
    appUserName: String
) {
    val headers = listOf("م", "اسم العميل", "تاريخ الطلب", "تاريخ السداد", "اسم المسدد", "المبلغ المدفوع", "المتبقي", "ملاحظات")
    val rows = mutableListOf<List<String>>()
    var serial = 1
    var grandTotalPaid = 0.0
    var grandTotalRemaining = 0.0

    customerOrders.forEach { custWithOrder ->
        val cust = custWithOrder.customer
        if (selectedCustomerNames.isEmpty() || selectedCustomerNames.contains(cust.name)) {
            custWithOrder.allOrders.forEach { orderDetail ->
                val ord = orderDetail.order
                val rawItems = orderDetail.items
                val hasPart = selectedPartNames.isEmpty() || rawItems.any { selectedPartNames.contains(it.part.name) }
                if (hasPart) {
                    val payments = orderDetail.payments
                    val orderTotal = orderDetail.totalAmount
                    val discount = ord.discount
                    val totalPaidForOrder = payments.sumOf { it.amount }
                    val orderRemaining = maxOf(0.0, orderTotal - discount - totalPaidForOrder)
                    grandTotalRemaining += orderRemaining

                    var cumPaid = 0.0
                    payments.sortedBy { it.paymentDate }.forEach { p ->
                        val dateMatch = (fromDate.isBlank() || p.paymentDate >= fromDate.trim()) &&
                                (toDate.isBlank() || p.paymentDate <= toDate.trim())
                        val payerMatch = selectedPayerNames.isEmpty() || selectedPayerNames.contains(p.payerName.trim())
                        if (dateMatch && payerMatch) {
                            cumPaid += p.amount
                            grandTotalPaid += p.amount
                            val rem = maxOf(0.0, orderTotal - discount - cumPaid)
                            rows.add(
                                listOf(
                                    "$serial",
                                    cust.name,
                                    ord.orderDate,
                                    p.paymentDate,
                                    p.payerName.ifBlank { cust.name },
                                    "${p.amount} ر.س",
                                    "$rem ر.س",
                                    p.note.ifBlank { "-" }
                                )
                            )
                            serial++
                        }
                    }
                }
            }
        }
    }

    val summaries = listOf(
        "إجمالي عدد حركات السداد الموثقة: ${rows.size}",
        "إجمالي المبالغ المسددة المستلمة: $grandTotalPaid ر.س",
        "إجمالي المبلغ المتبقي: $grandTotalRemaining ر.س"
    )

    if (isPdf) {
        ExportUtils.exportToPdf(
            context = context,
            fileName = "سجل_حركات_سداد_العملاء_${System.currentTimeMillis()}",
            title = "كشف شامل لحركات سداد طلبات العملاء",
            subtitle = "الفترة: ${fromDate.ifBlank { "البداية" }} إلى ${toDate.ifBlank { "الآن" }}",
            headers = headers,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    } else {
        ExportUtils.exportToExcelCsv(
            context = context,
            fileName = "سجل_حركات_سداد_العملاء_${System.currentTimeMillis()}",
            title = "كشف شامل لحركات سداد طلبات العملاء",
            headers = headers,
            rows = rows,
            summaryLines = summaries,
            appUserName = appUserName
        )
    }
}

