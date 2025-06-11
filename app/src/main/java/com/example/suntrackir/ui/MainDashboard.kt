package com.example.suntrackir.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.suntrackir.viewmodel.SunMoonViewModel
import com.example.suntrackir.data.HijriCalendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainDashboard(viewModel: SunMoonViewModel) {
    // مشاهده وضعیت ویومدل
    val uiState by viewModel.uiState.observeAsState(SunMoonUiState())
    // بارگذاری انیمیشن‌های Lottie
    val sunAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sun_animation))
    val moonAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.moon_animation))
    // انیمیشن Pulse برای گرگ‌ومیش
    val pulseAnimation = rememberInfiniteTransition()
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    // انیمیشن ستاره‌های چشمک‌زن
    val starAnimation = rememberInfiniteTransition()
    val starAlpha by starAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // بک‌گراند با گرادینت پویا و ستاره‌ها
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientForTime(uiState.currentMinutes, uiState.twilightType, starAlpha))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // نمایش تاریخ‌ها
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = uiState.persianDate,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.showCalendarDialog() }
                        .padding(8.dp)
                        .shadow(2.dp, shape = MaterialTheme.shapes.small)
                )
                Text(
                    text = uiState.hijriDate,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp).shadow(2.dp, shape = MaterialTheme.shapes.small)
                )
                Text(
                    text = uiState.gregorianDate,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp).shadow(2.dp, shape = MaterialTheme.shapes.small)
                )
            }
            Text(
                text = uiState.dayOfWeek,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )

            // قوس خورشید و ماه
            SunMoonArc(
                sunProgress = uiState.sunProgress,
                moonProgress = uiState.moonProgress,
                sunAnimation = sunAnimation,
                moonAnimation = moonAnimation,
                twilightType = uiState.twilightType,
                twilightTime = uiState.twilightTime,
                pulseScale = pulseScale
            )

            // گام‌شمار سال قمری
            Text(
                text = "روز ${uiState.daysPassed} از ${uiState.totalDays}، هفته ${uiState.weeksPassed}، ${uiState.daysRemaining} روز باقی‌مانده",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .shadow(2.dp, shape = MaterialTheme.shapes.small)
            )

            // نوارهای پیشرفت
            ProgressBar(
                progress = uiState.sunProgress,
                label = "خورشید: ${uiState.sunriseMinutes / 60}:${(uiState.sunriseMinutes % 60).toString().padStart(2, '0')}",
                drawableRes = R.drawable.progress_sun
            )
            ProgressBar(
                progress = uiState.moonProgress,
                label = "ماه: ${uiState.moonPhaseName} - ${uiState.moonMansion} (${uiState.moonPhasePercent})",
                drawableRes = R.drawable.progress_moon
            )

            // دکمه‌های ناوبری
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.changeDate(-1) }) { Text("روز قبل") }
                Button(onClick = { viewModel.showCalendarDialog() }) { Text("انتخاب تاریخ") }
                Button(onClick = { viewModel.changeDate(1) }) { Text("روز بعد") }
                Button(onClick = { viewModel.showSettingsDialog() }) { Text("تنظیمات") }
            }
        }
    }
}

@Composable
fun SunMoonArc(
    sunProgress: Float,
    moonProgress: Float,
    sunAnimation: LottieComposition?,
    moonAnimation: LottieComposition?,
    twilightType: String?,
    twilightTime: String?,
    pulseScale: Float
) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        val cx = size.width / 2
        val cy = size.height * 0.9f
        val radius = size.width * 0.35f
        val arcRect = RectF(cx - radius, cy - radius * 0.7f, cx + radius, cy + radius * 0.7f)

        // قوس اصلی (خاکستری)
        drawArc(
            color = Color.Gray,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4f),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )

        // قوس گرگ‌ومیش فعلی
        twilightType?.let {
            val twilightColor = when (it) {
                "Civil" -> Color(0xFF42A5F5) // آبی روشن
                "Nautical" -> Color(0xFF1565C0) // آبی تیره
                "Astronomical" -> Color.White // خط سفید برای گرگ‌ومیش نجومی
                else -> Color.Transparent
            }
            drawArc(
                color = twilightColor,
                startAngle = 180f,
                sweepAngle = 10f,
                useCenter = false,
                style = Stroke(width = 6f), // ضخامت 6dp برای گرگ‌ومیش نجومی
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )
            // افکت Glow برای گرگ‌ومیش نجومی
            if (it == "Astronomical") {
                drawArc(
                    color = Color.White.copy(alpha = 0.3f),
                    startAngle = 180f,
                    sweepAngle = 10f,
                    useCenter = false,
                    style = Stroke(width = 10f * pulseScale), // افکت Pulse
                    topLeft = arcRect.topLeft,
                    size = arcRect.size
                )
            }
            // نمایش ساعت گرگ‌ومیش
            twilightTime?.let { time ->
                drawText(
                    text = time,
                    x = cx,
                    y = cy - radius - 20f,
                    paint = Paint().apply {
                        color = Color.White
                        textSize = 40f
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(10f, 0f, 0f, Color.Black)
                    }
                )
            }
        }

        // انیمیشن خورشید
        LottieAnimation(
            composition = sunAnimation,
            progress = { sunProgress },
            modifier = Modifier
                .size(32.dp) // سایز بزرگ‌تر برای تأکید
                .offset(
                    x = (cx + radius * cos(Math.toRadians(180.0 + sunProgress * 180.0))).dp,
                    y = (cy - radius * 0.7f * sin(Math.toRadians(180.0 + sunProgress * 180.0))).dp
                )
        )

        // انیمیشن ماه
        LottieAnimation(
            composition = moonAnimation,
            progress = { moonProgress },
            modifier = Modifier
                .size(28.dp) // تعادل با خورشید
                .offset(
                    x = (cx + radius * cos(Math.toRadians(180.0 + moonProgress * 180.0))).dp,
                    y = (cy - radius * 0.7f * sin(Math.toRadians(180.0 + moonProgress * 180.0))).dp
                )
        )
    }
}

@Composable
fun ProgressBar(progress: Float, label: String, drawableRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
        Text(text = label, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
                .background(Color(drawableRes))
        )
    }
}

@Composable
fun gradientForTime(minutes: Int, twilightType: String?, starAlpha: Float): Brush {
    // گرادینت پویا با ستاره‌های چشمک‌زن
    return Brush.linearGradient(
        colors = when {
            twilightType == "Civil" -> listOf(Color(0xFF42A5F5), Color(0xFF2196F3)) // آبی روشن
            twilightType == "Nautical" -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1)) // آبی تیره
            twilightType == "Astronomical" -> listOf(
                Color(0xFF0D47A1),
                Color(0xFF1565C0).copy(alpha = starAlpha) // ستاره‌ها تو گرگ‌ومیش
            )
            minutes in 360..1080 -> listOf(Color(0xFFFF9800), Color(0xFF2196F3)) // روز
            else -> listOf(
                Color(0xFF0D47A1),
                Color(0xFF001020).copy(alpha = starAlpha) // ستاره‌های شب
            )
        }
    )
}