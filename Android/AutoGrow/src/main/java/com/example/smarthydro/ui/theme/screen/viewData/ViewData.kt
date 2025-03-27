package com.example.smarthydro.ui.theme.screen.viewData


import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smarthydro.R
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.GreenGradient
import com.example.smarthydro.ui.theme.LightGreen1
import com.example.smarthydro.ui.theme.PrimaryColor
import com.example.smarthydro.ui.theme.screen.ReadingType
import com.example.smarthydro.viewmodels.ComponentViewModel
import com.example.smarthydro.viewmodels.ReadingViewModel
import com.example.smarthydro.viewmodels.SensorViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.overlayingComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.extension.copyColor
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

// https://www.youtube.com/watch?v=8LuWMYXW6nw
class UiState(
    val speed: String = "",
    val ping: String = "-",
    val maxSpeed: String = "-",
    val arcValue: Float = 0f,
    val inProgress: Boolean = false
)
private var powerState : Boolean = true
private var readingValue : String = ""
private var toggleEC : String = "mn "
private var togglePH : String = ""
val openAlertDialogLow = mutableStateOf(false)
val openAlertDialogUp = mutableStateOf(false)


private var reading: ReadingType = ReadingType("",SensorModel(), "");
suspend fun startAnimation(animation: Animatable<Float, AnimationVector1D>) {
    animation.animateTo(1.00f, keyframes {

    })
}

fun Animatable<Float, AnimationVector1D>.toUiState(maxSpeed: Float) = UiState(
    arcValue = value,
    speed = "%.1f".format(value),
    ping = if (value > 0.2f) "${(value * 15).roundToInt()} ms" else "-",
    maxSpeed = if (maxSpeed > 0f) "%.1f mbps".format(maxSpeed) else "-",
    inProgress = isRunning
)
//@Preview
@Composable
fun SpeedTestScreen(navHostController: NavHostController, component: ComponentViewModel, readingViewModel: ReadingViewModel, sensorViewModel: SensorViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val animation = remember { Animatable(0f) }
    val maxSpeed = remember { mutableStateOf(0f) }
    val sensorData by sensorViewModel.sensorData.observeAsState(SensorModel())
    maxSpeed.value = max(maxSpeed.value, animation.value * 100f)

    reading = readingViewModel.getReadingType()!!

    SpeedTestScreen(animation.toUiState(maxSpeed.value),navHostController, reading.heading, component, sensorData ) {
        coroutineScope.launch {
            maxSpeed.value = 0f
            startAnimation(animation)
        }
    }
}

private fun getReadingUnit(readingString: String, data: SensorModel):ReadingType{

    val readingType = ReadingType(readingString, SensorModel(),"")

    when (readingString) {
        "Temperature" -> {
            readingType.heading = "Temperature"
            readingValue = data.temperature
            readingType.unit = "℃"
            readingType.unitColor = Color.White // Set unit color here

        }
        "Water" -> {
            readingType.heading = "Water Flow"
            readingValue = data.flowRate
            readingType.unit = "L/hr"
            readingType.unitColor = Color.White // Set unit color here
        }
        "Water pH" -> {
            readingType.heading = "Water pH"
            readingValue = data.pH
            readingType.unit = "pH"
            readingType.unitColor = Color.White // Set unit color here

        }
        "Humidity" -> {
            readingType.heading = "Humidity"
            readingValue = data.humidity
            readingType.unit = "RH" // RH = Relative Humidity
            readingType.unitColor = Color.White // Set unit color here
        }
        "EC Reading" -> {
            readingType.heading = "EC Reading"
            readingValue = data.eC
            readingType.unit = "ms/cm"
            readingType.unitColor = Color.White // Set unit color here
        }
        "Light" -> {
            readingType.heading = "Light"
            readingValue = data.light
            readingType.unit = "lumens"
            readingType.unitColor = Color.White // Set unit color here
        }
        else -> {}
    }

    return readingType
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedTestScreen(state: UiState,navHostController: NavHostController,readingString:String, component: ComponentViewModel, sensorModel: SensorModel, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(DeepBlue)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(top = 25.dp, bottom = 5.dp)
                .fillMaxWidth()
                .background(Color.Transparent), // Make the background transparent
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(onClick = { /* Handle back action here */
                navHostController.navigate("home")
            }) {
                Icon(

                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White // Make the back button white
                )
            }
        }

        reading = getReadingUnit(readingString = readingString, sensorModel)
        Header(reading.heading)

        // BarChart(values = barChartInputsPercent, xLabels = xAxis)
        Chart(
            data = mapOf(

                Pair(0.5f,"M"),
                Pair(0.6f,"T"),
                Pair(0.2f,"W"),
                Pair(0.7f,"T"),
                Pair(0.8f,"F"),
                Pair(0.3f,"S"),
                Pair(0.1f,"S"),
            ), max_value = 80
        )

        when (reading.heading) {
            "Water pH" -> {
                LowAndHighIconButtons(state = state, onClick = onClick, reading.unit, component)
            }
            "EC Reading" -> {
                LowAndHighIconButtons(state = state, onClick = onClick, reading.unit, component)
            }
            else -> {
                SpeedIndicator(state = state, onClick = onClick, reading.unit, component)
            }
        }

    }
}

