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
    amount: Int = 1,
) : GameObject(x, y) {
    var amount: Int = amount
        private set
    private val sprite = Sprite(Color.parseColor("#50FA7B"), 12f)

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) = Unit

    fun reset(x: Float, y: Float, amount: Int) {
        // 경험치 오브는 수량만 바뀌므로 좌표와 보상량만 되살려 재사용합니다.
        this.x = x
        this.y = y
        this.amount = amount
        isActive = true
    }

    fun updateToward(targetX: Float, targetY: Float, attractRadius: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance in 0f..attractRadius) {
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
