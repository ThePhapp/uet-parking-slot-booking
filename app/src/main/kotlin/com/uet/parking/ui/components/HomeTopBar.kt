package com.uet.parking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F9FB))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.clip(CircleShape),
                color = Color(0xFFDAE2FF),
                shape = CircleShape
            ) {
                Text(
                    text = "👤",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 18.sp
                )
            }

            Text(
                text = "Campus Parking",
                modifier = Modifier.padding(start = 12.dp),
                color = Color(0xFF003D9B),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        IconButton(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEEF4FF)),
            onClick = { }
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "History",
                tint = Color(0xFF5B6470)
            )
        }
    }
}