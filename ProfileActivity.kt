package com.example.mobilecomp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilecomp.ui.theme.MobilecompTheme
import org.mindrot.jbcrypt.BCrypt

class ProfileActivity : ComponentActivity() {
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
                first_name = shared_preferences.getString(username+"2", "default_value").toString()
                last_name = shared_preferences.getString(username+"3", "default_value").toString()

                var new_password by remember {
                    mutableStateOf("")
                }
                //var del_password by remember {
                //    mutableStateOf("")
                //}
                var password_visible: Boolean by remember { mutableStateOf(false) }
                //var password_visible_del: Boolean by remember { mutableStateOf(false) }

                //changes column
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Name: "+first_name+" "+last_name,fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = {
                        val intent = Intent(this@ProfileActivity, NameChangeActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }) {
                            Text(text = "Change name")
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    TextField(
                        value = new_password,
                        label = { Text("New password") },
                        singleLine = true,
                        onValueChange = { text ->
                            new_password = text
                        },
                        visualTransformation = if (password_visible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (password_visible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { password_visible = !password_visible }) {
                                if (password_visible) {
                                    Icon(imageVector = image, "Hide password")
                                } else {
                                    Icon(imageVector = image, "Show password")
                                }
                            }
                        })

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = {
                        if(new_password.isNotBlank()) {
                            val salt = BCrypt.gensalt()
                            val hash_pw = BCrypt.hashpw(new_password, salt)
                            editor.putString(username+"0", salt)
                            editor.putString(username+"1", hash_pw)
                            editor.apply()
                            Toast.makeText(this@ProfileActivity, "Password changed", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Change password")
                    }

                    Spacer(modifier = Modifier.height(200.dp))

                    /**
                    TextField(
                        value = del_password,
                        label = { Text("Type password to delete account") },
                        singleLine = true,
                        onValueChange = { text ->
                            del_password = text
                        },
                        visualTransformation = if (password_visible_del) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (password_visible_del)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { password_visible_del = !password_visible_del }) {
                                if (password_visible_del) {
                                    Icon(imageVector = image, "Hide password")
                                } else {
                                    Icon(imageVector = image, "Show password")
                                }
                            }
                        })

                        Spacer(modifier = Modifier.height(10.dp))
                    **/

                    Button(onClick = {
                        /**
                        if (CheckPassword(shared_preferences, username, del_password)) {
                            editor.remove(username)
                            editor.apply()
                            Toast.makeText(
                                this@ProfileActivity,
                                "Account deleted",
                                Toast.LENGTH_SHORT
                            )
                            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Wrong password",
                                Toast.LENGTH_SHORT
                            )
                        }
                        **/
                        editor.remove(username+"0")
                        editor.remove(username+"1")
                        editor.remove(username+"2")
                        editor.remove(username+"3")
                        editor.apply()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.putExtra("message", "Account deleted")
                        startActivity(intent)

                    }) { Text(text = "Delete account")
                    }

                }

                //return button
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(onClick = {
                        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }) {
                        Text(text = "<<<<")
                    }
                }

                //shows account name
                Column(modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Account: "+username,fontSize = 20.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    MobilecompTheme {
    }
}