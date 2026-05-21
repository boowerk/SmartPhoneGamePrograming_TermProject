package kr.ac.kpu.midnightsurvivor.game.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock

enum class GameSfx {
    PLAYER_HIT,
    LEVEL_UP,
    BOSS_ALERT,
    BOSS_CLEAR,
    VICTORY,
    DEFEAT,
}

object GameAudio {
    private var toneGenerator: ToneGenerator? = null
    private val lastPlayedAt = mutableMapOf<GameSfx, Long>()

    fun initialize() {
        if (toneGenerator == null) {
            // 별도 음원 파일이 없어도 발표 빌드에서 반응음을 줄 수 있도록 톤 기반 효과음을 사용합니다.
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        lastPlayedAt.clear()
    }

    fun play(sfx: GameSfx) {
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
