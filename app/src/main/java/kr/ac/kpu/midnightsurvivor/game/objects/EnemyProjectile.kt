package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject

data class EnemyShot(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val damage: Float,
    val radius: Float,
)

class EnemyProjectile(
    x: Float,
    y: Float,
    velocityX: Float,
    velocityY: Float,
    damage: Float,
    radius: Float = 10f,
    lifeTime: Float = 3.2f,
) : GameObject(x, y) {
    private var velocityX: Float = velocityX
    private var velocityY: Float = velocityY
    var damage: Float = damage
        private set
    private var radiusValue: Float = radius
    private var lifeTime: Float = lifeTime

    val radius: Float
        get() = radiusValue

    fun reset(
        x: Float,
        y: Float,
        velocityX: Float,
        velocityY: Float,
        damage: Float,
        radius: Float,
        lifeTime: Float,
    ) {
        // 적 탄환도 풀링해 원거리 적과 보스 패턴이 많아져도 프레임 드랍을 줄입니다.
        this.x = x
        this.y = y
        this.velocityX = velocityX
        this.velocityY = velocityY
        this.damage = damage
        this.radiusValue = radius
        this.lifeTime = lifeTime
        isActive = true
    }

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
        paint.color = Color.parseColor("#FF7A90")
        canvas.drawCircle(x, y, radiusValue, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.parseColor("#5B0E1E")
        canvas.drawCircle(x, y, radiusValue, paint)
    }
}
