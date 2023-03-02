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
import java.security.SecureRandom

class SignupActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            //sharedPreferences init
            sharedPreferences = getSharedPreferences("login_details", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            MobilecompTheme {
                var first_name by remember {
                    mutableStateOf("")
                }
                var last_name by remember {
                    mutableStateOf("")
                }
                var username by remember {
                    mutableStateOf("")
                }
                var password by remember {
                    mutableStateOf("")
                }
                var passwordVisible: Boolean by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = first_name,
                        label = { Text("First name") },
                        singleLine = true,
                        onValueChange = { text ->
                            first_name = text
                        })
                    TextField(
                        value = last_name,
                        label = { Text("Last name") },
                        singleLine = true,
                        onValueChange = { text ->
                            last_name = text
                        })
                    TextField(
                        value = username,
                        label = { Text("Username") },
                        singleLine = true,
                        onValueChange = { text ->
                            username = text
                        })
                    TextField(
                        value = password,
                        label = { Text("Password") },
                        singleLine = true,
                        onValueChange = { text ->
                            password = text
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                if (passwordVisible) {
                                    Icon(imageVector = image, "Hide password")
                                } else {
                                    Icon(imageVector = image, "Show password")
                                }
                            }
                        })

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = {
                        if (username.isNotBlank() && password.isNotBlank()) {

                            if (sharedPreferences.contains(username+"1")) {
                                Toast.makeText(this@SignupActivity, "Username already in use", Toast.LENGTH_SHORT).show()
                            } else {
                                //hash + salt the password
                                val salt = BCrypt.gensalt()
                                val hash_pw = BCrypt.hashpw(password, salt)

                                //saves the info
                                editor.putString(username+"0", salt)
                                editor.putString(username+"1", hash_pw)
                                editor.putString(username+"2", first_name)
                                editor.putString(username+"3", last_name)
                                editor.apply()

                                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                                intent.putExtra("message", "Account created")
                                startActivity(intent)
                            }
                        }
                    }) {
                        Text(text = "Sign up")
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(onClick = {
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
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
fun DefaultPreview3() {
    MobilecompTheme {
    }
}