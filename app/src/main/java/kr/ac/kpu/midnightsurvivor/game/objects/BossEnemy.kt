package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets

data class BossAction(
    val shots: List<EnemyShot>,
    val summonCount: Int,
)

class BossEnemy(
    x: Float,
    y: Float,
) : GameObject(x, y) {
    private var hp = 320f
    private var maxHp = 320f
    private var attackTimer = 1.8f
    private var volleyTimer = 0.8f
    private var summonTimer = 5.8f
    private var animationTime = 0f
    private var orbitDirection = 1f
    private var facingLeft = false

    val radius: Float
        get() = 56f

    fun reset(x: Float, y: Float) {
        // 보스전은 한 번뿐이지만 재시작 시 상태가 섞이지 않도록 명시적으로 초기화합니다.
        this.x = x
        this.y = y
        hp = 320f
        maxHp = 320f
        attackTimer = 1.8f
        volleyTimer = 0.8f
        summonTimer = 5.8f
        animationTime = 0f
        orbitDirection = 1f
        facingLeft = false
        isActive = true
    }

    override fun update(deltaTime: Float) = Unit

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float): BossAction {
        val shots = mutableListOf<EnemyShot>()
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy).coerceAtLeast(1f)
        val dirX = dx / distance
        val dirY = dy / distance
        val tangentX = -dirY * orbitDirection
        val tangentY = dirX * orbitDirection
        val enraged = hpRatio() < 0.5f
        val speedScale = if (enraged) 1.22f else 1f

        // 보스는 플레이어 주위를 감싸듯 움직이며 거리 유지와 측면 압박을 동시에 합니다.
        val chaseWeight = when {
            distance > 260f -> 0.92f
            distance < 170f -> -0.62f
            else -> 0.10f
        }
        x += (dirX * chaseWeight + tangentX * 0.48f) * 94f * speedScale * deltaTime
        y += (dirY * chaseWeight + tangentY * 0.48f) * 94f * speedScale * deltaTime
        facingLeft = dx < 0f
        animationTime += deltaTime * speedScale

        attackTimer -= deltaTime
        volleyTimer -= deltaTime
        summonTimer -= deltaTime

        if (attackTimer <= 0f) {
            val ringCount = if (enraged) 12 else 8
            val ringSpeed = if (enraged) 240f else 200f
            repeat(ringCount) { index ->
                val angle = animationTime * 0.9f + (Math.PI * 2.0 * index / ringCount).toFloat()
                shots += EnemyShot(
                    x = x,
                    y = y - 8f,
                    velocityX = cos(angle) * ringSpeed,
                    velocityY = sin(angle) * ringSpeed,
                    damage = if (enraged) 15f else 12f,
                    radius = 12f,
                )
            }
            attackTimer = if (enraged) 1.95f else 2.35f
        }

        if (volleyTimer <= 0f) {
            val baseAngle = atan2(dy, dx)
            val offsets = if (enraged) listOf(-0.28f, -0.1f, 0.1f, 0.28f) else listOf(-0.22f, 0f, 0.22f)
            offsets.forEach { offset ->
                val shotAngle = baseAngle + offset
                shots += EnemyShot(
                    x = x,
                    y = y - 10f,
                    velocityX = cos(shotAngle) * 280f,
                    velocityY = sin(shotAngle) * 280f,
                    damage = if (enraged) 13f else 10f,
                    radius = 10f,
                )
            }
            volleyTimer = if (enraged) 1.05f else 1.45f
        }

        val summonCount = if (summonTimer <= 0f) {
            summonTimer = if (enraged) 5.2f else 6.6f
            if (enraged) 2 else 1
        } else {
            0
        }

        if (distance < 120f || distance > 360f) {
            orbitDirection *= -1f
        }
        return BossAction(shots, summonCount)
    }

    fun hit(damage: Float): Boolean {
        hp -= damage
        if (hp <= 0f) {
            isActive = false
            return true
        }
        return false
    }

    fun hpRatio(): Float {
        return if (maxHp <= 0f) 0f else (hp / maxHp).coerceIn(0f, 1f)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val frame = SpriteAssets.impRun[((animationTime * 7f).toInt()) % SpriteAssets.impRun.size]
        val dest = RectF(x - 90f, y - 90f, x + 90f, y + 90f)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(90, 0, 0, 0)
        canvas.drawOval(x - 44f, y + 34f, x + 44f, y + 56f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = Color.parseColor("#FF7A90")
        canvas.drawCircle(x, y, radius + 14f, paint)

        if (facingLeft) {
            canvas.save()
            canvas.scale(-1f, 1f, x, y)
            canvas.drawBitmap(frame, null, dest, null)
            canvas.restore()
        } else {
            canvas.drawBitmap(frame, null, dest, null)
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFF2C9")
        canvas.drawCircle(x - 16f, y - 18f, 6f, paint)
        canvas.drawCircle(x + 16f, y - 18f, 6f, paint)
    }
}
