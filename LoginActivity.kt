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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobilecomp.ui.theme.MobilecompTheme
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            //sharedPreferences init
            sharedPreferences = getSharedPreferences("login_details", Context.MODE_PRIVATE)

            //checks for possible message
            if (intent.hasExtra("message")) {
                Toast.makeText(
                    this@LoginActivity,
                    intent.getStringExtra("message").toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            MobilecompTheme {
                var username by remember {
                    mutableStateOf("")
                }
                var password by remember {
                    mutableStateOf("")
                }
                var passwordVisible: Boolean by remember { mutableStateOf(false) }

                //login column
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                            IconButton(onClick = {passwordVisible = !passwordVisible}){
                                if (passwordVisible) {
                                    Icon(imageVector = image, "Hide password")
                                } else {
                                    Icon(imageVector = image, "Show password")
                                }
                            }
                        })
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(onClick = {
                        if(sharedPreferences.contains(username+"1")) {
                            if (checkPassword(sharedPreferences, username, password)) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("username", username)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@LoginActivity, "Wrong username or password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Wrong username or password", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Login")
                    }
                }

                //account creation button
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text(text = "Create an account")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

fun checkPassword (sp: SharedPreferences, username: String, password: String): Boolean{

    //salts and hashes
    val salt = sp.getString(username+"0", "default_value")
    val hash_pw = BCrypt.hashpw(password, salt)

    //checks if the passwords match
    val sp_password = sp.getString(username+"1", "default_value")

    if (sp_password == hash_pw) {
        return true
    }
    return false
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    MobilecompTheme {
    }
}