package kr.ac.kpu.midnightsurvivor.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.SystemClock
import kr.ac.kpu.midnightsurvivor.R

enum class GameSfx {
    PLAYER_HIT,
    LEVEL_UP,
    BOSS_ALERT,
    BOSS_CLEAR,
    VICTORY,
    DEFEAT,
    PICKUP_EXP,
    ATTACK,
    BUTTON,
}

object GameAudio {
    private var toneGenerator: ToneGenerator? = null
    private var soundPool: SoundPool? = null
    private var bgmPlayer: MediaPlayer? = null
    private val lastPlayedAt = mutableMapOf<GameSfx, Long>()
    private val loadedSamples = mutableSetOf<Int>()
    private val sampleIds = mutableMapOf<GameSfx, Int>()

    fun initialize(context: Context) {
        if (toneGenerator == null) {
            // Legacy synth cues stay in place so the existing combat/result feedback keeps working.
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        }
        if (soundPool == null) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(12)
                .build()
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSamples += sampleId
                }
            }
            val appContext = context.applicationContext
            sampleIds[GameSfx.PICKUP_EXP] = soundPool?.load(appContext, R.raw.fall, 1) ?: 0
            sampleIds[GameSfx.ATTACK] = soundPool?.load(appContext, R.raw.arrow_sound, 1) ?: 0
            sampleIds[GameSfx.BUTTON] = soundPool?.load(appContext, R.raw.button_sound, 1) ?: 0
        }
        if (bgmPlayer == null) {
            // The BGM is started once and looped across scenes so the run feels continuous.
            bgmPlayer = MediaPlayer.create(context.applicationContext, R.raw.blood_arcade)?.apply {
                isLooping = true
                setVolume(0.30f, 0.30f)
                start()
            }
        } else if (bgmPlayer?.isPlaying == false) {
            bgmPlayer?.start()
        }
    }

    fun release() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
        soundPool?.release()
        soundPool = null
        toneGenerator?.release()
        toneGenerator = null
        lastPlayedAt.clear()
        loadedSamples.clear()
        sampleIds.clear()
    }

    fun play(sfx: GameSfx) {
        when (sfx) {
            GameSfx.PICKUP_EXP,
            GameSfx.ATTACK,
            GameSfx.BUTTON,
            -> playSample(sfx)

            else -> playTone(sfx)
        }
    }

    private fun playSample(sfx: GameSfx) {
        val pool = soundPool ?: return
        val sampleId = sampleIds[sfx] ?: return
        if (sampleId == 0 || sampleId !in loadedSamples) return

        val volume = when (sfx) {
            GameSfx.PICKUP_EXP -> 0.65f
            GameSfx.ATTACK -> 0.55f
            GameSfx.BUTTON -> 0.70f
            else -> 0.60f
        }
        pool.play(sampleId, volume, volume, 1, 0, 1f)
    }

    private fun playTone(sfx: GameSfx) {
        val generator = toneGenerator ?: return
        val now = SystemClock.elapsedRealtime()
        val minGap = when (sfx) {
            GameSfx.PLAYER_HIT -> 150L
            else -> 0L
        }
        val lastTime = lastPlayedAt[sfx] ?: Long.MIN_VALUE
        if (now - lastTime < minGap) return

        val tone = when (sfx) {
            GameSfx.PLAYER_HIT -> ToneGenerator.TONE_PROP_NACK
            GameSfx.LEVEL_UP -> ToneGenerator.TONE_PROP_ACK
            GameSfx.BOSS_ALERT -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            GameSfx.BOSS_CLEAR -> ToneGenerator.TONE_CDMA_ONE_MIN_BEEP
            GameSfx.VICTORY -> ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP
            GameSfx.DEFEAT -> ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE
            else -> return
        }
        val duration = when (sfx) {
            GameSfx.BOSS_ALERT -> 450
            GameSfx.VICTORY -> 420
            GameSfx.DEFEAT -> 380
            else -> 180
        }
        generator.startTone(tone, duration)
        lastPlayedAt[sfx] = now
    }
}
