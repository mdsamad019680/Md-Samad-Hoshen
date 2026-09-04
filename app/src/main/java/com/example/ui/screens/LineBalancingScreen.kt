package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OperationEntity
import com.example.ui.components.PermissionDeniedNotice
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
fun LineBalancingScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val operations by viewModel.operations.collectAsState()
    val lineBalances by viewModel.lineBalances.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    val canEdit = viewModel.canEditLineBalancing()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingOperation by remember { mutableStateOf<OperationEntity?>(null) }
    var showSimulator by remember { mutableStateOf(false) }

    val lineOps = operations.filter { it.lineNo == selectedLine }
    val currentBalance = lineBalances.find { it.lineNo == selectedLine }

    // Summary calculations
    val totalSmv = lineOps.sumOf { it.smv }
    val totalManpower = lineOps.sumOf { it.manpower }
    val bottleneckOp = lineOps.find { it.isBottleneck } ?: lineOps.maxByOrNull { it.cycleTimeMinutes }
    val maxCycleTime = lineOps.maxOfOrNull { it.cycleTimeMinutes } ?: 0.65
    val pitchTime = if (totalManpower > 0) totalSmv / totalManpower else 0.65
    val balancingEff = if (maxCycleTime > 0 && totalManpower > 0) {
        (totalSmv / (maxCycleTime * totalManpower)) * 100.0
    } else 82.5

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("line_balancing_screen"),
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_operation_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Operation")
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
                    text = "Line Balancing & Bottleneck Analysis",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Balance SMV, cycle times, operator pitch & eliminate floor bottlenecks",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            if (!canEdit) {
                item {
                    PermissionDeniedNotice(
                        requiredAction = "Line Balancing Modifications",
                        allowedRoles = "Admin, Production Officer, Line Chief"
                    )
                }
            }

            // Line Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Line 1", "Line 2", "Line 3", "Line 4").forEach { line ->
                        FilterChip(
                            selected = selectedLine == line,
                            onClick = { viewModel.setSelectedLine(line) },
                            label = { Text(line, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Line KPI Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("STYLE: ST-POLO-802 ($selectedLine)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Pitch Time: ${String.format(Locale.US, "%.2f", pitchTime)} min  |  Total SMV: ${String.format(Locale.US, "%.2f", totalSmv)} min", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (balancingEff >= 80) SuccessGreen.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Line Eff: ${String.format(Locale.US, "%.1f", balancingEff)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (balancingEff >= 80) SuccessGreen else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Manpower", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("$totalManpower operators", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("Hourly Target", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("160 pcs/hr", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                            }
                            Column {
                                Text("Bottleneck Cycle", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                Text("${String.format(Locale.US, "%.2f", maxCycleTime)} min", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                            }
                        }
                    }
                }
            }

            // Prominent Bottleneck Diagnosis & Adjustment Recommendation
            if (bottleneckOp != null && bottleneckOp.isBottleneck) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("bottleneck_recommendation_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "CRITICAL BOTTLENECK: ${bottleneckOp.operationName}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Cycle Time: ${String.format(Locale.US, "%.2f", bottleneckOp.cycleTimeMinutes)} min (Line Pitch Time is ${String.format(Locale.US, "%.2f", pitchTime)} min)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Manpower Adjustment Action Plan:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Slate700
                                        )
                                        Text(
                                            text = bottleneckOp.recommendation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Visual Pitch Diagram: Bar comparison of each operation's cycle time vs Pitch Time
            item {
                PitchDiagramCard(
                    operations = lineOps,
                    pitchTime = pitchTime
                )
            }

            // Operations Table / Card List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPERATIONS BREAKDOWN (${lineOps.size} STEPS)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Slate500
                    )
                }
            }

            // List of Operations
            items(lineOps, key = { it.id }) { op ->
                OperationDetailCard(
                    operation = op,
                    pitchTime = pitchTime,
                    canEdit = canEdit,
                    onEdit = { editingOperation = op },
                    onDelete = { viewModel.deleteOperation(op) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddEditOperationDialog(
            operation = null,
            lineNo = selectedLine,
            nextSeq = lineOps.size + 1,
            onDismiss = { showAddDialog = false },
            onSave = { op ->
                viewModel.addOrUpdateOperation(
                    id = op.id,
                    lineNo = op.lineNo,
                    styleNo = op.styleNo,
                    sequence = op.sequence,
                    operationName = op.operationName,
                    smv = op.smv,
                    manpower = op.manpower,
                    machineType = op.machineType,
                    actualPerHour = op.actualPerHour
                )
                showAddDialog = false
            }
        )
    }

    if (editingOperation != null) {
        AddEditOperationDialog(
            operation = editingOperation,
            lineNo = selectedLine,
            nextSeq = editingOperation?.sequence ?: 1,
            onDismiss = { editingOperation = null },
            onSave = { op ->
                viewModel.addOrUpdateOperation(
                    id = op.id,
                    lineNo = op.lineNo,
                    styleNo = op.styleNo,
                    sequence = op.sequence,
                    operationName = op.operationName,
                    smv = op.smv,
                    manpower = op.manpower,
                    machineType = op.machineType,
                    actualPerHour = op.actualPerHour
                )
                editingOperation = null
            }
        )
    }
}

@Composable
fun PitchDiagramCard(
    operations: List<OperationEntity>,
    pitchTime: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = "LINE PITCH & CYCLE TIME DIAGRAM",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Slate500
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ErrorRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bottleneck", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Balanced", style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val maxCt = (operations.maxOfOrNull { it.cycleTimeMinutes } ?: 1.0).coerceAtLeast(pitchTime * 1.2)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                operations.forEach { op ->
                    val fraction = (op.cycleTimeMinutes.toFloat() / maxCt.toFloat()).coerceIn(0.1f, 1f)
                    val isBottleneck = op.isBottleneck || op.cycleTimeMinutes > pitchTime

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", op.cycleTimeMinutes),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = if (isBottleneck) ErrorRed else PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isBottleneck) ErrorRed else PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#${op.sequence}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target Pitch Time: ${String.format(Locale.US, "%.2f", pitchTime)} min/pc",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = "Red bars exceed pitch capacity",
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed
                )
            }
        }
    }
}

