package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.Routine
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.data.UserPreferences
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.TextBlack
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserId = intent.getIntExtra("userId", intent.getIntExtra("UserId", -1))
        val receivedFullName = intent.getStringExtra("fullName") ?: "사용자"
        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())
        val viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                val context = LocalContext.current
                val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)
                val userPreferences = remember { UserPreferences(context) }

                LaunchedEffect(Unit) {
                    if (currentUserId != -1) {
                        viewModel.setUserId(currentUserId)

                        // 데이터베이스에서 실제 유저 정보를 로드하여 이메일 획득
                        val user = userRepository.getUserById(currentUserId)
                        val userEmail = user?.email ?: "$currentUserId@example.com"
                        val finalFullName = user?.fullName ?: receivedFullName

                        if (receivedFullName == "사용자") {
                            viewModel.setCurrentUserName(finalFullName)
                        } else {
                            viewModel.setCurrentUserName(receivedFullName)
                        }

                        // SharedPreferences에 최신 유저 정보 저장 (ID, 이름, 이메일)
                        userPreferences.saveUserInfo(currentUserId, finalFullName, userEmail)
                    }
                }

                val fullName by viewModel.currentUserName.collectAsState()
                val todayRoutines by viewModel.dailyRoutines.collectAsState(emptyList())
                val progress by viewModel.progress.collectAsState(0f)

                // 네비게이션에 사용할 이메일을 SharedPreferences에서 미리 로드
                val currentEmail = userPreferences.getUserEmail()

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 0) { index: Int ->
                            val nextActivityClass = when (index) {
                                1 -> CalendarActivity::class.java
                                2 -> ManagementActivity::class.java
                                3 -> DailyActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    // MypageActivity는 Prefs를 사용하지만, 다른 Activity와의 호환성을 위해 Intent로도 데이터를 전달
                                    putExtra("userId", currentUserId)
                                    putExtra("UserId", currentUserId)
                                    putExtra("fullName", fullName)
                                    putExtra("UserEmail", currentEmail)
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                ) { padding ->
                    HomeScreen(
                        fullName = fullName,
                        routines = todayRoutines,
                        progress = progress,
                        onRoutineToggle = viewModel::toggleRoutineCompletion,
                        onRoutineDelete = viewModel::deleteRoutine,
                        userId = currentUserId,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    fullName: String,
    routines: List<Routine>,
    progress: Float,
    onRoutineToggle: (Routine) -> Unit,
    onRoutineDelete: (Routine) -> Unit,
    userId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"

    val completedCount = routines.count { it.isCompleted }

    val RoutineBoxBackgroundColor = Color(0xFFF1F7F0)

    val isManagementMode = remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "${fullName}님, 환영합니다!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
            color = PrimaryGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formattedDate,
            fontSize = 16.sp,
            color = TextBlack.copy(alpha = 0.6f),
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoutineBoxBackgroundColor, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "오늘의 루틴",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                    )
                    Text(
                        text = if (isManagementMode.value) "완료" else "관리",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable { isManagementMode.value = !isManagementMode.value }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (routines.isEmpty()) {
                    Text(
                        text = "오늘은 등록된 일간 루틴이 없어요.",
                        fontSize = 16.sp,
                        color = TextBlack.copy(alpha = 0.6f),
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    routines.forEach { routine ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RoutineCheckItem(
                                name = routine.name,
                                isCompleted = routine.isCompleted,
                                onToggle = {
                                    if (!isManagementMode.value) {
                                        onRoutineToggle(routine)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (isManagementMode.value) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { onRoutineDelete(routine) }
                                        .padding(4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { context.startActivity(Intent(context, ManagementActivity::class.java).apply {
                            putExtra("UserId", userId)
                        }) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "루틴 추가하기",
                        fontSize = 16.sp,
                        color = PrimaryGreen,
                        fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "${(progress * routines.size).toInt()} / ${routines.size} 완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryGreen,
                    trackColor = RoutineBoxBackgroundColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "오늘의 루틴 진행도는 ${(progress * 100).toInt()}%예요!",
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "데일리와 함께 더 힘내봐요!",
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                    color = TextBlack
                )
            }
        }
    }
}

@Composable
fun RoutineCheckItem(name: String, isCompleted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(

        modifier = modifier
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = name + " 완료됨",
                    tint = PrimaryGreen,
                    modifier = Modifier
                        .size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            fontSize = 18.sp,
            color = PrimaryGreen,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
    }
}