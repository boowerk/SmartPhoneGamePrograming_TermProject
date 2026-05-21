package kr.ac.kpu.midnightsurvivor.game

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import kr.ac.kpu.midnightsurvivor.game.audio.GameAudio
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame

class GameView(context: Context) : View(context) {
    private val game = MainGame()
    private var lastFrameNanos = 0L

    init {
        SpriteAssets.initialize(resources)
        GameAudio.initialize()
        isFocusable = true
        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        game.onResize(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = System.nanoTime()
        val deltaTime = if (lastFrameNanos == 0L) {
            1f / 60f
        } else {
            ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.033f)
        }
        lastFrameNanos = now

        game.update(deltaTime)
        game.draw(canvas)
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return game.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        // View가 내려갈 때 톤 제너레이터를 정리해 재진입 시 중복 점유를 피합니다.
        GameAudio.release()
        super.onDetachedFromWindow()
    }
}
