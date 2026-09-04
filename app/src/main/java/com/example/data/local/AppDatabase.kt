package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.InputReceiveEntity
import com.example.data.model.LineBalanceEntity
import com.example.data.model.ManpowerEntity
import com.example.data.model.OperationEntity
import com.example.data.model.POEntity
import com.example.data.model.ProductionOutputEntity
import com.example.data.model.ReportRecordEntity
import com.example.data.model.StyleEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        StyleEntity::class,
        POEntity::class,
        InputReceiveEntity::class,
        ProductionOutputEntity::class,
        LineBalanceEntity::class,
        OperationEntity::class,
        ManpowerEntity::class,
        ReportRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun garmentsDao(): GarmentsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "garments_production.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
