package com.example.smarthydro.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.smarthydro.ui.theme.screen.ReadingType

class ReadingViewModel: ViewModel() {
    private var readingTypeViewModel by mutableStateOf<ReadingType?>(null)

    fun setReadingType(type: ReadingType){
        readingTypeViewModel = type
    }

    fun getReadingType(): ReadingType?{
        return readingTypeViewModel
    }

}