package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PartyEntity
import com.example.data.model.PartWithInventory
import com.example.data.model.StockIssueWithDetails
import com.example.ui.components.AddPartyDialog
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockIssueScreen(
    preselectedPart: PartWithInventory?,
    allPartsWithInventory: List<PartWithInventory>,
    parties: List<PartyEntity>,
    stockIssuesWithDetails: List<StockIssueWithDetails>,
    onClearPreselectedPart: () -> Unit,
    onIssueStock: (
        partId: Long,
        quantity: Int,
        partyId: Long,
        personName: String,
        issueDate: String,
        note: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onAddNewParty: (name: String, type: String, phone: String, notes: String, onSuccess: (Long) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: نموذج الصرف, 1: سجل عمليات الصرف
    var showAddPartyDialog by remember { mutableStateOf(false) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Form states
    var selectedPartId by remember {
        mutableLongStateOf(preselectedPart?.part?.id ?: allPartsWithInventory.firstOrNull()?.part?.id ?: 0L)
    }

    LaunchedEffect(preselectedPart) {
        if (preselectedPart != null) {
            selectedPartId = preselectedPart.part.id
            selectedTab = 0
        }
    }

    val selectedPartItem = allPartsWithInventory.find { it.part.id == selectedPartId }
    val availableQty = selectedPartItem?.availableQty ?: 0

    var quantityInput by remember { mutableStateOf("1") }
    var selectedPartyId by remember {
        mutableLongStateOf(parties.firstOrNull()?.id ?: 0L)
    }
    var personName by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(todayStr) }
    var note by remember { mutableStateOf("") }

    // Dropdowns expanded state
    var partDropdownExpanded by remember { mutableStateOf(false) }
    var partyDropdownExpanded by remember { mutableStateOf(false) }

    val requestedQty = quantityInput.toIntOrNull() ?: 0
    val isQuantityExceeded = requestedQty > availableQty
    val isFormValid = requestedQty in 1..availableQty && selectedPartyId != 0L && personName.isNotBlank()

    val collator = remember {
        java.text.Collator.getInstance(java.util.Locale("ar", "SA")).apply {
            strength = java.text.Collator.PRIMARY
        }
    }
    var partSearchQuery by remember { mutableStateOf("") }
    val filteredSortedPartsForIssue = remember(allPartsWithInventory, partSearchQuery) {
        val filtered = if (partSearchQuery.isBlank()) allPartsWithInventory else {
            allPartsWithInventory.filter {
                it.part.name.contains(partSearchQuery, ignoreCase = true) ||
                it.part.carType.contains(partSearchQuery, ignoreCase = true) ||
                it.part.carModel.contains(partSearchQuery, ignoreCase = true)
            }
        }
        filtered.sortedWith { p1, p2 -> collator.compare(p1.part.name, p2.part.name) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F172A),
            contentColor = AmberSecondary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShoppingCartCheckout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 0) AmberSecondary else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "🛒 نموذج الصرف",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) AmberSecondary else Color.White
                        )
                    }
                },
                modifier = Modifier.testTag("tab_issue_form")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 1) AmberSecondary else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "📋 سجل الصرف (${stockIssuesWithDetails.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) AmberSecondary else Color.White
                        )
                    }
                },
                modifier = Modifier.testTag("tab_issue_history")
            )
        }

        if (selectedTab == 0) {
            // NEW STOCK ISSUE FORM
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛒 صرف قطعة من المخزون",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            if (preselectedPart != null) {
                                TextButton(onClick = {
                                    onClearPreselectedPart()
                                }) {
                                    Text("تغيير القطعة", fontSize = 12.sp)
                                }
                            }
                        }

                        HorizontalDivider()

                        // 1. SELECT PART (Exposed dropdown or selector with smart search and alphabetical sort)
                        Text("الصنف المراد صرفه (مرتبة أبجدياً أ - ي):", style = MaterialTheme.typography.labelMedium)
                        ExposedDropdownMenuBox(
                            expanded = partDropdownExpanded,
                            onExpandedChange = { partDropdownExpanded = !partDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPartItem?.let { "${it.part.name} - ${it.part.carType} (${it.part.carModel})" } ?: "اختر الصنف...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = partDropdownExpanded,
                                onDismissRequest = {
                                    partDropdownExpanded = false
                                    partSearchQuery = ""
                                }
                            ) {
                                // Smart Search Field inside Dropdown
                                OutlinedTextField(
                                    value = partSearchQuery,
                                    onValueChange = { partSearchQuery = it },
                                    placeholder = { Text("بحث عن صنف للصرف...", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (filteredSortedPartsForIssue.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("لا توجد أصناف تطابق البحث", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        onClick = {}
                                    )
                                } else {
                                    filteredSortedPartsForIssue.forEach { item ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(item.part.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text(
                                                            "${item.part.carType} • ${item.part.carModel}",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        "المتوفر: ${item.availableQty}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (item.availableQty <= item.part.minStock) RoseDanger else PrimaryBlue
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedPartId = item.part.id
                                                partDropdownExpanded = false
                                                partSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Real-time Available Stock Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (availableQty > 0) EmeraldLight else RoseLight,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الرصيد المتوفر حالياً بالمستودع:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$availableQty قطعة",
                                fontWeight = FontWeight.Bold,
                                color = if (availableQty > 0) EmeraldSuccess else RoseDanger,
                                fontSize = 15.sp
                            )
                        }

                        // 2. QUANTITY INPUT
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            label = { Text("الكمية المصروفة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isQuantityExceeded,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_issue_qty"),
                            shape = RoundedCornerShape(12.dp),
                            supportingText = {
                                if (isQuantityExceeded) {
                                    Text(
                                        text = "⚠️ الكمية المطلوبة ($requestedQty) تتجاوز المتوفر بالمخزون ($availableQty)!",
                                        color = RoseDanger,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )

                        // 3. SELECT PARTY (الجهة المصروفة لها)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الجهة المصروفة لها:", style = MaterialTheme.typography.labelMedium)
                            TextButton(
                                onClick = { showAddPartyDialog = true },
                                modifier = Modifier.testTag("btn_add_new_party")
                            ) {
                                Text("➕ إضافة جهة جديدة", fontSize = 12.sp, color = AmberSecondary, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (parties.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AmberLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "لا توجد جهات مسجلة بعد. يرجى إضافة جهة أولاً.",
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { showAddPartyDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
                                    ) {
                                        Text("إضافة", color = Color.Black, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            val selectedParty = parties.find { it.id == selectedPartyId } ?: parties.first()
                            ExposedDropdownMenuBox(
                                expanded = partyDropdownExpanded,
                                onExpandedChange = { partyDropdownExpanded = !partyDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = "${selectedParty.name} (${selectedParty.type})",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = partyDropdownExpanded,
                                    onDismissRequest = { partyDropdownExpanded = false }
                                ) {
                                    parties.forEach { party ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(party.name, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        party.type,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedPartyId = party.id
                                                partyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 4. PERSON NAME (اسم الشخص المستلم)
                        OutlinedTextField(
                            value = personName,
                            onValueChange = { personName = it },
                            label = { Text("اسم الشخص المستلم (مثال: أحمد محمد)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_receiver_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // 5. ISSUE DATE (التاريخ)
                        OutlinedTextField(
                            value = issueDate,
                            onValueChange = { issueDate = it },
                            label = { Text("تاريخ الصرف (yyyy-MM-dd)") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // 6. NOTES (ملاحظات)
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("ملاحظات / سبب الصرف (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirm Button
                        Button(
                            onClick = {
                                onIssueStock(
                                    selectedPartId,
                                    requestedQty,
                                    selectedPartyId,
                                    personName,
                                    issueDate,
                                    note
                                ) {
                                    // Reset form upon success
                                    quantityInput = "1"
                                    personName = ""
                                    note = ""
                                    selectedTab = 1 // Switch to history tab to view new record!
                                }
                            },
                            enabled = isFormValid,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_confirm_stock_issue"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تأكيد الصرف وخصم من المخزون",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        } else {
            // STOCK ISSUES HISTORY LOG TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 سجل عمليات الصرف الموثقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إجمالي العمليات: ${stockIssuesWithDetails.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (stockIssuesWithDetails.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد عمليات صرف مسجلة بعد.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(stockIssuesWithDetails) { item ->
                            StockIssueHistoryCard(item)
                        }
                    }
                }
            }
        }
    }

    if (showAddPartyDialog) {
        AddPartyDialog(
            onDismiss = { showAddPartyDialog = false },
            onSave = { name, type, phone, notes ->
                onAddNewParty(name, type, phone, notes) { newId ->
                    selectedPartyId = newId
                }
                showAddPartyDialog = false
            }
        )
    }
}

@Composable
fun StockIssueHistoryCard(item: StockIssueWithDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.partName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (item.carInfo.isNotBlank()) {
                        Text(
                            text = item.carInfo,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(RoseLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "- ${item.issue.quantity} قطعة",
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.partyName} (${item.partyType})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = AmberSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "المستلم: ${item.issue.personName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "التاريخ: ${item.issue.issueDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.issue.note.isNotBlank()) {
                    Text(
                        text = "ملاحظة: ${item.issue.note}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
