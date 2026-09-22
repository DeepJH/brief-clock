package com.briefclock.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    var isRecording: Boolean = false
        private set

    fun startRecording(outputFile: File): Boolean {
        stopRecording()
        stopPreview()

        return try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }

            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            currentRecordingFile = outputFile
            isRecording = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentRecordingFile
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            currentRecordingFile
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            currentRecordingFile
        }
    }

    fun previewAudio(file: File, onCompletion: () -> Unit = {}): Boolean {
        stopPreview()
        if (!file.exists() || file.length() == 0L) return false

        return try {
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    it.release()
                    player = null
                    onCompletion()
                }
                start()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            releasePlayer()
            false
        }
    }

    fun stopPreview() {
        releasePlayer()
    }

    fun isPlaying(): Boolean {
        return try {
            player?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun release() {
        releaseRecorder()
        releasePlayer()
    }

    private fun releaseRecorder() {
        try {
            recorder?.reset()
            recorder?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            recorder = null
            isRecording = false
        }
    }

    private fun releasePlayer() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            player = null
        }
    }

    companion object {
        fun createNewRecordingFile(context: Context): File {
            val dir = File(context.filesDir, "recordings")
            dir.mkdirs()
            return File(dir, "voice_alarm_${System.currentTimeMillis()}.m4a")
        }
    }
}
