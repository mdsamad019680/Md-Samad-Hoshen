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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.model.InputReceiveEntity
import com.example.ui.components.PermissionDeniedNotice
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.GarmentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InputReceiveScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val inputs by viewModel.inputReceives.collectAsState()
    val outputs by viewModel.productionOutputs.collectAsState()
    val canInput = viewModel.canInputReceive()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLineFilter by remember { mutableStateOf("All Lines") }

    val filteredInputs = inputs.filter { item ->
        (selectedLineFilter == "All Lines" || item.lineNo == selectedLineFilter) &&
                (searchQuery.isEmpty() ||
                        item.styleNo.contains(searchQuery, ignoreCase = true) ||
                        item.buyer.contains(searchQuery, ignoreCase = true) ||
                        item.poNo.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("input_receive_screen"),
        floatingActionButton = {
            if (canInput) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_input_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Input Receive")
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
                    text = "Input Receive Register",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Log cutting floor issues and track line input balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            if (!canInput) {
                item {
                    PermissionDeniedNotice(
                        requiredAction = "Input Entry",
                        allowedRoles = "Admin, Production Officer"
                    )
                }
            }

            // Executive Input Metrics Summary Card
            item {
                val totalCutting = inputs.sumOf { it.cuttingInputQty }
                val totalReceived = inputs.sumOf { it.receivedQty }
                val totalIn = inputs.sumOf { it.totalInput }
                val totalGoodOut = outputs.sumOf { it.goodOutput }
                val netBalance = (totalIn - totalGoodOut).coerceAtLeast(0)

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
                            Text("CUTTING ISSUED", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$totalCutting pcs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("FLOOR RECEIVED", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$totalReceived pcs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                        }
                        Column {
                            Text("TOTAL INPUT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$totalIn pcs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AccentCyan)
                        }
                        Column {
                            Text("BALANCE", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("$netBalance pcs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                        }
                    }
                }
            }

            // Search and Line Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).testTag("search_input_field"),
                        placeholder = { Text("Search Style, Buyer, PO...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // List of Input Records
            if (filteredInputs.isEmpty()) {
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
                            Icon(Icons.Default.Input, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Input Receive Records Found", fontWeight = FontWeight.SemiBold, color = Slate500)
                            Text("Tap '+' to log new cutting receipt", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                    }
                }
            } else {
                items(filteredInputs, key = { it.id }) { inputItem ->
                    // Calculate automatic line balance for this style
                    val styleOutputs = outputs.filter { it.lineNo == inputItem.lineNo && it.styleNo == inputItem.styleNo }
                    val goodOut = styleOutputs.sumOf { it.goodOutput }
                    val autoBalance = (inputItem.totalInput - goodOut).coerceAtLeast(0)

                    InputReceiveCard(
                        input = inputItem,
                        autoBalance = autoBalance,
                        totalOutput = goodOut,
                        canDelete = canInput,
                        onDelete = { viewModel.deleteInputReceive(inputItem) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddInputReceiveDialog(
            onDismiss = { showAddDialog = false },
            onSave = { date, buyer, styleNo, poNo, color, size, lineNo, cuttingQty, receivedQty, prevBal, receiver, notes ->
                viewModel.addInputReceive(
                    date = date,
                    buyer = buyer,
                    styleNo = styleNo,
                    poNo = poNo,
                    color = color,
                    size = size,
                    lineNo = lineNo,
                    cuttingInputQty = cuttingQty,
                    receivedQty = receivedQty,
                    previousBalance = prevBal,
                    receiverName = receiver,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InputReceiveCard(
    input: InputReceiveEntity,
    autoBalance: Int,
    totalOutput: Int,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("input_receive_item_${input.id}"),
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
                            text = input.lineNo,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = input.styleNo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buyer: ${input.buyer}  |  PO: ${input.poNo}  |  Color: ${input.color}  |  Size: ${input.size}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of Quantities
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, shape = RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cutting Qty", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${input.cuttingInputQty}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Received", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${input.receivedQty}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                }
                Column {
                    Text("Prev Balance", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${input.previousBalance}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Total Input", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate500)
                    Text("${input.totalInput}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AccentCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Automatic Balance = Total Input - Output
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECFDF5), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Automatic: Balance = Total Input ($input.totalInput) − Output ($totalOutput)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF065F46)
                )
                Text(
                    text = "$autoBalance pcs",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Receiver: ${input.receiverName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                Text(
                    text = "Date: ${input.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInputReceiveDialog(
    onDismiss: () -> Unit,
    onSave: (
        date: String,
        buyer: String,
        styleNo: String,
        poNo: String,
        color: String,
        size: String,
        lineNo: String,
        cuttingQty: Int,
        receivedQty: Int,
        prevBal: Int,
        receiver: String,
        notes: String
    ) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(currentDate) }
    var buyer by remember { mutableStateOf("H&M") }
    var styleNo by remember { mutableStateOf("ST-POLO-802") }
    var poNo by remember { mutableStateOf("PO-88210") }
    var color by remember { mutableStateOf("Navy Blue") }
    var size by remember { mutableStateOf("M") }
    var lineNo by remember { mutableStateOf("Line 1") }
    var cuttingQtyStr by remember { mutableStateOf("1000") }
    var receivedQtyStr by remember { mutableStateOf("1000") }
    var prevBalStr by remember { mutableStateOf("0") }
    var receiver by remember { mutableStateOf("Rahim Ahmed (Prod Officer)") }
    var notes by remember { mutableStateOf("") }

    val receivedQty = receivedQtyStr.toIntOrNull() ?: 0
    val prevBal = prevBalStr.toIntOrNull() ?: 0
    val totalInput = prevBal + receivedQty

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_input_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "New Input Receive Entry",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Record cutting receipt & floor issue",
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
                        value = lineNo,
                        onValueChange = { lineNo = it },
                        label = { Text("Line No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyer,
                        onValueChange = { buyer = it },
                        label = { Text("Buyer") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = styleNo,
                        onValueChange = { styleNo = it },
                        label = { Text("Style No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = poNo,
                        onValueChange = { poNo = it },
                        label = { Text("PO No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cuttingQtyStr,
                        onValueChange = { cuttingQtyStr = it },
                        label = { Text("Cutting/Input Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = receivedQtyStr,
                        onValueChange = { receivedQtyStr = it },
                        label = { Text("Received Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prevBalStr,
                        onValueChange = { prevBalStr = it },
                        label = { Text("Previous Balance") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calculated Total Input Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automatic Total Input (Prev + Received):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentCyan
                        )
                        Text(
                            text = "$totalInput pcs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = receiver,
                    onValueChange = { receiver = it },
                    label = { Text("Receiver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks") },
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
                            val cQty = cuttingQtyStr.toIntOrNull() ?: 0
                            val rQty = receivedQtyStr.toIntOrNull() ?: 0
                            val pBal = prevBalStr.toIntOrNull() ?: 0
                            onSave(date, buyer, styleNo, poNo, color, size, lineNo, cQty, rQty, pBal, receiver, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("save_input_button")
                    ) {
                        Text("Save Input Entry")
                    }
                }
            }
        }
    }
}
