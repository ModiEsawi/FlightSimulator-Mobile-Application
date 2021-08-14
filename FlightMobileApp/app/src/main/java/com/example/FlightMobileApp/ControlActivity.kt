package com.example.FlightMobileApp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.FlightMobileApp.util.showCustomToast
import kotlinx.android.synthetic.main.activity_control.*
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.sin

/**
 * Control Activity.
 * provide a control of the simulator by joystick and simulate the application' view
 */
class ControlActivity : AppCompatActivity() {
    private lateinit var url: String
    private lateinit var mainHandler: Handler

    /** update screenshots each predefined slice time */
    private val updateScreenshot = object : Runnable {
        override fun run() {
            getImage()
            //to call again and again
            mainHandler.postDelayed(this, Constants.SCREENSHOT_SLICE_MILLISECONDS)
        }
    }
    private var command = Command(aileron = 0.0, rudder = 0.0, throttle = 0.0, elevator = 0.0)
    private var screenshotsFailuresCounter: Long = 0
    private var commandFailuresCounter : Long = 0
    private var screenshotToastShowed : Boolean = false
    private var commandsToastShowed : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        //get the server url from main activity
        if (intent.getStringExtra(Constants.URL_STRING) != null)
            this.url = intent.getStringExtra(Constants.URL_STRING).toString()
        mainHandler = Handler(Looper.getMainLooper()) //start execute

        /** send  command  after each change in throttle slider.*/
        throttleSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                command.throttle = i.toDouble()/10 // 0 , 0.1 , 0.2 ,.. , 1
                sendCommand() //send the new command to the server
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        /** send  command  after each change in rudder slider.*/
        rudderSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                command.rudder = (i.toDouble()/10)-1 // -1 , -0.9 ,...,0 , 0.1 ,.., 1
                sendCommand()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        /** send command according to joystick move*/
        joystick.setOnMoveListener { angle, strength ->
            //translate angle and strength to "aileron" and "elevator" values
            val inRadians = Math.toRadians(angle.toDouble())
            var aileron = (strength* cos(inRadians))/100
            var elevator = (strength * sin(inRadians))/100
            //two digits after points
            aileron = DecimalFormat("##.##").format(aileron).toDouble()
            elevator = DecimalFormat("##.##").format(elevator).toDouble()
            val threshold = Constants.SIGNIFICANT_CHANGE_IN_JOYSTICK_MOVE
            //significant move
            if (aileron >= command.aileron+threshold || elevator >= command.elevator+threshold
                || aileron <= command.aileron-threshold || elevator <=command.elevator-threshold){
                command.aileron=aileron
                command.elevator = elevator
                sendCommand() //send the command to the server
            }
        }
    }

    /**
     * send http request to the server to get screenshot and update the screen.
     */
    private fun getImage() {
        val apiService = RestApiService(url)
        apiService.getImg  { //send request
            if (it != null) { //success
                simulatorImg.setImageBitmap(it)
                screenshotsFailuresCounter = 0 //recovery
                if(screenshotToastShowed) // connection back (after failure)
                    customToast("success","connection back! updating screenshots",false)
                screenshotToastShowed = false
            } else { //failure
                screenshotsFailuresCounter++ //count failures
                //if have passed 10 seconds without getting screenshot
                if( (screenshotsFailuresCounter >=
                    Constants.MAX_TIMEOUT_MILLISECONDS/Constants.SCREENSHOT_SLICE_MILLISECONDS)
                    && !screenshotToastShowed){
                    customToast("fail","can't update screenshots! back to main screen or wait"
                        ,true)
                    screenshotToastShowed = true
                }
            }
        }
    }

    /**
     * send http request to the server which contain the command , in json format /
     */
    private fun sendCommand(){
        val apiService = RestApiService(url)
        apiService.sendCommand(command) { //send request
            if(it == 0 || it == 1){ //inconsistent values in simulator (error)
                commandFailuresCounter ++ //count consistent failures
                if(commandFailuresCounter == Constants.MAX_CONSISTENT_COMMAND_FAILURES
                    && !commandsToastShowed){ //consistent series of failures
                    customToast("fail","can't update values in simulator! " +
                            "back to main screen or wait..",true)
                    commandsToastShowed = true
                }
            }else{ //success
                commandFailuresCounter = 0 //recovery
                if(commandsToastShowed) // connection back (after failure)
                    customToast("success","connection back , keep controlling!",false)
                commandsToastShowed = false
            }
        }
    }

    /**
     * show custom toast.
     * @param status : "success", "fail" or "warning" . decides toast style
     * @param message : to be showed
     * @param long : toast duration , long or short.
     * */
    private fun customToast(status: String, message: String,long:Boolean) {
        Toast(this@ControlActivity).showCustomToast(status, message, this@ControlActivity,long)
    }
    /** onBackPressed , stop requesting screenshots*/
    override fun onBackPressed() {
        super.onBackPressed()
        mainHandler.removeCallbacks(updateScreenshot)
        this.finish() //close the activity
    }

    /** onPause , stop requesting screenshots*/
    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateScreenshot)
    }

    /** onResume , continue requesting screenshots*/
    override fun onResume() {
        super.onResume()
        mainHandler.post(updateScreenshot)
    }


}