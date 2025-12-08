package com.example.dailymate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.ui.theme.PrimaryGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasswordChangeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("UserId", -1)

        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())
        val viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)

            PasswordChangeScreen(userId, userRepository)
        }
    }
}

@Composable
fun PasswordChangeScreen(
    userId: Int,
    userRepository: UserRepository
) {
    val context = LocalContext.current
    val (newPassword, setNewPassword) = remember { mutableStateOf("") }
    val (confirmPassword, setConfirmPassword) = remember { mutableStateOf("") }

    val isButtonEnabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword

    fun handlePasswordChange() {
        if (userId == -1) {
            Toast.makeText(context, "유저 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(context, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val newPasswordHash = newPassword.hashCode().toString()

        CoroutineScope(Dispatchers.IO).launch {
            val updatedRows = userRepository.updatePassword(userId, newPasswordHash)

            CoroutineScope(Dispatchers.Main).launch {
                if (updatedRows > 0) {
                    Toast.makeText(context, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    (context as? ComponentActivity)?.finish()
                } else {
                    Toast.makeText(context, "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "비밀번호 변경",
            fontSize = 24.sp,
            color = PrimaryGreen,
            modifier = Modifier.padding(top = 32.dp, bottom = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = setNewPassword,
            label = { Text("새 비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            // 🌟 [수정] focusedBorderColor -> focusedIndicatorColor
            // 🌟 [수정] unfocusedBorderColor -> unfocusedIndicatorColor
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = Color.LightGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = setConfirmPassword,
            label = { Text("새 비밀번호 확인") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = Color.LightGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = ::handlePasswordChange,
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("비밀번호 변경 완료", fontSize = 18.sp)
        }
    }
}