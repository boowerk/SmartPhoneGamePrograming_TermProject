package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene
import kr.ac.kpu.midnightsurvivor.game.objects.Enemy
import kr.ac.kpu.midnightsurvivor.game.objects.EnemyType
import kr.ac.kpu.midnightsurvivor.game.objects.ExpGem
import kr.ac.kpu.midnightsurvivor.game.objects.Player
import kr.ac.kpu.midnightsurvivor.game.objects.Projectile

class MainScene(game: MainGame) : Scene(game) {
    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val gems = mutableListOf<ExpGem>()
    private val stars = mutableListOf<Pair<Float, Float>>()
    private var initialized = false
    private var dragActive = false
    private var cameraX = 0f
    private var cameraY = 0f
    private var worldWidth = 0f
    private var worldHeight = 0f
    private var joystickBaseX = 0f
    private var joystickBaseY = 0f
    private var dragX = 0f
    private var dragY = 0f
    private var elapsedTime = 0f
    private var spawnTimer = 0f
    private var shotTimer = 0f
    private var defeatedEnemies = 0

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        if (!initialized && width > 0f && height > 0f) {
            worldWidth = maxOf(width * 2.4f, 1400f)
            worldHeight = maxOf(height * 2.1f, 2200f)
            player = Player(worldWidth * 0.5f, worldHeight * 0.5f)
            repeat(120) {
                stars += Random.nextFloat() * worldWidth to Random.nextFloat() * worldHeight
            }
            updateCamera()
            initialized = true
        }
    }

    override fun update(deltaTime: Float) {
        if (!initialized) return

        elapsedTime += deltaTime
        spawnTimer -= deltaTime
        shotTimer -= deltaTime

        updatePlayer(deltaTime)
        updateEnemies(deltaTime)
        updateProjectiles(deltaTime)
        updateGems(deltaTime)

        if (spawnTimer <= 0f) {
            spawnEnemy()
        }
        if (shotTimer <= 0f) {
            fireProjectile()
        }

        if (player.hp <= 0f) {
            game.replaceScene(ResultScene(game, elapsedTime, defeatedEnemies, false))
        } else if (elapsedTime >= 90f) {
            game.replaceScene(ResultScene(game, elapsedTime, defeatedEnemies, true))
        }
    }

    private fun updatePlayer(deltaTime: Float) {
        val moveX = if (dragActive) dragX - joystickBaseX else 0f
        val moveY = if (dragActive) dragY - joystickBaseY else 0f
        player.setMoveVector(moveX, moveY)
        player.update(deltaTime)
        player.clampToBounds(worldWidth, worldHeight)
        updateCamera()
    }

    private fun updateEnemies(deltaTime: Float) {
        enemies.forEach { enemy ->
            enemy.updateToward(player.x, player.y, deltaTime)
            if (isColliding(player.x, player.y, player.radius, enemy.x, enemy.y, enemy.radius)) {
                if (player.takeDamage(enemy.damage)) {
                    val dx = player.x - enemy.x
                    val dy = player.y - enemy.y
                    val distance = hypot(dx, dy).coerceAtLeast(1f)
                    player.nudge((dx / distance) * 34f, (dy / distance) * 34f)
                    player.clampToBounds(worldWidth, worldHeight)
                    updateCamera()
                }
            }
        }
        enemies.removeAll { !it.isActive }
    }

    private fun updateProjectiles(deltaTime: Float) {
        projectiles.forEach { projectile ->
            projectile.update(deltaTime)
            for (enemy in enemies) {
                if (!projectile.isActive || !enemy.isActive) continue
                if (isColliding(projectile.x, projectile.y, projectile.radius, enemy.x, enemy.y, enemy.radius)) {
                    projectile.isActive = false
                    if (enemy.hit(projectile.damage)) {
                        defeatedEnemies += 1
                        gems += ExpGem(enemy.x, enemy.y, enemy.expReward)
                    }
                }
            }
        }
        projectiles.removeAll { !it.isActive }
    }

    private fun updateGems(deltaTime: Float) {
        var leveledUp = false
        gems.forEach { gem ->
            gem.updateToward(player.x, player.y, deltaTime)
            if (isColliding(player.x, player.y, player.radius, gem.x, gem.y, gem.radius)) {
                gem.isActive = false
                if (!leveledUp && player.gainExp(gem.amount)) {
                    leveledUp = true
                }
            }
        }
        gems.removeAll { !it.isActive }

        if (leveledUp) {
            game.pushScene(LevelUpScene(game, buildUpgradeOptions(), ::applyUpgrade))
        }
    }

    private fun spawnEnemy() {
        val margin = 40f
        val edge = Random.nextInt(4)
        val spawnX: Float
        val spawnY: Float
        val cameraRight = (cameraX + width).coerceAtMost(worldWidth)
        val cameraBottom = (cameraY + height).coerceAtMost(worldHeight)
        when (edge) {
            0 -> {
                spawnX = Random.nextFloat() * width + cameraX
                spawnY = (cameraY - margin).coerceAtLeast(0f)
            }
            1 -> {
                spawnX = (cameraRight + margin).coerceAtMost(worldWidth)
                spawnY = Random.nextFloat() * height + cameraY
            }
            2 -> {
                spawnX = Random.nextFloat() * width + cameraX
                spawnY = (cameraBottom + margin).coerceAtMost(worldHeight)
            }
            else -> {
                spawnX = (cameraX - margin).coerceAtLeast(0f)
                spawnY = Random.nextFloat() * height + cameraY
            }
        }

        val difficulty = 1f + elapsedTime / 45f
        val enemyType = if (elapsedTime > 18f && Random.nextFloat() < 0.35f) {
            EnemyType.DASHER
        } else {
            EnemyType.CHASER
        }
        enemies += Enemy(
            x = spawnX,
            y = spawnY,
            type = enemyType,
            moveSpeed = if (enemyType == EnemyType.CHASER) 80f + difficulty * 25f else 110f + difficulty * 20f,
            hp = if (enemyType == EnemyType.CHASER) 18f + difficulty * 8f else 12f + difficulty * 6f,
            damage = if (enemyType == EnemyType.CHASER) 10f else 14f,
            expReward = if (enemyType == EnemyType.CHASER) 1 else 2,
        )
        spawnTimer = (1.2f - elapsedTime / 120f).coerceAtLeast(0.45f)
    }

    private fun fireProjectile() {
        val target = enemies.minByOrNull { distanceSquared(player.x, player.y, it.x, it.y) } ?: run {
            shotTimer = 0.25f
            return
        }
        val angle = atan2(target.y - player.y, target.x - player.x)
        val speed = 520f
        projectiles += Projectile(
            x = player.x,
            y = player.y,
            velocityX = cos(angle) * speed,
            velocityY = sin(angle) * speed,
            damage = 12f + player.attackPower * 4f,
        )
        shotTimer = 0.45f
    }

    private fun buildUpgradeOptions(): List<UpgradeOption> {
        return listOf(
            UpgradeOption("speed", "Swift Step", "Move speed +15%"),
            UpgradeOption("power", "Moon Shot", "Projectile damage +4"),
            UpgradeOption("heal", "Night Bloom", "Recover 25 HP"),
        )
    }

    private fun applyUpgrade(option: UpgradeOption) {
        when (option.id) {
            "speed" -> player.moveSpeed *= 1.15f
            "power" -> player.attackPower += 1
            "heal" -> player.heal(25f)
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#08111F"))
        drawBackground(canvas)

        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        drawWorldFloor(canvas)
        gems.forEach { it.draw(canvas, paint) }
        projectiles.forEach { it.draw(canvas, paint) }
        enemies.forEach { it.draw(canvas, paint) }
        player.draw(canvas, paint)
        canvas.restore()

        drawHud(canvas)
        drawJoystick(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        stars.forEachIndexed { index, star ->
            paint.color = if (index % 3 == 0) Color.parseColor("#17304E") else Color.parseColor("#11243B")
            val parallax = if (index % 3 == 0) 0.18f else 0.1f
            val screenX = star.first - cameraX * parallax
            val screenY = star.second - cameraY * parallax
            if (screenX in -8f..width + 8f && screenY in -8f..height + 8f) {
                canvas.drawCircle(screenX, screenY, if (index % 3 == 0) 4f else 2f, paint)
            }
        }
    }

    private fun drawWorldFloor(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0B1727")
        canvas.drawRect(0f, 0f, worldWidth, worldHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#14314D")
        var x = 0f
        while (x <= worldWidth) {
            canvas.drawLine(x, 0f, x, worldHeight, paint)
            x += 140f
        }
        var y = 0f
        while (y <= worldHeight) {
            canvas.drawLine(0f, y, worldWidth, y, paint)
            y += 140f
        }

        paint.strokeWidth = 8f
        paint.color = Color.parseColor("#335C81")
        canvas.drawRect(0f, 0f, worldWidth, worldHeight, paint)
    }

    private fun drawHud(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 34f
        canvas.drawText("HP ${player.hp.toInt()}/${player.maxHp.toInt()}", 28f, 48f, paint)
        canvas.drawText("LV ${player.level}", 28f, 92f, paint)
        canvas.drawText("TIME ${elapsedTime.toInt()}s", 28f, 136f, paint)
        canvas.drawText("KILLS $defeatedEnemies", 28f, 180f, paint)

        val barLeft = 28f
        val barTop = 205f
        val barWidth = width - 56f
        paint.color = Color.parseColor("#20354C")
        canvas.drawRoundRect(barLeft, barTop, barLeft + barWidth, barTop + 20f, 10f, 10f, paint)

        paint.color = Color.parseColor("#50FA7B")
        canvas.drawRoundRect(
            barLeft,
            barTop,
            barLeft + barWidth * player.expRatio(),
            barTop + 20f,
            10f,
            10f,
            paint,
        )

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#C9D1D9")
        paint.textSize = 24f
        canvas.drawText("World ${player.x.toInt()}, ${player.y.toInt()}", width - 24f, 44f, paint)
        canvas.drawText("Enemies ${enemies.size}", width - 24f, 76f, paint)
    }

    private fun drawJoystick(canvas: Canvas) {
        if (!dragActive) return

        val knob = clampedJoystickKnob()
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(90, 140, 170, 210)
        canvas.drawCircle(joystickBaseX, joystickBaseY, 92f, paint)

        paint.color = Color.argb(180, 241, 250, 140)
        canvas.drawCircle(knob.first, knob.second, 42f, paint)
    }

    private fun clampedJoystickKnob(): Pair<Float, Float> {
        val dx = dragX - joystickBaseX
        val dy = dragY - joystickBaseY
        val distance = hypot(dx, dy)
        val maxRadius = 82f
        if (distance <= maxRadius || distance == 0f) {
            return dragX to dragY
        }
        val scale = maxRadius / distance
        return joystickBaseX + dx * scale to joystickBaseY + dy * scale
    }

    private fun updateCamera() {
        cameraX = (player.x - width * 0.5f).coerceIn(0f, (worldWidth - width).coerceAtLeast(0f))
        cameraY = (player.y - height * 0.5f).coerceIn(0f, (worldHeight - height).coerceAtLeast(0f))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragActive = true
                joystickBaseX = event.x
                joystickBaseY = event.y
                dragX = event.x
                dragY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                dragActive = true
                dragX = event.x
                dragY = event.y
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                dragActive = false
            }
        }
        return true
    }

    private fun isColliding(
        x1: Float,
        y1: Float,
        r1: Float,
        x2: Float,
        y2: Float,
        r2: Float,
    ): Boolean {
        return distanceSquared(x1, y1, x2, y2) <= (r1 + r2) * (r1 + r2)
    }

    private fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }
}
