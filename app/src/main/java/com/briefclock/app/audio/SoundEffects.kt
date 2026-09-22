package com.briefclock.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundEffects {

    private const val SAMPLE_RATE = 44100
    private val random = Random()

    fun playGunshot(context: Context) {
        vibrateGunshot(context)
        thread(start = true, name = "GunshotAudioThread") {
            try {
                val durationMs = 700
                val totalSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(totalSamples)

                // Procedural Gunshot synthesis:
                // 1. Transient burst: First 15ms high amplitude white noise
                // 2. Low-frequency punch: 100Hz -> 30Hz exponential pitch drop
                // 3. Body & Reverb decay tail
                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val progress = i.toFloat() / totalSamples

                    // Fast envelope: sharp attack (0-3ms), then exponential decay
                    val attack = (i.toFloat() / (SAMPLE_RATE * 0.003f)).coerceAtMost(1f)
                    val decay = exp(-7.0 * t).toFloat()
                    val envelope = attack * decay

                    // Low-frequency explosion body
                    val freq = 110f * exp(-15.0 * t).toFloat() + 35f
                    val lowPunch = sin(2.0 * PI * freq * t).toFloat()

                    // White noise blast (filtered)
                    val noise = (random.nextFloat() * 2f - 1f)

                    // Combine punch (60%) + noise blast (40%)
                    val sampleVal = (lowPunch * 0.65f + noise * 0.35f) * envelope

                    // Hard limiter / clipping saturation for punchy gun impact
                    val saturated = (sampleVal * 1.8f).coerceIn(-1f, 1f)
                    buffer[i] = (saturated * Short.MAX_VALUE * 0.95f).toInt().toShort()
                }

                playBuffer(buffer, SAMPLE_RATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTriggerClick(context: Context) {
        vibrateClick(context, 40)
        thread(start = true, name = "ClickAudioThread") {
            try {
                val durationMs = 50
                val totalSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(totalSamples)

                // Procedural empty chamber metallic click:
                // High Q resonant ring at 2800 Hz and 1350 Hz with very fast decay
                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val attack = (i.toFloat() / (SAMPLE_RATE * 0.001f)).coerceAtMost(1f)
                    val decay = exp(-80.0 * t).toFloat()
                    val env = attack * decay

                    val wave1 = sin(2.0 * PI * 2800.0 * t).toFloat()
                    val wave2 = sin(2.0 * PI * 1350.0 * t).toFloat()
                    val click = (wave1 * 0.6f + wave2 * 0.4f) * env

                    buffer[i] = (click * Short.MAX_VALUE * 0.85f).toInt().toShort()
                }

                playBuffer(buffer, SAMPLE_RATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playHammerCock(context: Context) {
        vibrateClick(context, 25)
        thread(start = true, name = "HammerCockAudioThread") {
            try {
                val durationMs = 120
                val totalSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(totalSamples)

                // Double detent ratchet click: first click at 0ms, second heavier click at 55ms
                val click2Start = (SAMPLE_RATE * 0.055f).toInt()

                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE

                    var sample = 0f
                    // Click 1
                    if (i < click2Start + 300) {
                        val decay1 = exp(-90.0 * t).toFloat()
                        sample += sin(2.0 * PI * 2200.0 * t).toFloat() * decay1 * 0.5f
                    }
                    // Click 2 (heavier lock)
                    if (i >= click2Start) {
                        val t2 = (i - click2Start).toFloat() / SAMPLE_RATE
                        val decay2 = exp(-75.0 * t2).toFloat()
                        sample += sin(2.0 * PI * 1800.0 * t2).toFloat() * decay2 * 0.8f
                    }

                    buffer[i] = (sample * Short.MAX_VALUE * 0.85f).toInt().toShort()
                }

                playBuffer(buffer, SAMPLE_RATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playCylinderSpin(context: Context) {
        vibrateClick(context, 15)
        thread(start = true, name = "CylinderSpinAudioThread") {
            try {
                val durationMs = 350
                val totalSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(totalSamples)

                // 6 consecutive cylinder ratchet clicks
                val clickInterval = totalSamples / 6
                for (clickIndex in 0 until 6) {
                    val start = clickIndex * clickInterval
                    for (i in 0 until (clickInterval.coerceAtMost(totalSamples - start))) {
                        val idx = start + i
                        val t = i.toFloat() / SAMPLE_RATE
                        val decay = exp(-120.0 * t).toFloat()
                        val freq = 2400.0 + (clickIndex % 2) * 400.0
                        val valSample = sin(2.0 * PI * freq * t).toFloat() * decay * 0.6f
                        buffer[idx] = (valSample * Short.MAX_VALUE * 0.7f).toInt().toShort()
                    }
                }

                playBuffer(buffer, SAMPLE_RATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        // Wait for playback and release
        val playDuration = (buffer.size * 1000L) / sampleRate
        Thread.sleep(playDuration + 50)
        audioTrack.stop()
        audioTrack.release()
    }

    private fun vibrateGunshot(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(350)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateClick(context: Context, ms: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
