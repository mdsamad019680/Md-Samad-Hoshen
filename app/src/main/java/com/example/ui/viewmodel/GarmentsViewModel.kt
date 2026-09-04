package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InputReceiveEntity
import com.example.data.model.LineBalanceEntity
import com.example.data.model.ManpowerEntity
import com.example.data.model.OperationEntity
import com.example.data.model.POEntity
import com.example.data.model.ProductionOutputEntity
import com.example.data.model.ReportRecordEntity
import com.example.data.model.StyleEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.GarmentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BalanceSummary(
    val totalInput: Int = 0,
    val totalOutput: Int = 0,
    val totalAlter: Int = 0,
    val totalReject: Int = 0,
    val wipBalance: Int = 0,
    val achievementPercent: Double = 0.0,
    val efficiencyPercent: Double = 0.0
)

data class DashboardMetrics(
    val todayInput: Int = 0,
    val todayOutput: Int = 0,
    val totalWip: Int = 0,
    val averageEfficiency: Double = 0.0,
    val dailyTarget: Int = 0,
    val achievementPercent: Double = 0.0,
    val totalAlter: Int = 0,
    val totalReject: Int = 0
)

data class HourlyChartItem(
    val hourNumber: Int,
    val hourLabel: String,
    val target: Int,
    val actual: Int,
    val good: Int
)

data class LineProductionChartItem(
    val lineNo: String,
    val target: Int,
    val goodOutput: Int,
    val efficiency: Double
)

class GarmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GarmentsRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GarmentsRepository(db.garmentsDao())
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Active User / Role
    private val _currentUser = MutableStateFlow(
        UserEntity(1, "admin", "Alex Morrison", UserRole.ADMIN, "Plant Management")
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    private val _currentUserRole = MutableStateFlow(UserRole.ADMIN)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(NavScreen.DASHBOARD)
    val currentScreen: StateFlow<NavScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: NavScreen) {
        _currentScreen.value = screen
    }

    fun switchUserRole(role: UserRole) {
        _currentUserRole.value = role
        val current = _currentUser.value
        _currentUser.value = current.copy(
            role = role,
            fullName = when (role) {
                UserRole.ADMIN -> "Alex Morrison (Admin)"
                UserRole.PRODUCTION_OFFICER -> "Rahim Ahmed (Prod Officer)"
                UserRole.LINE_CHIEF -> "Tariqul Islam (Line Chief)"
                UserRole.QC -> "Fatima Begum (QC Inspector)"
                UserRole.VIEWER -> "Sarah Jenkins (Viewer)"
            }
        )
    }

    fun setCurrentUserRole(role: UserRole) = switchUserRole(role)

    // Role permissions
    fun canInputReceive(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER
    }

    fun canProductionOutput(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER || role == UserRole.LINE_CHIEF
    }

    fun canQCEntry(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER || role == UserRole.LINE_CHIEF || role == UserRole.QC
    }

    fun canEditStyles(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER
    }

    fun canEditLineBalancing(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER || role == UserRole.LINE_CHIEF
    }

    fun canEditManpower(): Boolean {
        val role = _currentUserRole.value
        return role == UserRole.ADMIN || role == UserRole.PRODUCTION_OFFICER || role == UserRole.LINE_CHIEF
    }

    // Raw Entities
    val users = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val styles = repository.allStyles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pos = repository.allPOs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inputReceives = repository.allInputReceives.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val productionOutputs = repository.allProductionOutputs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lineBalances = repository.allLineBalances.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val operations = repository.allOperations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val manpower = repository.allManpower.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val reports = repository.allReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Line Filter for Line Balancing & Output
    private val _selectedLine = MutableStateFlow("Line 1")
    val selectedLine: StateFlow<String> = _selectedLine.asStateFlow()

    fun setSelectedLine(line: String) {
        _selectedLine.value = line
    }

    // Balance Summary Computed
    val balanceSummary: StateFlow<BalanceSummary> = combine(
        repository.allInputReceives,
        repository.allProductionOutputs
    ) { inputs, outputs ->
        val totalIn = inputs.sumOf { it.totalInput }
        val totalOut = outputs.sumOf { it.goodOutput }
        val alters = outputs.sumOf { it.alterQty }
        val rejects = outputs.sumOf { it.rejectQty }
        val wip = (totalIn - totalOut - alters - rejects).coerceAtLeast(0)
        val targetSum = outputs.sumOf { it.hourlyTarget }
        val achPercent = if (targetSum > 0) (totalOut.toDouble() / targetSum.toDouble()) * 100.0 else 0.0

        BalanceSummary(
            totalInput = totalIn,
            totalOutput = totalOut,
            totalAlter = alters,
            totalReject = rejects,
            wipBalance = wip,
            achievementPercent = achPercent,
            efficiencyPercent = if (outputs.isNotEmpty()) outputs.map { it.achievementPercent }.average() else 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    // Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        repository.allInputReceives,
        repository.allProductionOutputs,
        repository.allOperations
    ) { inputs, outputs, ops ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayInputs = inputs.filter { it.date == today }
        val todayOutputs = outputs.filter { it.date == today }

        val todayIn = if (todayInputs.isNotEmpty()) todayInputs.sumOf { it.receivedQty } else inputs.sumOf { it.receivedQty }
        val todayOut = if (todayOutputs.isNotEmpty()) todayOutputs.sumOf { it.goodOutput } else outputs.sumOf { it.goodOutput }
        val totalIn = inputs.sumOf { it.totalInput }
        val totalOut = outputs.sumOf { it.goodOutput }
        val alters = outputs.sumOf { it.alterQty }
        val rejects = outputs.sumOf { it.rejectQty }
        val wip = (totalIn - totalOut - alters - rejects).coerceAtLeast(0)

        val target = if (todayOutputs.isNotEmpty()) todayOutputs.sumOf { it.hourlyTarget } else outputs.sumOf { it.hourlyTarget }
        val achievement = if (target > 0) (todayOut.toDouble() / target.toDouble()) * 100.0 else 0.0
        val avgEff = if (ops.isNotEmpty()) ops.map { it.efficiencyPercent }.average() else 82.5

        DashboardMetrics(
            todayInput = todayIn,
            todayOutput = todayOut,
            totalWip = wip,
            averageEfficiency = avgEff,
            dailyTarget = target,
            achievementPercent = achievement,
            totalAlter = alters,
            totalReject = rejects
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Hourly Production Chart data
    val hourlyChartData: StateFlow<List<HourlyChartItem>> = productionOutputs.combine(_selectedLine) { outputs, line ->
        val lineOutputs = outputs.filter { it.lineNo == line }
        val hoursGrouped = lineOutputs.groupBy { it.hourNumber }

        (1..8).map { hNum ->
            val entries = hoursGrouped[hNum] ?: emptyList()
            HourlyChartItem(
                hourNumber = hNum,
                hourLabel = "H$hNum",
                target = entries.sumOf { it.hourlyTarget }.let { if (it == 0) 160 else it },
                actual = entries.sumOf { it.actualOutput },
                good = entries.sumOf { it.goodOutput }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Line-wise production comparison
    val lineProductionData: StateFlow<List<LineProductionChartItem>> = productionOutputs.combine(repository.allLineBalances) { outputs, balances ->
        val lines = listOf("Line 1", "Line 2", "Line 3", "Line 4")
        lines.map { line ->
            val lineOuts = outputs.filter { it.lineNo == line }
            val good = lineOuts.sumOf { it.goodOutput }
            val target = lineOuts.sumOf { it.hourlyTarget }.let { if (it == 0) 1280 else it }
            val balance = balances.find { it.lineNo == line }
            val eff = balance?.lineEfficiencyPercent ?: if (target > 0) (good.toDouble() / target * 100.0).coerceAtMost(100.0) else 0.0
            LineProductionChartItem(
                lineNo = line,
                target = target,
                goodOutput = good,
                efficiency = eff
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addInputReceive(
        date: String,
        buyer: String,
        styleNo: String,
        poNo: String,
        color: String,
        size: String,
        lineNo: String,
        cuttingInputQty: Int,
        receivedQty: Int,
        previousBalance: Int,
        receiverName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val totalIn = previousBalance + receivedQty
            val input = InputReceiveEntity(
                date = date,
                buyer = buyer,
                styleNo = styleNo,
                poNo = poNo,
                color = color,
                size = size,
                lineNo = lineNo,
                cuttingInputQty = cuttingInputQty,
                receivedQty = receivedQty,
                previousBalance = previousBalance,
                totalInput = totalIn,
                receiverName = receiverName,
                notes = notes
            )
            repository.insertInputReceive(input)
        }
    }

    fun deleteInputReceive(input: InputReceiveEntity) {
        viewModelScope.launch {
            repository.deleteInputReceive(input)
        }
    }

    fun addProductionOutput(
        date: String,
        lineNo: String,
        styleNo: String,
        poNo: String,
        hourLabel: String,
        hourNumber: Int,
        hourlyTarget: Int,
        actualOutput: Int,
        alterQty: Int,
        rejectQty: Int,
        qcInspector: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val good = (actualOutput - alterQty - rejectQty).coerceAtLeast(0)
            val achPercent = if (hourlyTarget > 0) (good.toDouble() / hourlyTarget.toDouble()) * 100.0 else 0.0
            val output = ProductionOutputEntity(
                date = date,
                lineNo = lineNo,
                styleNo = styleNo,
                poNo = poNo,
                hourLabel = hourLabel,
                hourNumber = hourNumber,
                hourlyTarget = hourlyTarget,
                actualOutput = actualOutput,
                alterQty = alterQty,
                rejectQty = rejectQty,
                goodOutput = good,
                achievementPercent = achPercent,
                enteredBy = _currentUser.value.fullName,
                qcInspector = qcInspector,
                remarks = remarks
            )
            repository.insertProductionOutput(output)
        }
    }

    fun deleteProductionOutput(output: ProductionOutputEntity) {
        viewModelScope.launch {
            repository.deleteProductionOutput(output)
        }
    }

    fun addStyle(styleNo: String, buyer: String, itemType: String, smv: Double, totalOrderQty: Int) {
        viewModelScope.launch {
            repository.insertStyle(StyleEntity(styleNo = styleNo, buyer = buyer, itemType = itemType, smv = smv, totalOrderQty = totalOrderQty))
        }
    }

    fun deleteStyle(style: StyleEntity) {
        viewModelScope.launch {
            repository.deleteStyle(style)
        }
    }

    fun addPO(poNo: String, styleNo: String, buyer: String, orderQty: Int, color: String, size: String, shipDate: String) {
        viewModelScope.launch {
            repository.insertPO(POEntity(poNo = poNo, styleNo = styleNo, buyer = buyer, orderQty = orderQty, color = color, size = size, shipDate = shipDate))
        }
    }

    fun deletePO(po: POEntity) {
        viewModelScope.launch {
            repository.deletePO(po)
        }
    }

    fun addOrUpdateOperation(
        id: Long = 0,
        lineNo: String,
        styleNo: String,
        sequence: Int,
        operationName: String,
        smv: Double,
        manpower: Int,
        machineType: String,
        actualPerHour: Int
    ) {
        viewModelScope.launch {
            val cycleTime = if (manpower > 0) smv / manpower else smv
            val targetPerHour = if (smv > 0) ((60.0 / smv) * manpower).toInt() else 100
            val eff = if (targetPerHour > 0) (actualPerHour.toDouble() / targetPerHour.toDouble()) * 100.0 else 0.0

            // Pitch time threshold (standard benchmark ~0.65 min or 60/target)
            val pitchTime = 0.65
            val isBottleneck = cycleTime > pitchTime || eff < 70.0
            val reason = if (isBottleneck) {
                "Cycle time (${String.format(Locale.US, "%.2f", cycleTime)}m) exceeds pitch time (${String.format(Locale.US, "%.2f", pitchTime)}m)"
            } else "Operating within pitch balance"

            val rec = if (isBottleneck) {
                val neededManpower = Math.ceil(smv / pitchTime).toInt()
                val diff = neededManpower - manpower
                if (diff > 0) "Allocate +$diff operator(s) or add helper to reduce cycle time to ${String.format(Locale.US, "%.2f", smv / neededManpower)}m"
                else "Provide pre-trimmed sub-assemblies or upgrade machine guide"
            } else "Line flow optimal"

            val entity = OperationEntity(
                id = id,
                lineNo = lineNo,
                styleNo = styleNo,
                sequence = sequence,
                operationName = operationName,
                smv = smv,
                manpower = manpower,
                machineType = machineType,
                targetPerHour = targetPerHour,
                actualPerHour = actualPerHour,
                efficiencyPercent = eff,
                cycleTimeMinutes = cycleTime,
                isBottleneck = isBottleneck,
                bottleneckReason = reason,
                recommendation = rec
            )

            if (id == 0L) {
                repository.insertOperation(entity)
            } else {
                repository.updateOperation(entity)
            }
        }
    }

    fun deleteOperation(op: OperationEntity) {
        viewModelScope.launch {
            repository.deleteOperation(op)
        }
    }

    fun addManpower(
        operatorId: String,
        name: String,
        lineNo: String,
        skillGrade: String,
        assignedOperation: String,
        machineType: String,
        hourlyTarget: Int,
        isPresent: Boolean
    ) {
        viewModelScope.launch {
            repository.insertManpower(
                ManpowerEntity(
                    operatorId = operatorId,
                    name = name,
                    lineNo = lineNo,
                    skillGrade = skillGrade,
                    assignedOperation = assignedOperation,
                    machineType = machineType,
                    hourlyTarget = hourlyTarget,
                    isPresent = isPresent
                )
            )
        }
    }

    fun toggleManpowerAttendance(mp: ManpowerEntity) {
        viewModelScope.launch {
            repository.updateManpower(mp.copy(isPresent = !mp.isPresent))
        }
    }

    fun deleteManpower(mp: ManpowerEntity) {
        viewModelScope.launch {
            repository.deleteManpower(mp)
        }
    }

    fun addOperator(name: String, code: String, line: String, operation: String, grade: String, machine: String) {
        addManpower(
            operatorId = code,
            name = name,
            lineNo = line,
            skillGrade = grade,
            assignedOperation = operation,
            machineType = machine,
            hourlyTarget = 160,
            isPresent = true
        )
    }

    fun deleteOperator(op: ManpowerEntity) = deleteManpower(op)

    fun toggleOperatorAttendance(op: ManpowerEntity) = toggleManpowerAttendance(op)

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    fun clearAllData() = resetData()

    fun reseedDemoData() {
        viewModelScope.launch {
            repository.seedFullDemoData()
        }
    }

    fun seedDemoData() = reseedDemoData()

    fun exportReportCSV(context: Context, reportType: String, line: String = "All Lines") {
        shareReport(context, reportType, line)
    }

    // Export report to CSV and Share
    fun exportReportCsv(context: Context, reportType: String, line: String): File? {
        return try {
            val today = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "Garments_${reportType.replace(" ", "_")}_$today.csv"
            val file = File(context.cacheDir, filename)
            val fos = FileOutputStream(file)

            // UTF-8 BOM for Microsoft Excel compatibility
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val summary = balanceSummary.value
            val metrics = dashboardMetrics.value

            val sb = StringBuilder()
            sb.append("GARMENTS INPUT OUTPUT & LINE BALANCING REPORT\n")
            sb.append("Report Type:,$reportType\n")
            sb.append("Generated Date:,${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
            sb.append("Filtered Line:,$line\n")
            sb.append("Generated By:,${_currentUser.value.fullName} (${_currentUser.value.role.displayName})\n\n")

            sb.append("EXECUTIVE SUMMARY\n")
            sb.append("Metric,Value\n")
            sb.append("Total Cutting / Received Input,${summary.totalInput}\n")
            sb.append("Total Good Output,${summary.totalOutput}\n")
            sb.append("Total Alterations,${summary.totalAlter}\n")
            sb.append("Total Rejects,${summary.totalReject}\n")
            sb.append("Work In Progress (WIP) Balance,${summary.wipBalance}\n")
            sb.append("Achievement %,${String.format(Locale.US, "%.2f", summary.achievementPercent)}%\n")
            sb.append("Average Line Efficiency,${String.format(Locale.US, "%.2f", metrics.averageEfficiency)}%\n\n")

            when (reportType) {
                "Daily Production Report", "Monthly Production Report" -> {
                    sb.append("PRODUCTION OUTPUT DETAILS\n")
                    sb.append("Date,Line,Style,PO,Hour,Target,Actual,Alter,Reject,Good Output,Achievement %\n")
                    for (out in productionOutputs.value) {
                        sb.append("${out.date},${out.lineNo},${out.styleNo},${out.poNo},${out.hourLabel},${out.hourlyTarget},${out.actualOutput},${out.alterQty},${out.rejectQty},${out.goodOutput},${String.format(Locale.US, "%.1f", out.achievementPercent)}%\n")
                    }
                }
                "Input Receiving Report" -> {
                    sb.append("INPUT RECEIVING DETAILS\n")
                    sb.append("Date,Line,Buyer,Style,PO,Color,Size,Cutting Qty,Received Qty,Prev Balance,Total Input,Receiver\n")
                    for (inp in inputReceives.value) {
                        sb.append("${inp.date},${inp.lineNo},${inp.buyer},${inp.styleNo},${inp.poNo},${inp.color},${inp.size},${inp.cuttingInputQty},${inp.receivedQty},${inp.previousBalance},${inp.totalInput},${inp.receiverName}\n")
                    }
                }
                "Line Efficiency Report" -> {
                    sb.append("LINE BALANCING & OPERATIONS\n")
                    sb.append("Line,Style,Seq,Operation,SMV,Manpower,Machine,Target/hr,Actual/hr,Efficiency %,Cycle Time(min),Bottleneck,Recommendation\n")
                    for (op in operations.value) {
                        sb.append("${op.lineNo},${op.styleNo},${op.sequence},\"${op.operationName}\",${op.smv},${op.manpower},\"${op.machineType}\",${op.targetPerHour},${op.actualPerHour},${String.format(Locale.US, "%.1f", op.efficiencyPercent)}%,${String.format(Locale.US, "%.2f", op.cycleTimeMinutes)},${if (op.isBottleneck) "YES" else "NO"},\"${op.recommendation}\"\n")
                    }
                }
                else -> {
                    sb.append("BALANCE / WIP REPORT BREAKDOWN\n")
                    sb.append("Line,Style,PO,Input Qty,Good Output,Alter,Reject,WIP Balance\n")
                    val grouped = inputReceives.value.groupBy { "${it.lineNo}__${it.styleNo}" }
                    for ((key, inps) in grouped) {
                        val parts = key.split("__")
                        val l = parts[0]
                        val s = parts.getOrElse(1) { "" }
                        val inQty = inps.sumOf { it.totalInput }
                        val outList = productionOutputs.value.filter { it.lineNo == l && it.styleNo == s }
                        val goodOut = outList.sumOf { it.goodOutput }
                        val alt = outList.sumOf { it.alterQty }
                        val rej = outList.sumOf { it.rejectQty }
                        val wipVal = (inQty - goodOut - alt - rej).coerceAtLeast(0)
                        sb.append("$l,$s,${inps.firstOrNull()?.poNo ?: ""},$inQty,$goodOut,$alt,$rej,$wipVal\n")
                    }
                }
            }

            fos.write(sb.toString().toByteArray())
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareReport(context: Context, reportType: String, line: String) {
        val summary = balanceSummary.value
        val metrics = dashboardMetrics.value
        val shareText = """
            📊 GARMENTS PRODUCTION & LINE BALANCING REPORT
            Type: $reportType
            Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}
            Line: $line
            Prepared by: ${_currentUser.value.fullName} (${_currentUser.value.role.displayName})
            
            Key Performance Indicators:
            - Total Input: ${summary.totalInput} pcs
            - Total Output: ${summary.totalOutput} pcs
            - Alterations: ${summary.totalAlter} pcs
            - Rejects: ${summary.totalReject} pcs
            - WIP Floor Balance: ${summary.wipBalance} pcs
            - Target Achievement: ${String.format(Locale.US, "%.1f", summary.achievementPercent)}%
            - Line Efficiency: ${String.format(Locale.US, "%.1f", metrics.averageEfficiency)}%
            
            Generated via Garments Input Output & Line Balancing MES.
        """.trimIndent()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "$reportType - Garments MES")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Report via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}

enum class NavScreen(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "Dashboard"),
    INPUT_RECEIVE("Input Receive", "Input"),
    PRODUCTION_OUTPUT("Production Output", "PrecisionManufacturing"),
    BALANCE_DASHBOARD("Input–Output Balance", "Balance"),
    LINE_BALANCING("Line Balancing", "Tune"),
    STYLE_PO("Style / PO", "Inventory2"),
    MANPOWER("Operator & Manpower", "People"),
    REPORTS("Reports", "Assessment"),
    SETTINGS("Settings", "Settings")
}
