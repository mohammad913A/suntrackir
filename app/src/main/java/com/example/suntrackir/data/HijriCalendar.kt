package com.example.suntrackir.data

import com.example.suntrackir.data.SunCalculator
import com.example.suntrackir.data.MoonCalculator
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import kotlin.math.abs

object HijriCalendar {
    private const val RAMADAN_START_1403 = "1403/12/10"
    private val monthDays = mutableListOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)
    private val monthNames = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الثانية",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun getHijriDate(date: LocalDate): String {
        // تبدیل تاریخ شمسی به قمری با شروع از رمضان
        val startDate = LocalDate.of(1403, 12, 10) // 1 رمضان 1446
        val daysSinceStart = date.toEpochDay() - startDate.toEpochDay()
        var totalDays = daysSinceStart
        var year = 1446
        var monthIndex = 8 // رمضان
        var day = 1

        while (totalDays >= 0) {
            val yearDays = calculateYearDays(year)
            if (totalDays >= yearDays) {
                totalDays -= yearDays
                year++
                monthIndex = 0
                day = 1
            } else {
                break
            }
        }

        while (totalDays > 0) {
            val daysInMonth = getDaysInMonth(monthIndex, year, date.minusDays(totalDays))
            if (totalDays >= daysInMonth) {
                totalDays -= daysInMonth
                monthIndex = (monthIndex + 1) % 12
                day = 1
            } else {
                day += totalDays.toInt()
                totalDays = 0
            }
        }

        return "$day ${monthNames[monthIndex]} $year"
    }

    fun calculateYearDays(year: Int): Int {
        var totalDays = monthDays.sum()
        if (totalDays == 354) {
            // اعمال قوانین کبیسه
            when {
                monthDays[11] == 29 -> monthDays[11] = 30 // ذی‌الحجه
                monthDays[1] == 29 -> monthDays[1] = 30 // صفر
                monthDays[0] == 29 -> monthDays[0] = 30 // محرم
            }
            totalDays = monthDays.sum()
        }
        return totalDays
    }

    fun getDaysInMonth(monthIndex: Int, year: Int, date: LocalDate): Int {
        // تنظیم تعداد روزهای ماه‌ها
        return when (monthIndex) {
            8 -> 30 // رمضان
            10 -> 30 // ذی‌القعده
            7 -> 29 // شعبان
            else -> {
                // بررسی نزدیکی طلوع خورشید و ماه + منزل اول
                if (checkSunMoonProximity(date) && isFirstMansion(date)) 30 else 29
            }
        }
    }

    fun isLeapYear(year: Int): Boolean {
        return calculateYearDays(year) == 355
    }

    fun getYearProgress(date: LocalDate): Triple<Int, Int, Int> {
        val startDate = LocalDate.of(1403, 12, 10)
        val daysPassed = (date.toEpochDay() - startDate.toEpochDay()).toInt()
        val totalDays = calculateYearDays(1446)
        val weeksPassed = daysPassed / 7
        val daysRemaining = totalDays - daysPassed
        return Triple(daysPassed, weeksPassed, daysRemaining)
    }

    private fun checkSunMoonProximity(date: LocalDate): Boolean {
        // بررسی نزدیکی طلوع خورشید و ماه (اختلاف کمتر از 10 دقیقه)
        val sunInfo = SunCalculator.getSunInfoForDate(date)
        val moonInfo = MoonCalculator.getMoonInfoForDate(date)
        val sunrise = sunInfo.lines().find { it.contains("طلوع خورشید") }?.substringAfter(": ")?.trim()
        val moonrise = moonInfo.lines().find { it.contains("طلوع ماه") }?.substringAfter(": ")?.trim()
        if (sunrise == "نامشخص" || moonrise == "نامشخص") return false
        val sunriseTime = LocalTime.parse(sunrise, DateTimeFormatter.ofPattern("HH:mm"))
        val moonriseTime = LocalTime.parse(moonrise, DateTimeFormatter.ofPattern("HH:mm"))
        val diffMinutes = abs(sunriseTime.toMinutes() - moonriseTime.toMinutes())
        return diffMinutes <= 10
    }

    private fun isFirstMansion(date: LocalDate): Boolean {
        // بررسی ورود ماه به منزل اول
        val moonInfo = MoonCalculator.getMoonInfoForDate(date)
        val mansion = moonInfo.lines().find { it.contains("منزله ماه") }?.substringAfter("(")?.substringBefore(" از")?.trim()
        return mansion == "1"
    }

    private fun LocalTime.toMinutes(): Int = hour * 60 + minute
}