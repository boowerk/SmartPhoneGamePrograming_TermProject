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
    type: EnemyType,
    moveSpeed: Float,
    hp: Float,
    damage: Float,
    expReward: Int,
) : GameObject(x, y) {
    var type: EnemyType = type
        private set
    private var moveSpeed: Float = moveSpeed
    private var hp: Float = hp
    private var maxHp: Float = hp
    var damage: Float = damage
        private set
    var expReward: Int = expReward
        private set
    private var dashCooldown = 1.6f
    private var dashTime = 0f
    private var dashDirX = 0f
    private var dashDirY = 0f
    private var animationTime = 0f
    private var facingLeft = false
    private var strafeDirection = 1f
    private var rangedShotCooldown = 1.4f

    val radius: Float
        get() = when (type) {
            EnemyType.CHASER -> 20f
            EnemyType.DASHER -> 18f
            EnemyType.TANK -> 28f
            EnemyType.RANGER -> 22f
        }

    override fun update(deltaTime: Float) = Unit

    fun reset(
        x: Float,
        y: Float,
        type: EnemyType,
        moveSpeed: Float,
        hp: Float,
        damage: Float,
        expReward: Int,
    ) {
        // 재사용 시 타입별 상태를 함께 초기화해 풀링된 적이 이전 행동을 끌고 오지 않게 합니다.
        this.x = x
        this.y = y
        this.type = type
        this.moveSpeed = moveSpeed
        this.hp = hp
        this.maxHp = hp
        this.damage = damage
        this.expReward = expReward
        dashCooldown = 1.6f
        dashTime = 0f
        dashDirX = 0f
        dashDirY = 0f
        animationTime = 0f
        facingLeft = false
        strafeDirection = 1f
        rangedShotCooldown = 1.4f
        isActive = true
    }

    fun updateToward(targetX: Float, targetY: Float, deltaTime: Float): EnemyShot? {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance <= 0f) return null

        var shot: EnemyShot? = null

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

            EnemyType.TANK -> {
                // 탱커는 느리지만 꾸준히 압박하도록 단순 추적에 체급만 키웁니다.
                facingLeft = dx < 0f
                x += (dx / distance) * moveSpeed * deltaTime
                y += (dy / distance) * moveSpeed * deltaTime
                animationTime += deltaTime * 0.55f
            }

            EnemyType.RANGER -> {
                // 원거리 적은 거리를 유지하며 측면 이동으로 탄막 준비 자세를 만듭니다.
                facingLeft = dx < 0f
                val dirX = dx / distance
                val dirY = dy / distance
                val tangentX = -dirY * strafeDirection
                val tangentY = dirX * strafeDirection
                val chaseWeight = when {
                    distance > 360f -> 0.85f
                    distance < 220f -> -0.65f
                    else -> 0.08f
                }
                x += (dirX * chaseWeight + tangentX * 0.55f) * moveSpeed * deltaTime
                y += (dirY * chaseWeight + tangentY * 0.55f) * moveSpeed * deltaTime
                animationTime += deltaTime * 0.9f

                rangedShotCooldown -= deltaTime
                if (distance < 520f && rangedShotCooldown <= 0f) {
                    shot = EnemyShot(
                        x = x,
                        y = y - 6f,
                        velocityX = dirX * 230f,
                        velocityY = dirY * 230f,
                        damage = 9f,
                        radius = 9f,
                    )
                    rangedShotCooldown = if (distance < 260f) 1.15f else 1.55f
                }
                if (distance < 180f || distance > 420f) {
                    strafeDirection *= -1f
                }
            }
        }
        return shot
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
            EnemyType.CHASER,
            EnemyType.TANK,
            -> SpriteAssets.goblinRun

            EnemyType.DASHER,
            EnemyType.RANGER,
            -> SpriteAssets.impRun
        }
        val frame = frames[((animationTime * 8f).toInt()) % frames.size]
        val size = when (type) {
            EnemyType.CHASER -> 48f
            EnemyType.DASHER -> 44f
            EnemyType.TANK -> 64f
            EnemyType.RANGER -> 50f
        }
        val dest = RectF(x - size, y - size, x + size, y + size)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(x - radius * 0.9f, y + radius * 0.6f, x + radius * 0.9f, y + radius * 1.2f, paint)

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
            type == EnemyType.TANK -> Color.parseColor("#5C2E0D")
            type == EnemyType.RANGER -> Color.parseColor("#6C1F7A")
            else -> Color.parseColor("#2A0B0B")
        }
        canvas.drawCircle(x + 4f, y - 14f, 3f, paint)

        if (type == EnemyType.TANK) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.parseColor("#F6C85F")
            canvas.drawCircle(x, y - 2f, radius + 6f, paint)
        } else if (type == EnemyType.RANGER) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#BD93F9")
            canvas.drawCircle(x, y - 4f, radius + 8f, paint)
        }
    }

    fun hpRatio(): Float {
        return if (maxHp <= 0f) 0f else (hp / maxHp).coerceIn(0f, 1f)
    }
}
