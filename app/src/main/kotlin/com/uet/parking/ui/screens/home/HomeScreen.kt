package com.uet.parking.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.components.DebtCard
import com.uet.parking.ui.components.EventCard
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue

data class EventUiModel(
    val title: String,
    val location: String,
    val time: String = "",
    val featured: Boolean = false
)

@Composable
fun HomeScreen(
    onBookNow: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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

            item { HomeSectionHeader() }

            items(events) { event ->
                EventCard(event = event)
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HomeSectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "THÔNG BÁO MỚI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                letterSpacing = 1.sp
            )
            Text(
                text = "Sự kiện trường học",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        Text(
            text = "Xem tất cả",
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
