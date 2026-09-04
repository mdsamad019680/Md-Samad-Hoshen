package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface GarmentsDao {

    // USERS
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    // STYLES
    @Query("SELECT * FROM styles ORDER BY styleNo ASC")
    fun getAllStyles(): Flow<List<StyleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyle(style: StyleEntity): Long

    @Delete
    suspend fun deleteStyle(style: StyleEntity)

    // POS
    @Query("SELECT * FROM pos ORDER BY poNo ASC")
    fun getAllPOs(): Flow<List<POEntity>>

    @Query("SELECT * FROM pos WHERE styleNo = :styleNo")
    fun getPOsForStyle(styleNo: String): Flow<List<POEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPO(po: POEntity): Long

    @Delete
    suspend fun deletePO(po: POEntity)

    // INPUT RECEIVE
    @Query("SELECT * FROM input_receive ORDER BY timestamp DESC")
    fun getAllInputReceives(): Flow<List<InputReceiveEntity>>

    @Query("SELECT * FROM input_receive WHERE lineNo = :lineNo ORDER BY timestamp DESC")
    fun getInputReceivesForLine(lineNo: String): Flow<List<InputReceiveEntity>>

    @Query("SELECT * FROM input_receive WHERE styleNo = :styleNo ORDER BY timestamp DESC")
    fun getInputReceivesForStyle(styleNo: String): Flow<List<InputReceiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInputReceive(input: InputReceiveEntity): Long

    @Delete
    suspend fun deleteInputReceive(input: InputReceiveEntity)

    // PRODUCTION OUTPUT
    @Query("SELECT * FROM production_output ORDER BY date DESC, hourNumber DESC")
    fun getAllProductionOutputs(): Flow<List<ProductionOutputEntity>>

    @Query("SELECT * FROM production_output WHERE date = :date ORDER BY hourNumber ASC")
    fun getProductionOutputsForDate(date: String): Flow<List<ProductionOutputEntity>>

    @Query("SELECT * FROM production_output WHERE lineNo = :lineNo ORDER BY date DESC, hourNumber ASC")
    fun getProductionOutputsForLine(lineNo: String): Flow<List<ProductionOutputEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionOutput(output: ProductionOutputEntity): Long

    @Delete
    suspend fun deleteProductionOutput(output: ProductionOutputEntity)

    // LINE BALANCE & OPERATIONS
    @Query("SELECT * FROM line_balance WHERE lineNo = :lineNo LIMIT 1")
    fun getLineBalance(lineNo: String): Flow<LineBalanceEntity?>

    @Query("SELECT * FROM line_balance")
    fun getAllLineBalances(): Flow<List<LineBalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLineBalance(lineBalance: LineBalanceEntity): Long

    @Query("SELECT * FROM operations WHERE lineNo = :lineNo ORDER BY sequence ASC")
    fun getOperationsForLine(lineNo: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations ORDER BY lineNo ASC, sequence ASC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperations(operations: List<OperationEntity>)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)

    @Query("DELETE FROM operations WHERE lineNo = :lineNo")
    suspend fun deleteOperationsForLine(lineNo: String)

    // MANPOWER
    @Query("SELECT * FROM manpower ORDER BY lineNo ASC, name ASC")
    fun getAllManpower(): Flow<List<ManpowerEntity>>

    @Query("SELECT * FROM manpower WHERE lineNo = :lineNo ORDER BY name ASC")
    fun getManpowerForLine(lineNo: String): Flow<List<ManpowerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManpower(manpower: ManpowerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManpowerList(manpowerList: List<ManpowerEntity>)

    @Update
    suspend fun updateManpower(manpower: ManpowerEntity)

    @Delete
    suspend fun deleteManpower(manpower: ManpowerEntity)

    // REPORTS
    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<ReportRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportRecordEntity): Long

    // CLEAR ALL FOR DEMO SEEDING / RESET
    @Query("DELETE FROM input_receive")
    suspend fun clearInputReceives()

    @Query("DELETE FROM production_output")
    suspend fun clearProductionOutputs()

    @Query("DELETE FROM styles")
    suspend fun clearStyles()

    @Query("DELETE FROM pos")
    suspend fun clearPOs()

    @Query("DELETE FROM operations")
    suspend fun clearOperations()

    @Query("DELETE FROM line_balance")
    suspend fun clearLineBalances()

    @Query("DELETE FROM manpower")
    suspend fun clearManpower()

    @Query("DELETE FROM reports")
    suspend fun clearReports()

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
