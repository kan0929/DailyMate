package com.example.dailymate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.rememberImagePainter
import com.example.dailymate.data.UserPreferences

val Color_Header_Text = Color(0xFF4CAF50)
val Color_Box_Background = Color(0xFFE8F5E9)
val Color_Label_Text = Color(0xFF757575)

class ProfileActivity : ComponentActivity() {

    private fun loadUserInfo(context: Context): Triple<Int, String, String> {
        val userPrefs = UserPreferences(context)
        val userId = userPrefs.getUserId()
        val fullName = userPrefs.getFullName()
        val userEmail = userPrefs.getUserEmail()
        return Triple(userId, fullName, userEmail)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (currentUserId, receivedFullName, userEmail) = loadUserInfo(applicationContext)

        setContent {
            val context = LocalContext.current

            var imageUri by remember { mutableStateOf<Uri?>(null) }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                imageUri = uri
            }

            Scaffold(
                bottomBar = {
                    BottomNavigationBar(currentIndex = 4) { index ->
                        val nextActivityClass = when (index) {
                            0 -> MainActivity::class.java
                            2 -> ManagementActivity::class.java
                            3 -> DailyActivity::class.java
                            else -> null
                        }

                        if (nextActivityClass != null) {
                            val intent = Intent(context, nextActivityClass).apply {
                                putExtra("UserId", currentUserId)
                                putExtra("fullName", receivedFullName)
                                putExtra("UserEmail", userEmail)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                            finish()
                        }
                    }
                }
            ) { paddingValues ->
                ProfileScreenContent(
                    paddingValues = paddingValues,
                    currentUserId = currentUserId,
                    receivedFullName = receivedFullName,
                    userEmail = userEmail,
                    imageUri = imageUri,
                    onImageClick = { imagePickerLauncher.launch("image/*") }
                )
            }
        }
    }
}

@Composable
fun ProfileScreenContent(
    paddingValues: PaddingValues,
    currentUserId: Int,
    receivedFullName: String,
    userEmail: String,
    imageUri: Uri?,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "마이페이지",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color_Header_Text,
            modifier = Modifier.padding(top = 40.dp, bottom = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color_Box_Background, shape = RoundedCornerShape(16.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onImageClick)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberImagePainter(data = imageUri),
                        contentDescription = "프로필 사진",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileFieldActual(
                    label = "이름",
                    value = receivedFullName,
                    backgroundColor = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileFieldActual(
                    label = "이메일",
                    value = userEmail,
                    backgroundColor = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))

                PasswordChangeButtonActual(
                    userId = currentUserId,
                    backgroundColor = Color.White
                )
            }
        }
    }
}

@Composable
fun ProfileFieldActual(label: String, value: String, backgroundColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color_Label_Text
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PasswordChangeButtonActual(userId: Int, backgroundColor: Color) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable {
                if (userId != -1) {
                    val intent = Intent(context, PasswordChangeActivity::class.java).apply {
                        putExtra("UserId", userId)
                    }
                    context.startActivity(intent)
                }
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "비밀번호 변경",
            fontSize = 15.sp,
            color = Color_Label_Text
        )
    }
}