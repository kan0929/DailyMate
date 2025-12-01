package com.example.dailymate

import android.os.Bundle
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
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.TextBlack
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.GrayText
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.*

val RoutineBoxBackgroundColor = Color(0xFFF3FFF1)
val RoutineFieldPlaceholderColor = Color(0xFF707070)

enum class RoutineType {
    DAILY, WEEKLY, MONTHLY
}

class ManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyMateTheme {
                val context = LocalContext.current
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(currentIndex = 2) { index ->
                            when (index) {
                                0 -> context.startActivity(android.content.Intent(context, MainActivity::class.java))
                                3 -> context.startActivity(android.content.Intent(context, DailyActivity::class.java))
                                4 -> context.startActivity(android.content.Intent(context, MypageActivity::class.java))
                            }
                        }
                    }
                ) { padding ->
                    ManagementScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun ManagementScreen(modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.KOREAN)
    val formattedDate = "${today.year}년 ${today.monthValue}월 ${today.dayOfMonth}일 ($dayOfWeek)"

    var selectedRoutineType by remember { mutableStateOf(RoutineType.DAILY) }

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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        RoutinePlaceholderText("루틴 제목")
                        RoutinePlaceholderText("목표량")
                        RoutinePlaceholderText("시작 날짜")
                        RoutinePlaceholderText("종료 날짜")
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        RoutineEditableFieldInBox("루틴 제목")
                        RoutineEditableFieldInBox("목표량")
                        RoutineEditableFieldInBox("시작 날짜")
                        RoutineEditableFieldInBox("종료 시간")
                    }
                }
            }
        }
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
fun RoutineEditableFieldInBox(placeholder: String) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (text.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 16.sp,
                color = RoutineFieldPlaceholderColor.copy(alpha = 0.6f),
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            )
        }

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = TextBlack,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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