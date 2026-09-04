package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PolishOutlineVariant
import com.example.ui.theme.PolishSurfaceLow
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondaryContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.HourlyChartItem
import com.example.ui.viewmodel.LineProductionChartItem
import java.util.Locale

@Composable
fun HourlyProductionChart(
    data: List<HourlyChartItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PolishOutlineVariant),
        colors = CardDefaults.cardColors(containerColor = PolishSurfaceLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOURLY PRODUCTION TREND",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = PolishTextSecondary
                    )
                    Text(
                        text = "Actual vs Target per Hour",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextSecondary.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PurplePrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Good", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PolishOutlineVariant))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val maxVal = (data.maxOfOrNull { maxOf(it.target, it.actual) } ?: 200).coerceAtLeast(100)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val goodFraction = (item.good.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
                    val targetFraction = (item.target.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "${item.good}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (item.good >= item.target) SuccessGreen else PurplePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.height(100.dp)
                        ) {
                            // Target bar
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight(targetFraction)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(PolishOutlineVariant)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            // Good Output bar
                            val barColor = if (item.good >= item.target) SuccessGreen else PurplePrimary
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight(goodFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.hourLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = PolishTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineProductionChart(
    data: List<LineProductionChartItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PolishOutlineVariant),
        colors = CardDefaults.cardColors(containerColor = PolishSurfaceLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "LINE-WISE PRODUCTION & EFFICIENCY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PolishTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            val maxTarget = (data.maxOfOrNull { it.target } ?: 1500).coerceAtLeast(500)

            data.forEach { item ->
                val fraction = (item.goodOutput.toFloat() / maxTarget.toFloat()).coerceIn(0f, 1f)
                val animatedFraction by animateFloatAsState(targetValue = fraction, label = "line_bar")

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.lineNo,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PolishTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${item.goodOutput} / ${item.target} pcs",
                                style = MaterialTheme.typography.bodySmall,
                                color = PolishTextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val effColor = if (item.efficiency >= 80) SuccessGreen else if (item.efficiency >= 70) WarningAmber else ErrorRed
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = effColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", item.efficiency)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = effColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(PurpleSecondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (item.efficiency >= 80) SuccessGreen else PurplePrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputVsOutputProgressCard(
    totalInput: Int,
    goodOutput: Int,
    alter: Int,
    reject: Int,
    wipBalance: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PolishOutlineVariant),
        colors = CardDefaults.cardColors(containerColor = PolishSurfaceLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INPUT VS OUTPUT FLOW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PolishTextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PurpleContainer
                ) {
                    Text(
                        text = "Total Input: $totalInput pcs",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurpleOnContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val safeTotal = totalInput.coerceAtLeast(1).toFloat()
            val goodRatio = (goodOutput.toFloat() / safeTotal).coerceIn(0f, 1f)
            val alterRatio = (alter.toFloat() / safeTotal).coerceIn(0f, 1f)
            val rejectRatio = (reject.toFloat() / safeTotal).coerceIn(0f, 1f)
            val wipRatio = (wipBalance.toFloat() / safeTotal).coerceIn(0f, 1f)

            // Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PolishOutlineVariant.copy(alpha = 0.5f))
            ) {
                if (goodRatio > 0.01f) {
                    Box(modifier = Modifier.weight(goodRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(SuccessGreen))
                }
                if (alterRatio > 0.01f) {
                    Box(modifier = Modifier.weight(alterRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(WarningAmber))
                }
                if (rejectRatio > 0.01f) {
                    Box(modifier = Modifier.weight(rejectRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(ErrorRed))
                }
                if (wipRatio > 0.01f) {
                    Box(modifier = Modifier.weight(wipRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(PurplePrimary))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(label = "Good Output", value = "$goodOutput", color = SuccessGreen)
                LegendItem(label = "Alter", value = "$alter", color = WarningAmber)
                LegendItem(label = "Reject", value = "$reject", color = ErrorRed)
                LegendItem(label = "Floor WIP", value = "$wipBalance", color = PurplePrimary)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = PolishTextPrimary,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
