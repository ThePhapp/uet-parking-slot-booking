package com.uet.parking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.screens.home.EventUiModel

@Composable
fun EventCard(event: EventUiModel) {
    if (event.featured) {
        FeaturedEventCard(event)
    } else {
        NormalEventCard(event)
    }
}

@Composable
private fun FeaturedEventCard(event: EventUiModel) {
    Card(
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5B7ED6),
                            Color(0xFF1F2F5C)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(text = event.location, bg = Color(0xFF003D9B), fg = Color.White)
                Chip(text = event.time, bg = Color(0x33FFFFFF), fg = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = event.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF003D9B)
                )
            ) {
                Text(
                    text = "Đặt vé ngay",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NormalEventCard(event: EventUiModel) {
    Card(
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        color = Color(0xFFD9E6FF),
                        shape = RoundedCornerShape(18.dp)
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = event.title,
                color = Color(0xFF191C1E),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 ${event.location}",
                color = Color(0xFF737685),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF003D9B)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Color(0xFFDAE2FF)
                )
            ) {
                Text(
                    text = "Đặt vé ngay",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    bg: Color,
    fg: Color
) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}