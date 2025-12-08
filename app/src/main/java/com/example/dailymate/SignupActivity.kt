package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.User
import com.example.dailymate.data.UserRepository
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.PrimaryGreen
val TextFieldBackgroundColor = Color(0xFFE8F5E9)
val SignupButtonColor = Color(0xFFA5D6A7)
val TextBlack = Color(0xFF333333)

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())
        val viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                SignupScreen(
                    viewModelFactory = viewModelFactory,
                    onSignupSuccess = { newUserId, fullName ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("UserId", newUserId)
                            putExtra("fullName", fullName)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToSignin = {
                        startActivity(Intent(this, SigninActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onSignupSuccess: (Int, String) -> Unit,
    onNavigateToSignin: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Join with",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
        Text(
            text = "DailyMate!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )

        Spacer(modifier = Modifier.height(48.dp))

        SignupTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Full Name"
        )
        Spacer(modifier = Modifier.height(20.dp))

        SignupTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email"
        )
        Spacer(modifier = Modifier.height(20.dp))

        SignupTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(20.dp))

        SignupTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirm Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }


        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "비밀번호가 일치하지 않습니다."
                } else if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "모든 필드를 채워주세요."
                } else {
                    errorMessage = null
                    val passwordHash = hashPassword(password)
                    val newUser = User(
                        fullName = fullName,
                        email = email,
                        passwordHash = passwordHash
                    )
                    viewModel.signup(newUser, onSuccess = { userId, returnedFullName ->
                        onSignupSuccess(userId, returnedFullName)
                    }, onError = {
                        errorMessage = "이미 등록된 이메일이거나 회원가입에 실패했습니다."
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignupButtonColor,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Sign up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign In",
            fontSize = 14.sp,
            color = PrimaryGreen,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
            modifier = Modifier.clickable(onClick = onNavigateToSignin)
        )
    }
}

@Composable
fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = PrimaryGreen,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = TextFieldBackgroundColor,
                shape = RoundedCornerShape(30.dp)
            ),
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TextFieldBackgroundColor,
            unfocusedContainerColor = TextFieldBackgroundColor,
            disabledContainerColor = TextFieldBackgroundColor,
            cursorColor = PrimaryGreen,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(
            color = TextBlack,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        ),
        singleLine = true
    )
}