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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.QrCodeScanner
import com.example.ui.theme.TechCyan
import androidx.compose.material3.OutlinedTextField
import com.example.ui.util.Text
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
    existingCarTypes: List<String> = emptyList(),
    existingCarNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, carType: String, carName: String, carModel: String, purchasePrice: Double, price: Double, minStock: Int, initialQty: Int, barcode: String) -> Unit
) {
    val strings = com.example.ui.util.LocalAppStrings.current
    var name by remember { mutableStateOf(initialName) }
    var carType by remember { mutableStateOf("") }
    var carName by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("عام") }
    var purchasePriceText by remember { mutableStateOf("0") }
    var priceText by remember { mutableStateOf("0") }
    var minStockText by remember { mutableStateOf("0") }
    var initialQtyText by remember { mutableStateOf("0") }
    var barcode by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Validation states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var carTypeError by remember { mutableStateOf(false) }
    var carNameError by remember { mutableStateOf(false) }

    // Unique car types list: entered types first, followed by default common brands
    val defaultCommonCarTypes = listOf("تويوتا", "هونداي", "نيسان", "كيا", "فورد", "شفروليه", "ميتسوبيشي", "إيسوزو", "مازدا", "عام")
    val availableCarTypes = remember(existingCarTypes) {
        (existingCarTypes + defaultCommonCarTypes).distinct().filter { it.isNotBlank() }
    }

    // Unique car names list: entered names first, followed by popular models
    val defaultCommonCarNames = listOf("كامري", "كورولا", "سوناتا", "النترا", "يارس", "هايلوكس", "لاندكروزر", "أكسنت", "باترول", "تاهو", "شاص", "أفالون", "سبورتاج", "سيراتو", "دابو")
    val availableCarNames = remember(existingCarNames) {
        (existingCarNames + defaultCommonCarNames).distinct().filter { it.isNotBlank() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.addNewPart,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.close)
                }
            }

            // Error banner when required fields are empty
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 1. Item Name (اسم الصنف)
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) {
                        nameError = false
                        errorMessage = null
                    }
                },
                label = { Text("اسم الصنف * (مطلوب)") },
                placeholder = { Text("مثال: فلتر زيت، تيل فرامل، بواجي...") },
                isError = nameError,
                supportingText = if (nameError) { { Text("حقل اسم الصنف مطلوب", color = MaterialTheme.colorScheme.error) } } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Car Type (نوع السيارة) with entered types chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("نوع السيارة * (اختر من الأنواع المدخلة أو اكتب أدناه):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableCarTypes) { type ->
                        FilterChip(
                            selected = carType == type,
                            onClick = {
                                carType = type
                                carTypeError = false
                                errorMessage = null
                            },
                            label = { Text(type) }
                        )
                    }
                }

                OutlinedTextField(
                    value = carType,
                    onValueChange = {
                        carType = it
                        if (it.isNotBlank()) {
                            carTypeError = false
                            errorMessage = null
                        }
                    },
                    label = { Text("نوع السيارة * (تويوتا، هونداي، نيسان...)") },
                    placeholder = { Text("انقر نوعاً من القائمة أعلاه أو اكتب هنا") },
                    isError = carTypeError,
                    supportingText = if (carTypeError) { { Text("حقل نوع السيارة مطلوب", color = MaterialTheme.colorScheme.error) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 3. Car Name (اسم السيارة) with entered names chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("اسم السيارة * (اختر من الأسماء المدخلة أو اكتب أدناه مباشرة):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableCarNames) { cName ->
                        FilterChip(
                            selected = carName == cName,
                            onClick = {
                                carName = cName
                                carNameError = false
                                errorMessage = null
                            },
                            label = { Text(cName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = carName,
                    onValueChange = {
                        carName = it
                        if (it.isNotBlank()) {
                            carNameError = false
                            errorMessage = null
                        }
                    },
                    label = { Text("اسم السيارة * (كامري، كورولا، سوناتا، النترا...)") },
                    placeholder = { Text("انقر اسماً من القائمة أعلاه أو اكتب هنا مباشرة") },
                    isError = carNameError,
                    supportingText = if (carNameError) { { Text("حقل اسم السيارة مطلوب", color = MaterialTheme.colorScheme.error) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 4. Car Model / Years (الموديل أو السنوات)
            OutlinedTextField(
                value = carModel,
                onValueChange = { carModel = it },
                label = { Text("الموديل أو السنوات (مثال: 2020-2024 أو عام)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 5. Purchase Price & Selling Price (Both default to 0)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it },
                    label = { Text("سعر الشراء (ر.س)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("سعر البيع (ر.س)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // 6. Minimum Stock & Opening Quantity (Both default to 0)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it },
                    label = { Text("الحد الأدنى") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = initialQtyText,
                    onValueChange = { initialQtyText = it },
                    label = { Text("الكمية الافتتاحية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // 7. Barcode Field with Scanner and Generator
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("الباركود (اختياري - للمسح السريع والتتبع)") },
                placeholder = { Text("مثال: 628100101001") },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            barcode = "628" + (100000000..999999999).random().toString()
                        }) {
                            Text("توليد", fontSize = 11.sp, color = TechCyan)
                        }
                        IconButton(onClick = { showBarcodeScanner = true }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح باركود", tint = TechCyan)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons with Validation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedCarType = carType.trim()
                        val trimmedCarName = carName.trim()

                        val emptyFields = mutableListOf<String>()
                        if (trimmedName.isBlank()) {
                            nameError = true
                            emptyFields.add(strings.partName)
                        }
                        if (trimmedCarType.isBlank()) {
                            carTypeError = true
                            emptyFields.add(strings.carType)
                        }
                        if (trimmedCarName.isBlank()) {
                            carNameError = true
                            emptyFields.add(strings.carName)
                        }

                        if (emptyFields.isNotEmpty()) {
                            errorMessage = "⚠️ ${strings.requiredFieldsWarning} (${emptyFields.joinToString(", ")})"
                            return@Button
                        }

                        val purchasePrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        val minStock = minStockText.toIntOrNull() ?: 0
                        val initialQty = initialQtyText.toIntOrNull() ?: 0

                        onSave(
                            trimmedName,
                            trimmedCarType,
                            trimmedCarName,
                            carModel.trim().ifBlank { "عام" },
                            purchasePrice,
                            price,
                            minStock,
                            initialQty,
                            barcode.trim()
                        )
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(strings.save, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (showBarcodeScanner) {
                BarcodeScannerDialog(
                    onDismiss = { showBarcodeScanner = false },
                    onBarcodeScanned = { scanned ->
                        barcode = scanned
                        showBarcodeScanner = false
                    }
                )
            }
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
    onSave: ((name: String, phone: String, notes: String) -> Unit)? = null,
    onConfirm: ((name: String, phone: String, notes: String) -> Unit)? = null
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
            Button(onClick = {
                onSave?.invoke(name, phone, notes)
                onConfirm?.invoke(name, phone, notes)
            }) {
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
    part: PartEntity? = null,
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

@Composable
fun MultiSelectSelector(
    label: String,
    allOptions: List<String>,
    selectedOptions: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val summaryText = when {
        selectedOptions.isEmpty() -> "الكل ($label)"
        selectedOptions.size == allOptions.size -> "الكل ($label)"
        selectedOptions.size == 1 -> selectedOptions.first()
        else -> "${selectedOptions.size} محددة من $label"
    }

    Column(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (expanded) {
            AlertDialog(
                onDismissRequest = { expanded = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تحديد $label", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row {
                            TextButton(onClick = { onSelectionChanged(allOptions.toSet()) }) {
                                Text("الكل", fontSize = 11.sp)
                            }
                            TextButton(onClick = { onSelectionChanged(emptySet()) }) {
                                Text("إلغاء", fontSize = 11.sp)
                            }
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("بحث في $label...", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )
                        val filteredOptions = allOptions.filter {
                            searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
                        }
                        if (filteredOptions.isEmpty()) {
                            Text("لا توجد خيارات مطابقة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredOptions) { opt ->
                                    val isChecked = selectedOptions.contains(opt)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val newSet = selectedOptions.toMutableSet()
                                                if (isChecked) newSet.remove(opt) else newSet.add(opt)
                                                onSelectionChanged(newSet)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                val newSet = selectedOptions.toMutableSet()
                                                if (isChecked) newSet.remove(opt) else newSet.add(opt)
                                                onSelectionChanged(newSet)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(opt, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { expanded = false }) {
                        Text(if (selectedOptions.isEmpty()) "تأكيد (الكل)" else "تأكيد (${selectedOptions.size})")
                    }
                }
            )
        }
    }
}

