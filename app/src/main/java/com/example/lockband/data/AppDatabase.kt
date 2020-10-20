package com.example.lockband.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lockband.utils.DATABASE_NAME
import com.example.lockband.utils.MODES
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(entities = [AppState::class, Mode::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appStateDao() : AppStateDao
    abstract fun modeDao() : ModeDao

    companion object{
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context : Context) : AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        GlobalScope.launch {
                            val appList: MutableList<ApplicationInfo> = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                            val entities : MutableList<AppState> = mutableListOf()
                            appList.forEach{
                                entities.add(AppState(it.packageName))
                            }
                            getInstance(context).appStateDao().insertAll(entities)
                            getInstance(context).modeDao().insertAll(MODES)
                        }
                    }
                }
            ).build()
        }
    }
}