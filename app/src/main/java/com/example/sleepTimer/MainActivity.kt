package com.example.sleepTimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.app.admin.DevicePolicyManager
import android.content.ComponentName

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableAdmin()

        hourInput.setText(padZero(App.prefs.hour))
        minuteInput.setText(padZero(App.prefs.minute))

        saveBtn.setOnClickListener {
            val hour = Integer.parseInt(hourInput.text.toString())
            val minute = Integer.parseInt(minuteInput.text.toString())

            if(hour < 0 || hour >= 24){
                Toast.makeText(this, "시간 : 0 이상 24 미만", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(minute < 0 || minute >= 60){
                Toast.makeText(this, "분 : 0 이상 60 미만", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            App.prefs.hour = hour
            App.prefs.minute = minute

            Toast.makeText(this, "${hour}시간 ${minute}분으로 저장되었습니다.", Toast.LENGTH_LONG).show()
        }
    }

    fun enableAdmin(){
        val compName = ComponentName(this, DeviceAdmin::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "잠금에 필요한 권한입니다.")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityForResult(intent, 1)
    }

    fun padZero (number: Int): String {
        when(number < 10){
            true -> return "0${number}"
            false -> return "${number}"
        }
    }

}
