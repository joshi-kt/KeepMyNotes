package com.example.keepmynotes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.keepmynotes.R
import com.example.keepmynotes.ui.theme.lightBlue
import com.example.keepmynotes.ui.viewmodel.AuthViewModel
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.PLEASE_WAIT
import com.example.keepmynotes.utils.Utils.SCREEN_AUTH
import com.example.keepmynotes.utils.Utils.SCREEN_NOTES
import com.example.keepmynotes.utils.Utils.isSignInCredentialValid
import com.example.keepmynotes.utils.Utils.isSignUpCredentialValid
import com.example.keepmynotes.utils.Utils.logger
import com.example.keepmynotes.utils.Utils.showToast
import com.example.keepmynotes.utils.Utils.validateEmail
import com.example.keepmynotes.utils.Utils.validateName
import com.example.keepmynotes.utils.Utils.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlin.time.Duration

@Composable
fun AuthScreen(navController: NavController){

    val authViewModel : AuthViewModel = hiltViewModel()
    val context = LocalContext.current

    val authenticationState by authViewModel.authenticationState.observeAsState()
    val authErrorText by authViewModel.authErrorText.observeAsState()
    val isLoading by authViewModel.showLoading.observeAsState()

    if (authenticationState == Utils.AuthenticationState.AUTHENTICATED) {
        navController.navigate(SCREEN_NOTES) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    if (!authErrorText.isNullOrBlank()) {
        showToast(LocalContext.current, authErrorText!!)
        authViewModel.resetErrorText()
    }

    if (isLoading == true) {
        MyProgressDialog(PLEASE_WAIT)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var isSignIn by rememberSaveable {
            mutableStateOf(true)
        }

        Text(text = "Welcome To Keep My Notes",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif
        )

        if (isSignIn) {
            SignInScreen(changeToSignUp = {
                isSignIn = false
            }, signIn = { email,password ->
                if (isSignInCredentialValid(email,password)) {
                    authViewModel.signIn(email, password)
                }
            }, signInUsingGoogle = {
                authViewModel.signInUsingGoogle(context)
            })
        } else {
            SignUpScreen(changeToSignIn = {
                isSignIn = true
            }, signUp = { name , email, password ->
                if (isSignUpCredentialValid(name, email, password)) {
                    authViewModel.createUser(name, email, password)
                }
            })
        }

    }
}

@Composable
fun SignInScreen(
    changeToSignUp : () -> Unit,
    signIn : (String,String) -> Unit,
    signInUsingGoogle : () -> Unit
) {

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    Card(modifier = Modifier
        .padding(top = 20.dp)
        .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = lightBlue)
    ) {
        Column(modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black)

            EditTextAuth(value = email, label = "Email", onValueChanged = {
                email = it.trim()
            }, keyboardType = KeyboardType.Email, isError = !validateEmail(email), errorText = "Please add a valid email")

            EditTextAuth(value = password, label = "Password", onValueChanged = {
                password = it.trim()
            }, keyboardType = KeyboardType.Password, isError = !validatePassword(password), errorText = "Please add a valid password")

            Button(onClick = {signIn(email.trim(), password.trim())},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.padding(top = 10.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(text = "Login",
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

            Button(onClick = { signInUsingGoogle() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.padding(top = 10.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {

                Image(painter = painterResource(id = R.drawable.google), contentDescription = "Google log in", modifier = Modifier.size(32.dp))

                Text(text = "Login with Google",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Row(modifier = Modifier.padding(top = 15.dp)) {

                Text(text = "New to this app ?",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 2.dp)
                )

                Text(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Sign up now")
                    }
                },
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable {
                            changeToSignUp()
                        },
                    fontSize = 18.sp,
                    color = Color.Black,
                )
            }

        }
    }
}

@Composable
fun SignUpScreen(
    changeToSignIn : () -> Unit,
    signUp : (String,String,String) -> Unit
) {

    var name by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    Card(modifier = Modifier.padding(top = 20.dp),
        colors = CardDefaults.cardColors(containerColor = lightBlue)
    ) {
        Column(modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Signup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black)

            EditTextAuth(value = name, label = "Name", onValueChanged = {
                name = it
            }, keyboardType = KeyboardType.Text, isError = !validateName(name), errorText = "Please add a valid name")

            EditTextAuth(value = email, label = "Email", onValueChanged = {
                email = it.trim()
            }, keyboardType = KeyboardType.Email, isError = !validateEmail(email), errorText = "Please add a valid email")

            EditTextAuth(value = password, label = "Password", onValueChanged = {
                password = it.trim()
            }, keyboardType = KeyboardType.Password, isError = !validatePassword(password), errorText = "Please add a valid password")

            Button(onClick = {
                signUp(name.trim(),email.trim(),password.trim())
                             },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.padding(top = 10.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(text = "Signup",
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

            Button(onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.padding(top = 10.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {

                Image(painter = painterResource(id = R.drawable.google), contentDescription = "Google signup", modifier = Modifier.size(32.dp))

                Text(text = "Signup with Google",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Row(modifier = Modifier.padding(top = 15.dp)) {

                Text(text = "Already saved a Todo ?",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 2.dp)
                )

                Text(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Login now")
                    }
                },
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable {
                            changeToSignIn()
                        },
                    fontSize = 18.sp,
                    color = Color.Black,
                )
            }

        }
    }
}

@Composable
fun EditTextAuth(value : String,
                 onValueChanged : (String) -> Unit,
                 label : String,
                 keyboardType: KeyboardType,
                 errorText : String,
                 isError : Boolean,
) {
    OutlinedTextField(
        value = value,
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth(),
        textStyle = TextStyle(fontSize = 18.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp),
                )
            } },
        onValueChange = { if (it.length < 50) {onValueChanged(it)} },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black,
            disabledBorderColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            cursorColor = Color.Black,
            errorBorderColor = Color.Black,
            errorLabelColor = Color.Black,
            errorCursorColor = Color.Black,
        ),
        shape = RoundedCornerShape(10.dp),
        label = { Text(text = label) },
        trailingIcon = {
            if (isError) {
                Icon(Icons.Filled.Warning, contentDescription = "Error icon", tint = Color.Red)
            }
        }
    )

}

@Composable
fun MyProgressDialog(loadingText : String) {
    Dialog(onDismissRequest = {}){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(bottom = 5.dp))
            Text(text = loadingText,
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 5.dp),
                color = Color.White
            )
        }
    }
}