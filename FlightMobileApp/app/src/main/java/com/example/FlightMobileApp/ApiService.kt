package com.example.FlightMobileApp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/** simulator API */
interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("api/command")
    fun sendCommand(@Body command: Command): Call<ResponseBody>
    @GET("/screenshot")
    fun getScreenshot(): Call<ResponseBody>
}

/**
 * define ServiceBuilder for http requests to retrofit calling
 * @param urlInput : endpoint
 * @param timeoutInSeconds : request's time out
 * */
class ServiceBuilder(urlInput:String, timeoutInSeconds:Long) {
    private val url = urlInput
    private val client = OkHttpClient.Builder().callTimeout(timeoutInSeconds
        , TimeUnit.SECONDS).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}

/**
 * manage simulator http requests
 * @param urlInput : endpoint
 */
class RestApiService(urlInput:String) {
    private val url = urlInput

    /**
     * send command to simulator.
     * @param command : to send
     * @param onResult : to keep the result , 0 : server not connected ,
     *                                        1 : server returns 404 or 502
     *                                        2 : server return OK 200
     */
    fun sendCommand(command: Command, onResult: (Int) -> Unit){
        //init retrofit object
        val retrofit = ServiceBuilder(url,Constants.SEND_COMMAND_REQUEST_TIMEOUT_SEC)
            .buildService(ApiService::class.java)
        //send the request
        retrofit.sendCommand(command).enqueue(object : Callback<ResponseBody> {
            override fun onResponse( call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful)  //server returns 200
                    onResult(2)
                else
                    onResult(1) // server return 404 or 502
            }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(0) //server not connected
                }
            }
        )
    }

    /**
     * get screenshot from simulator.
     * @param onResult : to keep the result , null : error in server
     *                                        bitmap : screenshot
     */
    fun getImg(onResult: (Bitmap?) -> Unit){
        //init retrofit object
        val retrofit = ServiceBuilder(url,Constants.GET_SCREENSHOT_REQUEST_TIMEOUT_SEC)
            .buildService(ApiService::class.java)
        //send  the request
        retrofit.getScreenshot().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) { //server returns 200 Ok (and the image)
                    val bytes = response.body()?.bytes()
                    val bitmap = bytes?.size?.let { BitmapFactory.decodeByteArray(bytes, 0, it) }
                    onResult(bitmap)
                } else // server return 404 or 502 .
                    onResult(null)
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(null) //server not connected
            }
        })
    }
}