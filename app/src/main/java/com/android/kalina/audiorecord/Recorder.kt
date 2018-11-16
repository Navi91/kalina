package com.android.kalina.audiorecord

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import java.io.File

/**
 * Created by Dmitriy on 08.02.2018.
 */
class Recorder(val context: Context, val fileName: String, val minimumTimeMilliseconds: Long) {

    private val mediaRecorder = MediaRecorder()
    private var time: Long = 0
    private var run = false

    fun startRecord() {
        val fileName = createFileName()

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        mediaRecorder.setOutputFile(fileName)
        mediaRecorder.setAudioEncodingBitRate(44100);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.prepare()
        mediaRecorder.start()

        run = true
    }

    fun stopRecord() {
        if (run) {
            run = false
            try {
                mediaRecorder.stop()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            mediaRecorder.release()
        }
    }

    fun isRun() = run

    fun getAudioFile(): File? {
        val file = File(createFileName())
        if (time > minimumTimeMilliseconds) {
            return file
        }

        return null
    }

    private fun createFileName() = AudioHelper.createFileName(context, fileName)

    fun setTime(time: Long) {
        this.time = time
    }
}