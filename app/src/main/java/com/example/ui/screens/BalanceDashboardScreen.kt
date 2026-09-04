package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InputVsOutputProgressCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.GarmentsViewModel
import java.util.Locale

@Composable
fun BalanceDashboardScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.balanceSummary.collectAsState()
    val inputs by viewModel.inputReceives.collectAsState()
    val outputs by viewModel.productionOutputs.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Line-wise, 1: Style/PO-wise

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("balance_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Input–Output Balance Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Real-time floor balance, WIP reconciliation and scrap monitoring",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        // 3. Prompt Specific Balance Dashboard Table Example
        // Item | Qty
        // Total Input | 10,000
        // Total Output | 7,500
        // Alter | 150
        // Reject | 50
        // WIP/Balance | 2,300
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("balance_example_table"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXECUTIVE BALANCE RECONCILIATION",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = PrimaryBlue
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Balanced",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Item", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Slate700)
                        Text("Qty (pcs)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Slate700)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 1: Total Input
                    BalanceTableRow(
                        label = "Total Input",
                        qty = balance.totalInput,
                        color = AccentCyan,
                        badge = "100%"
                    )
                    HorizontalDivider(color = Slate100, thickness = 1.dp)

                    // Row 2: Total Output
                    val safeIn = balance.totalInput.coerceAtLeast(1).toDouble()
                    val outPct = (balance.totalOutput.toDouble() / safeIn) * 100.0
                    BalanceTableRow(
                        label = "Total Output",
                        qty = balance.totalOutput,
                        color = SuccessGreen,
                        badge = "${String.format(Locale.US, "%.1f", outPct)}%"
                    )
                    HorizontalDivider(color = Slate100, thickness = 1.dp)

                    // Row 3: Alter
                    val alterPct = (balance.totalAlter.toDouble() / safeIn) * 100.0
                    BalanceTableRow(
                        label = "Alter",
                        qty = balance.totalAlter,
                        color = WarningAmber,
                        badge = "${String.format(Locale.US, "%.1f", alterPct)}%"
                    )
                    HorizontalDivider(color = Slate100, thickness = 1.dp)

                    // Row 4: Reject
                    val rejPct = (balance.totalReject.toDouble() / safeIn) * 100.0
                    BalanceTableRow(
                        label = "Reject",
                        qty = balance.totalReject,
                        color = ErrorRed,
                        badge = "${String.format(Locale.US, "%.1f", rejPct)}%"
                    )
                    HorizontalDivider(color = Slate100, thickness = 1.dp)

                    // Row 5: WIP/Balance
                    val wipPct = (balance.wipBalance.toDouble() / safeIn) * 100.0
                    BalanceTableRow(
                        label = "WIP / Balance",
                        qty = balance.wipBalance,
                        color = PrimaryBlue,
                        badge = "${String.format(Locale.US, "%.1f", wipPct)}%",
                        isHighlighted = true
                    )
                }
            }
        }

        // Visual Progress Card
        item {
            InputVsOutputProgressCard(
                totalInput = balance.totalInput,
                goodOutput = balance.totalOutput,
                alter = balance.totalAlter,
                reject = balance.totalReject,
                wipBalance = balance.wipBalance
            )
        }

        // Tabs: Line-wise Balance vs Style/PO Balance
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Line-Wise Balance", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Style & PO Balance", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Line-Wise Breakdown
            val lines = listOf("Line 1", "Line 2", "Line 3", "Line 4")
            items(lines) { line ->
                val lineInputs = inputs.filter { it.lineNo == line }
                val lineOutputs = outputs.filter { it.lineNo == line }
                val lineIn = lineInputs.sumOf { it.totalInput }
                val lineOut = lineOutputs.sumOf { it.goodOutput }
                val lineAlt = lineOutputs.sumOf { it.alterQty }
                val lineRej = lineOutputs.sumOf { it.rejectQty }
                val lineWip = (lineIn - lineOut - lineAlt - lineRej).coerceAtLeast(0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(line, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (lineWip > 0) PrimaryBlue.copy(alpha = 0.1f) else Slate100
                            ) {
                                Text(
                                    text = "Floor WIP: $lineWip pcs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Input", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$lineIn", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("Good Out", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$lineOut", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                            }
                            Column {
                                Text("Alter", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$lineAlt", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = WarningAmber)
                            }
                            Column {
                                Text("Reject", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$lineRej", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                            }
                            Column {
                                Text("WIP / Bal", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$lineWip", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                            }
                        }
                    }
                }
            }
        } else {
            // Style / PO Breakdown
            val styleGroups = inputs.groupBy { it.styleNo }
            items(styleGroups.keys.toList()) { styleNo ->
                val styleInputs = inputs.filter { it.styleNo == styleNo }
                val styleOutputs = outputs.filter { it.styleNo == styleNo }
                val sIn = styleInputs.sumOf { it.totalInput }
                val sOut = styleOutputs.sumOf { it.goodOutput }
                val sAlt = styleOutputs.sumOf { it.alterQty }
                val sRej = styleOutputs.sumOf { it.rejectQty }
                val sWip = (sIn - sOut - sAlt - sRej).coerceAtLeast(0)
                val buyer = styleInputs.firstOrNull()?.buyer ?: "N/A"
                val po = styleInputs.firstOrNull()?.poNo ?: "N/A"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(styleNo, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Buyer: $buyer  |  PO: $po", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Balance: $sWip pcs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Input: $sIn", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("Output: $sOut", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = SuccessGreen)
                            Text("Alter: $sAlt", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                            Text("Reject: $sRej", style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BalanceTableRow(
    label: String,
    qty: Int,
    color: Color,
    badge: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isHighlighted) Modifier.background(PrimaryBlue.copy(alpha = 0.06f), shape = RoundedCornerShape(6.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium
                ),
                color = if (isHighlighted) PrimaryNavy else MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$qty",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
