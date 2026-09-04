package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductionOutputEntity
import com.example.ui.components.PermissionDeniedNotice
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.GarmentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductionOutputScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val outputs by viewModel.productionOutputs.collectAsState()
    val canEnterOutput = viewModel.canProductionOutput()
    val canQc = viewModel.canQCEntry()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLineFilter by remember { mutableStateOf("All Lines") }

    val filteredOutputs = outputs.filter {
        selectedLineFilter == "All Lines" || it.lineNo == selectedLineFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("production_output_screen"),
        floatingActionButton = {
            if (canEnterOutput) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_output_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Output")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Production Output Register",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hour-by-hour line output, alter, reject & achievement %",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            if (!canEnterOutput) {
                item {
                    PermissionDeniedNotice(
                        requiredAction = "Production Output Entry",
                        allowedRoles = "Admin, Production Officer, Line Chief"
                    )
                }
            }

            // Quick Line Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All Lines", "Line 1", "Line 2", "Line 3", "Line 4").forEach { line ->
                        FilterChip(
                            selected = selectedLineFilter == line,
                            onClick = { selectedLineFilter = line },
                            label = { Text(line, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Production Summary for Filtered View
            item {
                val totalTarget = filteredOutputs.sumOf { it.hourlyTarget }
                val totalActual = filteredOutputs.sumOf { it.actualOutput }
                val totalAlter = filteredOutputs.sumOf { it.alterQty }
                val totalReject = filteredOutputs.sumOf { it.rejectQty }
                val totalGood = filteredOutputs.sumOf { it.goodOutput }
                val avgAch = if (totalTarget > 0) (totalGood.toDouble() / totalTarget.toDouble()) * 100.0 else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TARGET", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$totalTarget", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("ACTUAL", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$totalActual", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                            }
                            Column {
                                Text("GOOD OUTPUT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$totalGood", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                            }
                            Column {
                                Text("ACHIEVEMENT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("${String.format(Locale.US, "%.1f", avgAch)}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (avgAch >= 95) SuccessGreen else WarningAmber)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Total Alter: $totalAlter pcs (${String.format(Locale.US, "%.1f", if (totalActual > 0) totalAlter * 100.0 / totalActual else 0.0)}%)", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                            Text("Total Reject: $totalReject pcs (${String.format(Locale.US, "%.1f", if (totalActual > 0) totalReject * 100.0 / totalActual else 0.0)}%)", style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                        }
                    }
                }
            }

            // List of Outputs
            if (filteredOutputs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Production Output Recorded", fontWeight = FontWeight.SemiBold, color = Slate500)
                            Text("Tap '+' to log hourly output", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                    }
                }
            } else {
                items(filteredOutputs, key = { it.id }) { output ->
                    ProductionOutputCard(
                        output = output,
                        canDelete = canEnterOutput,
                        onDelete = { viewModel.deleteProductionOutput(output) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddProductionOutputDialog(
            canQc = canQc,
            onDismiss = { showAddDialog = false },
            onSave = { date, line, style, po, hourLabel, hourNum, target, actual, alter, reject, qc, remarks ->
                viewModel.addProductionOutput(
                    date = date,
                    lineNo = line,
                    styleNo = style,
                    poNo = po,
                    hourLabel = hourLabel,
                    hourNumber = hourNum,
                    hourlyTarget = target,
                    actualOutput = actual,
                    alterQty = alter,
                    rejectQty = reject,
                    qcInspector = qc,
                    remarks = remarks
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProductionOutputCard(
    output: ProductionOutputEntity,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("production_output_card_${output.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = output.lineNo,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = output.hourLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val achColor = if (output.achievementPercent >= 100) SuccessGreen else if (output.achievementPercent >= 85) WarningAmber else ErrorRed
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = achColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", output.achievementPercent)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = achColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Style: ${output.styleNo}  |  PO: ${output.poNo}  |  Date: ${output.date}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, shape = RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${output.hourlyTarget}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Actual", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${output.actualOutput}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                }
                Column {
                    Text("Alter", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${output.alterQty}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = WarningAmber)
                }
                Column {
                    Text("Reject", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${output.rejectQty}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                }
                Column {
                    Text("Good Output", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${output.goodOutput}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Automatic Achievement calculation formula banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDF4), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Automatic: Achievement % = Good Output (${output.goodOutput}) ÷ Target (${output.hourlyTarget}) × 100",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF166534)
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", output.achievementPercent)}%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Entered by: ${output.enteredBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                if (output.qcInspector.isNotEmpty()) {
                    Text(
                        text = "QC: ${output.qcInspector}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductionOutputDialog(
    canQc: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        date: String,
        line: String,
        style: String,
        po: String,
        hourLabel: String,
        hourNum: Int,
        target: Int,
        actual: Int,
        alter: Int,
        reject: Int,
        qc: String,
        remarks: String
    ) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(currentDate) }
    var line by remember { mutableStateOf("Line 1") }
    var style by remember { mutableStateOf("ST-POLO-802") }
    var po by remember { mutableStateOf("PO-88210") }
    var hourNum by remember { mutableStateOf(1) }
    var hourLabel by remember { mutableStateOf("Hour 1 (08:00 - 09:00)") }
    var targetStr by remember { mutableStateOf("160") }
    var actualStr by remember { mutableStateOf("162") }
    var alterStr by remember { mutableStateOf("12") }
    var rejectStr by remember { mutableStateOf("3") }
    var qcInspector by remember { mutableStateOf("Fatima Begum (QC)") }
    var remarks by remember { mutableStateOf("") }

    val target = targetStr.toIntOrNull() ?: 160
    val actual = actualStr.toIntOrNull() ?: 0
    val alter = alterStr.toIntOrNull() ?: 0
    val reject = rejectStr.toIntOrNull() ?: 0
    val goodOutput = (actual - alter - reject).coerceAtLeast(0)
    val achPercent = if (target > 0) (goodOutput.toDouble() / target.toDouble()) * 100.0 else 0.0

    val hoursList = listOf(
        1 to "Hour 1 (08:00 - 09:00)",
        2 to "Hour 2 (09:00 - 10:00)",
        3 to "Hour 3 (10:00 - 11:00)",
        4 to "Hour 4 (11:00 - 12:00)",
        5 to "Hour 5 (13:00 - 14:00)",
        6 to "Hour 6 (14:00 - 15:00)",
        7 to "Hour 7 (15:00 - 16:00)",
        8 to "Hour 8 (16:00 - 17:00)",
        9 to "Hour 9 (17:00 - 18:00)",
        10 to "Hour 10 (18:00 - 19:00)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_output_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "New Production Output",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enter hourly output count and inspection counts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = line,
                        onValueChange = { line = it },
                        label = { Text("Line") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = style,
                        onValueChange = { style = it },
                        label = { Text("Style") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = po,
                        onValueChange = { po = it },
                        label = { Text("PO No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hour Selector dropdown
                var hourExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = hourExpanded,
                    onExpandedChange = { hourExpanded = it }
                ) {
                    OutlinedTextField(
                        value = hourLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hour") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hourExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = hourExpanded,
                        onDismissRequest = { hourExpanded = false }
                    ) {
                        hoursList.forEach { (hNum, hLabel) ->
                            DropdownMenuItem(
                                text = { Text(hLabel) },
                                onClick = {
                                    hourNum = hNum
                                    hourLabel = hLabel
                                    hourExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Hourly Target") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = actualStr,
                        onValueChange = { actualStr = it },
                        label = { Text("Actual Output") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alterStr,
                        onValueChange = { alterStr = it },
                        label = { Text("Alter (Rework)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rejectStr,
                        onValueChange = { rejectStr = it },
                        label = { Text("Reject (Defect)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Automatic Good Output & Achievement % Calculation Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Good Output (Actual − Alter − Reject):",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "$goodOutput pcs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Automatic: Achievement % (Good ÷ Target × 100):",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", achPercent)}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (achPercent >= 95) SuccessGreen else WarningAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qcInspector,
                    onValueChange = { qcInspector = it },
                    label = { Text("QC Inspector Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Alter Reasons") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val t = targetStr.toIntOrNull() ?: 160
                            val a = actualStr.toIntOrNull() ?: 0
                            val alt = alterStr.toIntOrNull() ?: 0
                            val rej = rejectStr.toIntOrNull() ?: 0
                            onSave(date, line, style, po, hourLabel, hourNum, t, a, alt, rej, qcInspector, remarks)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("save_output_button")
                    ) {
                        Text("Save Production Output")
                    }
                }
            }
        }
    }
}