@Composable
fun OperationDetailCard(
    operation: OperationEntity,
    pitchTime: Double,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBottleneck = operation.isBottleneck || operation.cycleTimeMinutes > pitchTime

    Card(
        modifier = Modifier.fillMaxWidth().testTag("operation_card_${operation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isBottleneck) androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRed) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isBottleneck) ErrorRed else PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${operation.sequence}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = operation.operationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isBottleneck) ErrorRed.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isBottleneck) "🔴 Bottleneck" else "🟢 Balanced",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isBottleneck) ErrorRed else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (canEdit) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate500, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Fields requested:
            // Operation SMV, Manpower, Machine, Target, Actual, Efficiency %
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, shape = RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SMV", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${operation.smv}m", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Manpower", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${operation.manpower}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                }
                Column {
                    Text("Machine", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text(operation.machineType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column {
                    Text("Target/hr", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${operation.targetPerHour}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Actual/hr", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${operation.actualPerHour}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                }
                Column {
                    Text("Efficiency %", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${String.format(Locale.US, "%.1f", operation.efficiencyPercent)}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (operation.efficiencyPercent >= 80) SuccessGreen else WarningAmber)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cycle time vs Pitch Time & Recommendation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isBottleneck) Color(0xFFFEF2F2) else Color(0xFFF0FDF4), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBottleneck) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isBottleneck) ErrorRed else SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBottleneck) operation.recommendation else "Cycle Time: ${String.format(Locale.US, "%.2f", operation.cycleTimeMinutes)}m is within pitch time (${String.format(Locale.US, "%.2f", pitchTime)}m)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isBottleneck) Color(0xFF991B1B) else Color(0xFF166534)
                )
            }
        }
    }
}

@Composable
fun AddEditOperationDialog(
    operation: OperationEntity?,
    lineNo: String,
    nextSeq: Int,
    onDismiss: () -> Unit,
    onSave: (OperationEntity) -> Unit
) {
    var opName by remember { mutableStateOf(operation?.operationName ?: "") }
    var smvStr by remember { mutableStateOf(operation?.smv?.toString() ?: "1.20") }
    var manpowerStr by remember { mutableStateOf(operation?.manpower?.toString() ?: "2") }
    var machineType by remember { mutableStateOf(operation?.machineType ?: "SNLS (Lockstitch)") }
    var actualPerHourStr by remember { mutableStateOf(operation?.actualPerHour?.toString() ?: "155") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_edit_operation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (operation == null) "Add Operation to $lineNo" else "Edit Operation #${operation.sequence}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure SMV, allocated operators, and machine type",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = opName,
                    onValueChange = { opName = it },
                    label = { Text("Operation Name (e.g. Collar Join)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = smvStr,
                        onValueChange = { smvStr = it },
                        label = { Text("SMV (Minutes)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manpowerStr,
                        onValueChange = { manpowerStr = it },
                        label = { Text("Manpower (Operators)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = machineType,
                    onValueChange = { machineType = it },
                    label = { Text("Machine Type (e.g. Overlock, SNLS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = actualPerHourStr,
                    onValueChange = { actualPerHourStr = it },
                    label = { Text("Actual Output per Hour") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val smv = smvStr.toDoubleOrNull() ?: 1.0
                            val mp = manpowerStr.toIntOrNull() ?: 1
                            val actual = actualPerHourStr.toIntOrNull() ?: 100
                            val entity = (operation ?: OperationEntity(
                                lineNo = lineNo,
                                styleNo = "ST-POLO-802",
                                sequence = nextSeq,
                                operationName = opName,
                                smv = smv,
                                manpower = mp,
                                machineType = machineType,
                                targetPerHour = ((60.0 / smv) * mp).toInt(),
                                actualPerHour = actual,
                                efficiencyPercent = (actual.toDouble() / ((60.0 / smv) * mp)) * 100.0,
                                cycleTimeMinutes = smv / mp,
                                isBottleneck = (smv / mp) > 0.65
                            )).copy(
                                operationName = opName,
                                smv = smv,
                                manpower = mp,
                                machineType = machineType,
                                actualPerHour = actual
                            )
                            onSave(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("save_operation_button")
                    ) {
                        Text("Save Operation")
                    }
                }
            }
        }
    }
}
