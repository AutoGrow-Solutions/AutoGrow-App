package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.repositories.SensorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {
    private val repository = SensorRepository()

    private val _sensorData = MutableLiveData<SensorModel>()
    val sensorData: LiveData<SensorModel> = _sensorData


    fun fetchSensorData() {
        viewModelScope.launch {
            try {
                val data = repository.getSensorData()
                _sensorData.value = data
            } catch (e: Exception) {
                Log.e("SENSOR ERROR", e.message.toString())
            }
            //The second url has been used to get the data in the following try catch block
            try {
                val data2 = repository.getSensorData2()
                _sensorData.value = data2
            } catch (e: Exception) {
                Log.e("SENSOR ERROR", e.message.toString())
            }
        }

    }

    fun fetchSensorPeriodically(milliseconds: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                fetchSensorData()
                delay(milliseconds)
            }
        }
    }
}