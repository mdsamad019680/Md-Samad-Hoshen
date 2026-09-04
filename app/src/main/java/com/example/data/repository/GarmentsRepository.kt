package com.example.data.repository

import com.example.data.local.DemoDataSeeder
import com.example.data.local.GarmentsDao
import com.example.data.model.InputReceiveEntity
import com.example.data.model.LineBalanceEntity
import com.example.data.model.ManpowerEntity
import com.example.data.model.OperationEntity
import com.example.data.model.POEntity
import com.example.data.model.ProductionOutputEntity
import com.example.data.model.ReportRecordEntity
import com.example.data.model.StyleEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GarmentsRepository(private val dao: GarmentsDao) {

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allStyles: Flow<List<StyleEntity>> = dao.getAllStyles()
    val allPOs: Flow<List<POEntity>> = dao.getAllPOs()
    val allInputReceives: Flow<List<InputReceiveEntity>> = dao.getAllInputReceives()
    val allProductionOutputs: Flow<List<ProductionOutputEntity>> = dao.getAllProductionOutputs()
    val allLineBalances: Flow<List<LineBalanceEntity>> = dao.getAllLineBalances()
    val allOperations: Flow<List<OperationEntity>> = dao.getAllOperations()
    val allManpower: Flow<List<ManpowerEntity>> = dao.getAllManpower()
    val allReports: Flow<List<ReportRecordEntity>> = dao.getAllReports()

    fun getOperationsForLine(lineNo: String): Flow<List<OperationEntity>> = dao.getOperationsForLine(lineNo)
    fun getLineBalance(lineNo: String): Flow<LineBalanceEntity?> = dao.getLineBalance(lineNo)
    fun getManpowerForLine(lineNo: String): Flow<List<ManpowerEntity>> = dao.getManpowerForLine(lineNo)
    fun getProductionOutputsForDate(date: String): Flow<List<ProductionOutputEntity>> = dao.getProductionOutputsForDate(date)

    suspend fun checkAndSeedInitialData() {
        val users = dao.getAllUsers().first()
        if (users.isEmpty()) {
            seedFullDemoData()
        }
    }

    suspend fun seedFullDemoData() {
        dao.clearUsers()
        dao.clearStyles()
        dao.clearPOs()
        dao.clearInputReceives()
        dao.clearProductionOutputs()
        dao.clearOperations()
        dao.clearLineBalances()
        dao.clearManpower()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        dao.insertUsers(DemoDataSeeder.getInitialUsers())
        for (s in DemoDataSeeder.getInitialStyles()) dao.insertStyle(s)
        for (po in DemoDataSeeder.getInitialPOs()) dao.insertPO(po)
        for (inp in DemoDataSeeder.getInitialInputReceives(todayDate)) dao.insertInputReceive(inp)
        for (out in DemoDataSeeder.getInitialProductionOutputs(todayDate)) dao.insertProductionOutput(out)
        dao.insertOrUpdateLineBalance(DemoDataSeeder.getInitialLineBalance())
        dao.insertOperations(DemoDataSeeder.getInitialOperations())
        dao.insertManpowerList(DemoDataSeeder.getInitialManpower())
    }

    suspend fun resetAllData() {
        dao.clearUsers()
        dao.clearStyles()
        dao.clearPOs()
        dao.clearInputReceives()
        dao.clearProductionOutputs()
        dao.clearOperations()
        dao.clearLineBalances()
        dao.clearManpower()
        dao.clearReports()
    }

    // Insert / update methods
    suspend fun insertInputReceive(input: InputReceiveEntity) = dao.insertInputReceive(input)
    suspend fun deleteInputReceive(input: InputReceiveEntity) = dao.deleteInputReceive(input)

    suspend fun insertProductionOutput(output: ProductionOutputEntity) = dao.insertProductionOutput(output)
    suspend fun deleteProductionOutput(output: ProductionOutputEntity) = dao.deleteProductionOutput(output)

    suspend fun insertStyle(style: StyleEntity) = dao.insertStyle(style)
    suspend fun deleteStyle(style: StyleEntity) = dao.deleteStyle(style)

    suspend fun insertPO(po: POEntity) = dao.insertPO(po)
    suspend fun deletePO(po: POEntity) = dao.deletePO(po)

    suspend fun insertOperation(op: OperationEntity) = dao.insertOperation(op)
    suspend fun updateOperation(op: OperationEntity) = dao.updateOperation(op)
    suspend fun deleteOperation(op: OperationEntity) = dao.deleteOperation(op)

    suspend fun insertOrUpdateLineBalance(lb: LineBalanceEntity) = dao.insertOrUpdateLineBalance(lb)

    suspend fun insertManpower(mp: ManpowerEntity) = dao.insertManpower(mp)
    suspend fun updateManpower(mp: ManpowerEntity) = dao.updateManpower(mp)
    suspend fun deleteManpower(mp: ManpowerEntity) = dao.deleteManpower(mp)

    suspend fun insertReport(report: ReportRecordEntity) = dao.insertReport(report)
}
