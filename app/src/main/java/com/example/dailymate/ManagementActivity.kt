package com.example.dailymate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailymate.data.DailyMateDatabase
import com.example.dailymate.data.DailyMateViewModel
import com.example.dailymate.data.DailyMateViewModelFactory
import com.example.dailymate.data.Routine
import com.example.dailymate.data.RoutineRepository
import com.example.dailymate.data.UserRepository
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.TextBlack
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.GrayText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.*

val RoutineBoxBackgroundColor = Color(0xFFE8F5E9)
val RoutineFieldPlaceholderColor = Color(0xFF707070)
val DateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")

enum class RoutineType {
    DAILY, WEEKLY, MONTHLY
}

class ManagementActivity : ComponentActivity() {
    lateinit var viewModelFactory: DailyMateViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUserId = intent.getIntExtra("userId", intent.getIntExtra("UserId", -1))
        val receivedFullName = intent.getStringExtra("fullName") ?: "사용자"

        val db = DailyMateDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(db.dailyMateDao())
        val routineRepository = RoutineRepository(db.dailyMateDao())

        viewModelFactory = DailyMateViewModelFactory(userRepository, routineRepository)

        setContent {
            DailyMateTheme {
                val context = LocalContext.current
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 2) { index ->

                            val nextActivityClass = when (index) {
                                1 -> CalendarActivity::class.java
                                0 -> MainActivity::class.java
                                3 -> DailyActivity::class.java
                                4 -> MypageActivity::class.java
                                else -> null
                            }

                            if (nextActivityClass != null) {
                                val intent = Intent(context, nextActivityClass).apply {
                                    putExtra("userId", currentUserId)
                                    putExtra("UserId", currentUserId)
                                    putExtra("fullName", receivedFullName)
                                    flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                ) { padding ->
                    ManagementScreen(
                        modifier = Modifier.padding(padding),
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun ManagementScreen(modifier: Modifier = Modifier, currentUserId: Int) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"

    val context = LocalContext.current
    val viewModel: DailyMateViewModel = viewModel(factory = (context as ManagementActivity).viewModelFactory)

    var routineTitle by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today) }
    var selectedRoutineType by remember { mutableStateOf(RoutineType.DAILY) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        startDate.year,
        startDate.monthValue - 1,
        startDate.dayOfMonth
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            endDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        endDate.year,
        endDate.monthValue - 1,
        endDate.dayOfMonth
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "루틴 관리",
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
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoutineTab(
                        label = "일간",
                        selected = selectedRoutineType == RoutineType.DAILY,
                        onClick = { selectedRoutineType = RoutineType.DAILY }
                    )
                    RoutineTab(
                        label = "주간",
                        selected = selectedRoutineType == RoutineType.WEEKLY,
                        onClick = { selectedRoutineType = RoutineType.WEEKLY }
                    )
                    RoutineTab(
                        label = "월간",
                        selected = selectedRoutineType == RoutineType.MONTHLY,
                        onClick = { selectedRoutineType = RoutineType.MONTHLY }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        RoutinePlaceholderText("루틴 제목")
                        RoutinePlaceholderText("목표량")
                        RoutinePlaceholderText("시작 날짜")
                        RoutinePlaceholderText("종료 날짜")
                    }

                    Column(modifier = Modifier.weight(2f)) {
                        RoutineEditableFieldInBox(
                            value = routineTitle,
                            onValueChange = { routineTitle = it },
                            placeholder = "루틴 제목",
                            isDate = false,
                            onDateClick = {}
                        )
                        RoutineEditableFieldInBox(
                            value = goalAmount,
                            onValueChange = { goalAmount = it },
                            placeholder = "목표량",
                            isDate = false,
                            onDateClick = {}
                        )
                        RoutineEditableFieldInBox(
                            value = startDate.format(DateFormat),
                            onValueChange = { },
                            placeholder = "시작 날짜",
                            isDate = true,
                            onDateClick = { datePickerDialog.show() }
                        )
                        RoutineEditableFieldInBox(
                            value = endDate.format(DateFormat),
                            onValueChange = { },
                            placeholder = "종료 날짜",
                            isDate = true,
                            onDateClick = { endDatePickerDialog.show() }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))

        AddButton(onClick = {
            val trimmedTitle = routineTitle.trim()
            val trimmedGoal = goalAmount.trim()

            // ⭐ [핵심 수정 1] currentUserId 유효성 검사를 먼저 명확하게 처리
            if (currentUserId == -1) {
                Toast.makeText(context, "오류: 사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show()
                return@AddButton
            }

            // ⭐ [핵심 수정 2] 제목/목표량 유효성 검사
            if (trimmedTitle.isEmpty() || trimmedGoal.isEmpty()) {
                Toast.makeText(context, "제목과 목표량을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@AddButton
            }

            // ⭐ [핵심 수정 3] 날짜 유효성 검사
            if (startDate.isAfter(endDate)) {
                Toast.makeText(context, "시작 날짜는 종료 날짜보다 늦을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@AddButton
            }

            // 모든 검사 통과 시 루틴 추가
            val startDayLong = startDate.toEpochDay()
            val endDayLong = endDate.toEpochDay()

            val newRoutine = Routine(
                userId = currentUserId,
                name = trimmedTitle,
                days = selectedRoutineType.name,
                isCompleted = false,
                goalAmount = trimmedGoal,
                routineType = selectedRoutineType.name,
                startDate = startDayLong,
                endDate = endDayLong
            )
            viewModel.addRoutine(newRoutine)
            Toast.makeText(context, "루틴 '${trimmedTitle}'이 추가되었습니다.", Toast.LENGTH_SHORT).show()

            routineTitle = ""
            goalAmount = ""
            startDate = today
            endDate = today
        })
    }
}

@Composable
fun RoutineEditableFieldInBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDate: Boolean,
    onDateClick: () -> Unit
) {
    val modifier = if (isDate) {
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 12.dp)
            .clickable(enabled = isDate, onClick = onDateClick)
    } else {
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 12.dp)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 16.sp,
                color = RoutineFieldPlaceholderColor.copy(alpha = 0.6f),
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = TextBlack,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            readOnly = isDate
        )
    }
}


@Composable
fun AddButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8F5E9),
            contentColor = PrimaryGreen
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "추가하기",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
    }
}

@Composable
fun RoutinePlaceholderText(placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = placeholder,
            fontSize = 16.sp,
            color = RoutineFieldPlaceholderColor,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
    }
}

@Composable
fun RoutineTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) PrimaryGreen else Color.Transparent
    val textColor = if (selected) Color.White else GrayText

    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
    }
}