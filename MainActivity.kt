package com.example.mobilecomp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobilecomp.ui.theme.MobilecompTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class MainActivity : ComponentActivity() {
    private lateinit var db: ReminderDB
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //creates the channel
        val notifApp = NotificationApplication()
        notifApp.createChannel(this)

        setContent {

            //this.deleteDatabase("reminder.db")
            db = ReminderDB()
            db.initDB(this)

            MobilecompTheme {

                //initates variables and whatnot
                sharedPreferences = getSharedPreferences("login_details", Context.MODE_PRIVATE)
                username = intent.getStringExtra("username").toString()

                var msg by remember {
                    mutableStateOf("")
                }
                var selected_msg by remember {
                    mutableStateOf("")
                }
                var messages by remember {
                    mutableStateOf(listOf<String>())
                }
                LaunchedEffect(Unit) {
                    val reminders = withContext(Dispatchers.IO) {
                        db.getReminders(username)
                    }
                    messages = reminders.map {reminder -> reminder.message}
                }
                var picked_date by remember {
                    mutableStateOf(LocalDate.now())
                }
                var picked_time by remember {
                    mutableStateOf(LocalTime.MIDNIGHT)
                }
                var show_all_reminders by remember {
                    mutableStateOf(false)
                }
                var notif_state by remember {
                    mutableStateOf(false)
                }
                var calendar_state by remember {
                    mutableStateOf(false)
                }
                val dateDialogState = rememberMaterialDialogState()
                val timeDialogState = rememberMaterialDialogState()

                //add, edit and remove stuff
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {

                        Column() {
                            Button(onClick = {
                                dateDialogState.show()
                            }) {
                                Text(text = "Pick date")
                            }
                            Text(text = picked_date.toString())
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Column() {
                            Button(onClick = {
                                timeDialogState.show()
                            }) {
                                Text(text = "Pick time")
                            }
                            Text(text = picked_time.toString())
                        }
                    }

                    MaterialDialog(
                        dialogState = dateDialogState,
                        buttons = {
                            positiveButton(text = "ok"){
                            }
                            negativeButton(text = "cancel"){
                            }
                        }
                    ){
                        datepicker(
                            initialDate = LocalDate.now(),
                        ) {
                            picked_date = it
                        }
                    }

                    MaterialDialog(
                        dialogState = timeDialogState,
                        buttons = {
                            positiveButton(text = "ok"){
                            }
                            negativeButton(text = "cancel"){
                            }
                        }
                    ){
                        timepicker(
                            initialTime = LocalTime.MIDNIGHT,
                        ) {
                            picked_time = it
                        }
                    }

                    Row() {
                        Button(onClick = {
                            calendar_state = !calendar_state
                        }) {
                            if (calendar_state) {
                                Text(text = "Calendar input")
                            } else {
                                Text(text = "No calendar input")
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(onClick = {
                            notif_state = !notif_state
                        }) {
                            if (notif_state) {
                                Text(text = "Notification added")
                            } else {
                                Text(text = "No notification")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = msg,
                        label = { Text("Add a reminder") },
                        singleLine = true,
                        onValueChange = { text ->
                            msg = text
                        })

                    Spacer(modifier = Modifier.height(10.dp))

                    Row() {
                        Button(onClick = {
                            if(msg.isNotBlank()) {

                                //updates database
                                val msg_db = msg
                                val creation_time = System.currentTimeMillis()

                                val formatter_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val formatter_time = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val date = formatter_date.parse(picked_date.toString()) ?: Date()
                                val time = formatter_time.parse(picked_time.toString()) ?: Date()
                                val date_time = Date(date.year, date.month, date.date, time.hours, time.minutes)


                                GlobalScope.launch {
                                    db.addMessage(msg_db, creation_time, username, date_time.time)
                                    //val reminders = db.getTimelyReminders(username, System.currentTimeMillis())
                                    val reminders = loadMessages(db, username, show_all_reminders)
                                    messages = reminders.map {reminder -> reminder.message}
                                }
                                msg = ""


                                //adds to calendar
                                if (calendar_state) {
                                    val cal_intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, msg_db)
                                        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                                        putExtra(
                                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            date_time.time
                                        )
                                        putExtra(
                                            CalendarContract.EXTRA_EVENT_END_TIME,
                                            date_time.time + 3600000
                                        )
                                    }
                                    this@MainActivity.startActivity(cal_intent)
                                }


                                //activates notification
                                if (notif_state) {
                                    if (date_time.time - creation_time > 0) {
                                        setNotif(
                                            msg_db,
                                            date_time.time - creation_time,
                                            this@MainActivity
                                        )
                                    }
                                }

                            }
                        }) {
                            Text(text = "Add")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(onClick = {
                            if(selected_msg.isNotBlank() && msg.isNotBlank()) {

                                val new_msg = msg
                                val old_msg = selected_msg
                                val r_time = picked_date.toString() + " " +  picked_time.toString()
                                GlobalScope.launch {
                                    db.editMessage(new_msg, old_msg, username, r_time)
                                    val reminders = loadMessages(db, username, show_all_reminders)
                                    messages = reminders.map { reminder -> reminder.message }
                                }
                                msg = ""
                                selected_msg = ""
                            }
                        }) {
                            Text(text = "Edit")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(onClick = {
                            if(selected_msg.isNotBlank()) {

                                val msg_db = selected_msg
                                GlobalScope.launch {
                                    db.deleteMessage(msg_db, username)
                                    val reminders = loadMessages(db, username, show_all_reminders)
                                    messages = reminders.map { reminder -> reminder.message }
                                }
                                selected_msg = ""
                            }
                        }) {
                            Text(text = "Remove")
                        }

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                }

                //extra buttons
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Account: "+username,fontSize = 20.sp)
                    Button(onClick = {
                        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }) {
                        Text(text = "Edit account")
                    }
                    Button(onClick = {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text(text = "Logout")
                    }
                    Button(onClick = {
                        GlobalScope.launch {
                            val reminders = loadMessages(db, username, show_all_reminders)
                            messages = reminders.map { reminder -> reminder.message }
                        }
                        show_all_reminders = !show_all_reminders
                    }) {
                        if (!show_all_reminders) {
                            Text(text = "Show all")
                        } else {
                            Text(text = "Show due")
                        }
                    }
                }

                //lazy column
                Box(
                    modifier = Modifier
                        .width(LocalConfiguration.current.screenWidthDp.dp / 2)
                        .height(LocalConfiguration.current.screenHeightDp.dp * 2 / 3)
                ) {
                    LazyColumn (verticalArrangement = Arrangement.spacedBy(15.dp)){
                        items(messages) {current_msg ->
                            val backgroundColor =
                                if (current_msg == selected_msg) Color.LightGray else Color.Transparent
                            Text(
                                text = current_msg,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .background(backgroundColor)
                                    .clickable { selected_msg = current_msg }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun loadMessages(database: ReminderDB, username: String, load_all: Boolean): List<Reminder> {
    if (load_all){
        return database.getReminders(username)
    } else {
        return database.getTimelyReminders(username, System.currentTimeMillis())
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MobilecompTheme {
    }
}