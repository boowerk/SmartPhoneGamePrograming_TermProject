package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

class ExpGem(
    x: Float,
    y: Float,
    val amount: Int = 1,
) : GameObject(x, y) {
    private val sprite = Sprite(Color.parseColor("#50FA7B"), 12f)

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) = Unit

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance in 0f..180f) {
            x += (dx / distance) * 220f * deltaTime
            y += (dy / distance) * 220f * deltaTime
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)
    }
}
