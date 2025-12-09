package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.TextBlack
import com.example.dailymate.R

val LogoutTextColor = Color(0xFFB0B0B0)
val SectionBackgroundColor = Color(0xFFF7F7F7)

class MypageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUserId = intent.getIntExtra("userId",  intent.getIntExtra("UserId", -1))
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
                        BottomNavigationBar(currentIndex = 4) { index ->
                            val nextActivityClass = when (index) {
                                1 -> CalendarActivity::class.java
                                0 -> MainActivity::class.java
                                2 -> ManagementActivity::class.java
                                3 -> DailyActivity::class.java
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
                                finish()
                            }
                        }
                    }
                ) { padding ->
                    MypageScreen(
                        userId = currentUserId,
                        userName = receivedFullName,
                        viewModelFactory = viewModelFactory,
                        onLogout = {
                            startActivity(Intent(this, SigninActivity::class.java))
                            finish()
                        },
                        onAccountDeleted = { startActivity(Intent(this, SigninActivity::class.java))
                            finish()
                        },
                        // ⭐ [수정] Scaffold에서 받은 padding을 modifier에 적용
                        modifier = Modifier.padding(padding),
                        // ⭐ [추가] 프로필 클릭 시 ProfileActivity로 이동하는 로직
                        onProfileClick = {
                            val intent = Intent(context, ProfileActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra("UserId", currentUserId)
                                putExtra("fullName", receivedFullName)
                            }

                            // 액티비티 시작
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MypageScreen(
    userId: Int,
    userName: String,
    viewModelFactory: ViewModelProvider.Factory,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
    onProfileClick: () -> Unit, // ⭐ [추가] onProfileClick 콜백
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: DailyMateViewModel = viewModel(factory = viewModelFactory)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ⭐ [추가] 정보 팝업 관리를 위한 상태 변수
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogContent by remember { mutableStateOf("") }


    // 회원 탈퇴 확인 팝업
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("회원 탈퇴 확인", fontWeight = FontWeight.Bold) },
            text = { Text("정말로 계정을 삭제하시겠어요? 모든 정보는 영구적으로 삭제됩니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(userId)
                        showDeleteDialog = false
                        onAccountDeleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("탈퇴")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LogoutTextColor)
                ) {
                    Text("취소")
                }
            }
        )
    }

    // ⭐ [추가] 공지사항/FAQ 등의 정보 팝업
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(infoDialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(infoDialogContent) },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("확인")
                }
            }
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "마이페이지",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp))
                .height(100.dp)
                .clickable(onClick = onProfileClick) // ⭐ [수정] 프로필 클릭 이벤트 연결
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.user_icon),
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Column {
                        Text(
                            text = "${userName}님",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                        )
                        Text(
                            text = "프로필 변경하기",
                            fontSize = 14.sp,
                            color = PrimaryGreen,
                            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "프로필 변경",
                    tint = TextBlack.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp)
        ) {
            MypageMenuItem(label = "공지사항", onClick = {
                infoDialogTitle = "공지사항"
                infoDialogContent = "현재 DailyMate는 안정화 단계에 있습니다. 새로운 기능은 추후 업데이트 예정입니다."
                showInfoDialog = true
            })
            MypageMenuItem(label = "FAQ", onClick = {
                infoDialogTitle = "FAQ"
                infoDialogContent = "Q. 살 빼고 싶어요 A. 술을 그만 머거"
                showInfoDialog = true
            })
            MypageMenuItem(label = "이용약관 및 개인정보 처리방침", onClick = {
                infoDialogTitle = "이용약관 및 개인정보 처리방침"
                infoDialogContent = "이용약관이 머에요?? 지수님 배고파요 술 사조우우우우"
                showInfoDialog = true
            })
            MypageMenuItem(label = "앱 설정", onClick = {
                infoDialogTitle = "앱 설정"
                infoDialogContent = "현재 앱 설정 기능은 준비 중입니다. 기본 테마를 사용합니다."
                showInfoDialog = true
            })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "로그아웃",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = LogoutTextColor,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(onClick = onLogout)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "회원 탈퇴",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Red,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable {
                    showDeleteDialog = true
                }
        )
    }
}

@Composable
fun MypageMenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = TextBlack,
            fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo))
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "이동",
            tint = TextBlack.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
}