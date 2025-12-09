package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.Routine
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.GrayText
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.TextBlack
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class CalendarActivity : ComponentActivity() {
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
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 1) { index ->
                            val nextActivityClass = when (index) {
                                0 -> MainActivity::class.java
                                2 -> ManagementActivity::class.java
                                3 -> DailyActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    putExtra("userId", currentUserId)
                                    putExtra("UserId", currentUserId)
                                    putExtra("fullName", receivedFullName)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                ) { padding ->
                    CalendarScreen(
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
fun CalendarScreen(
    modifier: Modifier = Modifier,
    userId: Int,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)
    val allRoutines by viewModel.getAllRoutines.observeAsState(initial = emptyList<Routine>())

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${selectedDate.year}년 ${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 (${dayOfWeek})"

    val routinesForSelectedDay = remember(allRoutines, selectedDate) {
        allRoutines.filter { routine ->
            routine.userId == userId && routine.days.contains(dayOfWeek)
        }
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
            text = "캘린더",
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

        Spacer(modifier = Modifier.height(24.dp))

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp)),
            factory = { context ->
                CalendarView(context).apply {
                    date = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    }
                }
            },
            update = { calendarView ->
                val newDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (calendarView.date != newDate) {
                    calendarView.date = newDate
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoutineBoxBackgroundColor, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            if (routinesForSelectedDay.isEmpty()) {
                Text(
                    text = "선택한 날짜에 등록된 루틴이 없어요.",
                    fontSize = 16.sp,
                    color = TextBlack.copy(alpha = 0.7f),
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    routinesForSelectedDay.forEach { routine ->
                        Text(
                            text = "• ${routine.name}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen,
                            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                        )
                    }
                }
            }
        }
    }
}