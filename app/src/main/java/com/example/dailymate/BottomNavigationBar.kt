package com.example.dailymate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.foundation.layout.navigationBarsPadding // navigationBarsPadding을 사용하기 위한 import 유지
import com.example.dailymate.ui.theme.PrimaryGreen
import com.example.dailymate.ui.theme.enhangreen

@Composable
fun BottomNavigationBar(currentIndex: Int, onItemClick: (Int) -> Unit) {
    val iconSize = 32.dp

    val customColors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryGreen,
        unselectedIconColor = PrimaryGreen,
        indicatorColor = Color.Transparent
    )

    // ⭐ [핵심 수정] 좌우 여백(horizontal=24.dp)을 추가하고, 하단 여백과 고정 높이를 제거
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp) // 좌우 여백 추가 (툴바를 화면 중앙에 띄움)
            .clip(RoundedCornerShape(16.dp)) // 모든 모서리 둥글게 유지
            .navigationBarsPadding(), // 시스템 바 아래로 배경이 길게 늘어지는 현상 방지
        color = enhangreen
    ) {
        NavigationBar(
            // ⭐ [수정] 고정된 높이를 제거하여 콘텐츠(아이콘) 크기에 맞게 자동 조절
            containerColor = enhangreen
        ) {
            NavigationBarItem(
                selected = currentIndex == 0,
                onClick = { onItemClick(0) },
                icon = { Image(painterResource(id = R.drawable.home_icon), contentDescription = "홈", modifier = Modifier.size(iconSize)) },
                label = null,
                colors = customColors
            )
            NavigationBarItem(
                selected = currentIndex == 1,
                onClick = { onItemClick(1) },
                icon = { Image(painterResource(id = R.drawable.calinder_icon), contentDescription = "캘린더", modifier = Modifier.size(iconSize)) },
                label = null,
                colors = customColors
            )
            NavigationBarItem(
                selected = currentIndex == 2,
                onClick = { onItemClick(2) },
                icon = { Image(painterResource(id = R.drawable.add_icon), contentDescription = "관리", modifier = Modifier.size(iconSize)) },
                label = null,
                colors = customColors
            )
            NavigationBarItem(
                selected = currentIndex == 3,
                onClick = { onItemClick(3) },
                icon = { Image(painterResource(id = R.drawable.record_icon), contentDescription = "데일리", modifier = Modifier.size(iconSize)) },
                label = null,
                colors = customColors
            )
            NavigationBarItem(
                selected = currentIndex == 4,
                onClick = { onItemClick(4) },
                icon = { Image(painterResource(id = R.drawable.mypage_icon), contentDescription = "마이페이지", modifier = Modifier.size(iconSize)) },
                label = null,
                colors = customColors
            )
        }
    }
}