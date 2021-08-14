package com.example.FlightMobileApp.util

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.FlightMobileApp.R

/**
 * show custom class
 * @param status : "success","fail","warning" . decides toast style.
 * @param message : to be showed
 * @param activity : caller activity
 * @param long : toast duration , long or short.
 */
fun Toast.showCustomToast(status:String ,message: String, activity: Activity,long:Boolean) {
    var layout : View? = null
    if (status == "success"){ //green toast style
        layout = activity.layoutInflater.inflate(
            R.layout.custom_success_toast, //xml style
            activity.findViewById(R.id.cl_customToastContainer)
        )
    }
    if(status == "fail"){ //red toast style
        layout = activity.layoutInflater.inflate(
            R.layout.custom_fail_toast, //xml style
            activity.findViewById(R.id.cl_customToastContainer)
        )
    }
    if(status == "warning"){ //yellow toast style
        layout = activity.layoutInflater.inflate(
            R.layout.custom_warning_toast,
            activity.findViewById(R.id.cl_customToastContainer)
        )
    }

    val textView = layout?.findViewById<TextView>(R.id.tv_message)
    textView?.text = message

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 40)
        duration = if(long)
            Toast.LENGTH_LONG
        else
            Toast.LENGTH_SHORT
        view = layout
        show()
    }
}