//Pass data reading header here
@Composable
fun Header(heading: String) {
    Text(
        // Heading Value Change
        text = heading,
        fontSize = 56.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 26.dp),
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.leaguespartan_regular, FontWeight.Bold, FontStyle.Normal))
        ),
        color = Color.White
    )
}

//Pass Unit value here
@Composable
fun SpeedIndicator(state: UiState, onClick: () -> Unit, unit: String, component: ComponentViewModel) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f)
        IconButtonOnOff(onClick, component)
        SpeedValue(readingValue,unit)
    }
}

@Composable
fun LowAndHighIconButtons(state: UiState, onClick: () -> Unit, unit: String, component: ComponentViewModel) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            horizontalArrangement = Arrangement.Center)
        {
            ToggleLowButton(onClick, component)
            Spacer(modifier = Modifier.width(24.dp))
            IconButtonOnOff(onClick, component)
            Spacer(modifier = Modifier.width(24.dp))
            ToggleHighButton(onClick, component)
        }

        SpeedValue(readingValue,unit)
    }
}

@Composable
fun ToggleLowButton(onClick: () -> Unit, componentViewModel: ComponentViewModel) {
    var iconColor by remember { mutableStateOf(Color.Red) }

    IconButton(
        modifier = Modifier
            .size(72.dp)
            .padding(top = 20.dp),
        onClick = {
            powerState = !powerState

            when (reading.heading) {
                "Water pH" -> {
                    openAlertDialogLow.value = true
                }
                "EC Reading" -> {
                    openAlertDialogLow.value = true
                }
                else -> {}
            }

            if (powerState) {
                iconColor = Color.Red
                onClick()
            }else {
                iconColor = Color.Green
            }
        })
    {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = "Option to toggle the pump to be lower",
            tint = Color(0xFFFFA500), // Orange color
            modifier = Modifier.size(size = 100.dp)
        )
    }

    LowerSolution(openAlertDialog = openAlertDialogLow, componentViewModel, reading.heading)
}

@Composable
fun ToggleHighButton(onClick: () -> Unit, componentViewModel: ComponentViewModel) {
    var iconColor by remember { mutableStateOf(Color.Red) }

    IconButton(
        modifier = Modifier
            .size(72.dp)
            .padding(top = 20.dp),
        onClick = {
            powerState = !powerState

            when (reading.heading) {
                "Water pH" -> {
                    openAlertDialogUp.value = true
                }
                "EC Reading" -> {
                    openAlertDialogUp.value = true
                }
                else -> {}
            }
            if (powerState) {
                iconColor = Color.Red
                onClick()
            }else {
                iconColor = Color.Green
            }
        })
    {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_up),
            contentDescription = "Option to toggle the pump to be higher",
            tint = Color.Cyan,
            modifier = Modifier.size(size = 100.dp)
            )
    }

    HigherSolution(openAlertDialog = openAlertDialogUp, componentViewModel, reading.heading)
}

