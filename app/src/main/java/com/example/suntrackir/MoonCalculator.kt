package com.example.suntrackir

import com.example.suntrackir.astronomy.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

object MoonCalculator {
    private val defaultLatitude = 30.2849
    private val defaultLongitude = 57.0834
    private val defaultElevation = 1750.0
    private val zone: ZoneId = ZoneId.of("Asia/Tehran")

    // تبدیل Time? به LocalTime
    private fun timeToLocalTime(t: Time?): LocalTime? {
        if (t == null) return null
        val dt = t.toDateTime()
        val ldt = LocalDateTime.of(dt.year, dt.month, dt.day, dt.hour, dt.minute, dt.second.toInt())
        return ZonedDateTime.of(ldt, ZoneOffset.UTC).withZoneSameInstant(zone).toLocalTime()
    }

    private fun formatTime(localTime: LocalTime?): String =
        localTime?.let { DateTimeFormatter.ofPattern("HH:mm", Locale("fa")).format(it) } ?: "نامشخص"

    // طلوع ماه با شروع 3 صبح
    private fun getMoonrise(date: LocalDate, observer: Observer): Time? {
        // ساعت شروع را از 0 نصف شب بگذار
        val moonriseStart = Time(date.year, date.monthValue, date.dayOfMonth, 0, 0, 0.0)
        // افق را -0.3 قرار بده برای دقت بهتر
        return searchAltitude(Body.Moon, observer, Direction.Rise, moonriseStart, 1.0, -0.9)
    }
    // غروب ماه با شروع 12 ظهر
    private fun getMoonset(date: LocalDate, observer: Observer): Time? {
        val moonsetStart = Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0)
        return searchAltitude(Body.Moon, observer, Direction.Set, moonsetStart, 1.0, -0.8)
    }

    // سن ماه (زمان از آخرین ماه نو)
    private fun getMoonAge(now: Time): Double? {
        val prevNewMoon = searchMoonPhase(0.0, now, -30.0)
        return prevNewMoon?.let { (now.ut - it.ut) }
    }

    // فاز ماه به صورت نام فارسی
    private fun phaseName(phaseAngle: Double): String =
        when {
            phaseAngle < 10 || phaseAngle > 350 -> "ماه نو"
            phaseAngle < 80 -> "هلال افزاینده"
            phaseAngle < 100 -> "تربیع اول"
            phaseAngle < 170 -> "کوژ افزاینده"
            phaseAngle < 190 -> "ماه کامل"
            phaseAngle < 260 -> "کوژ کاهنده"
            phaseAngle < 280 -> "تربیع دوم"
            phaseAngle < 350 -> "هلال کاهنده"
            else -> "نامشخص"
        }

    // تبدیل ra/dec به طول دایره‌البروجی (ecliptic longitude) بر حسب درجه
    private fun raDecToEclipticLongitude(ra: Double, dec: Double, time: Time): Double {
        // زاویه مایل بودن محور زمین (Obliquity of the ecliptic) به رادیان
        val eps = Math.toRadians(23.439291)
        val raRad = ra
        val decRad = dec
        val sinBeta = sin(decRad) * cos(eps) - cos(decRad) * sin(eps) * sin(raRad)
        val y = sin(raRad) * cos(eps) + tan(decRad) * sin(eps)
        val x = cos(raRad)
        val lambda = atan2(y, x)
        // تبدیل به درجه و برگرداندن به دامنه 0...360
        var deg = Math.toDegrees(lambda)
        if (deg < 0) deg += 360.0
        return deg
    }

    fun getMoonInfoForDate(
        date: LocalDate = LocalDate.now(zone),
        latitude: Double = defaultLatitude,
        longitude: Double = defaultLongitude,
        elevation: Double = defaultElevation
    ): String {
        val observer = Observer(latitude, longitude, elevation)
        val timeNoon = Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0)

        // طلوع و غروب ماه
        val moonriseTimeObj = getMoonrise(date, observer)
        val moonsetTimeObj = getMoonset(date, observer)
        val moonrise = timeToLocalTime(moonriseTimeObj)
        val moonset = timeToLocalTime(moonsetTimeObj)

        // زاویه فاز ماه (۰=ماه نو، ۱۸۰=ماه کامل)
        val phaseAngle = moonPhase(timeNoon)
        val phasePercent = (1 - cos(Math.toRadians(phaseAngle))) / 2 * 100 // درصد روشنایی تقریبی

        // سن ماه
        val ageDays = getMoonAge(timeNoon)?.div(1.0)

        // فاز ماه به زبان فارسی
        val phaseNameFa = phaseName(phaseAngle)

        // منزله ماه (با تبدیل ra/dec به دایره البروجی)
        val lunarMansionNames = arrayOf(
            "الشرطین", "البطین", "الثریا", "الدبران", "الهقعه", "الهنعه", "الذراع", "النثره",
            "الطرف", "الجبهه", "الزبره", "الصرفه", "العواء", "السواء", "الغفر", "الزبانا",
            "الاكلیل", "القلب", "الشوله", "النعايم", "البلده", "سعد الذابح", "سعد بلع", "سعد السعود",
            "سعد الاخبیة", "المقدم", "المؤخر", "الرشا"
        )
        val eq = equator(Body.Moon, timeNoon, observer, EquatorEpoch.OfDate, Aberration.Corrected)
        val ra = eq?.ra
        val dec = eq?.dec
        val lambda = if (ra != null && dec != null) raDecToEclipticLongitude(ra, dec, timeNoon) else null
        val lunarMansion = lambda?.let { ((it / (360.0 / 28)).toInt() + 1) }
        val lunarMansionName = if (lunarMansion != null)
            lunarMansionNames.getOrNull((lunarMansion-1).coerceIn(0,27))
        else null
        return buildString {
            append("اطلاعات ماه برای مختصات ($latitude, $longitude) در تاریخ $date:\n\n")
            append("طلوع ماه: ${formatTime(moonrise)}\n")
            append("غروب ماه: ${formatTime(moonset)}\n")
            append("سن ماه: ${if (ageDays != null) "%.1f روز".format(ageDays) else "نامشخص"}\n")
            append("فاز ماه: %.0f%% (%s)\n".format(phasePercent, phaseNameFa))
           // append("منزله ماه: ${if (lunarMansion != null) "$lunarMansion از ۲۸" else "نامشخص"}\n")
            append("منزله ماه: ${
                if (lunarMansion != null && lunarMansionName != null) "$lunarMansionName ($lunarMansion از ۲۸)" else "نامشخص"
            }\n")
        }
    }
}