package com.example.mobilecomp

import GeofencingHandler
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import kotlin.math.pow


class MainActivity : ComponentActivity() {
    private lateinit var db: ReminderDB
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var username: String

    private lateinit var locationHandler: LocationHandler

    /**
    private val mapsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("map", "testi")
            val data = result.data
            val location12 = data?.getParcelableExtra<LatLng>("location")
            Log.d("map", location12.toString())
        }
    }
    **/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //creates the channel
        val notifApp = NotificationApplication()
        notifApp.createChannel(this)

        locationHandler = LocationHandler(this)


        setContent {

            this.deleteDatabase("reminder.db")
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
                var longitude by remember {
                    mutableStateOf(0.0)
                }
                var latitude by remember {
                    mutableStateOf(0.0)
                }
                var given_lng by remember {
                    mutableStateOf(0.0)
                }
                var given_lat by remember {
                    mutableStateOf(0.0)
                }
                val dateDialogState = rememberMaterialDialogState()
                val timeDialogState = rememberMaterialDialogState()
                val selected_location = remember { mutableStateOf<LatLng?>(null) }
                val show_dialog = remember { mutableStateOf(false) }

                if (show_dialog.value) {
                    Dialog(onDismissRequest = { show_dialog.value = false }) {
                        LocationDialog { loc_x, loc_y ->
                            // Do something with the entered name
                            given_lng = loc_x.toDouble()
                            given_lat = loc_y.toDouble()
                            show_dialog.value = false
                        }
                    }
                }

                //updates the location
                locationHandler.startLocationUpdates { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("loc", latitude.toString() + " " + longitude.toString())
                }

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
                                    db.addMessage(msg_db, given_lat, given_lng, creation_time, username, date_time.time)
                                    //val reminders = db.getTimelyReminders(username, System.currentTimeMillis())
                                    val reminders = loadMessages(db, username, show_all_reminders)
                                    val msgs = checkDistance(reminders, latitude, longitude, 100.0)
                                    if (msgs.isNotEmpty()) {
                                        GlobalScope.launch {
                                            setNotif(
                                                msgs[0],
                                                10,
                                                this@MainActivity
                                            )
                                        }
                                    }
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
                                    val msgs = checkDistance(reminders, latitude, longitude, 100.0)
                                    if (msgs.isNotEmpty()) {
                                        GlobalScope.launch {
                                            setNotif(
                                                msgs[0],
                                                10,
                                                this@MainActivity
                                            )
                                        }
                                    }
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
                                    val msgs = checkDistance(reminders, latitude, longitude, 100.0)
                                    if (msgs.isNotEmpty()) {
                                        GlobalScope.launch {
                                            setNotif(
                                                msgs[0],
                                                10,
                                                this@MainActivity
                                            )
                                        }
                                    }
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
                            val msgs = checkDistance(reminders, latitude, longitude, 100.0)
                            if (msgs.isNotEmpty()) {
                                setNotif(
                                    msgs[0],
                                10,
                                this@MainActivity
                                )
                            }
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
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("geo:0,0?q=Googleplex")
                        }
                        selected_location.value = null // Reset the selected location
                        this@MainActivity.startActivity(intent)
                    }) {
                        Text("Open Google Maps")
                    }

                    Button(
                        onClick = { show_dialog.value = true }
                    ) {
                        Text("Give location")
                    }

                    // Show the selected location data
                    if (selected_location.value != null) {
                        Text("Selected location: ${selected_location.value}")
                    }
                }

                DisposableEffect(Unit) {
                    val activity = this@MainActivity as? AppCompatActivity
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            activity?.supportFragmentManager?.popBackStack()
                            selected_location.value = null // Reset the selected location
                        }
                    }
                    activity?.onBackPressedDispatcher?.addCallback(callback)
                    onDispose {
                        callback.remove()
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

    /**
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // Handle the result of launching the Maps activity
            val location = data?.getParcelableExtra<LatLng>("location")
            // Use the selected location in your app
            Log.d("map", "testi53254")
        }
    }
    **/

    private fun checkDistance(reminders: List<Reminder>, x: Double, y: Double,
                              distance: Double) : List<String>{


        val rems = reminders.filter { ((it.location_x - x).pow(2) + (it.location_y -y).pow(2)).pow(0.5) < distance }
        val msgs = rems.map{reminder -> reminder.message}
        Log.d("asd", msgs.toString())
        return msgs
    }

}

private fun loadMessages(database: ReminderDB, username: String, load_all: Boolean): List<Reminder> {
    if (load_all){
        return database.getReminders(username)
    } else {
        return database.getTimelyReminders(username, System.currentTimeMillis())
    }

}


@Composable
fun MapButton() {
    var location by remember { mutableStateOf<String?>(null) }
    val mapsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.let { data ->
                val selectedLocation = data.getParcelableExtra<LatLng>("location")
                //location = "Selected location: ${selectedLocation.latitude}, ${selectedLocation.longitude}"
                Log.d("map", selectedLocation.toString())
            }
        }
    }

    Button(onClick = {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        }
        mapsLauncher.launch(intent)
    }) {
        Text("Select Location")
    }

    location?.let {
        Text(it, modifier = Modifier.padding(16.dp))
    }
}

/**
@Composable
fun MapView() {
    val context = LocalContext.current
    AndroidView(
        factory = { context ->
            val mapFragment = MapFragment.newInstance()
            mapFragment.getMapAsync { map ->
                map.setOnMapClickListener { latLng ->
                    // Do something with the selected location
                    Log.d("mapview", latLng.toString())
                }
            }
            // Wrap the MapFragment inside a FrameLayout
            val frameLayout = FrameLayout(context)
            frameLayout.addView(mapFragment.view)
            frameLayout
        },
        update = { view ->
            // Do something with the FrameLayout
            Log.d("mapview", view.toString())
        }
    )
}
**/

@Composable
fun MapScreen() {
    var isMapShown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        Button(onClick = { isMapShown = true }) {
            Text(text = "Show Map")
        }
        if (isMapShown) {
            val mapView = MapView(context).apply {
                onCreate(null)
                getMapAsync { googleMap ->
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.7749, -122.4194), 12f))
                    googleMap.addMarker(MarkerOptions().position(LatLng(37.7749, -122.4194)).title("San Francisco"))
                }
            }

            AndroidView(
                factory = { mapView },
                update = { view ->
                    view.onCreate(null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun LocationDialog(onLocEntered: (String, String) -> Unit) {
    var loc_x by remember { mutableStateOf("") }
    var loc_y by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { },
        title = { Text("Enter the location") },
        text = {
            Column {
                TextField(
                    value = loc_x,
                    onValueChange = { loc_x = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = loc_y,
                    onValueChange = { loc_y = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLocEntered(loc_x, loc_y)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { }
            ) {
                Text("Cancel")
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MobilecompTheme {
    }
}