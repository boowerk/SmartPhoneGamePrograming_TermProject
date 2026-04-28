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
    val type: EnemyType,
    private val moveSpeed: Float,
    private var hp: Float,
    val damage: Float,
    val expReward: Int,
) : GameObject(x, y) {
    private val sprite = Sprite(
        color = when (type) {
            EnemyType.CHASER -> Color.parseColor("#FF6B6B")
            EnemyType.DASHER -> Color.parseColor("#FF9E64")
        },
        radius = when (type) {
            EnemyType.CHASER -> 26f
            EnemyType.DASHER -> 22f
        },
    )
    private var dashCooldown = 1.6f
    private var dashTime = 0f
    private var dashDirX = 0f
    private var dashDirY = 0f

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) = Unit

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance <= 0f) return

        when (type) {
            EnemyType.CHASER -> {
                x += (dx / distance) * moveSpeed * deltaTime
                y += (dy / distance) * moveSpeed * deltaTime
            }

            EnemyType.DASHER -> {
                if (dashTime > 0f) {
                    x += dashDirX * moveSpeed * 2.8f * deltaTime
                    y += dashDirY * moveSpeed * 2.8f * deltaTime
                    dashTime -= deltaTime
                    return
                }

                x += (dx / distance) * moveSpeed * 0.6f * deltaTime
                y += (dy / distance) * moveSpeed * 0.6f * deltaTime

                dashCooldown -= deltaTime
                if (dashCooldown <= 0f && distance < 420f) {
                    dashDirX = dx / distance
                    dashDirY = dy / distance
                    dashTime = 0.25f
                    dashCooldown = 1.9f
                }
            }
        }
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

        paint.color = when (type) {
            EnemyType.CHASER -> Color.parseColor("#5C1D1D")
            EnemyType.DASHER -> if (dashTime > 0f) Color.WHITE else Color.parseColor("#7A3415")
        }
        canvas.drawCircle(x + 5f, y - 6f, 5f, paint)
    }
}
