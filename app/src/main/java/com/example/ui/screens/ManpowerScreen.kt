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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ManpowerEntity
import com.example.ui.components.PermissionDeniedNotice
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.GarmentsViewModel

@Composable
fun ManpowerScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val operators by viewModel.manpower.collectAsState()
    val canEdit = viewModel.canEditManpower()
    var selectedLineFilter by remember { mutableStateOf("All Lines") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredOps = operators.filter {
        selectedLineFilter == "All Lines" || it.lineNo == selectedLineFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("manpower_screen"),
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_operator_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Operator")
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
                    text = "Operator & Manpower Allocation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Floor line staffing, skill grading, attendance & machine allocation",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            if (!canEdit) {
                item {
                    PermissionDeniedNotice(
                        requiredAction = "Manpower Allocation Modifications",
                        allowedRoles = "Admin, Production Officer, Line Chief"
                    )
                }
            }

            // Line Filter
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

            // Summary of Manpower
            item {
                val total = filteredOps.size
                val present = filteredOps.count { it.isPresent }
                val absent = total - present
                val gradeA = filteredOps.count { it.skillGrade.startsWith("A") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL ROSTER", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("PRESENT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$present", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                        }
                        Column {
                            Text("ABSENT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$absent", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (absent > 0) ErrorRed else Slate700)
                        }
                        Column {
                            Text("A/A+ GRADE", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$gradeA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                        }
                    }
                }
            }

            // Operators List
            items(filteredOps, key = { it.id }) { op ->
                OperatorCard(
                    operator = op,
                    canEdit = canEdit,
                    onToggleAttendance = { viewModel.toggleOperatorAttendance(op) },
                    onDelete = { viewModel.deleteOperator(op) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddOperatorDialog(
            defaultLine = if (selectedLineFilter == "All Lines") "Line 1" else selectedLineFilter,
            onDismiss = { showAddDialog = false },
            onSave = { name, code, line, op, grade, machine ->
                viewModel.addOperator(name, code, line, op, grade, machine)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun OperatorCard(
    operator: ManpowerEntity,
    canEdit: Boolean,
    onToggleAttendance: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("operator_card_${operator.id}"),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = operator.name.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = operator.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ID: ${operator.operatorId}  |  Line: ${operator.lineNo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (gBg, gFg) = when (operator.skillGrade) {
                        "A+" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                        "A" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
                        "B" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                        else -> Slate100 to Slate700
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = gBg) {
                        Text(
                            text = "Grade ${operator.skillGrade}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = gFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (canEdit) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Operation", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(operator.assignedOperation, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column {
                    Text("Machine", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(operator.machineType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (operator.isPresent) "Present" else "Absent",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (operator.isPresent) SuccessGreen else ErrorRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = operator.isPresent,
                        onCheckedChange = { if (canEdit) onToggleAttendance() },
                        enabled = canEdit,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddOperatorDialog(
    defaultLine: String,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, line: String, operation: String, grade: String, machine: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("OP-110") }
    var line by remember { mutableStateOf(defaultLine) }
    var operation by remember { mutableStateOf("Collar Join") }
    var grade by remember { mutableStateOf("A") }
    var machine by remember { mutableStateOf("SNLS (Lockstitch)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_operator_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Register Operator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Operator Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Operator ID / Badge") },
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

                OutlinedTextField(
                    value = operation,
                    onValueChange = { operation = it },
                    label = { Text("Assigned Operation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Skill Grade (A+, A, B, C)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = machine,
                        onValueChange = { machine = it },
                        label = { Text("Machine Specialty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, code, line, operation, grade, machine) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save Operator")
                    }
                }
            }
        }
    }
}
