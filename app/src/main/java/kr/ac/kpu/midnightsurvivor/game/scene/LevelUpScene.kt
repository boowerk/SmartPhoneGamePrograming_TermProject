package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene

class LevelUpScene(
    game: MainGame,
    private val options: List<UpgradeOption>,
    private val onSelect: (UpgradeOption) -> Unit,
) : Scene(game) {
    override val isTransparent: Boolean = true

    private val cards = mutableListOf<RectF>()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        cards.clear()
        val margin = 40f
        val cardWidth = width - margin * 2
        val cardHeight = 150f
        val startY = height * 0.26f
        for (index in options.indices) {
            val top = startY + index * (cardHeight + 28f)
            cards += RectF(margin, top, margin + cardWidth, top + cardHeight)
        }
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(180, 7, 10, 20)
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width, height, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        paint.textSize = 58f
        canvas.drawText("LEVEL UP", width * 0.5f, height * 0.16f, paint)

        for (index in options.indices) {
            val option = options[index]
            val card = cards[index]

            paint.color = Color.parseColor("#1F4068")
            canvas.drawRoundRect(card, 24f, 24f, paint)

            paint.color = Color.parseColor("#F1FA8C")
            paint.textSize = 36f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(option.title, card.left + 28f, card.top + 52f, paint)

            paint.color = Color.WHITE
            paint.textSize = 26f
            canvas.drawText(option.description, card.left + 28f, card.top + 102f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        for (index in cards.indices) {
            if (cards[index].contains(event.x, event.y)) {
                onSelect(options[index])
                game.popScene()
                return true
            }
        }
        return true
    }
}
