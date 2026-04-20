package com.uet.parking.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uet.parking.ui.components.DebtCard
import com.uet.parking.ui.components.EventCard
import com.uet.parking.ui.components.HomeBottomBar
import com.uet.parking.ui.components.HomeTopBar

data class EventUiModel(
    val title: String,
    val location: String,
    val time: String = "",
    val featured: Boolean = false
)

@Composable
fun HomeScreen(
    onBookNow: () -> Unit = {}
) {
    val events = listOf(
        EventUiModel(
            title = "Hội thảo Công nghệ Blockchain trong Giáo dục 2024",
            location = "Giảng đường A1",
            time = "14:00 - 20/10",
            featured = true
        ),
        EventUiModel(
            title = "Đêm nhạc Acoustic: Giai điệu mùa thu Sinh viên",
            location = "Sân hội trường C"
        ),
        EventUiModel(
            title = "Kỹ năng mềm: Tư duy thiết kế trong khởi nghiệp",
            location = "Phòng 402, Nhà B"
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB)),
        containerColor = Color(0xFFF7F9FB),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                HomeTopBar()
            }
        },
        bottomBar = {
            HomeBottomBar(
                modifier = Modifier.navigationBarsPadding(),
                onBookClick = onBookNow
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DebtCard(
                    debt = "50.000đ",
                    cardType = "Sinh Viên",
                    studentCode = "SV2024-089"
                )
            }

            item {
                HomeSectionHeader()
            }

            items(events) { event ->
                EventCard(event = event)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HomeSectionHeader() {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            androidx.compose.material3.Text(
                text = "THÔNG BÁO MỚI",
                color = Color(0xFF003D9B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.material3.Text(
                text = "Sự kiện trường học",
                color = Color(0xFF191C1E)
            )
        }

        androidx.compose.material3.Text(
            text = "Xem tất cả",
            color = Color(0xFF003D9B)
        )
    }
}