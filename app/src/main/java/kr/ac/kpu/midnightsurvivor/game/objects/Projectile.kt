package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.hypot
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
    bitmap: Bitmap? = null,
    rotationDegrees: Float = 0f,
    rotationSpeed: Float = 0f,
    remainingHits: Int = 1,
) : GameObject(x, y) {
    private var velocityX: Float = velocityX
    private var velocityY: Float = velocityY
    var damage: Float = damage
        private set
    private var lifeTime: Float = lifeTime
    private var sprite = Sprite(Color.parseColor("#F1FA8C"), spriteRadius)
    private var bitmap: Bitmap? = bitmap
    private var rotationDegrees: Float = rotationDegrees
    private var rotationSpeed: Float = rotationSpeed
    private var remainingHits: Int = remainingHits

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
        bitmap: Bitmap? = null,
        rotationDegrees: Float = 0f,
        rotationSpeed: Float = 0f,
        remainingHits: Int = 1,
    ) {
        // Pool-reset keeps the expanding weapon roster from creating extra GC churn.
        this.x = x
        this.y = y
        this.velocityX = velocityX
        this.velocityY = velocityY
        this.damage = damage
        this.lifeTime = lifeTime
        this.sprite = Sprite(Color.parseColor("#F1FA8C"), spriteRadius)
        this.bitmap = bitmap
        this.rotationDegrees = rotationDegrees
        this.rotationSpeed = rotationSpeed
        this.remainingHits = remainingHits.coerceAtLeast(1)
        isActive = true
    }

    override fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        rotationDegrees += rotationSpeed * deltaTime
        lifeTime -= deltaTime
        if (lifeTime <= 0f) {
            isActive = false
        }
    }

    fun registerHit() {
        remainingHits -= 1
        if (remainingHits <= 0) {
            isActive = false
        }
    }

    fun skipAhead(distance: Float) {
        val speed = hypot(velocityX, velocityY)
        if (speed <= 0f) return
        x += (velocityX / speed) * distance
        y += (velocityY / speed) * distance
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val activeBitmap = bitmap
        if (activeBitmap != null) {
            val dest = RectF(x - sprite.radius, y - sprite.radius, x + sprite.radius, y + sprite.radius)
            canvas.save()
            canvas.rotate(rotationDegrees, x, y)
            canvas.drawBitmap(activeBitmap, null, dest, null)
            canvas.restore()
            return
        }

        paint.style = Paint.Style.FILL
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)
    }
}
