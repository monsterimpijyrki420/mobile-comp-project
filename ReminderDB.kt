package com.example.mobilecomp

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "reminder_table", primaryKeys = ["creation_time", "creator_id"])
data class Reminder(
    @ColumnInfo(name = "message") val message: String,

    //location not implemented yet
    @ColumnInfo(name = "location_x") val location_x: Double = 0.0,
    @ColumnInfo(name = "location_y") val location_y: Double = 0.0,

    @ColumnInfo(name = "reminder_time") val reminder_time: Long,
    @ColumnInfo(name = "creation_time") val creation_time: Long,
    @ColumnInfo(name = "creator_id") val creator_id: String,
    @ColumnInfo(name = "reminder_seen") val reminder_seen: Boolean = false
)

@Dao
interface ReminderDao {
    @Insert
    fun insert(reminder: Reminder)

    @Query("UPDATE reminder_table SET message = :new_msg WHERE " +
            "message LIKE :old_msg and creator_id LIKE :creator_id")
    fun update_msg(new_msg: String, old_msg: String, creator_id: String)

    @Query("UPDATE reminder_table SET reminder_time = :r_time WHERE " +
            "message LIKE :old_msg and creator_id LIKE :creator_id")
    fun update_rtime(r_time: String, old_msg: String, creator_id: String)


    @Query("DELETE FROM reminder_table WHERE message LIKE :message and " +
                                "creator_id LIKE :creator_id")
    fun delete(message: String, creator_id: String)

    @Query("SELECT * FROM reminder_table WHERE creator_id LIKE :creator_id")
    fun getReminders(creator_id: String): List<Reminder>

    @Query("SELECT * FROM reminder_table WHERE creator_id LIKE :creator_id and reminder_time < :current_time")
    fun getTimelyReminders(creator_id: String, current_time: Long): List<Reminder>
}

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}

class ReminderDB {

    private lateinit var db: ReminderDatabase
    private lateinit var reminderDao: ReminderDao

    fun initDB(context: Context) {

        Log.d("ReminderDB", "Building the database")
        Log.d("ReminderDB", context.toString())
        db = Room.databaseBuilder(
            context,
            ReminderDatabase::class.java, "reminder.db"
        ).build()

        reminderDao = db.reminderDao()

        Log.d("ReminderDB", "Database build")
    }

    fun getReminders(creator_id: String): List<Reminder> {
        return reminderDao.getReminders(creator_id)
    }

    fun getTimelyReminders(creator_id: String, current_time: Long): List<Reminder> {
        return reminderDao.getTimelyReminders(creator_id, current_time)
    }

    //fun addMessage(message: String, location_x: Double, location_y: Double,
    //                       reminder_time: Long, creation_time: Long, creator_id: String) {
    fun addMessage(message: String, creation_time: Long, creator_id: String, reminder_time: Long) {
        val reminder = Reminder(
            message = message,
            //location_x = location_x,
            //location_y = location_y,
            reminder_time = reminder_time,
            creation_time = creation_time,
            creator_id = creator_id
        )
        reminderDao.insert(reminder)
    }

    fun editMessage(new_msg: String, old_msg: String, creator_id: String, r_time: String) {
        reminderDao.update_msg(new_msg, old_msg, creator_id)
        reminderDao.update_rtime(r_time, new_msg, creator_id)
    }

    fun deleteMessage(message: String, creator_id: String) {
        reminderDao.delete(message, creator_id)
    }
}
