package com.example.smarthydro

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import com.example.smarthydro.repositories.SensorRepository
import com.example.smarthydro.ui.theme.screen.home.GET_SENSOR_DATA_DELAY_MS
import com.example.smarthydro.viewmodels.SensorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FarmingWidgetProvider : AppWidgetProvider() {
    private val repository = SensorRepository()
    private val viewModel = SensorViewModel()
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            // Create RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val darkTheme = isSystemInDarkTheme(context)
            //val textColor = if (darkTheme) Color.WHITE else Color.CYAN
            val textColor =  Color.BLACK
            // Launch coroutine to fetch data in a background thread
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.fetchSensorPeriodically(GET_SENSOR_DATA_DELAY_MS)

                try {
                    // Call the suspend function in the coroutine
                    val data = repository.getSensorData()
                    //views.setInt(R.id.temperatureReading, "setBackgroundColor", primaryColor)

                    views.setTextColor(R.id.temperatureReading, textColor)
                    views.setTextColor(R.id.phReading, textColor)
                    views.setTextColor(R.id.ecReading, textColor)
                    views.setTextColor(R.id.pumpReading, textColor)
                    views.setTextColor(R.id.lightReading, textColor)
                    views.setTextColor(R.id.humidityReading, textColor)
                    // Update LiveData or Views on the main thread
                    withContext(Dispatchers.Main) {
                        // Update the widget's TextView with the fetched data
                        views.setTextViewText(R.id.temperatureReading, "     Temperature: ${data.temperature}°C")
                        views.setTextViewText(R.id.phReading, "     pH: ${data.pH}")
                        views.setTextViewText(R.id.ecReading, "     eC: ${data.eC}")
                        views.setTextViewText(R.id.lightReading, "     Light: ${data.light} ")
                        views.setTextViewText(R.id.pumpReading, "     Water: ${data.flowRate}")
                        views.setTextViewText(R.id.humidityReading, "     Humidity: ${data.humidity}")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    Log.e("SENSOR ERROR", e.message.toString())
                }
            }
        }
    }
    fun isSystemInDarkTheme(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

}
