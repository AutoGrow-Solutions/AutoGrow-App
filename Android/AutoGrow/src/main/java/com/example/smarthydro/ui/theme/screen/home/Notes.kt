package com.example.smarthydro.ui.theme.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthydro.R
import leagueSpartan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(navController: NavController) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(8.dp, color = Color(0xff00AFEF)),
        modifier = Modifier
            .height(210.dp)
            .padding(10.dp)
            .clickable {
                navController.navigate("NoteScreen")
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp)) // Adjust this value as needed

                Text(
                    text = "Notepad",
                    fontSize = 25.sp,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = leagueSpartan),
                    fontWeight = FontWeight.SemiBold
                )

                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.menu_book_24px),
                    contentDescription = null,
                    modifier = Modifier.size(width = 100.dp, height = 140.dp)
                )
            }
        }
    }
}


