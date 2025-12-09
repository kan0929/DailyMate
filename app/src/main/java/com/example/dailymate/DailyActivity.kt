package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete // 삭제 아이콘 import
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                        BottomNavigationBar(currentIndex = 3) { index ->

                            val nextActivityClass = when (index) {
                                1 -> CalendarActivity::class.java
                                0 -> MainActivity::class.java
                                2 -> ManagementActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    putExtra("UserId", currentUserId)
                                    putExtra("userId", currentUserId)
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

    // userId를 ViewModel에 설정하여 루틴을 로드합니다.
    LaunchedEffect(userId) {
        if (userId != -1) {
            viewModel.setUserId(userId)
        }
    }

    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"

    // ViewModel에서 타입별 루틴 목록을 가져옵니다.
    val dailyRoutines by viewModel.dailyRoutines.collectAsState(initial = emptyList())
    val weeklyRoutines by viewModel.weeklyRoutines.collectAsState(initial = emptyList())
    val monthlyRoutines by viewModel.monthlyRoutines.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
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

        // 일간 루틴 섹션
        RoutineSection(
            title = "일간 루틴",
            routines = dailyRoutines,
            onToggle = viewModel::toggleRoutineCompletion,
            onDelete = viewModel::deleteRoutine, // 삭제 함수 전달
            userId = userId
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 주간 루틴 섹션
        RoutineSection(
            title = "주간 루틴",
            routines = weeklyRoutines,
            onToggle = viewModel::toggleRoutineCompletion,
            onDelete = viewModel::deleteRoutine, // 삭제 함수 전달
            userId = userId
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 월간 루틴 섹션
        RoutineSection(
            title = "월간 루틴",
            routines = monthlyRoutines,
            onToggle = viewModel::toggleRoutineCompletion,
            onDelete = viewModel::deleteRoutine, // 삭제 함수 전달
            userId = userId
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// 타입별 루틴 목록을 표시하는 재사용 가능한 컴포넌트
@Composable
fun RoutineSection(
    title: String,
    routines: List<Routine>,
    onToggle: (Routine) -> Unit,
    onDelete: (Routine) -> Unit, // 삭제 콜백 추가
    userId: Int
) {
    val context = LocalContext.current
    // 관리 모드 상태 추가
    val isManagementMode = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F7F0), shape = RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // 헤더 Row로 변경하여 관리 버튼 추가
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
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
                    text = "오늘은 등록된 ${title}이 없어요.",
                    fontSize = 16.sp,
                    color = GrayText,
                    fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                routines.forEach { routine ->
                    // 루틴 아이템과 삭제 버튼을 포함하는 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DailyRoutineCheckItem(
                            name = routine.name,
                            isCompleted = routine.isCompleted,
                            onToggle = {
                                if (!isManagementMode.value) { // 관리 모드가 아닐 때만 토글 가능
                                    onToggle(routine)
                                }
                            },
                            modifier = Modifier.weight(1f) // 루틴 아이템이 남은 공간을 모두 차지
                        )

                        // 관리 모드일 때만 삭제 버튼 표시
                        if (isManagementMode.value) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onDelete(routine) }
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


@Composable
fun DailyRoutineCheckItem(name: String, isCompleted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) { // Modifier 인자 추가
    Row(
        modifier = modifier // 받은 modifier 사용 (fillMaxWidth 대신 weight를 사용할 수 있도록)
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