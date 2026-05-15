package com.uet.parking.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uet.parking.data.local.dao.*
import com.uet.parking.data.model.*

@Database(
    entities = [
        User::class,
        ParkingLot::class,
        HourlyLoad::class,
        Payment::class,
        Ticket::class,
        Schedule::class,
        UserInfo::class,
        AdminInfo::class,
        BookingEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun parkingLotDao(): ParkingLotDao
    abstract fun ticketDao(): TicketDao
    abstract fun hourlyLoadDao(): HourlyLoadDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun adminInfoDao(): AdminInfoDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 1 to 2: thêm bảng booking
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `booking` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `fieldId` INTEGER NOT NULL,
                        `bookingDate` TEXT NOT NULL,
                        `bookingTime` TEXT NOT NULL,
                        `slot` INTEGER NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'Pending',
                        `createdAt` TEXT NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `user`(`userId`) ON DELETE CASCADE,
                        FOREIGN KEY(`fieldId`) REFERENCES `parkinglot`(`parkingId`) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parking_system.db"
                )
                .createFromAsset("database.db")
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

