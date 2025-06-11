package com.example.suntrackir.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suntrackir.data.HijriCalendar
import com.example.suntrackir.data.SunCalculator
import com.example.suntrackir.data.MoonCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class SunMoonViewModel : ViewModel() {
    private val _uiState = MutableLiveData(SunMoonUiState())
    val uiState: LiveData<SunMoonUiState> = _uiState
    var latitude: Double = 30.2849 // کرمان
    var longitude: Double = 57.0834
    var elevation: Double = 1750.0
    var currentDate: LocalDate by mutableStateOf(LocalDate.now())

    init {
        // به‌روزرسانی زمان هر دقیقه
        viewModelScope.launch {
            while (true) {
                updateTime()
                delay(60000)
            }
        }
    }

    fun updateTime() {
        val now = LocalDateTime.now()
        val minutes = now.hour * 60 + now.minute
        // اطلاعات خورشید
        val sunInfo = SunCalculator.getSunInfoForDate(currentDate, latitude, longitude, elevation)
        val sunTimes = parseSunInfo(sunInfo)
        // اطلاعات ماه
        val moonInfo = MoonCalculator.getMoonInfoForDate(currentDate, latitude, longitude, elevation)
        val moonData = parseMoonInfo(moonInfo)

        _uiState.value = SunMoonUiState(
            persianDate = JalaliDate.fromGregorian(currentDate).toString(),
            hijriDate = HijriCalendar.getHijriDate(currentDate),
            gregorianDate = currentDate.toString(),
            dayOfWeek = currentDate.dayOfWeek.toString(),
            currentMinutes = minutes,
            sunriseMinutes = sunTimes.first?.toMinutes() ?: 360,
            sunsetMinutes = sunTimes.second?.toMinutes() ?: 1080,
            moonriseMinutes = moonData.moonrise?.toMinutes() ?: 0,
            moonsetMinutes = moonData.moonset?.toMinutes() ?: 1440,
            sunProgress = calculateProgress(minutes, sunTimes.first?.toMinutes() ?: 360, sunTimes.second?.toMinutes() ?: 1080),
            moonProgress = calculateProgress(minutes, moonData.moonrise?.toMinutes() ?: 0, moonData.moonset?.toMinutes() ?: 1440),
            moonPhase = moonData.phasePercent,
            moonPhaseName = moonData.phaseName,
            moonMansion = moonData.mansion,
            moonPhasePercent = "${moonData.phasePercent?.toInt()}%",
            twilightType = getCurrentTwilightType(minutes, sunTimes.twilightTimes),
            twilightTime = getCurrentTwilightTime(minutes, sunTimes.twilightTimes),
            daysPassed = HijriCalendar.getYearProgress(currentDate).first,
            weeksPassed = HijriCalendar.getYearProgress(currentDate).second,
            daysRemaining = HijriCalendar.getYearProgress(currentDate).third,
            totalDays = HijriCalendar.calculateYearDays(1446)
        )
    }

    fun changeDate(days: Int) {
        currentDate = currentDate.plusDays(days.toLong())
        updateTime()
    }

    fun showCalendarDialog() {
        // نمایش دیالوگ تقویم ماهانه
    }

    fun showSettingsDialog() {
        // نمایش دیالوگ تنظیمات
    }

    private fun parseSunInfo(info: String): Pair<LocalTime?, LocalTime?> {
        // پارس اطلاعات خورشید (طلوع و غروب)
        val sunrise = info.lines().find { it.contains("طلوع خورشید") }?.substringAfter(": ")?.trim()
        val sunset = info.lines().find { it.contains("غروب خورشید") }?.substringAfter(": ")?.trim()
        val civilTwilight = info.lines().find { it.contains("سپیده‌دم مدنی") }?.substringAfter(": ")?.trim()
        val nauticalTwilight = info.lines().find { it.contains("سپیده‌دم دریایی") }?.substringAfter(": ")?.trim()
        val astroTwilight = info.lines().find { it.contains("سپیده‌دم نجومی") }?.substringAfter(": ")?.trim()
        return Pair(
            sunrise.takeUnless { it == "نامشخص" }?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) },
            sunset.takeUnless { it == "نامشخص" }?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) }
        )
    }

    private fun parseMoonInfo(info: String): MoonData {
        // پارس اطلاعات ماه
        val moonrise = info.lines().find { it.contains("طلوع ماه") }?.substringAfter(": ")?.trim()
        val moonset = info.lines().find { it.contains("غروب ماه") }?.substringAfter(": ")?.trim()
        val phase = info.lines().find { it.contains("فاز ماه") }?.substringAfter(": ")?.substringBefore("%")?.trim()?.toDoubleOrNull()
        val phaseName = info.lines().find { it.contains("فاز ماه") }?.substringAfter("(")?.substringBefore(")")?.trim()
        val mansion = info.lines().find { it.contains("منزله ماه") }?.substringAfter(": ")?.trim()
        return MoonData(
            moonrise = moonrise.takeUnless { it == "نامشخص" }?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) },
            moonset = moonset.takeUnless { it == "نامشخص" }?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) },
            phasePercent = phase,
            phaseName = phaseName ?: "نامشخص",
            mansion = mansion ?: "نامشخص"
        )
    }

    private fun LocalTime.toMinutes(): Int = hour * 60 + minute

    private fun calculateProgress(current: Int, start: Int, end: Int): Float {
        return ((current - start).toFloat() / (end - start)).coerceIn(0f, 1f)
    }

    private fun getCurrentTwilightType(minutes: Int, twilightTimes: Map<String, Pair<LocalTime?, LocalTime?>>): String? {
        twilightTimes.forEach { (type, times) ->
            val start = times.first?.toMinutes() ?: return@forEach
            val end = times.second?.toMinutes() ?: return@forEach
            if (minutes in start..end) return type
        }
        return null
    }

    private fun getCurrentTwilightTime(minutes: Int, twilightTimes: Map<String, Pair<LocalTime?, LocalTime?>>): String? {
        twilightTimes.forEach { (type, times) ->
            val start = times.first?.toMinutes() ?: return@forEach
            val end = times.second?.toMinutes() ?: return@forEach
            if (minutes in start..end) {
                return "${minutes / 60}:${(minutes % 60).toString().padStart(2, '0')}"
            }
        }
        return null
    }
}

data class SunMoonUiState(
    val persianDate: String = "",
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val dayOfWeek: String = "",
    val currentMinutes: Int = 720,
    val sunriseMinutes: Int = 360,
    val sunsetMinutes: Int = 1080,
    val moonriseMinutes: Int = 0,
    val moonsetMinutes: Int = 1440,
    val sunProgress: Float = 0f,
    val moonProgress: Float = 0f,
    val moonPhase: Double? = null,
    val moonPhaseName: String = "",
    val moonMansion: String = "",
    val moonPhasePercent: String = "",
    val twilightType: String? = null,
    val twilightTime: String? = null,
    val daysPassed: Int = 0,
    val weeksPassed: Int = 0,
    val daysRemaining: Int = 0,
    val totalDays: Int = 354
)

data class MoonData(
    val moonrise: LocalTime?,
    val moonset: LocalTime?,
    val phasePercent: Double?,
    val phaseName: String,
    val mansion: String
)