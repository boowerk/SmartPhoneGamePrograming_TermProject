package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene

class TitleScene(game: MainGame) : Scene(game) {
    private val startButton = RectF()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        val buttonWidth = width * 0.58f
        val buttonHeight = 120f
        val left = (width - buttonWidth) * 0.5f
        val top = height * 0.68f
        startButton.set(left, top, left + buttonWidth, top + buttonHeight)
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0B1020"))

        paint.color = Color.parseColor("#F8F8F2")
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 82f
        canvas.drawText("Midnight", width * 0.5f, height * 0.28f, paint)
        canvas.drawText("Survivor", width * 0.5f, height * 0.39f, paint)

        paint.textSize = 36f
        paint.color = Color.parseColor("#8BE9FD")
        canvas.drawText("Kotlin + CustomView Prototype", width * 0.5f, height * 0.48f, paint)

        paint.color = Color.parseColor("#1F4068")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(startButton, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 46f
        canvas.drawText("START", startButton.centerX(), startButton.centerY() + 16f, paint)

        paint.color = Color.parseColor("#C9D1D9")
        paint.textSize = 28f
        canvas.drawText("Drag to move, auto attack to survive", width * 0.5f, height * 0.82f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && startButton.contains(event.x, event.y)) {
            game.replaceScene(MainScene(game))
            return true
        }
        return startButton.contains(event.x, event.y)
    }
}
