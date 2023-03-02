package com.example.mobilecomp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilecomp.ui.theme.MobilecompTheme
import org.mindrot.jbcrypt.BCrypt

class NameChangeActivity : ComponentActivity() {
    private lateinit var shared_preferences: SharedPreferences
    private lateinit var username: String
    private lateinit var first_name: String
    private lateinit var last_name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            //sharedPreferences init
            shared_preferences = getSharedPreferences("login_details", Context.MODE_PRIVATE)
            val editor = shared_preferences.edit()

            MobilecompTheme {

                username = intent.getStringExtra("username").toString()
                first_name =
                    shared_preferences.getString(username + "2", "default_value").toString()
                last_name = shared_preferences.getString(username + "3", "default_value").toString()

                var new_first_name by remember {
                    mutableStateOf("")
                }
                var new_last_name by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Current name: " + first_name + " " + last_name, fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = new_first_name,
                        label = { Text("Give first name") },
                        singleLine = true,
                        onValueChange = { text ->
                            new_first_name = text
                        })
                    TextField(
                        value = new_last_name,
                        label = { Text("Give last name") },
                        singleLine = true,
                        onValueChange = { text ->
                            new_last_name = text
                        })

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = {
                        editor.putString(username + "2", new_first_name)
                        editor.putString(username + "3", new_last_name)
                        editor.apply()
                        Toast.makeText(this@NameChangeActivity, "Name changed", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        Text(text = "Change name")
                    }
                }

                //return button
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(onClick = {
                        val intent =
                            Intent(this@NameChangeActivity, ProfileActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }) {
                        Text(text = "<<<<")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview5() {
    MobilecompTheme {
    }
}