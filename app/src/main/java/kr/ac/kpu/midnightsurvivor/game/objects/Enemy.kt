package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

class Enemy(
    x: Float,
    y: Float,
    private val moveSpeed: Float,
    private var hp: Float,
    val damage: Float,
) : GameObject(x, y) {
    private val sprite = Sprite(Color.parseColor("#FF6B6B"), 26f)

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) = Unit

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance <= 0f) return
        x += (dx / distance) * moveSpeed * deltaTime
        y += (dy / distance) * moveSpeed * deltaTime
    }

    fun hit(power: Float): Boolean {
        hp -= power
        if (hp <= 0f) {
            isActive = false
            return true
        }
        return false
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)
        paint.color = Color.parseColor("#5C1D1D")
        canvas.drawCircle(x + 5f, y - 6f, 5f, paint)
    }
}
