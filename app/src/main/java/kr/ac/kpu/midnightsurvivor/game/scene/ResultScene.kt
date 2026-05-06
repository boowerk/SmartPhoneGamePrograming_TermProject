package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene

class ResultScene(
    game: MainGame,
    private val survivedTime: Float,
    private val defeatedEnemies: Int,
    private val reachedLevel: Int,
    private val victory: Boolean,
) : Scene(game) {
    private val restartButton = RectF()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        val buttonWidth = width * 0.6f
        val left = (width - buttonWidth) * 0.5f
        restartButton.set(left, height * 0.72f, left + buttonWidth, height * 0.72f + 110f)
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0F172A"))

        paint.textAlign = Paint.Align.CENTER
        paint.color = if (victory) Color.parseColor("#F1FA8C") else Color.parseColor("#FF6B6B")
        paint.textSize = 72f
        canvas.drawText(if (victory) "SURVIVED" else "DEFEATED", width * 0.5f, height * 0.24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 38f
        canvas.drawText("Time  ${"%.1f".format(survivedTime)} sec", width * 0.5f, height * 0.42f, paint)
        canvas.drawText("Defeated  $defeatedEnemies", width * 0.5f, height * 0.50f, paint)
        canvas.drawText("Level  $reachedLevel", width * 0.5f, height * 0.58f, paint)

        paint.color = Color.parseColor("#1F4068")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(restartButton, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("BACK TO TITLE", restartButton.centerX(), restartButton.centerY() + 14f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && restartButton.contains(event.x, event.y)) {
            game.replaceScene(TitleScene(game))
            return true
        }
        return restartButton.contains(event.x, event.y)
    }
}
