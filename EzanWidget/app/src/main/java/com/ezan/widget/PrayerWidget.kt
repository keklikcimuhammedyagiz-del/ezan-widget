package com.ezan.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.util.Calendar

class PrayerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        // Varsayılan: İstanbul
        private const val DEFAULT_LAT = 41.01
        private const val DEFAULT_LNG = 28.98
        private const val DEFAULT_NAME = "İstanbul"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("ezan", Context.MODE_PRIVATE)
            val lat = prefs.getFloat("lat", DEFAULT_LAT.toFloat()).toDouble()
            val lng = prefs.getFloat("lng", DEFAULT_LNG.toFloat()).toDouble()
            val city = prefs.getString("city", DEFAULT_NAME) ?: DEFAULT_NAME

            val times = PrayerCalc.calculate(lat, lng)
            val names = PrayerCalc.names()
            val next = PrayerCalc.nextIndex(times)

            val views = RemoteViews(context.packageName, R.layout.widget_prayer)
            views.setTextViewText(R.id.widget_location, city)
            views.setTextViewText(R.id.widget_next, "Sıradaki: ${names[next]}")
            views.setTextViewText(R.id.widget_next_time, PrayerCalc.format(times[next]))

            val ids = intArrayOf(R.id.t0, R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5)
            for (i in ids.indices) {
                views.setTextViewText(ids[i], "${names[i]}\n${PrayerCalc.format(times[i])}")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
