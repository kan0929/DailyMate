package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.dailymate.data.*
import com.example.dailymate.ui.theme.AddRoutineActivity
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.TextBlack
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚨 수정 1: SigninActivity에서 "userId"로 넘겨줬으므로 키를 "userId"로 수정.
        val currentUserId = intent.getIntExtra("userId", -1)
        // 🚨 수정 2: Intent에서 "fullName"을 받아와서 사용할 준비.
        val receivedFullName = intent.getStringExtra("fullName") ?: "사용자"

        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())
        val viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                val context = LocalContext.current

                val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)

                LaunchedEffect(Unit) {
                    if (currentUserId != -1) {
                        viewModel.setUserId(currentUserId)
                        // 🚨 수정 3: Intent로 받은 fullName을 ViewModel 상태에 즉시 설정.
                        viewModel.setCurrentUserName(receivedFullName)
                    }
                }

                val fullName by viewModel.currentUserName.collectAsState()
                val todayRoutines by viewModel.todayRoutines.collectAsState(emptyList())
                val progress by viewModel.progress.collectAsState(0f)

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 0) { index ->
                            val nextActivityClass = when (index) {
                                2 -> ManagementActivity::class.java
                                3 -> DailyActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    putExtra("CURRENT_USER_ID", currentUserId)
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
    userId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"
    val completedCount = routines.count { it.isCompleted }

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
                Text(
                    text = "오늘의 루틴",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                )

                Spacer(modifier = Modifier.height(16.dp))

                routines.forEach { routine ->
                    RoutineCheckItem(
                        name = routine.name,
                        isCompleted = routine.isCompleted,
                        onToggle = { onRoutineToggle(routine) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { context.startActivity(Intent(context, AddRoutineActivity::class.java).apply {
                            putExtra("CURRENT_USER_ID", userId)
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
                    text = "${completedCount} / ${routines.size} 완료",
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
fun RoutineCheckItem(name: String, isCompleted: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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