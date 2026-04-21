package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene
import kr.ac.kpu.midnightsurvivor.game.objects.Enemy
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
    private var dragX = 0f
    private var dragY = 0f
    private var elapsedTime = 0f
    private var spawnTimer = 0f
    private var shotTimer = 0f
    private var defeatedEnemies = 0

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        if (!initialized && width > 0f && height > 0f) {
            player = Player(width * 0.5f, height * 0.55f)
            repeat(48) {
                stars += Random.nextFloat() * width to Random.nextFloat() * height
            }
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
        val moveX = if (dragActive) dragX - player.x else 0f
        val moveY = if (dragActive) dragY - player.y else 0f
        player.setMoveVector(moveX, moveY)
        player.update(deltaTime)
        player.clampToBounds(width, height)
    }

    private fun updateEnemies(deltaTime: Float) {
        enemies.forEach { enemy ->
            enemy.updateToward(player.x, player.y, deltaTime)
            if (isColliding(player.x, player.y, player.radius, enemy.x, enemy.y, enemy.radius)) {
                player.takeDamage(enemy.damage)
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
                        gems += ExpGem(enemy.x, enemy.y)
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
        when (edge) {
            0 -> {
                spawnX = Random.nextFloat() * width
                spawnY = -margin
            }
            1 -> {
                spawnX = width + margin
                spawnY = Random.nextFloat() * height
            }
            2 -> {
                spawnX = Random.nextFloat() * width
                spawnY = height + margin
            }
            else -> {
                spawnX = -margin
                spawnY = Random.nextFloat() * height
            }
        }

        val difficulty = 1f + elapsedTime / 45f
        enemies += Enemy(
            x = spawnX,
            y = spawnY,
            moveSpeed = 80f + difficulty * 25f,
            hp = 18f + difficulty * 8f,
            damage = 10f,
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

        gems.forEach { it.draw(canvas, paint) }
        projectiles.forEach { it.draw(canvas, paint) }
        enemies.forEach { it.draw(canvas, paint) }
        player.draw(canvas, paint)

        drawHud(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        stars.forEachIndexed { index, star ->
            paint.color = if (index % 3 == 0) Color.parseColor("#17304E") else Color.parseColor("#11243B")
            canvas.drawCircle(star.first, star.second, if (index % 3 == 0) 4f else 2f, paint)
        }
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
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
