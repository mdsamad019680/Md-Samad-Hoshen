package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val description: String) {
    ADMIN("Admin", "Full access to all modules and configurations"),
    PRODUCTION_OFFICER("Production Officer", "Input receive, output entry, balance & styles"),
    LINE_CHIEF("Line Chief", "Production entry and line balancing"),
    QC("QC Inspector", "Quality inspection, alter and reject logs"),
    VIEWER("Viewer", "Reports and dashboard view only")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val department: String = "Production"
)

@Entity(tableName = "styles")
data class StyleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val styleNo: String,
    val buyer: String,
    val itemType: String, // e.g. "Basic Crew Neck T-Shirt", "Polo Shirt", "Denim 5-Pocket"
    val smv: Double,      // Standard Minute Value (e.g. 14.5 mins)
    val totalOrderQty: Int
)

@Entity(tableName = "pos")
data class POEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poNo: String,
    val styleNo: String,
    val buyer: String,
    val orderQty: Int,
    val color: String,
    val size: String,
    val shipDate: String
)

@Entity(tableName = "input_receive")
data class InputReceiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val buyer: String,
    val styleNo: String,
    val poNo: String,
    val color: String,
    val size: String,
    val lineNo: String,           // e.g. "Line 1"
    val cuttingInputQty: Int,     // Cutting issued
    val receivedQty: Int,         // Physical floor received
    val previousBalance: Int,     // WIP prior to this input
    val totalInput: Int,          // previousBalance + receivedQty
    val receiverName: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "production_output")
data class ProductionOutputEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val lineNo: String,
    val styleNo: String,
    val poNo: String,
    val hourLabel: String,        // e.g. "Hour 1 (08:00 - 09:00)"
    val hourNumber: Int,          // 1 to 10
    val hourlyTarget: Int,
    val actualOutput: Int,
    val alterQty: Int,
    val rejectQty: Int,
    val goodOutput: Int,          // actualOutput - alterQty - rejectQty
    val achievementPercent: Double, // (goodOutput / hourlyTarget) * 100
    val enteredBy: String,
    val qcInspector: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "line_balance")
data class LineBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lineNo: String,
    val styleNo: String,
    val targetPerHour: Int,
    val pitchTimeMinutes: Double,
    val totalManpower: Int,
    val totalSMV: Double,
    val lineEfficiencyPercent: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lineNo: String,
    val styleNo: String,
    val sequence: Int,
    val operationName: String,
    val smv: Double,
    val manpower: Int,
    val machineType: String,
    val targetPerHour: Int,
    val actualPerHour: Int,
    val efficiencyPercent: Double,
    val cycleTimeMinutes: Double,
    val isBottleneck: Boolean,
    val bottleneckReason: String = "",
    val recommendation: String = ""
)

@Entity(tableName = "manpower")
data class ManpowerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operatorId: String,
    val name: String,
    val lineNo: String,
    val skillGrade: String,       // "A+", "A", "B", "C"
    val assignedOperation: String,
    val machineType: String,
    val hourlyTarget: Int,
    val isPresent: Boolean = true
)

@Entity(tableName = "reports")
data class ReportRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportType: String,
    val title: String,
    val dateGenerated: String,
    val filterLine: String,
    val filterStyle: String,
    val totalInput: Int,
    val totalOutput: Int,
    val alterTotal: Int,
    val rejectTotal: Int,
    val balanceWip: Int,
    val averageEfficiency: Double
)
