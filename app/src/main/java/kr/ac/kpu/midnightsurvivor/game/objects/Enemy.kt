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
            EnemyType.SKELETON -> 19f
            EnemyType.SHAMAN -> 24f
            EnemyType.OGRE -> 34f
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
        // Reset every behavior timer when reusing pooled enemies so each spawn starts cleanly.
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
        val dirX = dx / distance
        val dirY = dy / distance
        val tangentX = -dirY * strafeDirection
        val tangentY = dirX * strafeDirection

        when (type) {
            EnemyType.CHASER -> {
                facingLeft = dx < 0f
                x += dirX * moveSpeed * deltaTime
                y += dirY * moveSpeed * deltaTime
                animationTime += deltaTime
            }

            EnemyType.DASHER -> {
                if (dashTime > 0f) {
                    x += dashDirX * moveSpeed * 2.8f * deltaTime
                    y += dashDirY * moveSpeed * 2.8f * deltaTime
                    facingLeft = dashDirX < 0f
                    animationTime += deltaTime * 1.8f
                    dashTime -= deltaTime
                    return null
                }

                facingLeft = dx < 0f
                x += dirX * moveSpeed * 0.6f * deltaTime
                y += dirY * moveSpeed * 0.6f * deltaTime
                animationTime += deltaTime * 0.8f

                dashCooldown -= deltaTime
                if (dashCooldown <= 0f && distance < 420f) {
                    dashDirX = dirX
                    dashDirY = dirY
                    dashTime = 0.25f
                    dashCooldown = 1.9f
                }
            }

            EnemyType.TANK -> {
                // Tanks stay simple and relentless so they act as readable wall pressure.
                facingLeft = dx < 0f
                x += dirX * moveSpeed * deltaTime
                y += dirY * moveSpeed * deltaTime
                animationTime += deltaTime * 0.55f
            }

            EnemyType.RANGER -> {
                // Rangers orbit at mid range and poke the player to force movement.
                facingLeft = dx < 0f
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

            EnemyType.SKELETON -> {
                // Skeletons weave around the player a bit so the larger horde feels less linear.
                facingLeft = dx < 0f
                val chaseWeight = if (distance > 260f) 0.95f else 0.48f
                x += (dirX * chaseWeight + tangentX * 0.42f) * moveSpeed * deltaTime
                y += (dirY * chaseWeight + tangentY * 0.42f) * moveSpeed * deltaTime
                animationTime += deltaTime * 1.2f
                if (distance < 140f || distance > 330f) {
                    strafeDirection *= -1f
                }
            }

            EnemyType.SHAMAN -> {
                // Shamans hold a lane and add ranged pressure distinct from the lighter ranger.
                facingLeft = dx < 0f
                val chaseWeight = when {
                    distance > 420f -> 0.88f
                    distance < 240f -> -0.78f
                    else -> 0.03f
                }
                x += (dirX * chaseWeight + tangentX * 0.28f) * moveSpeed * deltaTime
                y += (dirY * chaseWeight + tangentY * 0.28f) * moveSpeed * deltaTime
                animationTime += deltaTime * 0.78f

                rangedShotCooldown -= deltaTime
                if (distance < 560f && rangedShotCooldown <= 0f) {
                    shot = EnemyShot(
                        x = x,
                        y = y - 10f,
                        velocityX = dirX * 255f,
                        velocityY = dirY * 255f,
                        damage = 12f,
                        radius = 11f,
                    )
                    rangedShotCooldown = if (distance < 300f) 1.0f else 1.35f
                }
                if (distance < 210f || distance > 440f) {
                    strafeDirection *= -1f
                }
            }

            EnemyType.OGRE -> {
                if (dashTime > 0f) {
                    x += dashDirX * moveSpeed * 2.1f * deltaTime
                    y += dashDirY * moveSpeed * 2.1f * deltaTime
                    facingLeft = dashDirX < 0f
                    animationTime += deltaTime * 0.85f
                    dashTime -= deltaTime
                    return null
                }

                facingLeft = dx < 0f
                x += dirX * moveSpeed * 0.74f * deltaTime
                y += dirY * moveSpeed * 0.74f * deltaTime
                animationTime += deltaTime * 0.5f

                dashCooldown -= deltaTime
                if (dashCooldown <= 0f && distance < 320f) {
                    dashDirX = dirX
                    dashDirY = dirY
                    dashTime = 0.40f
                    dashCooldown = 2.7f
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

            EnemyType.SKELETON -> SpriteAssets.skeletonRun
            EnemyType.SHAMAN -> SpriteAssets.shamanRun
            EnemyType.OGRE -> SpriteAssets.ogreRun
        }
        val frame = frames[((animationTime * 8f).toInt()) % frames.size]
        val size = when (type) {
            EnemyType.CHASER -> 48f
            EnemyType.DASHER -> 44f
            EnemyType.TANK -> 64f
            EnemyType.RANGER -> 50f
            EnemyType.SKELETON -> 50f
            EnemyType.SHAMAN -> 62f
            EnemyType.OGRE -> 86f
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
            type == EnemyType.SKELETON -> Color.parseColor("#D9E2EC")
            type == EnemyType.SHAMAN -> Color.parseColor("#8BE9FD")
            type == EnemyType.OGRE -> Color.parseColor("#FFB86C")
            else -> Color.parseColor("#2A0B0B")
        }
        canvas.drawCircle(x + 4f, y - 14f, 3f, paint)

        when (type) {
            EnemyType.TANK -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                paint.color = Color.parseColor("#F6C85F")
                canvas.drawCircle(x, y - 2f, radius + 6f, paint)
            }

            EnemyType.RANGER,
            EnemyType.SHAMAN,
            -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = if (type == EnemyType.SHAMAN) {
                    Color.parseColor("#8BE9FD")
                } else {
                    Color.parseColor("#BD93F9")
                }
                canvas.drawCircle(x, y - 4f, radius + 8f, paint)
            }

            EnemyType.OGRE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                paint.color = Color.parseColor("#7C4D12")
                canvas.drawCircle(x, y + 4f, radius + 4f, paint)
            }

            else -> Unit
        }
    }

    fun hpRatio(): Float {
        return if (maxHp <= 0f) 0f else (hp / maxHp).coerceIn(0f, 1f)
    }
}
