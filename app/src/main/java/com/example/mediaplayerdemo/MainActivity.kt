package com.example.mediaplayerdemo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mediaplayerdemo.service.MediaPlayerService
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private lateinit var myBinder: MediaPlayerService.MyBinder

    private val myServiceConnection = MyServiceConnection()

    private val myHandler = MyHandler()

    private lateinit var seekBarThread: SeekBarThread

    private var threadExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPreferences = getSharedPreferences("myData", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putString("name", "小明")
        edit.commit()
        edit.putString("name", "小花")
        edit.apply()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, listOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray(), 1)
        } else {
            bindService(Intent(this, MediaPlayerService::class.java), myServiceConnection, Context.BIND_AUTO_CREATE)
        }

        addClickListenersForMediaPlayer()

        // 监听获取短信验证码
        val smsObserver = SmsObserver(myHandler, this)
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindService(Intent(this, MediaPlayerService::class.java), myServiceConnection, BIND_AUTO_CREATE)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun addClickListenersForMediaPlayer() {
        val playImage = BitmapFactory.decodeResource(resources, R.mipmap.play)
        val pauseImage = BitmapFactory.decodeResource(resources, R.mipmap.pause)

        play_or_pause.setOnClickListener {
            if (myBinder.isPlaying()) {
                play_or_pause.setImageBitmap(playImage)
                myBinder.pause()
            } else {
                play_or_pause.setImageBitmap(pauseImage)
                myBinder.play()
            }
        }

        previous.setOnClickListener {
            play_time.text = "00:00"
            seek_bar.progress = 0
            myBinder.previous()
        }

        next.setOnClickListener {
            seek_bar.progress = 0
            play_time.text = "00:00"
            myBinder.next()
        }
    }

    override fun onDestroy() {
        println("====onDestroy====")
        super.onDestroy()
        threadExit = true
//        myBinder.closeMediaPlayer()
        unbindService(myServiceConnection)
    }

    inner class MyServiceConnection: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("===service connected===")
            myBinder = service as MediaPlayerService.MyBinder
            seek_bar.max = myBinder.getProgress()
            seek_bar.setOnSeekBarChangeListener(MyOnSeekBarChangeListener())
            seekBarThread = SeekBarThread()
            seekBarThread.run()
        }
    }

    inner class MyOnSeekBarChangeListener: OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                if (seekBar != null) {
                    myBinder.seekToPosition(seekBar.progress)
                    play_time.text = SimpleDateFormat("mm:ss").format(seekBar.progress)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    }

    inner class MyHandler: Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                2 -> {
                    if (!threadExit) {
                        seek_bar.progress = msg.obj as Int
                        play_time.text = SimpleDateFormat("mm:ss").format(msg.obj)
                        myHandler.postDelayed(seekBarThread, 800)
                    }
                }
                1 -> {
                    println("msg.obj: ${msg.obj}")
                    validate_code.text = msg.obj as CharSequence
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }


    inner class SeekBarThread: Thread() {
        override fun run() {
            println("===seek bar thread run===")
            if (!threadExit) {
                myHandler.obtainMessage(2, myBinder.getPosition()).sendToTarget()
            }
        }
    }
}
