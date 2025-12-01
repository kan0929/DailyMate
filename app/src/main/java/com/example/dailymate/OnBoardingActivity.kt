package com.example.dailymate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailymate.ui.theme.DailyMateTheme


class OnBoardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyMateTheme {
                OnBoardingScreen()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, SignupActivity()::class.java))
            finish()
        }, 2000)
    }
}


@Composable
fun OnBoardingScreen() {
    Column(
        modifier = Modifier
            .width(402.dp)
            .height(874.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "앱 로고",
            contentScale = ContentScale.None
        )
        Text(
            text = "DailyMate",
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 36.sp,
                lineHeight = 150.sp,
                fontFamily = FontFamily(Font(R.font.spoqa_han_sans_neo)),
                fontWeight = FontWeight(700),
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnBoardingScreenPreview() {
    DailyMateTheme {
        OnBoardingScreen()
    }
}