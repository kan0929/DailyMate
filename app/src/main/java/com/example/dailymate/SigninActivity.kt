package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.*
import kotlinx.coroutines.launch

class SigninActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DailyMateDatabase.getDatabase(application)
        val dailyMateDao = database.dailyMateDao()

        val userRepository = UserRepository(dailyMateDao)
        val routineRepository = RoutineRepository(dailyMateDao)

        val factory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                val viewModel: DailyMateViewModel = viewModel(factory = factory)

                SigninScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { userId, fullName ->
                        val intent = Intent(this@SigninActivity, MainActivity::class.java).apply {
                            // ⭐ 수정: "userId" -> "UserId" (대문자 D로 통일)
                            putExtra("UserId", userId)
                            putExtra("fullName", fullName)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onSignupClick = {
                        val intent = Intent(this@SigninActivity, SignupActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigninField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = PrimaryGreen) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = TextBlack),
        shape = RoundedCornerShape(25.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            disabledContainerColor = FieldBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = PrimaryGreen
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun SigninScreen(
    viewModel: DailyMateViewModel,
    onLoginSuccess: (Int, String) -> Unit,
    onSignupClick: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(
            color = BackgroundWhite,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome back!",
                    style = TextStyle(
                        fontSize = 36.sp,
                        lineHeight = 50.sp,
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp,
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                SigninField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SigninField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        val passwordHash = hashPassword(password)
                        viewModel.signin(
                            email = email,
                            passwordHash = passwordHash,
                            onSuccess = { userId, fullName ->
                                onLoginSuccess(userId, fullName)
                            },
                            onError = {
                                val errorMessage = "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage,
                                        actionLabel = "확인",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = enhangreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("SIGN IN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "계정이 없으신가요?",
                        fontSize = 14.sp,
                        color = GrayText,
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "회원가입",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                        modifier = Modifier.clickable(onClick = onSignupClick)
                    )

                }
            }
        }
    }
}