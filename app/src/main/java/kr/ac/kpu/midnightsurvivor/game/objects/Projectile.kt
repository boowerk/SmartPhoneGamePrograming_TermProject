package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

class Projectile(
    x: Float,
    y: Float,
    private val velocityX: Float,
    private val velocityY: Float,
    val damage: Float,
    private val spriteRadius: Float = 10f,
    private var lifeTime: Float = 1.5f,
) : GameObject(x, y) {
    private val sprite = Sprite(Color.parseColor("#F1FA8C"), spriteRadius)

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        lifeTime -= deltaTime
        if (lifeTime <= 0f) {
            isActive = false
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)
    }
}
