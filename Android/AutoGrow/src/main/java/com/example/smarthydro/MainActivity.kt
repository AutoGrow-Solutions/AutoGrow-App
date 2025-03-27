package com.example.smarthydro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthydro.ui.theme.SmartHydroTheme
import com.example.smarthydro.ui.theme.screen.home.HomeScreen
import com.example.smarthydro.ui.theme.screen.note.NoteScreen
import com.example.smarthydro.ui.theme.screen.note.ViewNotes
import com.example.smarthydro.ui.theme.screen.note.WriteToNote
import com.example.smarthydro.ui.theme.screen.stream.CameraStreamScreen
import com.example.smarthydro.ui.theme.screen.viewData.SpeedTestScreen
import com.example.smarthydro.viewmodels.ComponentViewModel
import com.example.smarthydro.viewmodels.ReadingViewModel
import com.example.smarthydro.viewmodels.SensorViewModel

sealed class Destination(val route: String) {
    object Home : Destination("home")
    object ViewData : Destination("viewData")
}

class MainActivity : ComponentActivity() {
    private val sensorViewModel: SensorViewModel by viewModels()
    private val component: ComponentViewModel by viewModels()
    private val reading: ReadingViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && !isPermissionNotificationShown()) {
            pushNotification(this, "Permission Granted", "You can now receive notifications.", isSilent = false)
            setPermissionNotificationShown(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SmartHydroTheme {
                val navController = rememberNavController()
                NavAppHost(navController = navController, sensorViewModel, component, reading)
            }
        }
    }

    private fun pushNotification(context: Context, title: String, message: String, isSilent: Boolean = false) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val CHANNEL_ID = "sensor_alerts"
        val NOTIFICATION_ID = 1

        if (isNotificationShown(NOTIFICATION_ID)) return

        createNotificationChannel(context, CHANNEL_ID, isSilent)
        val builder = buildNotification(context, CHANNEL_ID, title, message, isSilent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
            setNotificationShown(NOTIFICATION_ID, true)
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String, isSilent: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sensor Alerts"
            val descriptionText = "Notifications for sensor readings out of range"
            val importance = if (isSilent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
            val soundUri = if (isSilent) Uri.EMPTY else Uri.parse("android.resource://${context.packageName}/${R.raw.water_flow}")

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(soundUri, null)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context, channelId: String, title: String, message: String, isSilent: Boolean): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isSilent) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setSound(if (isSilent) Uri.EMPTY else Uri.parse("android.resource://${context.packageName}/${R.raw.water_flow}"))
    }

    private fun isPermissionNotificationShown(): Boolean {
        return getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).getBoolean("permission_notification_shown", false)
    }

    private fun setPermissionNotificationShown(shown: Boolean) {
        getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("permission_notification_shown", shown)
            apply()
        }
    }

    private fun isNotificationShown(notificationId: Int): Boolean {
        return getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).getBoolean("notification_shown_$notificationId", false)
    }

    private fun setNotificationShown(notificationId: Int, shown: Boolean) {
        getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("notification_shown_$notificationId", shown)
            apply()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavAppHost(
    navController: NavHostController,
    sensorViewModel: SensorViewModel,
    componentViewModel: ComponentViewModel,
    readingViewModel: ReadingViewModel
) {
    NavHost(navController = navController, startDestination = Destination.Home.route) {
        composable(Destination.Home.route) {
            HomeScreen(
                viewModel = sensorViewModel,
                navController,
                readingViewModel = readingViewModel
            )
        }
        composable(Destination.ViewData.route) {
            SpeedTestScreen(
                navController,
                componentViewModel,
                readingViewModel = readingViewModel,
                sensorViewModel = sensorViewModel,

            )
        }
        composable("NoteScreen") {
            NoteScreen(navController = navController)
        }
        composable("WriteToNote") {
            WriteToNote()
        }
        composable("ViewNotes") {
            ViewNotes()
        }
        composable(
            route = "CameraStreamScreen/{url}",
            arguments = listOf(navArgument("url") { defaultValue = "http://192.168.1.108/viewer" })
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url")) ?: "http://192.168.1.108/viewer"
            CameraStreamScreen(url = url)
        }
    }
}
