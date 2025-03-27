package com.example.smarthydro.ui.theme.screen.note

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.launch
import leagueSpartan
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

data class Note(
    val title: String = "",
    val timestamp: Long = 0L,
    val description: String = "",
    val image: String = ""
)


class ViewNotesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference.child("notes")
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set

    var isLoading by mutableStateOf(true) // Add loading state

    init {
        fetchNotes()
    }

    private fun fetchNotes() {
        viewModelScope.launch {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val noteList = mutableListOf<Note>()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.let { noteList.add(it) }
                    }
                    notes = noteList
                    isLoading = false // Set loading to false once data is fetched
                    Log.d("ViewNotesViewModel", "Notes fetched: ${notes.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewNotesViewModel", "Error fetching notes", error.toException())
                    isLoading = false // Set loading to false even if there's an error
                }
            })
        }
    }
}

fun createPdfFromNotes(context: Context, notes: List<Note>) {
    val document = Document()
    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/notes.pdf"

    try {
        PdfWriter.getInstance(document, FileOutputStream(filePath))
        document.open()

        for (note in notes) {
            document.add(Paragraph("Title: ${note.title}"))
            document.add(Paragraph("Date: ${formatTimestamp(note.timestamp)}"))
            document.add(Paragraph("Description: ${note.description}"))

            // Add image to PDF if available
            note.image.takeIf { it.isNotEmpty() }?.let {
                val bitmap = decodeBase64Image(it)
                bitmap?.let { bmp ->
                    val imageStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageStream)
                    val imageBytes = imageStream.toByteArray()
                    val image = com.itextpdf.text.Image.getInstance(imageBytes)
                    document.add(image)
                }
            }
            document.add(Paragraph("\n"))
        }

        document.close()
        Toast.makeText(context, "PDF created successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: DocumentException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


@Composable
fun ViewNotes() {
    val viewModel: ViewNotesViewModel = viewModel()
    val notes by remember { derivedStateOf { viewModel.notes } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current.density
    val itemWidthPx = 320.dp.toPx(density)
    val maxOffsetPx = 30.dp.toPx(density)

    val snapIndex by derivedStateOf {
        val visibleItemInfos = scrollState.layoutInfo.visibleItemsInfo
        visibleItemInfos.minByOrNull { itemInfo ->
            val centerX = scrollState.layoutInfo.viewportSize.width / 2
            val itemCenterX = itemInfo.offset + (itemWidthPx / 2)
            Math.abs(itemCenterX - centerX)
        }?.index ?: 0
    }

    LaunchedEffect(snapIndex) {
        coroutineScope.launch {
            scrollState.animateScrollToItem(snapIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF121212))
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading...",
                    color = Color(0xff00AFEF),
                    style = TextStyle(fontSize = 65.sp),
                    fontFamily = leagueSpartan,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (notes.isNotEmpty()) {
            LazyRow(
                state = scrollState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(notes) { index, note ->
                    val viewportWidthPx = scrollState.layoutInfo.viewportSize.width.toFloat()
                    val itemOffsetPx = scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == index }?.offset ?: 0
                    val itemCenterPx = itemOffsetPx + (itemWidthPx / 2)
                    val centerX = viewportWidthPx / 2
                    val distanceFromCenter = itemCenterPx - centerX

                    val maxScale = 1.1f
                    val minScale = 0.9f
                    val scale = maxScale - (Math.abs(distanceFromCenter) / (viewportWidthPx / 2)) * (maxScale - minScale)

                    val offsetX = (distanceFromCenter / viewportWidthPx) * maxOffsetPx

                    NoteCard(
                        note = note,
                        onDownloadClick = {
                            createPdfFromNotes(context, listOf(note))
                        },
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .padding(10.dp)
                            .padding(start = 8.dp)
                            .width(320.dp)
                            .height(700.dp)
                            .offset(x = offsetX.toDp(density))
                    )
                }
            }
        } else {
            Text(
                "No notes available",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}




@Composable
fun Float.toDp(density: Float): Dp {
    return (this / density).dp
}


@Composable
fun NoteCard(note: Note, onDownloadClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = modifier
            .padding(1.dp)
            .offset(y = 16.dp)
            .graphicsLayer {
                shadowElevation = 20.dp.toPx()
            },
        shadowElevation = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Base64.decode(note.image, Base64.DEFAULT))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = note.title,
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = formatTimestamp(note.timestamp),
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 14.sp,
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = note.description,
                        style = TextStyle(
                            fontFamily = leagueSpartan,
                            fontSize = 16.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
            Button(
                onClick = { onDownloadClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    modifier = Modifier.size(24.dp), // Adjust size as needed
                    tint = Color(0xFF121212) // Set icon color to match text
                )
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(
                    text = "Download PDF",
                    color = Color(0xFF121212) // Set the text color to 0xFF121212
                )
            }


        }
    }
}


// Utility function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val date = java.util.Date(timestamp)
    return sdf.format(date)
}

@Composable
fun Dp.toPx(density: Float): Float {
    return this.value * density
}

private val bitmapCache = mutableMapOf<String, Bitmap?>()

private fun decodeBase64Image(base64String: String): Bitmap? {
    return bitmapCache[base64String] ?: try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, this)
            val imageHeight = outHeight
            val imageWidth = outWidth
            val reqWidth = 200 // target width in pixels
            val reqHeight = 200 // target height in pixels
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
        bitmapCache[base64String] = bitmap
        bitmap
    } catch (e: IllegalArgumentException) {
        Log.e("NoteCard", "Invalid Base64 string", e)
        null
    }
}


private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (width: Int, height: Int) = options.outWidth to options.outHeight
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

