package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.GarmentsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inputs by viewModel.inputReceives.collectAsState()
    val outputs by viewModel.productionOutputs.collectAsState()
    val operations by viewModel.operations.collectAsState()
    val balance by viewModel.balanceSummary.collectAsState()

    val reportTypes = listOf(
        "Daily Production Report",
        "Monthly Production Report",
        "Input Receiving Report",
        "Line Efficiency Report",
        "Balance / WIP Report",
        "Style & PO Report"
    )

    var selectedReport by remember { mutableStateOf(reportTypes[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Factory Reports & Analytics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Generate and export production, input-output reconciliation and efficiency data",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        // Report Selector & Export Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT REPORT TEMPLATE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedReport,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            reportTypes.forEach { report ->
                                DropdownMenuItem(
                                    text = { Text(report) },
                                    onClick = {
                                        selectedReport = report
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportReportCSV(context, selectedReport)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("export_csv_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.exportReportCSV(context, selectedReport)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("share_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share File")
                        }
                    }
                }
            }
        }

        // Live Table Preview of the Selected Report
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedReport.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                        Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                            Text(
                                text = "Live Data Preview",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedReport) {
                        "Daily Production Report", "Monthly Production Report" -> {
                            ProductionReportTable(outputs = outputs)
                        }
                        "Input Receiving Report" -> {
                            InputReportTable(inputs = inputs)
                        }
                        "Line Efficiency Report" -> {
                            EfficiencyReportTable(operations = operations)
                        }
                        "Balance / WIP Report" -> {
                            WipReportTable(balance = balance, inputs = inputs, outputs = outputs)
                        }
                        "Style & PO Report" -> {
                            StyleReportTable(inputs = inputs, outputs = outputs)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProductionReportTable(outputs: List<com.example.data.model.ProductionOutputEntity>) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hour / Line", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
            Text("Target", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Actual", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Good", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Ach %", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        outputs.take(8).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.hourLabel.substringBefore(" (")} (${item.lineNo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                Text("${item.hourlyTarget}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${item.actualOutput}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${item.goodOutput}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SuccessGreen, modifier = Modifier.weight(1f))
                Text("${String.format(Locale.US, "%.1f", item.achievementPercent)}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Slate100, thickness = 1.dp)
        }
    }
}

@Composable
fun InputReportTable(inputs: List<com.example.data.model.InputReceiveEntity>) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Style / Line", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
            Text("Cutting", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Received", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Total In", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        inputs.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.styleNo} (${item.lineNo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                Text("${item.cuttingInputQty}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${item.receivedQty}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${item.totalInput}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Slate100, thickness = 1.dp)
        }
    }
}

@Composable
fun EfficiencyReportTable(operations: List<com.example.data.model.OperationEntity>) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Operation", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
            Text("SMV", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(0.8f))
            Text("MP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(0.6f))
            Text("Eff %", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Status", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        operations.forEach { op ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(op.operationName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                Text("${op.smv}m", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
                Text("${op.manpower}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.6f))
                Text("${String.format(Locale.US, "%.1f", op.efficiencyPercent)}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                Text(if (op.isBottleneck) "🔴 Bottleneck" else "🟢 OK", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Slate100, thickness = 1.dp)
        }
    }
}

@Composable
fun WipReportTable(
    balance: com.example.ui.viewmodel.BalanceSummary,
    inputs: List<com.example.data.model.InputReceiveEntity>,
    outputs: List<com.example.data.model.ProductionOutputEntity>
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Line", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("Input", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("Output", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("Alter/Rej", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("Floor WIP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(4.dp))

        listOf("Line 1", "Line 2", "Line 3", "Line 4").forEach { line ->
            val lIn = inputs.filter { it.lineNo == line }.sumOf { it.totalInput }
            val lOut = outputs.filter { it.lineNo == line }.sumOf { it.goodOutput }
            val lAlt = outputs.filter { it.lineNo == line }.sumOf { it.alterQty + it.rejectQty }
            val lWip = (lIn - lOut - lAlt).coerceAtLeast(0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(line, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                Text("$lIn", style = MaterialTheme.typography.bodySmall)
                Text("$lOut", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                Text("$lAlt", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                Text("$lWip", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
            }
            HorizontalDivider(color = Slate100, thickness = 1.dp)
        }
    }
}

@Composable
fun StyleReportTable(
    inputs: List<com.example.data.model.InputReceiveEntity>,
    outputs: List<com.example.data.model.ProductionOutputEntity>
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Style No", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
            Text("Buyer", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Input", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("Output", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("WIP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        inputs.groupBy { it.styleNo }.forEach { (styleNo, items) ->
            val buyer = items.firstOrNull()?.buyer ?: ""
            val sIn = items.sumOf { it.totalInput }
            val sOut = outputs.filter { it.styleNo == styleNo }.sumOf { it.goodOutput }
            val sWip = (sIn - sOut).coerceAtLeast(0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(styleNo, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                Text(buyer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("$sIn", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("$sOut", style = MaterialTheme.typography.bodySmall, color = SuccessGreen, modifier = Modifier.weight(1f))
                Text("$sWip", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Slate100, thickness = 1.dp)
        }
    }
}
