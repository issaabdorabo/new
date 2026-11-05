package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerPartRequirement
import com.example.data.model.SupplierOrderWithPart
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechCyanLight

/**
 * Dialog shown before reordering remaining supplier items.
 * Displays items with partial deliveries and enables the user to choose
 * how to distribute the remaining quantities among customers who requested the item.
 */
@Composable
fun ReorderRemainingDistributionDialog(
    partialItems: List<SupplierOrderWithPart>,
    onDismiss: () -> Unit,
    onConfirmDistribution: (allocations: Map<Pair<Long, Long>, Int>) -> Unit
) {
    // Allocation map: Pair(partId, customerId) -> allocatedQty
    val allocationMap = remember {
        mutableStateMapOf<Pair<Long, Long>, Int>().apply {
            partialItems.forEach { item ->
                val partId = item.supplierOrder.partId
                val rem = item.supplierOrder.qtyRemaining
                val custs = item.customersWhoOrdered
                if (custs.isNotEmpty()) {
                    var remainingToDistribute = rem
                    custs.forEach { req ->
                        val canAllocate = minOf(req.quantity, remainingToDistribute)
                        put(partId to req.customerId, canAllocate)
                        remainingToDistribute = maxOf(0, remainingToDistribute - canAllocate)
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("تقسيم طلب الصنف المتبقي بين العملاء", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("حدد كيفية توزيع الكميات المتبقية على العملاء الذين طلبوا هذا الصنف", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (partialItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد أصناف مستلمة جزئياً متبقية لإعادة الطلب", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(partialItems) { item ->
                            val part = item.part
                            val suppOrder = item.supplierOrder
                            val partId = suppOrder.partId
                            val partName = part?.name ?: "صنف #${partId}"
                            val totalRem = suppOrder.qtyRemaining
                            val totalOrd = suppOrder.qtyOrdered
                            val totalRec = suppOrder.qtyReceived
                            val custs = item.customersWhoOrdered

                            // Calculate current total allocated for this part
                            val allocatedSoFar = custs.sumOf { allocationMap[partId to it.customerId] ?: 0 }
                            val unallocated = totalRem - allocatedSoFar

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Part header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(partName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.primary)
                                            if (part != null) {
                                                Text("${part.carType} - ${part.carModel}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(TechCyanLight, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("متبقي: $totalRem قطعة", fontWeight = FontWeight.Bold, color = TechCyan, fontSize = 11.sp)
                                        }
                                    }

                                    // Part stats strip
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("مطلوب: $totalOrd", fontSize = 10.5.sp)
                                        Text("تم استلام: $totalRec", fontSize = 10.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "موزّع: $allocatedSoFar / $totalRem",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (unallocated == 0) EmeraldSuccess else RoseDanger
                                        )
                                    }

                                    // Quick distribution buttons
                                    if (custs.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    // Distribute evenly
                                                    val perCust = totalRem / custs.size
                                                    var extra = totalRem % custs.size
                                                    custs.forEach { req ->
                                                        val add = perCust + (if (extra > 0) { extra--; 1 } else 0)
                                                        allocationMap[partId to req.customerId] = add
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("توزيع متساوٍ", fontSize = 10.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    // Prioritize by paid amount
                                                    val sortedByPaid = custs.sortedByDescending { it.paidAmount }
                                                    var left = totalRem
                                                    sortedByPaid.forEach { req ->
                                                        val alloc = minOf(req.quantity, left)
                                                        allocationMap[partId to req.customerId] = alloc
                                                        left = maxOf(0, left - alloc)
                                                    }
                                                },
                                                modifier = Modifier.weight(1.2f).height(28.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("حسب المدفوع أولاً", fontSize = 10.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    custs.forEach { req ->
                                                        allocationMap[partId to req.customerId] = 0
                                                    }
                                                },
                                                modifier = Modifier.weight(0.8f).height(28.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("تصفير", fontSize = 10.sp, color = RoseDanger)
                                            }
                                        }
                                    }

                                    // Customers list with quantity requested, paid, and allocation input
                                    Text("العملاء الذين طلبوا هذا الصنف:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    if (custs.isEmpty()) {
                                        Text("لم يتم تحديد عملاء مرتبطين بهذه الطلبية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            custs.forEach { req ->
                                                val custKey = partId to req.customerId
                                                val currentAlloc = allocationMap[custKey] ?: 0

                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.Person, contentDescription = null, tint = TechCyan, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(req.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            }
                                                            Text("مطلوب: ${req.quantity} قطعة", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("المدفوع: ${req.paidAmount} ريال", fontSize = 10.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                                            Text("المتبقي عليه: ${req.remainingAmount} ريال", fontSize = 10.5.sp, color = if (req.remainingAmount > 0) RoseDanger else EmeraldSuccess)
                                                        }

                                                        // Stepper for reorder allocation
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("الكمية المخصصة لإعادة الطلب:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                IconButton(
                                                                    onClick = {
                                                                        if (currentAlloc > 0) {
                                                                            allocationMap[custKey] = currentAlloc - 1
                                                                        }
                                                                    },
                                                                    modifier = Modifier.size(26.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(14.dp))
                                                                }

                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(PrimaryLighter, RoundedCornerShape(6.dp))
                                                                        .padding(horizontal = 12.dp, vertical = 3.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "$currentAlloc",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 13.sp,
                                                                        color = PrimaryBlue
                                                                    )
                                                                }

                                                                IconButton(
                                                                    onClick = {
                                                                        allocationMap[custKey] = currentAlloc + 1
                                                                    },
                                                                    modifier = Modifier.size(26.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(14.dp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDistribution(allocationMap.toMap())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد التوزيع وتحويل إلى مصفوفة الطلبات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
