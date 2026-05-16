package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

class Projectile(
    x: Float,
    y: Float,
    velocityX: Float,
    velocityY: Float,
    damage: Float,
    spriteRadius: Float = 10f,
    lifeTime: Float = 1.5f,
) : GameObject(x, y) {
    private var velocityX: Float = velocityX
    private var velocityY: Float = velocityY
    var damage: Float = damage
        private set
    private var lifeTime: Float = lifeTime
    private var sprite = Sprite(Color.parseColor("#F1FA8C"), spriteRadius)

    val radius: Float
        get() = sprite.radius

    fun reset(
        x: Float,
        y: Float,
        velocityX: Float,
        velocityY: Float,
        damage: Float,
        spriteRadius: Float,
        lifeTime: Float,
    ) {
        // 탄환도 객체를 재사용해 웨이브 후반의 생성 비용을 줄입니다.
        this.x = x
        this.y = y
        this.velocityX = velocityX
        this.velocityY = velocityY
        this.damage = damage
        this.lifeTime = lifeTime
        this.sprite = Sprite(Color.parseColor("#F1FA8C"), spriteRadius)
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
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)
    }
}
