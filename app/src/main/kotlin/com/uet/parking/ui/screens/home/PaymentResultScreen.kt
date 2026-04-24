package com.uet.parking.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentResultScreen(
    isSuccess: Boolean,
    debtAmount: Double,
    currentBalance: Double,
    onBackHome: () -> Unit
) {
    val moneyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSuccess) "Thanh toán thành công" else "Thanh toán không thành công",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isSuccess) {
                "Bạn đã thanh toán khoản nợ ${moneyFormat.format(debtAmount)}."
            } else {
                "Số dư hiện tại ${moneyFormat.format(currentBalance)} không đủ để thanh toán ${moneyFormat.format(debtAmount)}."
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBackHome) {
            Text("Quay về trang chủ")
        }
    }
}