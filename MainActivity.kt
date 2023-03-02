package com.example.mobilecomp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobilecomp.ui.theme.MobilecompTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilecompTheme {

                //sharedPreferences init
                sharedPreferences = getSharedPreferences("login_details", Context.MODE_PRIVATE)
                username = intent.getStringExtra("username").toString()

                //var class datalle
                var msg by remember {
                    mutableStateOf("")
                }
                var messages by remember {
                    mutableStateOf(listOf<String>())
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = msg,
                        label = { Text("Add a reminder") },
                        singleLine = true,
                        onValueChange = { text ->
                            msg = text
                        })

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = {
                        if(msg.isNotBlank()) {
                            messages = messages + msg
                            msg = ""
                        }
                    }) {
                        Text(text = "Create")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                //extra buttons
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End
                    //padding
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
                }

                LazyColumn {
                    items(messages) {currentMsg ->
                        Text(text = currentMsg)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MobilecompTheme {
    }
}