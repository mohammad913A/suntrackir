package com.example.suntrackir

import com.example.suntrackir.astronomy.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

object SunCalculator {
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

    // جستجوی طلوع با شروع ۳ صبح
    private fun getSunrise(date: LocalDate, observer: Observer): Time? {
        val sunriseTime = Time(date.year, date.monthValue, date.dayOfMonth, 3, 0, 0.0)
        return searchAltitude(Body.Sun, observer, Direction.Rise, sunriseTime, 1.0, -0.833)
    }
    // جستجوی غروب با شروع ۱۲ ظهر
    private fun getSunset(date: LocalDate, observer: Observer): Time? {
        val sunsetTime = Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0)
        return searchAltitude(Body.Sun, observer, Direction.Set, sunsetTime, 1.0, -0.833)
    }

    fun getSunInfoForDate(
        date: LocalDate = LocalDate.now(zone),
        latitude: Double = defaultLatitude,
        longitude: Double = defaultLongitude,
        elevation: Double = defaultElevation
    ): String {
        val observer = Observer(latitude, longitude, elevation)

        // طلوع و غروب خورشید (با ساعت شروع مناسب)
        val sunriseTimeObj = getSunrise(date, observer)
        val sunsetTimeObj = getSunset(date, observer)
        val sunrise = timeToLocalTime(sunriseTimeObj)
        val sunset = timeToLocalTime(sunsetTimeObj)

        // ظهر خورشیدی و ارتفاع
        val noonTime = Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0)
        val solarNoonResult = searchHourAngle(Body.Sun, observer, 0.0, noonTime)
        val solarNoonTime = timeToLocalTime(solarNoonResult?.time)
        val eq = solarNoonResult?.time?.let { equator(Body.Sun, it, observer, EquatorEpoch.OfDate, Aberration.Corrected) }
        val declDeg = eq?.dec?.let { Math.toDegrees(it) } ?: Double.NaN
        val solarNoonAltitude = if (!declDeg.isNaN() && declDeg in -90.0..90.0)
            90.0 - Math.abs(latitude - declDeg)
        else Double.NaN
        // مدت زمان روز فقط اگر sunrise و sunset هر دو غیر null باشند
        val duration = if (sunrise != null && sunset != null && !sunrise.isAfter(sunset))
            Duration.between(sunrise, sunset)
        else null

        // سپیده‌دم‌ها
        fun twilightTimes(angle: Double): Pair<LocalTime?, LocalTime?> {
            val dawnTime = Time(date.year, date.monthValue, date.dayOfMonth, 3, 0, 0.0)
            val duskTime = Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0)
            val dawn = searchAltitude(Body.Sun, observer, Direction.Rise, dawnTime, 1.0, angle)
            val dusk = searchAltitude(Body.Sun, observer, Direction.Set, duskTime, 1.0, angle)
            return Pair(timeToLocalTime(dawn), timeToLocalTime(dusk))
        }
        val (civilDawn, civilDusk) = twilightTimes(-6.0)
        val (nauticalDawn, nauticalDusk) = twilightTimes(-12.0)
        val (astroDawn, astroDusk) = twilightTimes(-18.0)

        // ساعت طلایی (از -6 تا +6 درجه)
        val goldenMorningStart = timeToLocalTime(searchAltitude(Body.Sun, observer, Direction.Rise, Time(date.year, date.monthValue, date.dayOfMonth, 3, 0, 0.0), 1.0, -6.0))
        val goldenMorningEnd   = timeToLocalTime(searchAltitude(Body.Sun, observer, Direction.Rise, Time(date.year, date.monthValue, date.dayOfMonth, 3, 0, 0.0), 1.0, 6.0))
        val goldenEveningStart = timeToLocalTime(searchAltitude(Body.Sun, observer, Direction.Set, Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0), 1.0, 6.0))
        val goldenEveningEnd   = timeToLocalTime(searchAltitude(Body.Sun, observer, Direction.Set, Time(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0.0), 1.0, -6.0))

        // ساعت آبی (بین سپیده‌دم مدنی و طلوع/غروب)
        val blueMorningStart = civilDawn
        val blueMorningEnd = sunrise
        val blueEveningStart = sunset
        val blueEveningEnd = civilDusk

        // مرتب سازی بازه‌ها (اگر ساعت اول بعد از ساعت دوم بود، جابجا شود)
        fun orderTimes(a: LocalTime?, b: LocalTime?): Pair<LocalTime?, LocalTime?> =
            if (a != null && b != null && a.isAfter(b)) Pair(b, a) else Pair(a, b)

        val (goldenMorningS, goldenMorningE) = orderTimes(goldenMorningStart, goldenMorningEnd)
        val (goldenEveningS, goldenEveningE) = orderTimes(goldenEveningStart, goldenEveningEnd)
        val (blueMorningS, blueMorningE) = orderTimes(blueMorningStart, blueMorningEnd)
        val (blueEveningS, blueEveningE) = orderTimes(blueEveningStart, blueEveningEnd)

        return buildString {
            append("اطلاعات خورشید برای مختصات ($latitude, $longitude) در تاریخ $date:\n\n")
            append("طلوع خورشید: ${formatTime(sunrise)}\n")
            append("غروب خورشید: ${formatTime(sunset)}\n")
            append("مدت زمان روز: ${
                if (duration != null) "${duration.toHours()} ساعت و ${(duration.toMinutes() % 60)} دقیقه"
                else "نامشخص"
            }\n")
            append("اوج خورشید (ظهر خورشیدی): ${formatTime(solarNoonTime)}\n")
            append("ارتفاع خورشید در اوج: ${
                if (!solarNoonAltitude.isNaN()) "%.2f".format(solarNoonAltitude) else "نامشخص"
            } درجه\n\n")
            append("سپیده‌دم مدنی: ${formatTime(civilDawn)} تا ${formatTime(civilDusk)}\n")
            append("سپیده‌دم دریایی: ${formatTime(nauticalDawn)} تا ${formatTime(nauticalDusk)}\n")
            append("سپیده‌دم نجومی: ${formatTime(astroDawn)} تا ${formatTime(astroDusk)}\n\n")
            append("ساعت طلایی صبح: ${formatTime(goldenMorningS)} تا ${formatTime(goldenMorningE)}\n")
            append("ساعت طلایی عصر: ${formatTime(goldenEveningS)} تا ${formatTime(goldenEveningE)}\n")
            append("ساعت آبی صبح: ${formatTime(blueMorningS)} تا ${formatTime(blueMorningE)}\n")
            append("ساعت آبی عصر: ${formatTime(blueEveningS)} تا ${formatTime(blueEveningE)}\n")
        }
    }
}