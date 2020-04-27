package com.example.mediaplayerdemo.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import java.io.File

class MediaPlayerService : Service() {

    private val mediaPlayer = MediaPlayer()
    private lateinit var allMusics: MutableList<String>
    private var playingMusicIndex = 0

    override fun onCreate() {
        println("====onCreate====")
        super.onCreate()
        allMusics = getAllMusicFilePaths()
        if (allMusics.size > 0) {
            initMediaPlayer(playingMusicIndex)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("====onStartCommand====")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        println("====onBind====")
        return MyBinder()
    }

    override fun onDestroy() {
        println("====onDestroy====")
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    inner class MyBinder : Binder() {
        // play status
        fun isPlaying(): Boolean {
            return mediaPlayer.isPlaying
        }

        //play
        fun play() {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }

        //pause
        fun pause() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }

        //next
        fun next() {
            if (playingMusicIndex == allMusics.size - 1) {
                Toast.makeText(applicationContext, "已经到底啦", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer.reset()
                initMediaPlayer(playingMusicIndex + 1)
                playingMusicIndex += 1
                play()
            }
        }

        //previous
        fun previous() {
            if (playingMusicIndex == 0) {
                Toast.makeText(applicationContext, "已经到顶啦", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer.reset()
                initMediaPlayer(playingMusicIndex - 1)
                playingMusicIndex -= 1
                play()
            }
        }

        fun getProgress() : Int {
            return mediaPlayer.duration;
        }

        fun getPosition() : Int {
            return mediaPlayer.currentPosition
        }

        fun seekToPosition(msec : Int) {
            mediaPlayer.seekTo(msec);
        }

    }



    private fun initMediaPlayer(index: Int) {
        mediaPlayer.setDataSource(allMusics[index])
        mediaPlayer.prepare()
    }


    private fun getAllMusicFilePaths(): MutableList<String> {
        var filePaths = mutableListOf<String>()
        // /storage/emulated/0
        val file = File("/storage/emulated/0/MIUI/music/mp3/")
        if (file.listFiles() != null) {
            for (item in file.listFiles()) {
                println("=====${item.absolutePath}")
                filePaths.add(item.absolutePath)
            }
        }
        return filePaths
    }
}
