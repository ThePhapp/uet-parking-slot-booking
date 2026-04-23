package com.uet.parking.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uet.parking.data.local.dao.HourlyLoadDao
import com.uet.parking.data.local.dao.ParkingLotDao
import com.uet.parking.data.local.dao.TicketDao
import com.uet.parking.data.local.dao.UserDao
import com.uet.parking.data.model.*

@Database(
    entities = [
        User::class,
        ParkingLot::class,
        HourlyLoad::class,
        Payment::class,
        Ticket::class,
        Schedule::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun parkingLotDao(): ParkingLotDao
    abstract fun ticketDao(): TicketDao
    abstract fun hourlyLoadDao(): HourlyLoadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parking_system.db"
                )
                .createFromAsset("database.db")
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
