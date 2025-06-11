package com.example.suntrackir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suntrackir.viewmodel.SunMoonUiState
import com.example.suntrackir.viewmodel.SunMoonViewModel

@Composable
fun SunTrackirDashboard(
    viewModel: SunMoonViewModel,
    onShowDatePicker: () -> Unit,
    onShowSettings: () -> Unit,
    onRequestGps: (callback: (Double, Double) -> Unit) -> Unit
) {
    val uiState by viewModel.uiState.observeAsState(SunMoonUiState())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day of the Week
        Text(
            text = uiState.dayOfWeek,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // Dates Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x20FFFFFF))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = uiState.persianDate, color = Color.White, fontSize = 18.sp)
            Text(text = uiState.hijriDate, color = Color.White, fontSize = 18.sp)
            Text(text = uiState.gregorianDate, color = Color.White, fontSize = 18.sp)
        }

        // Sun and Moon Animation (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "انیمیشن خورشید و ماه (اضافه کردن فایل‌های Lottie)",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Time Graph (Simple Canvas Implementation)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // خط سفید برای قوس گرگ‌ومیش
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 4.dp.toPx()
            )
            // می‌تونید اینجا منطق گراف زمان رو با sunriseMinutes و sunsetMinutes اضافه کنید
        }

        // Sun Progress and Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = uiState.sunStatus, color = Color.White, fontSize = 18.sp)
            LinearProgressIndicator(
                progress = { uiState.sunProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.Yellow,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }

        // Moon Progress and Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${uiState.moonPhaseName} - ${uiState.moonMansion}",
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = "روشنایی: ${uiState.moonPhasePercent}",
                color = Color.White,
                fontSize = 16.sp
            )
            LinearProgressIndicator(
                progress = { uiState.moonProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.Cyan,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.changeDate(-1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("روز قبل", color = Color.White)
            }
            Button(
                onClick = { onShowDatePicker() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("انتخاب تاریخ", color = Color.White)
            }
            Button(
                onClick = { viewModel.changeDate(1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("روز بعد", color = Color.White)
            }
            Button(
                onClick = { onShowSettings() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("تنظیمات", color = Color.White)
            }
        }
    }
}

@Preview
@Composable
fun SunTrackirDashboardPreview() {
    SunTrackirDashboard(
        viewModel = SunMoonViewModel(),
        onShowDatePicker = {},
        onShowSettings = {},
        onRequestGps = {}
    )
}