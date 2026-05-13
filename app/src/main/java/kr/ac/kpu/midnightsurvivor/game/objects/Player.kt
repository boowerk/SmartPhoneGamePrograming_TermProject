package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets

class Player(
    x: Float,
    y: Float,
) : GameObject(x, y) {
    private var moveX = 0f
    private var moveY = 0f
    private var hitCooldown = 0f
    private var animationTime = 0f
    private var facingLeft = false
    var moveSpeed = 260f
        private set
    var maxHp = 100f
        private set
    var hp = 100f
    var level = 1
        private set
    var attackPower = 1
        private set
    var attackInterval = 0.45f
        private set
    var projectileCount = 1
        private set
    var projectileSpeed = 520f
        private set
    var projectileRadius = 10f
        private set
    var projectileLifetime = 1.5f
        private set
    var pickupRadius = 180f
        private set
    var bladeLevel = 0
        private set
    var bladeCount = 0
        private set
    var bladeDamage = 0f
        private set
    var bladeHitRadius = 16f
        private set
    var bladeOrbitRadius = 76f
        private set
    var bladeRotationSpeed = 3.6f
        private set
    var auraLevel = 0
        private set
    var auraRadius = 0f
        private set
    var auraDamage = 0f
        private set
    var auraTickInterval = 0.7f
        private set
    private var exp = 0

    val radius: Float
        get() = 24f

    fun setMoveVector(dx: Float, dy: Float) {
        moveX = dx
        moveY = dy
        if (dx != 0f) {
            facingLeft = dx < 0f
        }
    }

    override fun update(deltaTime: Float) {
        val length = hypot(moveX, moveY)
        if (length > 0f) {
            x += (moveX / length) * moveSpeed * deltaTime
            y += (moveY / length) * moveSpeed * deltaTime
            animationTime += deltaTime
        } else {
            animationTime += deltaTime * 0.45f
        }
        if (hitCooldown > 0f) {
            hitCooldown -= deltaTime
        }
    }

    fun clampToBounds(width: Float, height: Float) {
        x = x.coerceIn(radius, width - radius)
        y = y.coerceIn(radius, height - radius)
    }

    fun takeDamage(amount: Float): Boolean {
        if (hitCooldown > 0f) return false
        hp = (hp - amount).coerceAtLeast(0f)
        hitCooldown = 0.4f
        return true
    }

    fun nudge(dx: Float, dy: Float) {
        x += dx
        y += dy
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

    fun increaseMoveSpeed(multiplier: Float) {
        moveSpeed *= multiplier
    }

    fun increaseAttackPower(amount: Int) {
        attackPower += amount
    }

    fun improveFireRate(multiplier: Float, minInterval: Float) {
        attackInterval = (attackInterval * multiplier).coerceAtLeast(minInterval)
    }

    fun addProjectileCount(amount: Int, maxCount: Int) {
        projectileCount = (projectileCount + amount).coerceAtMost(maxCount)
    }

    fun increaseMaxHp(amount: Float) {
        maxHp += amount
        hp = (hp + amount).coerceAtMost(maxHp)
    }

    fun increasePickupRadius(amount: Float) {
        pickupRadius += amount
    }

    fun increaseProjectileScale(radiusDelta: Float, lifetimeDelta: Float) {
        projectileRadius += radiusDelta
        projectileLifetime += lifetimeDelta
    }

    fun projectileDamage(): Float {
        return 10f + attackPower * 3f
    }

    fun upgradeBlades() {
        // 칼날은 레벨이 오를수록 개수, 피해량, 회전 반경을 함께 키웁니다.
        bladeLevel += 1
        bladeCount = when (bladeLevel) {
            1 -> 1
            2 -> 2
            3 -> 3
            else -> 4
        }
        bladeDamage += 7f
        bladeHitRadius = (bladeHitRadius + 1.5f).coerceAtMost(24f)
        bladeOrbitRadius = (bladeOrbitRadius + 10f).coerceAtMost(120f)
        bladeRotationSpeed = (bladeRotationSpeed + 0.35f).coerceAtMost(5.4f)
    }

    fun upgradeAura() {
        // 오라는 범위와 틱 속도를 함께 보정해 근접 생존 수단으로 씁니다.
        auraLevel += 1
        auraRadius = if (auraRadius == 0f) 72f else auraRadius + 18f
        auraDamage += 5f
        auraTickInterval = (auraTickInterval - 0.06f).coerceAtLeast(0.28f)
    }

    fun expRatio(): Float {
        val nextLevelExp = 4 + (level - 1) * 2
        return exp.toFloat() / nextLevelExp.toFloat()
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val moving = hypot(moveX, moveY) > 10f
        val frames = if (moving) SpriteAssets.playerRun else SpriteAssets.playerIdle
        val frame = frames[((animationTime * 8f).toInt()) % frames.size]
        val dest = RectF(x - 28f, y - 36f, x + 28f, y + 36f)

        paint.color = Color.argb(80, 0, 0, 0)
        paint.style = Paint.Style.FILL
        canvas.drawOval(x - 18f, y + 18f, x + 18f, y + 28f, paint)

        if (facingLeft) {
            canvas.save()
            canvas.scale(-1f, 1f, x, y)
            canvas.drawBitmap(frame, null, dest, null)
            canvas.restore()
        } else {
            canvas.drawBitmap(frame, null, dest, null)
        }

        if (hitCooldown > 0f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.WHITE
            canvas.drawOval(dest, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#1B1F2A")
        canvas.drawCircle(x + 10f, y - 18f, 4f, paint)
    }
}
