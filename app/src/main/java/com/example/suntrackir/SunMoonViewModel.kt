package com.example.suntrackir

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.suntrackir.jalaliDate.JalaliDate
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

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
    val sunProgress: Float = 0.5f,
    val moonProgress: Float = 0.5f,
    val moonPhase: Float = 0.5f,
    val moonPhasePercent: String = "-",
    val moonPhaseName: String = "-",
    val moonMansion: String = "-",
    val sunStatus: String = "خورشید در آسمان",
    val civilTwilightRange: Pair<Int, Int>? = null,
    val nauticalTwilightRange: Pair<Int, Int>? = null,
    val astronomicalTwilightRange: Pair<Int, Int>? = null,
    val goldenHourMorningRange: Pair<Int, Int>? = null,
    val goldenHourEveningRange: Pair<Int, Int>? = null,
    val blueHourMorningRange: Pair<Int, Int>? = null,
    val blueHourEveningRange: Pair<Int, Int>? = null
)

class SunMoonViewModel : ViewModel() {
    var latitude: Double = 30.2849
    var longitude: Double = 57.0834
    var currentDate: LocalDate = LocalDate.now(ZoneId.of("Asia/Tehran"))

    private val _uiState = MutableLiveData<SunMoonUiState>()
    val uiState: LiveData<SunMoonUiState> = _uiState

    init {
        updateUiState()
    }

    fun updateTime() {
        updateUiState()
    }

    fun changeDate(days: Int) {
        currentDate = currentDate.plusDays(days.toLong())
        updateUiState()
    }

    fun setDate(date: LocalDate) {
        currentDate = date
        updateUiState()
    }

    fun updateLocation(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
        updateUiState()
    }

    private fun updateUiState() {
        val moonString = MoonCalculator.getMoonInfoForDate(
            latitude = latitude, longitude = longitude, date = currentDate
        )
        val sunString = SunCalculator.getSunInfoForDate(
            latitude = latitude, longitude = longitude, date = currentDate
        )

        // Parse sun info
        val sunLines = sunString.lines()
        val sunrise = sunLines.find { it.startsWith("طلوع خورشید:") }?.substringAfter("طلوع خورشید:")?.trim() ?: "-"
        val sunset = sunLines.find { it.startsWith("غروب خورشید:") }?.substringAfter("غروب خورشید:")?.trim() ?: "-"
        val civilTwilight = sunLines.find { it.startsWith("سپیده‌دم مدنی:") }?.substringAfter("سپیده‌دم مدنی:")?.trim() ?: "-"
        val nauticalTwilight = sunLines.find { it.startsWith("سپیده‌دم دریایی:") }?.substringAfter("سپیده‌دم دریایی:")?.trim() ?: "-"
        val astronomicalTwilight = sunLines.find { it.startsWith("سپیده‌دم نجومی:") }?.substringAfter("سپیده‌دم نجومی:")?.trim() ?: "-"
        val goldenMorning = sunLines.find { it.startsWith("ساعت طلایی صبح:") }?.substringAfter("ساعت طلایی صبح:")?.trim() ?: "-"
        val goldenEvening = sunLines.find { it.startsWith("ساعت طلایی عصر:") }?.substringAfter("ساعت طلایی عصر:")?.trim() ?: "-"
        val blueMorning = sunLines.find { it.startsWith("ساعت آبی صبح:") }?.substringAfter("ساعت آبی صبح:")?.trim() ?: "-"
        val blueEvening = sunLines.find { it.startsWith("ساعت آبی عصر:") }?.substringAfter("ساعت آبی عصر:")?.trim() ?: "-"

        // Parse moon info
        val moonLines = moonString.lines()
        val moonrise = moonLines.find { it.startsWith("طلوع ماه:") }?.substringAfter("طلوع ماه:")?.trim() ?: "-"
        val moonset = moonLines.find { it.startsWith("غروب ماه:") }?.substringAfter("غروب ماه:")?.trim() ?: "-"
        val phaseLine = moonLines.find { it.startsWith("فاز ماه:") }?.substringAfter("فاز ماه:")?.trim() ?: "-"
        val phasePercent = phaseLine.split('(').firstOrNull()?.trim() ?: "-"
        val phaseName = phaseLine.substringAfter('(').substringBefore(')').trim()
        val mansion = moonLines.find { it.startsWith("منزله ماه:") }?.substringAfter("منزله ماه:")?.trim() ?: "-"

        // Calculate progress
        val nowMins = LocalTime.now(ZoneId.of("Asia/Tehran")).let { it.hour * 60 + it.minute }
        val sunriseMins = timeToMinutes(sunrise)
        val sunsetMins = timeToMinutes(sunset)
        val moonriseMins = timeToMinutes(moonrise)
        val moonsetMins = timeToMinutes(moonset)

        val sunProgress = if (sunriseMins < sunsetMins) {
            ((nowMins - sunriseMins).toFloat() / (sunsetMins - sunriseMins).toFloat()).coerceIn(0f, 1f)
        } else 0.5f
        val moonProgress = if (moonriseMins < moonsetMins) {
            ((nowMins - moonriseMins).toFloat() / (moonsetMins - moonriseMins).toFloat()).coerceIn(0f, 1f)
        } else 0.5f

        val moonPhase = try {
            phasePercent.replace("%", "").trim().toFloat() / 100f
        } catch (e: Exception) {
            0.5f
        }

        // Calculate sun status
        val sunStatus = when {
            nowMins < sunriseMins -> "پیش از طلوع"
            nowMins in sunriseMins..sunriseMins + 30 -> "طلوع خورشید"
            nowMins in (sunriseMins + sunsetMins) / 2 - 30..(sunriseMins + sunsetMins) / 2 + 30 -> "اوج خورشید"
            nowMins in sunsetMins - 30..sunsetMins -> "غروب خورشید"
            else -> "خورشید در آسمان"
        }

        // Update UI state
        _uiState.value = SunMoonUiState(
            persianDate = JalaliDate(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth).let {
                "${it.year}/${it.month}/${it.day}"
            },
            hijriDate = getHijriDate(currentDate),
            gregorianDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
            dayOfWeek = currentDate.dayOfWeek.toString(),
            currentMinutes = nowMins,
            sunriseMinutes = sunriseMins,
            sunsetMinutes = sunsetMins,
            moonriseMinutes = moonriseMins,
            moonsetMinutes = moonsetMins,
            sunProgress = sunProgress,
            moonProgress = moonProgress,
            moonPhase = moonPhase,
            moonPhasePercent = phasePercent,
            moonPhaseName = phaseName,
            moonMansion = mansion,
            sunStatus = sunStatus,
            civilTwilightRange = parseTimeRange(civilTwilight),
            nauticalTwilightRange = parseTimeRange(nauticalTwilight),
            astronomicalTwilightRange = parseTimeRange(astronomicalTwilight),
            goldenHourMorningRange = parseTimeRange(goldenMorning),
            goldenHourEveningRange = parseTimeRange(goldenEvening),
            blueHourMorningRange = parseTimeRange(blueMorning),
            blueHourEveningRange = parseTimeRange(blueEvening)
        )
    }

    private fun parseTimeRange(range: String): Pair<Int, Int>? {
        return try {
            val times = range.split("تا").map { it.trim() }
            if (times.size != 2) return null
            val start = timeToMinutes(times[0])
            val end = timeToMinutes(times[1])
            Pair(start, end)
        } catch (e: Exception) {
            null
        }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return if (parts.size >= 2) (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0) else 0
    }

    private fun getHijriDate(date: LocalDate): String {
        // Placeholder; replace with accurate conversion
        return "10 رمضان 1446"
    }
}