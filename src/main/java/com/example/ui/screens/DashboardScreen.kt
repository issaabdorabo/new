package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.data.model.PartWithInventory
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight

@Composable
fun DashboardScreen(
    partsWithInventory: List<PartWithInventory>,
    pendingSupplierOrdersCount: Int,
    activeCustomersCount: Int,
    activePartiesCount: Int,
    recentTransactions: List<TransactionEntity>,
    onNavigateToOrders: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToIssue: () -> Unit,
    onQuickAddPart: () -> Unit,
    onRestockPart: (PartWithInventory) -> Unit
) {
    val totalPiecesInStock = partsWithInventory.sumOf { it.availableQty }
    val lowStockItems = partsWithInventory.filter { it.isLowStock }

    // Car distribution map
    val carDistribution = partsWithInventory
        .groupBy { it.part.carType }
        .mapValues { entry -> entry.value.sumOf { it.availableQty } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Welcome Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryBlue, Color(0xFF1E40AF), Color(0xFF2563EB))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مرحباً بك في قطع غيار Pro",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "نظام متكامل لإدارة المخزون، الطلبات، والصرف دون اتصال",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onQuickAddPart,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_add_part")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة قطعة", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToIssue,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_issue")
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صرف سريع", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4 Stat Cards
        item {
            Text(
                text = "📊 إحصائيات عامة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي المخزون",
                    value = "$totalPiecesInStock قطعة",
                    subtitle = "${partsWithInventory.size} أصناف مسجلة",
                    icon = Icons.Default.Inventory2,
                    containerColor = PrimaryLighter,
                    iconColor = PrimaryBlue,
                    onClick = onNavigateToInventory
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "نقص المخزون",
                    value = "${lowStockItems.size} صنف",
                    subtitle = if (lowStockItems.isEmpty()) "المخزون ممتاز" else "يحتاج إعادة طلب!",
                    icon = Icons.Default.Warning,
                    containerColor = if (lowStockItems.isNotEmpty()) RoseLight else EmeraldLight,
                    iconColor = if (lowStockItems.isNotEmpty()) RoseDanger else EmeraldSuccess,
                    onClick = onNavigateToInventory
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "شحنات الموردين",
                    value = "$pendingSupplierOrdersCount معلقة",
                    subtitle = "قيد المتابعة والتوريد",
                    icon = Icons.Default.LocalShipping,
                    containerColor = AmberLight,
                    iconColor = AmberSecondary,
                    onClick = onNavigateToOrders
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "العملاء والجهات",
                    value = "${activeCustomersCount + activePartiesCount}",
                    subtitle = "$activeCustomersCount عميل / $activePartiesCount جهة",
                    icon = Icons.Default.People,
                    containerColor = EmeraldLight,
                    iconColor = EmeraldSuccess,
                    onClick = onNavigateToOrders
                )
            }
        }

        // Smart Alert for Low Stock
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RoseLight.copy(alpha = 0.6f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RoseDanger, AmberSecondary)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger)
                            Text(
                                text = "تنبيه ذكي: أصناف وصلت للحد الأدنى (${lowStockItems.size})",
                                fontWeight = FontWeight.Bold,
                                color = RoseDanger,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lowStockItems.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.part.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${item.part.carType} - ${item.part.carModel} (المتبقي: ${item.availableQty} / الأدنى: ${item.part.minStock})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onRestockPart(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("طلب توريد", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Distribution by Car Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚗 توزيع المخزون حسب نوع السيارة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (carDistribution.isEmpty()) {
                        Text("لا توجد قطع مسجلة حالياً", style = MaterialTheme.typography.bodySmall)
                    } else {
                        val maxQty = carDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                        carDistribution.entries.sortedByDescending { it.value }.take(5).forEach { (car, qty) ->
                            val progress = qty.toFloat() / maxQty.toFloat()
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = car, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(text = "$qty قطعة", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryBlue,
                                    trackColor = PrimaryLighter
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit Movements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 آخر حركات المستودع (Audit Log)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (recentTransactions.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مسجلة بعد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        recentTransactions.take(4).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (badgeColor, sign) = when (tx.type) {
                                    "in" -> Pair(EmeraldSuccess, "+")
                                    "out" -> Pair(RoseDanger, "-")
                                    else -> Pair(AmberSecondary, "~")
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sign,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = tx.note, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = "$sign${tx.qty}",
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
