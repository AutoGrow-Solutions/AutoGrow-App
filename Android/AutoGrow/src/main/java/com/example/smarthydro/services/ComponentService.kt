package com.example.smarthydro.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://192.168.8.14/"

//The second url is declared here and will be set up to be used by retrofit
private const val BASE_URL2 = "http://192.168.1.102/"
object ComponentService {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IComponent::class.java)
    }
    //The second url is set up to be used by retrofit
    private val retrofit2 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IComponent::class.java)
    }

    fun buildService(): IComponent {
        return retrofit
    }
    //The second url's retrofit val is returned here
    fun buildService2(): IComponent {
        return retrofit2
    }
}

interface IComponent {
    @GET("light")
    suspend fun toggleLight()
    @GET("fan")
    suspend fun toggleFan()
    @GET("extract")
    suspend fun toggleExtractor()
    @GET("pump")
    suspend fun togglePump()
    @GET("ec")
    suspend fun ec()
    @GET("ecUp")
    suspend fun ecUp()
    @GET("ecDown")
    suspend fun ecDown()
    @GET("ph")
    suspend fun pH()
    @GET("phUp")
    suspend fun pHUp()
    @GET("phDown")
    suspend fun pHDown()
    //Endpoints for the second url is set up
    @GET("light")
    suspend fun toggleLight2()
    @GET("fan")
    suspend fun toggleFan2()
    @GET("extract")
    suspend fun toggleExtractor2()
    @GET("pump")
    suspend fun togglePump2()
    @GET("ec")
    suspend fun ec2()
    @GET("ecUp")
    suspend fun ecUp2()
    @GET("ecDown")
    suspend fun ecDown2()
    @GET("ph")
    suspend fun pH2()
    @GET("phUp")
    suspend fun pHUp2()
    @GET("phDown")
    suspend fun pHDown2()

}