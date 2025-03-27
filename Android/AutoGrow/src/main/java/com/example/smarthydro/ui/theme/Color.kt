package com.example.smarthydro.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val TextWhite = Color(0xffeeeeee)
val DeepBlue = Color(0xFF121212) //background color
val DarkerButtonBlue = Color(0xffeeeeee)
val AquaBlue = Color(0xffeeeeee)
val OrangeYellow1 = Color(0xffeeeeee)
val OrangeYellow2 = Color(0xffeeeeee)
val OrangeYellow3 = Color(0xffeeeeee)
val Beige1 = Color(0xffeeeeee)
val Beige2 = Color(0xffeeeeee)
val Beige3 = Color(0xffeeeeee)
val LightGreen1 = Color(0xffeeeeee)
val LightGreen2 = Color(0xffeeeeee)
val LightGreen3 = Color(0xffeeeeee)
val BlueViolet1 = Color(0xffeeeeee)
val BlueViolet2 = Color(0xffeeeeee)
val BlueViolet3 = Color(0xffeeeeee)
val Blue1 = Color(0xffeeeeee)
val Blue2 = Color(0xffeeeeee)
val Blue3 = Color(0xffeeeeee) //wave color for clean water
val Red1 = Color(0xffeeeeee)
val Red2 = Color(0xffeeeeee)
val Red3 = Color(0xffeeeeee)
val AutoBlue = Color(0xff00AFEF)
val PrimaryColor = Color(0xFFA8CF45)
val GreenGood = Color(0xff50c878 )
val RedBad = Color(0xFFD2042D)

val GreenGradient = Brush.linearGradient(
    colors = listOf(LightGreen1, LightGreen2),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)

val DarkGradient = Brush.verticalGradient(
    colors = listOf(DeepBlue, DarkerButtonBlue)
)