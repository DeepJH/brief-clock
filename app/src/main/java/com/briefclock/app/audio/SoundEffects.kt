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
import kotlin.math.*

object SoundEffects {

    private const val SAMPLE_RATE = 44100
    private val random = Random(42) // Fixed seed for reproducible crisp textures

    // Pre-synthesized PCM buffers for instant zero-latency playback
    private val gunshotBuffer: ShortArray by lazy { synthesizeGunshot() }
    private val clickBuffer: ShortArray by lazy { synthesizeTriggerClick() }
    private val hammerBuffer: ShortArray by lazy { synthesizeHammerCock() }
    private val cylinderBuffer: ShortArray by lazy { synthesizeCylinderSpin() }

    init {
        // Warm up sound synthesis in background thread upon class load
        thread(start = true, name = "SoundEffectsWarmup") {
            gunshotBuffer
            clickBuffer
            hammerBuffer
            cylinderBuffer
        }
    }

    fun playGunshot(context: Context) {
        vibrateGunshot(context)
        playBufferAsync(gunshotBuffer)
    }

    fun playTriggerClick(context: Context) {
        vibrateClick(context, 35)
        playBufferAsync(clickBuffer)
    }

    fun playHammerCock(context: Context) {
        vibrateClick(context, 25)
        playBufferAsync(hammerBuffer)
    }

    fun playCylinderSpin(context: Context) {
        vibrateClick(context, 15)
        playBufferAsync(cylinderBuffer)
    }

    private fun playBufferAsync(buffer: ShortArray) {
        thread(start = true, name = "SfxPlaybackThread") {
            try {
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
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                val playDurationMs = (buffer.size * 1000L) / SAMPLE_RATE
                Thread.sleep(playDurationMs + 40)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Synthesizes a heavy, cinematic magnum revolver gunshot:
     * - Supersonic shockwave crack transient
     * - Low-pass filtered explosive combustion body
     * - Deep pitch-dropping sub-bass chamber boom
     * - Reverb decay tail with soft-knee saturation
     */
    private fun synthesizeGunshot(): ShortArray {
        val durationMs = 650
        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val raw = FloatArray(totalSamples)

        // Generate raw noise
        val noise = FloatArray(totalSamples) { random.nextFloat() * 2f - 1f }

        // Low-pass filtered noise for combustion rumble (cutoff ~850Hz)
        val lowpass = FloatArray(totalSamples)
        val rc = 1.0 / (2 * PI * 850.0)
        val dt = 1.0 / SAMPLE_RATE
        val alpha = (dt / (rc + dt)).toFloat()
        var y = 0f
        for (i in 0 until totalSamples) {
            y += alpha * (noise[i] - y)
            lowpass[i] = y
        }

        // Sub-bass thump (160Hz -> 38Hz) + Transient Crack + Reverb Comb
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE

            // 1. Initial shockwave crack (first 25ms, sharp attack)
            val crackEnv = exp(-95.0 * t).toFloat()
            val crack = noise[i] * crackEnv * 1.4f

            // 2. Combustion body rumble (exponential decay tau = 20ms)
            val bodyEnv = exp(-22.0 * t).toFloat()
            val body = lowpass[i] * bodyEnv * 2.5f

            // 3. Sub-bass chamber thump: 160Hz down to 38Hz
            val freq = 160.0 * exp(-24.0 * t) + 38.0
            val phase = 2.0 * PI * freq * t
            val thumpEnv = exp(-18.0 * t).toFloat()
            val thump = (sin(phase) * 0.8f + sin(phase * 0.5) * 0.4f).toFloat() * thumpEnv * 1.6f

            // 4. Acoustic room reflection and tail (decaying noise burst)
            val tailEnv = exp(-6.5 * t).toFloat()
            val tail = (noise[i] * 0.35f + lowpass[i] * 0.65f) * tailEnv * 0.7f

            // Sum and apply hyperbolic tangent saturation limiter
            val sum = crack + body + thump + tail
            raw[i] = tanh(1.6f * sum)
        }

        return toShortArray(raw)
    }

    /**
     * Synthesizes a crisp, metallic dry-fire click:
     * - Firing pin metal strike transient (<1.5ms)
     * - Resonant high-Q metallic inharmonic ringing
     * - Receiver frame micro-thud
     */
    private fun synthesizeTriggerClick(): ShortArray {
        val durationMs = 60
        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val raw = FloatArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val attack = (i.toFloat() / (SAMPLE_RATE * 0.0008f)).coerceAtMost(1f)
            val decay = exp(-95.0 * t).toFloat()
            val env = attack * decay

            val ring1 = sin(2.0 * PI * 2250.0 * t).toFloat() * 0.6f
            val ring2 = sin(2.0 * PI * 3600.0 * t).toFloat() * 0.4f
            val ring3 = sin(2.0 * PI * 5200.0 * t).toFloat() * 0.25f
            val thud = sin(2.0 * PI * 130.0 * t).toFloat() * exp(-120.0 * t).toFloat() * 0.5f

            val burst = (random.nextFloat() * 2f - 1f) * exp(-200.0 * t).toFloat() * 0.3f
            val sum = (ring1 + ring2 + ring3 + thud + burst) * env
            raw[i] = tanh(1.5f * sum)
        }

        return toShortArray(raw)
    }

    /**
     * Synthesizes authentic two-stage single-action revolver cocking:
     * - Stage 1: Half-cock cylinder indexing pawl (t=0ms)
     * - Stage 2: Heavy mainspring hammer sear lock (t=45ms)
     */
    private fun synthesizeHammerCock(): ShortArray {
        val durationMs = 130
        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val raw = FloatArray(totalSamples)

        val detent2Start = (SAMPLE_RATE * 0.045f).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sample = 0f

            // Detent 1: pawl engagement
            if (i < detent2Start + 350) {
                val decay1 = exp(-110.0 * t).toFloat()
                sample += sin(2.0 * PI * 1950.0 * t).toFloat() * decay1 * 0.45f
                sample += (random.nextFloat() * 2f - 1f) * decay1 * 0.2f
            }

            // Detent 2: solid hammer sear lock
            if (i >= detent2Start) {
                val t2 = (i - detent2Start).toFloat() / SAMPLE_RATE
                val decay2 = exp(-80.0 * t2).toFloat()
                val ringA = sin(2.0 * PI * 1450.0 * t2).toFloat() * 0.7f
                val ringB = sin(2.0 * PI * 2900.0 * t2).toFloat() * 0.4f
                val thud = sin(2.0 * PI * 110.0 * t2).toFloat() * 0.5f
                val noiseSnap = (random.nextFloat() * 2f - 1f) * exp(-150.0 * t2).toFloat() * 0.4f
                sample += (ringA + ringB + thud + noiseSnap) * decay2 * 1.1f
            }

            raw[i] = tanh(1.6f * sample)
        }

        return toShortArray(raw)
    }

