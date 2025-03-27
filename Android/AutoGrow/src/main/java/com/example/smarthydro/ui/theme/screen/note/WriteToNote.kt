package com.example.smarthydro.ui.theme.screen.note

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smarthydro.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

// ViewModel to handle Realtime Database operations
class NoteViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    fun addNote(title: String, description: String, imageUri: Uri?, context: Context) {
        val noteId = database.child("notes").push().key
        if (noteId != null && imageUri != null) {
            viewModelScope.launch {
                try {
                    // Convert the image to a Base64 string
                    val imageBase64 = convertImageToBase64(imageUri, context)

                    // Get the current time in milliseconds
                    val timestamp = System.currentTimeMillis()

                    val note = mapOf(
                        "title" to title,
                        "description" to description,
                        "image" to imageBase64,
                        "timestamp" to timestamp  // Add this line
                    )

                    database.child("notes").child(noteId).setValue(note).await()
                    Log.d("NoteViewModel", "Note added successfully")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Error adding note", e)
                }
            }
        }
    }


    private fun convertImageToBase64(imageUri: Uri, context: Context): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteToNote() {
    val context = LocalContext.current
    val viewModel: NoteViewModel = viewModel()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val leagueSpartan = FontFamily(Font(R.font.leaguespartan_semibold))
    val customOutlinedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedTextColor = Color.Black, // For when the field is focused
        unfocusedTextColor = Color.Black, // Optional, for when the field is not focused
        focusedBorderColor = Color(0xff00AFEF),
        cursorColor = Color.Black
    )


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    fun createImageFile(context: Context): Uri {
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "AutoGrow"
        )
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val photoFile = File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
            capturedImageUri?.let { uri ->
                imageUri = uri
            }
        } else {
            capturedImageUri = null
            Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(createImageFile(context))
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF121212), RoundedCornerShape(10.dp))
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff00AFEF),
                    contentColor = Color(0xFF121212)
                )
            ) {
                Text(
                    "Upload Image",
                    style = TextStyle(fontFamily = leagueSpartan)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA8CF45),
                    contentColor = Color(0xFF121212)
                )
            ) {
                Text(
                    "Take Photo",
                    style = TextStyle(fontFamily = leagueSpartan)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title", style = TextStyle(fontFamily = leagueSpartan)) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(10.dp)),
            colors = customOutlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description", style = TextStyle(fontFamily = leagueSpartan)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White, RoundedCornerShape(10.dp)),
            colors = customOutlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && description.isNotEmpty() && imageUri != null) {
                    viewModel.addNote(title, description, imageUri, context)
                    title = ""
                    description = ""
                    imageUri = null
                    capturedImageUri = null
                } else {
                    Toast.makeText(context, "Please fill in all fields and add an image", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9D21),
                contentColor = Color(0xFF121212)
            )
        ) {
            Text(
                "Save Note",
                style = TextStyle(fontFamily = leagueSpartan, fontSize = 18.sp)
            )
        }
    }
}
