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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.data.Routine
import com.example.dailymate.ui.theme.AddRoutineActivity
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.TextBlack
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.GrayText
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.*

class DailyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUserId = intent.getIntExtra("UserId", -1)
        val receivedFullName = intent.getStringExtra("fullName") ?: "사용자"

        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())
        val viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                val context = LocalContext.current
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 3) { index ->

                            val nextActivityClass = when (index) {
                                0 -> MainActivity::class.java
                                2 -> ManagementActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    putExtra("UserId", currentUserId)
                                    putExtra("fullName", receivedFullName)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                ) { padding ->
                    DailyRoutineScreen(
                        modifier = Modifier.padding(padding),
                        userId = currentUserId,
                        viewModelFactory = viewModelFactory
                    )
                }
            }
        }
    }
}

@Composable
fun DailyRoutineScreen(
    modifier: Modifier = Modifier,
    userId: Int,
    viewModelFactory: ViewModelProvider.Factory
) {
    val context = LocalContext.current
    val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)

    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"

    // **핵심 수정: emptyList()에 Routine 타입을 명시**
    val allRoutines by viewModel.getAllRoutines.observeAsState(initial = emptyList<Routine>())

    val dailyRoutines = remember(allRoutines) {
        // 리스트 allRoutines가 List<Routine>임을 알게 되어 타입 명시가 필요 없어짐
        allRoutines.filter { it.userId == userId && it.days.contains(dayOfWeek) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "오늘의 루틴",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            fontSize = 16.sp,
            color = GrayText,
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

                dailyRoutines.forEach { routine ->
                    DailyRoutineCheckItem(
                        name = routine.name,
                        isCompleted = routine.isCompleted,
                        onToggle = {
                            viewModel.updateRoutineCompletion(routine.id, !routine.isCompleted)
                        }
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
                        contentDescription = "루틴 추가",
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
    }
}

@Composable
fun DailyRoutineCheckItem(name: String, isCompleted: Boolean, onToggle: () -> Unit) {
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