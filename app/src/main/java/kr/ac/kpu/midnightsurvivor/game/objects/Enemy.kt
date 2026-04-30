package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets

class Enemy(
    x: Float,
    y: Float,
    val type: EnemyType,
    private val moveSpeed: Float,
    private var hp: Float,
    val damage: Float,
    val expReward: Int,
) : GameObject(x, y) {
    private var dashCooldown = 1.6f
    private var dashTime = 0f
    private var dashDirX = 0f
    private var dashDirY = 0f
    private var animationTime = 0f
    private var facingLeft = false

    val radius: Float
        get() = when (type) {
            EnemyType.CHASER -> 20f
            EnemyType.DASHER -> 18f
        }

    override fun update(deltaTime: Float) = Unit

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance <= 0f) return

        when (type) {
            EnemyType.CHASER -> {
                facingLeft = dx < 0f
                x += (dx / distance) * moveSpeed * deltaTime
                y += (dy / distance) * moveSpeed * deltaTime
                animationTime += deltaTime
            }

            EnemyType.DASHER -> {
                if (dashTime > 0f) {
                    x += dashDirX * moveSpeed * 2.8f * deltaTime
                    y += dashDirY * moveSpeed * 2.8f * deltaTime
                    facingLeft = dashDirX < 0f
                    animationTime += deltaTime * 1.8f
                    dashTime -= deltaTime
                    return
                }

                facingLeft = dx < 0f
                x += (dx / distance) * moveSpeed * 0.6f * deltaTime
                y += (dy / distance) * moveSpeed * 0.6f * deltaTime
                animationTime += deltaTime * 0.8f

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
        val frames = when (type) {
            EnemyType.CHASER -> SpriteAssets.goblinRun
            EnemyType.DASHER -> SpriteAssets.impRun
        }
        val frame = frames[((animationTime * 8f).toInt()) % frames.size]
        val size = if (type == EnemyType.CHASER) 48f else 44f
        val dest = RectF(x - size, y - size, x + size, y + size)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(x - 16f, y + 14f, x + 16f, y + 24f, paint)

        if (facingLeft) {
            canvas.save()
            canvas.scale(-1f, 1f, x, y)
            canvas.drawBitmap(frame, null, dest, null)
            canvas.restore()
        } else {
            canvas.drawBitmap(frame, null, dest, null)
        }

        paint.style = Paint.Style.FILL
        paint.color = when {
            type == EnemyType.DASHER && dashTime > 0f -> Color.WHITE
            type == EnemyType.CHASER -> Color.parseColor("#0B1020")
            else -> Color.parseColor("#2A0B0B")
        }
        canvas.drawCircle(x + 4f, y - 14f, 3f, paint)
    }
}
