package com.example.smarthydro.ui.theme.screen.stream

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// Custom WebView to disable scrolling
class NonScrollableWebView(context: Context) : WebView(context) {
    init {
        // Disable scrolling
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        isScrollContainer = false // Disable scroll container
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.mediaPlaybackRequiresUserGesture = false
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        // Prevent touch events from scrolling the WebView
        return false
    }

    override fun onInterceptTouchEvent(event: android.view.MotionEvent): Boolean {
        // Intercept touch events to prevent any scrolling
        return true
    }
}

@Composable
fun CameraStreamScreen(url: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // Set the background color of the entire page
    ) {
        // Wrapping the WebView inside an AndroidView composable
        AndroidView(factory = { context ->
            NonScrollableWebView(context).apply {
                webViewClient = WebViewClient() // Handles URL loading within WebView

                // Load the camera stream URL
                loadUrl(url)
            }
        }, modifier = Modifier
            .fillMaxWidth() // Make the stream view fill the width of the screen
            .height(525.dp) // Set the height of the WebView to make it bigger
            .offset(x = 18.dp, y = 31.dp) // Move the stream view further to the right and down
        )

        // Ensure WebView reloads every time this screen is visited
        DisposableEffect(url) {
            // This block is called when the composable enters the composition
            onDispose {
                // Nothing needed here, the WebView will reload on screen entry
            }
        }
    }
}
