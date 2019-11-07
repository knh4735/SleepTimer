package com.example.sleepTimer

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class TileService: TileService() {

    override fun onClick() {
        super.onClick()

        if(qsTile.state == Tile.STATE_INACTIVE) {   // 타이머 실행
            val hour = App.prefs.hour
            val minute = App.prefs.minute

            if(hour > 0)
                Toast.makeText(this, "${hour}시간 ${minute}분 후 잠깁니다.", Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, "${minute}분 후 잠깁니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, SleepTimerService::class.java)
            intent.action = SleepTimerService.ACTION_START_TIMER
            startService(intent)

            qsTile.state = Tile.STATE_ACTIVE
        }
        else {  // 실행 취소
            Toast.makeText(this, "예약이 취소되었습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, SleepTimerService::class.java)
            intent.action = SleepTimerService.ACTION_STOP_TIMER
            startService(intent)

            qsTile.state = Tile.STATE_INACTIVE
        }

        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        if(SleepTimerService.isTimerRunning)
            qsTile.state = Tile.STATE_ACTIVE
        else
            qsTile.state = Tile.STATE_INACTIVE

        qsTile.updateTile()
    }


    fun padZero (number: Int): String {
        when(number < 10){
            true -> return "0${number}"
            false -> return "${number}"
        }
    }
}