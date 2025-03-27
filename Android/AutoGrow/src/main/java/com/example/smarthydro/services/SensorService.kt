package com.example.smarthydro.services

import com.example.smarthydro.models.SensorModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
private const val BASE_URL = "http://192.168.8.14/"
//The second url is declared below
private const val BASE_URL2 = "http://192.168.1.102/"
object SensorService{
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ISensors::class.java)
    }
    //The second url is set up to be used by retrofit
    private val retrofit2 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ISensors::class.java)
    }



    fun buildService(): ISensors {
        return retrofit
    }

    //THe retrofit val is returned for the second url
    fun buildService2(): ISensors {
        return retrofit2
    }
}

interface ISensors {
    @GET("/r/n/r/n")
    suspend fun getSensorData(): SensorModel

    //The endpoint for the second url is declared
    @GET("/r/n/r/n")
    suspend fun getSensorData2(): SensorModel
    @GET("getHistoricData")
    suspend fun getHistoricData(): SensorModel
}



