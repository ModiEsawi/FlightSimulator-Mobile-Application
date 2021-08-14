package com.example.FlightMobileApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.FlightMobileApp.db.DataBase
import com.example.FlightMobileApp.db.HostsEntity
import com.example.FlightMobileApp.util.showCustomToast
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    //init components
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataBase =
            Room.databaseBuilder(applicationContext, DataBase::class.java, "HostsDB").build()
        //init views
        val connectButton: Button = findViewById(R.id.connectButton)
        val firstHost: Button = findViewById(R.id.host1)
        val secondHost: Button = findViewById(R.id.host2)
        val thirdHost: Button = findViewById(R.id.host3)
        val fourthHost: Button = findViewById(R.id.host4)
        val fifthHost: Button = findViewById(R.id.host5)

        //keeping hosts views
        val hosts: Array<Button> = arrayOf(firstHost,secondHost,thirdHost,fourthHost,fifthHost)
        //when connect button pressed
        connectButton.setOnClickListener {
            connectClicked(dataBase,hosts)
        }
        //set listener for each host : copy host string to url input textView
        setClicksForHosts(hosts,urlText)

        //sort the hosts
        CoroutineScope(IO).launch {
            val list = dataBase.hostDAO().getHosts()
            if(list.isNotEmpty()){
                updateHosts(dataBase,hosts)
            }
        }
    }

    /**
     * copy host string to url input textView
     * @param hosts : list of hosts buttons
     * @param urlButton : where to copy the host string
     */
    private fun setClicksForHosts(hosts: Array<Button>, urlButton: TextView) {
        for (i in hosts) {
            i.setOnClickListener {
                urlButton.text = i.text
            }
        }
    }

    /**
     * show custom toast.
     * @param status : "success", "fail" or "warning" . decides toast style
     * @param message : to be showed
     * @param long : toast duration , long or short.
     * */
    private fun customToast(status:String, message: String,long:Boolean){
        Toast(this@MainActivity).showCustomToast (status,message,this@MainActivity,long)
    }

    /**
     * define the functionality when connect button pressed
     * @param dataBase : db
     * @param hosts : five hosts buttons
     */
    private fun connectClicked(dataBase : DataBase, hosts: Array<Button>) {

        // if the input text view empty, show appropriate toast
        if(urlText.text.toString().isEmpty()){
            customToast("fail","Please fill out URL field",false)
            return
        }

        //insert the url to db and update the hosts
        CoroutineScope(IO).launch {
            val list:List<HostsEntity> = dataBase.hostDAO().getHosts()
            if(list.isNotEmpty()) {
                updateLatencies(dataBase)
                delay(200)
            }
            val urlToInsert = urlText.text
            val host = HostsEntity() //new item
            host.host = urlToInsert.toString()
            host.latency = 0 // 0 indicates to  last inserted url
            for (x in list){
               if (x.host == host.host){
                   dataBase.hostDAO().deleteHost(host.host)
               }
            }
            dataBase.hostDAO().insertHost(host) //insert new item
            updateHosts(dataBase,hosts) //update the hosts in screen
        }
        val url = urlText.text
        /**
         * try to connect to the inserted url , and this done by send
         * http request to the server , which request a screenshot ,
         * if success , navigate to control activity, else, show appropriate toast
         * */
        try {
            //init retrofit object
            val retrofit = Retrofit.Builder().baseUrl(url.toString())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient()
                    .create()))
                    .build()
            val api = retrofit.create(ApiService::class.java)
            customToast("warning","connecting....",true)
            //send the request
            api.getScreenshot().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) { //the server returns status 200 (and the image)
                        customToast("success","Connected successfully!",false)
                        //move to next activity
                        val intent = Intent(this@MainActivity,ControlActivity::class.java)
                        intent.putExtra(Constants.URL_STRING,url.toString())
                        startActivity(intent)
                    }else{ //server return 404 or 502 status code
                        customToast("fail","Unable to get screenshots!!",false)
                    }
                }
                //server not connected , or error in network
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    customToast("fail","Unable to connect to host!",false)
                }
            })
        }catch (e :Exception){ //invalid inserted url
            customToast("fail","invalid URL!",false)
        }
    }

    /**increment the latencies for each host in db*/
    private fun updateLatencies(dataBase: DataBase){
        CoroutineScope(IO).launch {
            dataBase.hostDAO().updateLatencies()
        }
    }

    /** update the hosts according to the latencies
     * @param dataBase :db
     * @param hosts : all five hosts
     **/
    private fun updateHosts(dataBase: DataBase, hosts:  Array<Button>){
        CoroutineScope(IO).launch {
            val sortedHosts = dataBase.hostDAO().sortHosts()
            CoroutineScope(Main).launch {
                var index = 0
                for (host in sortedHosts) {
                    if (index < 5) {
                    hosts[index].text = host.host
                        index++
                    }
                    else
                        break
                }
            }
        }
    }
}
