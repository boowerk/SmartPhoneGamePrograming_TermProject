package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

class Player(
    x: Float,
    y: Float,
) : GameObject(x, y) {
    private val sprite = Sprite(Color.parseColor("#8BE9FD"), 32f)
    private var moveX = 0f
    private var moveY = 0f
    private var hitCooldown = 0f
    var moveSpeed = 260f
    var maxHp = 100f
    var hp = 100f
    var level = 1
        private set
    var attackPower = 1
    private var exp = 0

    val radius: Float
        get() = sprite.radius

    fun setMoveVector(dx: Float, dy: Float) {
        moveX = dx
        moveY = dy
    }

    override fun update(deltaTime: Float) {
        val length = hypot(moveX, moveY)
        if (length > 0f) {
            x += (moveX / length) * moveSpeed * deltaTime
            y += (moveY / length) * moveSpeed * deltaTime
        }
        if (hitCooldown > 0f) {
            hitCooldown -= deltaTime
        }
    }

    fun clampToBounds(width: Float, height: Float) {
        x = x.coerceIn(radius, width - radius)
        y = y.coerceIn(radius, height - radius)
    }

    fun takeDamage(amount: Float) {
        if (hitCooldown > 0f) return
        hp = (hp - amount).coerceAtLeast(0f)
        hitCooldown = 0.4f
    }

    fun gainExp(amount: Int): Boolean {
        exp += amount
        val nextLevelExp = 4 + (level - 1) * 2
        if (exp >= nextLevelExp) {
            exp -= nextLevelExp
            level += 1
            return true
        }
        return false
    }

    fun heal(amount: Float) {
        hp = (hp + amount).coerceAtMost(maxHp)
    }

    fun expRatio(): Float {
        val nextLevelExp = 4 + (level - 1) * 2
        return exp.toFloat() / nextLevelExp.toFloat()
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = if (hitCooldown > 0f) Color.WHITE else sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)

        paint.color = Color.parseColor("#1B1F2A")
        canvas.drawCircle(x + 8f, y - 8f, 6f, paint)
    }
}
