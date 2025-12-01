package com.example.dailymate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(currentIndex: Int, onItemClick: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentIndex == 0,
            onClick = { onItemClick(0) },
            icon = { Image(painterResource(id = R.drawable.home_icon), contentDescription = "홈", modifier = Modifier.size(24.dp)) },
            label = null
        )
        NavigationBarItem(
            selected = currentIndex == 1,
            onClick = { onItemClick(1) }, // 캘린더
            icon = { Image(painterResource(id = R.drawable.calinder_icon), contentDescription = "캘린더", modifier = Modifier.size(24.dp)) },
            label = null
        )
        NavigationBarItem(
            selected = currentIndex == 2,
            onClick = { onItemClick(2) }, // 관리
            icon = { Image(painterResource(id = R.drawable.add_icon), contentDescription = "관리", modifier = Modifier.size(24.dp)) },
            label = null
        )
        NavigationBarItem(
            selected = currentIndex == 3,
            onClick = { onItemClick(3) }, // 데일리
            icon = { Image(painterResource(id = R.drawable.record_icon), contentDescription = "데일리", modifier = Modifier.size(24.dp)) },
            label = null
        )
        NavigationBarItem(
            selected = currentIndex == 4,
            onClick = { onItemClick(4) }, // 마이페이지
            icon = { Image(painterResource(id = R.drawable.mypage_icon), contentDescription = "마이페이지", modifier = Modifier.size(24.dp)) },
            label = null
        )
    }
}