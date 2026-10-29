package com.ezan.widget

import java.util.Calendar
import kotlin.math.*

object PrayerCalc {
    private val NAMES = arrayOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")

    fun names() = NAMES

    /** Basit astronomik hesap (Diyanet usulüne yakın). lat/lng derece. */
    fun calculate(lat: Double, lng: Double, cal: Calendar = Calendar.getInstance()): List<Calendar> {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val tz = -cal.timeZone.rawOffset / 3600000.0 - cal.timeZone.dstSavings / 3600000.0

        val jd = toJulian(y, m + 1, d)
        val t = (jd - 2451545.0) / 36525.0
        val L0 = (280.46646 + 36000.76983 * t) % 360
        val M = Math.toRadians((357.52911 + 35999.05029 * t) % 360)
        val C = (1.914602 - 0.004817 * t) * sin(M) + 0.019993 * sin(2 * M)
        val sunLong = Math.toRadians(L0 + C)
        val eps = Math.toRadians(23.439 - 0.0000004 * (jd - 2451545.0))
        val sinDec = sin(eps) * sin(sunLong)
        val dec = asin(sinDec)
        val ra = atan2(cos(eps) * sin(sunLong), cos(sunLong))
        val eqt = (L0 - Math.toDegrees(ra) / 15.0).let {
            var e = it
            if (e > 12) e -= 24
            if (e < -12) e += 24
            e
        }

        val latR = Math.toRadians(lat)
        fun hourAngle(angleDeg: Double): Double {
            val a = Math.toRadians(angleDeg)
            val cosH = (sin(a) - sin(latR) * sin(dec)) / (cos(latR) * cos(dec))
            return Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
        }

        val noon = 12.0 + tz - lng / 15.0 - eqt
        // İmsak ~18°, Güneş 0.833°, Öğle, İkindi (gölge=1), Akşam 0.833°, Yatsı 17°
        val times = doubleArrayOf(
            noon - hourAngle(-18.0),          // İmsak
            noon - hourAngle(-0.833),         // Güneş
            noon + 0.05,                      // Öğle (küçük düzeltme)
            noon + hourAngle(atan(1 / (1 + tan(abs(latR - dec)))) * 180 / PI) / 15.0 * 0 + // placeholder
                    (noon + hourAngleForAsr(latR, dec)), // İkindi
            noon + hourAngle(-0.833),         // Akşam
            noon + hourAngle(-17.0)           // Yatsı
        )
        // İkindi'yi düzgün hesapla
        times[3] = noon + hourAngleForAsr(latR, dec)

        return times.map { hours ->
            val c = Calendar.getInstance()
            c.set(y, m, d, 0, 0, 0)
            c.set(Calendar.MILLISECOND, 0)
            val totalMin = (hours * 60).roundToInt()
            c.add(Calendar.MINUTE, totalMin)
            c
        }
    }

    private fun hourAngleForAsr(latR: Double, dec: Double): Double {
        val shadow = 1.0 // Şafi
        val angle = -atan(1.0 / (shadow + tan(abs(latR - dec))))
        val cosH = (sin(angle) - sin(latR) * sin(dec)) / (cos(latR) * cos(dec))
        return Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
    }

    private fun toJulian(y: Int, m: Int, d: Int): Double {
        var yy = y
        var mm = m
        if (mm <= 2) { yy--; mm += 12 }
        val a = yy / 100
        val b = 2 - a + a / 4
        return (365.25 * (yy + 4716)).toInt() + (30.6001 * (mm + 1)).toInt() + d + b - 1524.5
    }

    fun format(c: Calendar): String {
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        return "%02d:%02d".format(h, m)
    }

    fun nextIndex(times: List<Calendar>): Int {
        val now = Calendar.getInstance()
        for (i in times.indices) {
            if (times[i].after(now)) return i
        }
        return 0
    }
}