@Composable
fun LowerSolution(openAlertDialog: MutableState<Boolean>, componentViewModel: ComponentViewModel, heading: String){
    when {
        openAlertDialog.value -> {
            AlertDialogModel(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    //println("Confirmation registered") // Add logic here to handle confirmation.
                    when (heading) {
                        "Water pH" -> {
                            componentViewModel.setPhDown()
                        }
                        "EC Reading" -> {
                            componentViewModel.setEcDown()
                        }
                        else -> {
                        }
                    }
                    openAlertDialog.value = false
                },
                dialogTitle = "Decrease Nutrients",
                dialogText = "You are about decrease solution for this!",
                icon = Icons.Default.Info
            )
        }
    }
}


@Composable
fun HigherSolution(openAlertDialog: MutableState<Boolean>, componentViewModel: ComponentViewModel, heading: String){
    when {
        openAlertDialog.value -> {
            AlertDialogModel(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    when (heading) {
                        "Water pH" -> {
                            componentViewModel.setPhUp()
                        }
                        "EC Reading" -> {
                            componentViewModel.setEcUp()
                        }
                        else -> {
                        }
                    }
                    openAlertDialog.value = false
                },
                dialogTitle = "Increase Nutrients",
                dialogText = "You are about increase solution for this!",
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
fun IconButtonOnOff(onClick: () -> Unit, componentViewModel: ComponentViewModel) {
    var iconColor by remember { mutableStateOf(Color.Red) }

    IconButton(
        modifier = Modifier
            .size(72.dp)
            .padding(top = 20.dp),
        onClick = {
            powerState = !powerState

            when (reading.heading) {
                "Temperature" -> {
                    componentViewModel.setFan()
                }
                "Light" -> {
                    componentViewModel.setLight()
                }
                "Humidity" -> {
                    componentViewModel.setExtractor()
                }
                "Water Flow" -> {
                    componentViewModel.setPump()
                }
                "Water pH" -> {
                    componentViewModel.setPh()
                }
                "EC Reading" -> {
                    componentViewModel.setEc()
                }

                else -> {}
            }

            if (powerState) {
                iconColor = Color.Red
                onClick()
            }else {
                iconColor = Color.Green
            }
        })
    {
        Icon(
            painter = painterResource(id = R.drawable.ic_power),
            contentDescription = "Option to turn on or off the component",
            tint = iconColor,
            modifier = Modifier.size(size = 100.dp)
        )
    }
}

@Composable
fun SpeedValue(value: String, unit: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 45.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        //Measurement Unit change here
        Text(unit, style = MaterialTheme.typography.headlineMedium, color = Color.Cyan)

    }
}

@Composable
fun CircularSpeedIndicator(value: Float, angle: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        drawLines(value, angle)
        drawArcs(value, angle)
    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawBlur() {
        for (i in 0..20) {
            drawArc(
                color = PrimaryColor.copy(alpha = i / 900f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
            )
        }
    }

    fun drawStroke() {
        drawArc(
            color = PrimaryColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 86f, cap = StrokeCap.Round)
        )
    }

    fun drawGradient() {
        drawArc(
            brush = GreenGradient,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()
}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberOfLines: Int = 40) {
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                LightGreen1,
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}

private val model1 = entryModelOf(5, 10, 11, 20, 10)


//Graph Component

@Composable
fun LineChartDark() {
    Surface(
        shape = RoundedCornerShape(8.dp),
    ) {
        Chart(
            data = mapOf(

                Pair(1f,"M"),
                Pair(0.6f,"B"),
                Pair(0.2f,"C"),
                Pair(0.7f,"D"),
                Pair(0.8f,"E"),

                ), max_value = 80
        )
    }
}

@Composable
fun RegularLineChart() {
    Chart(
        chart = lineChart(),
        model = model1,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        isZoomEnabled = true,
        marker = rememberMarker(),
    )
}

@Composable
internal fun rememberMarker(): Marker {
    val labelBackgroundColor = MaterialTheme.colorScheme.surface
    val labelBackground = remember(labelBackgroundColor) {
        ShapeComponent(labelBackgroundShape, labelBackgroundColor.toArgb()).setShadow(
            radius = LABEL_BACKGROUND_SHADOW_RADIUS,
            dy = LABEL_BACKGROUND_SHADOW_DY,
            applyElevationOverlay = true,
        )
    }
    val label = textComponent(
        background = labelBackground,
        lineCount = LABEL_LINE_COUNT,
        padding = labelPadding,
        typeface = Typeface.MONOSPACE,
    )
    val indicatorInnerComponent = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surface)
    val indicatorCenterComponent = shapeComponent(Shapes.pillShape, Color.White)
    val indicatorOuterComponent = shapeComponent(Shapes.pillShape, Color.White)
    val indicator = overlayingComponent(
        outer = indicatorOuterComponent,
        inner = overlayingComponent(
            outer = indicatorCenterComponent,
            inner = indicatorInnerComponent,
            innerPaddingAll = indicatorInnerAndCenterComponentPaddingValue,
        ),
        innerPaddingAll = indicatorCenterAndOuterComponentPaddingValue,
    )
    val guideline = lineComponent(
        MaterialTheme.colorScheme.onSurface.copy(GUIDELINE_ALPHA),
        guidelineThickness,
        guidelineShape,
    )
    return remember(label, indicator, guideline) {
        object : MarkerComponent(label, indicator, guideline) {
            init {
                indicatorSizeDp = INDICATOR_SIZE_DP
                onApplyEntryColor = { entryColor ->
                    indicatorOuterComponent.color = entryColor.copyColor(INDICATOR_OUTER_COMPONENT_ALPHA)
                    with(indicatorCenterComponent) {
                        color = entryColor
                        setShadow(radius = INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS, color = entryColor)
                    }
                }
            }

            override fun getInsets(context: MeasureContext, outInsets: Insets, segmentProperties: SegmentProperties) =
                with(context) {
                    outInsets.top = label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels +
                            LABEL_BACKGROUND_SHADOW_RADIUS.pixels * SHADOW_RADIUS_MULTIPLIER -
                            LABEL_BACKGROUND_SHADOW_DY.pixels
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogModel(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}


private const val LABEL_BACKGROUND_SHADOW_RADIUS = 4f
private const val LABEL_BACKGROUND_SHADOW_DY = 2f
private const val LABEL_LINE_COUNT = 1
private const val GUIDELINE_ALPHA = .2f
private const val INDICATOR_SIZE_DP = 36f
private const val INDICATOR_OUTER_COMPONENT_ALPHA = 32
private const val INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS = 12f
private const val GUIDELINE_DASH_LENGTH_DP = 8f
private const val GUIDELINE_GAP_LENGTH_DP = 4f
private const val SHADOW_RADIUS_MULTIPLIER = 1.3f

private val labelBackgroundShape = MarkerCorneredShape(Corner.FullyRounded)
private val labelHorizontalPaddingValue = 8.dp
private val labelVerticalPaddingValue = 4.dp
private val labelPadding = dimensionsOf(labelHorizontalPaddingValue, labelVerticalPaddingValue)
private val indicatorInnerAndCenterComponentPaddingValue = 5.dp
private val indicatorCenterAndOuterComponentPaddingValue = 10.dp
private val guidelineThickness = 2.dp
private val guidelineShape = DashedShape(Shapes.pillShape, GUIDELINE_DASH_LENGTH_DP, GUIDELINE_GAP_LENGTH_DP)