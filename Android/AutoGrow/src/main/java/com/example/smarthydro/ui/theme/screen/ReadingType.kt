package com.example.smarthydro.ui.theme.screen

import androidx.compose.ui.graphics.Color
import com.example.smarthydro.models.SensorModel

data class ReadingType(
    var heading: String,
    var value: SensorModel,
    var unit: String,
    var unitColor: Color = Color.White // Assuming Color is imported correctly
)
