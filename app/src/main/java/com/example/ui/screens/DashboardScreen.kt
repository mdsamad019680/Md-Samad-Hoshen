package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HourlyProductionChart
import com.example.ui.components.InputVsOutputProgressCard
import com.example.ui.components.LineProductionChart
import com.example.ui.components.MetricKpiCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PolishOutlineVariant
import com.example.ui.theme.PolishSurfaceLow
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondaryContainer
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.GarmentsViewModel
import com.example.ui.viewmodel.NavScreen
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val balance by viewModel.balanceSummary.collectAsState()
    val hourlyData by viewModel.hourlyChartData.collectAsState()
    val lineData by viewModel.lineProductionData.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    val operations by viewModel.operations.collectAsState()

    val bottleneckOp = operations.find { it.isBottleneck }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Top Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Factory Production Overview",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "Real-time sewing floor input, output, WIP balance & line performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextSecondary
                    )
                }
            }
        }

        // Active Bottleneck Alert Banner if present
        if (bottleneckOp != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bottleneck_alert_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BOTTLENECK DETECTED: ${bottleneckOp.operationName} (${bottleneckOp.lineNo})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "${bottleneckOp.bottleneckReason}. Recommendation: ${bottleneckOp.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Button(
                            onClick = { viewModel.navigateTo(NavScreen.LINE_BALANCING) },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Balance", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 6 Top KPI Cards as requested:
        // Today's Input | Today's Output | WIP | Efficiency | Target | Achievement
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Today's Input & Today's Output
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Today's Input",
                        value = "${metrics.todayInput} pcs",
                        subtitle = "Cutting issued to floor",
                        icon = Icons.Default.Input,
                        accentColor = PurplePrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_today_input"
                    )
                    MetricKpiCard(
                        title = "Today's Output",
                        value = "${metrics.todayOutput} pcs",
                        subtitle = "Passed good garments",
                        icon = Icons.Default.PrecisionManufacturing,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_today_output"
                    )
                }

                // Row 2: WIP & Efficiency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Floor WIP / Balance",
                        value = "${metrics.totalWip} pcs",
                        subtitle = "Active pieces in sewing lines",
                        icon = Icons.Default.Balance,
                        accentColor = PurplePrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_wip_balance"
                    )
                    MetricKpiCard(
                        title = "Line Efficiency",
                        value = "${String.format(Locale.US, "%.1f", metrics.averageEfficiency)}%",
                        subtitle = "Plant average SMV output",
                        icon = Icons.Default.Speed,
                        accentColor = if (metrics.averageEfficiency >= 80) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_efficiency"
                    )
                }

                // Row 3: Target & Achievement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Daily Target",
                        value = "${metrics.dailyTarget} pcs",
                        subtitle = "Combined shift goal",
                        icon = Icons.Default.Assessment,
                        accentColor = PolishTextSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_target"
                    )
                    MetricKpiCard(
                        title = "Achievement",
                        value = "${String.format(Locale.US, "%.1f", metrics.achievementPercent)}%",
                        subtitle = if (metrics.achievementPercent >= 95) "On schedule" else "Under target",
                        icon = Icons.Default.CheckCircle,
                        accentColor = if (metrics.achievementPercent >= 95) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_achievement"
                    )
                }
            }
        }

        // Segmented Input vs Output Progress Bar
        item {
            InputVsOutputProgressCard(
                totalInput = balance.totalInput,
                goodOutput = balance.totalOutput,
                alter = balance.totalAlter,
                reject = balance.totalReject,
                wipBalance = balance.wipBalance
            )
        }

        // Line Filter Selector for Hourly Trend
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Hourly Chart:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = PolishTextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Line 1", "Line 2").forEach { line ->
                        FilterChip(
                            selected = selectedLine == line,
                            onClick = { viewModel.setSelectedLine(line) },
                            label = { Text(line, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleSecondaryContainer,
                                selectedLabelColor = PurplePrimary
                            )
                        )
                    }
                }
            }
        }

        // Hourly Production Chart (Hours 1 to 8)
        item {
            HourlyProductionChart(data = hourlyData)
        }

        // Line-wise production comparison
        item {
            LineProductionChart(data = lineData)
        }

        // Quick Navigation Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishOutlineVariant),
                colors = CardDefaults.cardColors(containerColor = PolishSurfaceLow)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "QUICK SHORTCUTS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = PolishTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(NavScreen.INPUT_RECEIVE) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.navigateTo(NavScreen.PRODUCTION_OUTPUT) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Output", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(NavScreen.LINE_BALANCING) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Balancing", fontSize = 12.sp)
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
