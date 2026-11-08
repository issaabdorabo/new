package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.ui.util.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RoseDanger

import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Notifications

data class NavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

fun getNavigationItems(strings: com.example.ui.util.StringsDictionary): List<NavItem> = listOf(
    NavItem(strings.navHome, Icons.Default.Home, "nav_home"),
    NavItem(strings.navOrders, Icons.Default.Assignment, "nav_orders"),
    NavItem(strings.navInventory, Icons.Default.Inventory2, "nav_inventory"),
    NavItem(strings.navDispense, Icons.Default.ShoppingCartCheckout, "nav_issue"),
    NavItem(strings.navReports, Icons.Default.BarChart, "nav_reports"),
    NavItem(strings.navSettings, Icons.Default.Settings, "nav_settings")
)

val NavigationItems = getNavigationItems(com.example.ui.util.ArabicStrings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    isOnline: Boolean = true,
    lowStockCount: Int = 0,
    unreadNotificationsCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    val barColor = MaterialTheme.colorScheme.primary

    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Online/Offline status indicator badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isOnline) EmeraldSuccess.copy(alpha = 0.25f) else RoseDanger.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(if (isOnline) EmeraldSuccess else RoseDanger, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "متصل" else "غير متصل",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "الإشعارات",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    if (unreadNotificationsCount > 0) {
                        Badge(
                            containerColor = RoseDanger,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Text(
                                text = if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = barColor
        )
    )
}

/**
 * Floating pill bottom navigation bar with ripple effects and luxury accent indicators
 */
@Composable
fun AppBottomNavBar(
    selectedIndex: Int,
    lowStockCount: Int,
    onSelectIndex: (Int) -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val strings = com.example.ui.util.LocalAppStrings.current
    val navItems = remember(strings) { getNavigationItems(strings) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val itemBgColor by animateColorAsState(
                        targetValue = if (isSelected) activeColor.copy(alpha = 0.14f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pill_bg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "icon_tint"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(itemBgColor)
                            .clickable { onSelectIndex(index) }
                            .padding(vertical = 6.dp)
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (index == 2 && lowStockCount > 0) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd),
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("$lowStockCount", color = Color.White, fontSize = 9.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