    /**
     * Synthesizes 10 rapid cylinder ratchet clicks with natural rotational deceleration
     */
    private fun synthesizeCylinderSpin(): ShortArray {
        val durationMs = 380
        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val raw = FloatArray(totalSamples)

        // 10 detent click time offsets with progressive deceleration
        val clickCount = 10
        val clickOffsets = IntArray(clickCount)
        var currentOffset = 0f
        var interval = totalSamples.toFloat() / (clickCount * 1.6f)
        for (c in 0 until clickCount) {
            clickOffsets[c] = currentOffset.toInt().coerceAtMost(totalSamples - 1)
            currentOffset += interval
            interval *= 1.12f // Gradual slowdown due to friction
        }

        for (c in 0 until clickCount) {
            val startIdx = clickOffsets[c]
            val clickLength = (SAMPLE_RATE * 0.025f).toInt()
            for (j in 0 until clickLength) {
                val idx = startIdx + j
                if (idx >= totalSamples) break
                val t = j.toFloat() / SAMPLE_RATE
                val decay = exp(-130.0 * t).toFloat()
                val freq = 2100.0 + (c % 3) * 350.0
                val sample = (sin(2.0 * PI * freq * t).toFloat() * 0.7f + (random.nextFloat() * 2f - 1f) * 0.3f) * decay * 0.6f
                raw[idx] += sample
            }
        }

        for (i in 0 until totalSamples) {
            raw[i] = tanh(1.5f * raw[i])
        }

        return toShortArray(raw)
    }

    private fun toShortArray(raw: FloatArray): ShortArray {
        var peak = 1e-6f
        for (v in raw) {
            val absV = abs(v)
            if (absV > peak) peak = absV
        }
        val norm = 0.95f / peak
        val out = ShortArray(raw.size)
        for (i in raw.indices) {
            val clamped = (raw[i] * norm).coerceIn(-1f, 1f)
            out[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun vibrateGunshot(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(320, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(320)
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
