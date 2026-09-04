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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.POEntity
import com.example.data.model.StyleEntity
import com.example.ui.components.PermissionDeniedNotice
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.GarmentsViewModel

@Composable
fun StylePOScreen(
    viewModel: GarmentsViewModel,
    modifier: Modifier = Modifier
) {
    val styles by viewModel.styles.collectAsState()
    val pos by viewModel.pos.collectAsState()
    val canEdit = viewModel.canEditStyles()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddStyleDialog by remember { mutableStateOf(false) }
    var showAddPoDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("style_po_screen"),
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 0) showAddStyleDialog = true
                        else showAddPoDialog = true
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_style_po_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                    text = "Style & Purchase Order Directory",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage buyer styles, target SMV, order quantities and PO delivery schedules",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            if (!canEdit) {
                item {
                    PermissionDeniedNotice(
                        requiredAction = "Style / PO Modifications",
                        allowedRoles = "Admin, Production Officer"
                    )
                }
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Styles (${styles.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Purchase Orders (${pos.size})", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Styles List
                items(styles, key = { it.id }) { style ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("style_card_${style.id}"),
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
                                            text = style.buyer,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = style.styleNo,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (canEdit) {
                                    IconButton(onClick = { viewModel.deleteStyle(style) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = style.itemType, style = MaterialTheme.typography.bodyMedium, color = Slate700)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate100, shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Standard SMV: ${style.smv} min", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = PrimaryBlue)
                                Text("Total Order: ${style.totalOrderQty} pcs", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = SuccessGreen)
                            }
                        }
                    }
                }
            } else {
                // POs List
                items(pos, key = { it.id }) { po ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("po_card_${po.id}"),
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
                                        color = AccentCyan.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = po.buyer,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AccentCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = po.poNo,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (canEdit) {
                                    IconButton(onClick = { viewModel.deletePO(po) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Style: ${po.styleNo}  |  Color: ${po.color}  |  Size: ${po.size}", style = MaterialTheme.typography.bodySmall, color = Slate500)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate100, shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Order Qty: ${po.orderQty} pcs", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                                Text("Ship Date: ${po.shipDate}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddStyleDialog) {
        AddStyleDialog(
            onDismiss = { showAddStyleDialog = false },
            onSave = { styleNo, buyer, itemType, smv, orderQty ->
                viewModel.addStyle(styleNo, buyer, itemType, smv, orderQty)
                showAddStyleDialog = false
            }
        )
    }

    if (showAddPoDialog) {
        AddPoDialog(
            styles = styles,
            onDismiss = { showAddPoDialog = false },
            onSave = { poNo, styleNo, buyer, orderQty, color, size, shipDate ->
                viewModel.addPO(poNo, styleNo, buyer, orderQty, color, size, shipDate)
                showAddPoDialog = false
            }
        )
    }
}

@Composable
fun AddStyleDialog(
    onDismiss: () -> Unit,
    onSave: (styleNo: String, buyer: String, itemType: String, smv: Double, orderQty: Int) -> Unit
) {
    var styleNo by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("H&M") }
    var itemType by remember { mutableStateOf("Polo Shirt") }
    var smvStr by remember { mutableStateOf("14.5") }
    var orderQtyStr by remember { mutableStateOf("25000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_style_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Style", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = styleNo,
                    onValueChange = { styleNo = it },
                    label = { Text("Style No (e.g. ST-POLO-900)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = buyer,
                    onValueChange = { buyer = it },
                    label = { Text("Buyer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemType,
                    onValueChange = { itemType = it },
                    label = { Text("Item Type / Garment Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = smvStr,
                        onValueChange = { smvStr = it },
                        label = { Text("Target SMV (min)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = orderQtyStr,
                        onValueChange = { orderQtyStr = it },
                        label = { Text("Order Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val smv = smvStr.toDoubleOrNull() ?: 12.0
                            val qty = orderQtyStr.toIntOrNull() ?: 1000
                            onSave(styleNo, buyer, itemType, smv, qty)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save Style")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPoDialog(
    styles: List<StyleEntity>,
    onDismiss: () -> Unit,
    onSave: (poNo: String, styleNo: String, buyer: String, orderQty: Int, color: String, size: String, shipDate: String) -> Unit
) {
    var poNo by remember { mutableStateOf("") }
    var styleNo by remember { mutableStateOf(styles.firstOrNull()?.styleNo ?: "ST-POLO-802") }
    var buyer by remember { mutableStateOf(styles.firstOrNull()?.buyer ?: "H&M") }
    var orderQtyStr by remember { mutableStateOf("10000") }
    var color by remember { mutableStateOf("Navy Blue") }
    var size by remember { mutableStateOf("M") }
    var shipDate by remember { mutableStateOf("2026-09-30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_po_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Purchase Order (PO)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = poNo,
                    onValueChange = { poNo = it },
                    label = { Text("PO Number (e.g. PO-88210)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = styleNo,
                        onValueChange = { styleNo = it },
                        label = { Text("Style No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = buyer,
                        onValueChange = { buyer = it },
                        label = { Text("Buyer") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = orderQtyStr,
                        onValueChange = { orderQtyStr = it },
                        label = { Text("Order Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = shipDate,
                        onValueChange = { shipDate = it },
                        label = { Text("Ship Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = orderQtyStr.toIntOrNull() ?: 1000
                            onSave(poNo, styleNo, buyer, qty, color, size, shipDate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save PO")
                    }
                }
            }
        }
    }
}
