package com.example.smarthydro.repositories

import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.services.SensorService

class SensorRepository {
    private val sensorService = SensorService.buildService()

    private val sensorService2 = SensorService.buildService2()
    suspend fun getSensorData(): SensorModel {
        return sensorService.getSensorData()
    }
    //the second url's method is defined
    suspend fun getSensorData2(): SensorModel {
        return sensorService2.getSensorData2()
    }
}