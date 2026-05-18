package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets
import kr.ac.kpu.midnightsurvivor.game.objects.BossEnemy
import kr.ac.kpu.midnightsurvivor.game.objects.Enemy
import kr.ac.kpu.midnightsurvivor.game.objects.EnemyProjectile
import kr.ac.kpu.midnightsurvivor.game.objects.EnemyShot
import kr.ac.kpu.midnightsurvivor.game.objects.EnemyType
import kr.ac.kpu.midnightsurvivor.game.objects.ExpGem
import kr.ac.kpu.midnightsurvivor.game.objects.Player
import kr.ac.kpu.midnightsurvivor.game.objects.Projectile

class MainScene(game: MainGame) : Scene(game) {
    private data class UpgradeDefinition(
        val id: String,
        val title: String,
        val maxRank: Int,
        val accentColor: Int,
        val description: (nextRank: Int) -> String,
    )

    private data class WaveDefinition(
        val label: String,
        val startTime: Float,
        val endTime: Float,
        val spawnInterval: Float,
        val batchSize: Int,
        val maxEnemies: Int,
        val enemyTypes: List<EnemyType>,
    )

    private lateinit var player: Player
    private var boss: BossEnemy? = null
    private val enemies = mutableListOf<Enemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val enemyProjectiles = mutableListOf<EnemyProjectile>()
    private val gems = mutableListOf<ExpGem>()
    private val enemyPool = ArrayDeque<Enemy>()
    private val projectilePool = ArrayDeque<Projectile>()
    private val enemyProjectilePool = ArrayDeque<EnemyProjectile>()
    private val gemPool = ArrayDeque<ExpGem>()
    private val stars = mutableListOf<Pair<Float, Float>>()
    private val upgradeRanks = mutableMapOf<String, Int>()
    private val upgradeDefinitions = listOf(
        UpgradeDefinition("power", "Moon Shot", 5, Color.parseColor("#F1FA8C")) { rank ->
            "Projectile damage +3  |  next rank $rank"
        },
        UpgradeDefinition("rapid", "Quick Trigger", 5, Color.parseColor("#8BE9FD")) { rank ->
            "Attack interval 12% faster  |  next rank $rank"
        },
        UpgradeDefinition("volley", "Twin Volley", 4, Color.parseColor("#FFB86C")) { rank ->
            "Projectile count +1  |  next rank $rank"
        },
        UpgradeDefinition("speed", "Swift Step", 4, Color.parseColor("#50FA7B")) { rank ->
            "Move speed +12%  |  next rank $rank"
        },
        UpgradeDefinition("vitality", "Vital Core", 4, Color.parseColor("#FF79C6")) { rank ->
            "Max HP +20 and heal 20  |  next rank $rank"
        },
        UpgradeDefinition("magnet", "Soul Magnet", 4, Color.parseColor("#BD93F9")) { rank ->
            "Pickup radius +40  |  next rank $rank"
        },
        UpgradeDefinition("focus", "Long Shot", 4, Color.parseColor("#FF5555")) { rank ->
            "Projectile size and range up  |  next rank $rank"
        },
        UpgradeDefinition("blade", "Orbit Blade", 4, Color.parseColor("#F6C85F")) { rank ->
            "Summon and strengthen rotating blades  |  next rank $rank"
        },
        UpgradeDefinition("aura", "Moon Aura", 4, Color.parseColor("#80FFEA")) { rank ->
            "Pulse damage around the player  |  next rank $rank"
        },
    )
    private val waveDefinitions = listOf(
        // 6주차 웨이브 테이블: 시간대별 적 조합과 동시 출현 수를 단계적으로 늘립니다.
        WaveDefinition("WAVE 1", 0f, 45f, 1.20f, 1, 12, listOf(EnemyType.CHASER)),
        WaveDefinition("WAVE 2", 45f, 95f, 1.05f, 2, 15, listOf(EnemyType.CHASER, EnemyType.DASHER)),
        WaveDefinition("WAVE 3", 95f, 155f, 0.92f, 2, 18, listOf(EnemyType.CHASER, EnemyType.DASHER, EnemyType.TANK)),
        WaveDefinition("WAVE 4", 155f, 220f, 0.80f, 3, 22, listOf(EnemyType.DASHER, EnemyType.TANK, EnemyType.RANGER)),
        WaveDefinition("WAVE 5", 220f, 300f, 0.70f, 3, 25, listOf(EnemyType.CHASER, EnemyType.DASHER, EnemyType.TANK, EnemyType.RANGER)),
    )
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
    private var bladeTickTimer = 0f
    private var auraTickTimer = 0f
    private var bladeSpinAngle = 0f
    private var defeatedEnemies = 0
    private var bossSpawned = false
    private var bossDefeated = false
    private var bossIntroTimer = 0f
    private val stageDuration = 300f
    private val bossSpawnTime = 240f

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
        bossIntroTimer = (bossIntroTimer - deltaTime).coerceAtLeast(0f)

        updatePlayer(deltaTime)
        updateEnemies(deltaTime)
        updateBoss(deltaTime)
        updateProjectiles(deltaTime)
        updateEnemyProjectiles(deltaTime)
        updateBlades(deltaTime)
        updateAura(deltaTime)
        updateGems(deltaTime)
        recycleInactiveObjects()

        if (!bossSpawned && elapsedTime >= bossSpawnTime) {
            spawnBoss()
        }
        if (!bossSpawned && spawnTimer <= 0f) {
            spawnEnemy()
        }
        if (shotTimer <= 0f) {
            fireProjectile()
        }

        if (player.hp <= 0f) {
            game.replaceScene(ResultScene(game, elapsedTime, defeatedEnemies, player.level, false))
        } else if (bossDefeated || (elapsedTime >= stageDuration && !bossSpawned)) {
            game.replaceScene(ResultScene(game, elapsedTime, defeatedEnemies, player.level, true))
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
            enemy.updateToward(player.x, player.y, deltaTime)?.let(::obtainEnemyProjectile)
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
    }

    private fun updateBoss(deltaTime: Float) {
        val activeBoss = boss ?: return
        if (!activeBoss.isActive) {
            handleBossDefeat()
            return
        }

        // 보스는 별도 루프에서 패턴과 소환을 처리해 웨이브 적과 독립적으로 제어합니다.
        val action = activeBoss.updateToward(player.x, player.y, deltaTime)
        action.shots.forEach(::obtainEnemyProjectile)
        if (action.summonCount > 0) {
            spawnBossMinions(action.summonCount)
        }

        if (isColliding(player.x, player.y, player.radius, activeBoss.x, activeBoss.y, activeBoss.radius)) {
            if (player.takeDamage(18f)) {
                val dx = player.x - activeBoss.x
                val dy = player.y - activeBoss.y
                val distance = hypot(dx, dy).coerceAtLeast(1f)
                player.nudge((dx / distance) * 48f, (dy / distance) * 48f)
                player.clampToBounds(worldWidth, worldHeight)
                updateCamera()
            }
        }
    }

    private fun updateProjectiles(deltaTime: Float) {
        projectiles.forEach { projectile ->
            projectile.update(deltaTime)
            val activeBoss = boss
            if (projectile.isActive && activeBoss?.isActive == true) {
                if (isColliding(projectile.x, projectile.y, projectile.radius, activeBoss.x, activeBoss.y, activeBoss.radius)) {
                    projectile.isActive = false
                    if (activeBoss.hit(projectile.damage)) {
                        handleBossDefeat()
                    }
                }
            }
            for (enemy in enemies) {
                if (!projectile.isActive || !enemy.isActive) continue
                if (isColliding(projectile.x, projectile.y, projectile.radius, enemy.x, enemy.y, enemy.radius)) {
                    projectile.isActive = false
                    if (enemy.hit(projectile.damage)) {
                        handleEnemyDefeat(enemy)
                    }
                }
            }
        }
    }

    private fun updateEnemyProjectiles(deltaTime: Float) {
        enemyProjectiles.forEach { projectile ->
            projectile.update(deltaTime)
            if (!projectile.isActive) return@forEach

            if (isColliding(player.x, player.y, player.radius, projectile.x, projectile.y, projectile.radius)) {
                projectile.isActive = false
                if (player.takeDamage(projectile.damage)) {
                    val dx = player.x - projectile.x
                    val dy = player.y - projectile.y
                    val distance = hypot(dx, dy).coerceAtLeast(1f)
                    player.nudge((dx / distance) * 22f, (dy / distance) * 22f)
                    player.clampToBounds(worldWidth, worldHeight)
                    updateCamera()
                }
            }
        }
    }

    private fun updateBlades(deltaTime: Float) {
        if (player.bladeLevel <= 0 || player.bladeCount <= 0) return

        bladeSpinAngle += player.bladeRotationSpeed * deltaTime
        bladeTickTimer -= deltaTime
        if (bladeTickTimer > 0f) return

        // 회전 칼날은 일정 틱마다만 충돌 판정을 해 과도한 근접 DPS를 막습니다.
        bladeTickTimer = 0.18f
        currentBladePositions().forEach { position ->
            val activeBoss = boss
            if (activeBoss?.isActive == true &&
                isColliding(
                    position.first,
                    position.second,
                    player.bladeHitRadius,
                    activeBoss.x,
                    activeBoss.y,
                    activeBoss.radius,
                )
            ) {
                if (activeBoss.hit(player.bladeDamage)) {
                    handleBossDefeat()
                }
                return@forEach
            }

            val target = enemies.firstOrNull { enemy ->
                enemy.isActive && isColliding(
                    position.first,
                    position.second,
                    player.bladeHitRadius,
                    enemy.x,
                    enemy.y,
                    enemy.radius,
                )
            } ?: return@forEach

            if (target.hit(player.bladeDamage)) {
                handleEnemyDefeat(target)
            }
        }
    }

    private fun updateAura(deltaTime: Float) {
        if (player.auraLevel <= 0 || player.auraRadius <= 0f) return

        auraTickTimer -= deltaTime
        if (auraTickTimer > 0f) return

        // 오라는 근접 적 다수를 천천히 깎아내는 생존형 무기로 동작합니다.
        auraTickTimer = player.auraTickInterval
        val activeBoss = boss
        if (activeBoss?.isActive == true &&
            isColliding(player.x, player.y, player.auraRadius, activeBoss.x, activeBoss.y, activeBoss.radius)
        ) {
            if (activeBoss.hit(player.auraDamage)) {
                handleBossDefeat()
            }
        }
        enemies.forEach { enemy ->
            if (!enemy.isActive) return@forEach
            if (isColliding(player.x, player.y, player.auraRadius, enemy.x, enemy.y, enemy.radius)) {
                if (enemy.hit(player.auraDamage)) {
                    handleEnemyDefeat(enemy)
                }
            }
        }
    }

    private fun updateGems(deltaTime: Float) {
        var leveledUp = false
        gems.forEach { gem ->
            gem.updateToward(player.x, player.y, player.pickupRadius, deltaTime)
            if (isColliding(player.x, player.y, player.radius, gem.x, gem.y, gem.radius)) {
                gem.isActive = false
                if (!leveledUp && player.gainExp(gem.amount)) {
                    leveledUp = true
                }
            }
        }

        if (leveledUp) {
            game.pushScene(LevelUpScene(game, buildUpgradeOptions(), ::applyUpgrade))
        }
    }

    private fun spawnEnemy() {
        val currentWave = currentWaveDefinition()
        if (enemies.size >= currentWave.maxEnemies) {
            spawnTimer = currentWave.spawnInterval * 0.35f
            return
        }

        val margin = 40f
        val cameraRight = (cameraX + width).coerceAtMost(worldWidth)
        val cameraBottom = (cameraY + height).coerceAtMost(worldHeight)
        repeat(currentWave.batchSize) {
            if (enemies.size >= currentWave.maxEnemies) return@repeat

            val edge = Random.nextInt(4)
            val spawnX: Float
            val spawnY: Float
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

            val difficulty = 1f + elapsedTime / 50f
            val enemyType = currentWave.enemyTypes.random()
            obtainEnemy(
                x = spawnX,
                y = spawnY,
                type = enemyType,
                moveSpeed = when (enemyType) {
                    EnemyType.CHASER -> 90f + difficulty * 22f
                    EnemyType.DASHER -> 118f + difficulty * 18f
                    EnemyType.TANK -> 62f + difficulty * 12f
                    EnemyType.RANGER -> 88f + difficulty * 14f
                },
                hp = when (enemyType) {
                    EnemyType.CHASER -> 18f + difficulty * 8f
                    EnemyType.DASHER -> 14f + difficulty * 6f
                    EnemyType.TANK -> 42f + difficulty * 16f
                    EnemyType.RANGER -> 22f + difficulty * 9f
                },
                damage = when (enemyType) {
                    EnemyType.CHASER -> 10f
                    EnemyType.DASHER -> 14f
                    EnemyType.TANK -> 18f
                    EnemyType.RANGER -> 11f
                },
                expReward = when (enemyType) {
                    EnemyType.CHASER -> 1
                    EnemyType.DASHER -> 2
                    EnemyType.TANK -> 3
                    EnemyType.RANGER -> 2
                },
            )
        }
        spawnTimer = currentWave.spawnInterval
    }

    private fun fireProjectile() {
        val target = enemies.minByOrNull { distanceSquared(player.x, player.y, it.x, it.y) } ?: run {
            shotTimer = 0.25f
            return
        }
        val angle = atan2(target.y - player.y, target.x - player.x)
        val spreadStep = 0.16f
        val count = player.projectileCount
        val startOffset = -spreadStep * (count - 1) * 0.5f
        repeat(count) { index ->
            val projectileAngle = angle + startOffset + spreadStep * index
            obtainProjectile(
                x = player.x,
                y = player.y,
                velocityX = cos(projectileAngle) * player.projectileSpeed,
                velocityY = sin(projectileAngle) * player.projectileSpeed,
                damage = player.projectileDamage(),
                spriteRadius = player.projectileRadius,
                lifeTime = player.projectileLifetime,
            )
        }
        shotTimer = player.attackInterval
    }

    private fun buildUpgradeOptions(): List<UpgradeOption> {
        val available = upgradeDefinitions.filter { definition ->
            upgradeRanks.getOrDefault(definition.id, 0) < definition.maxRank
        }.shuffled()

        val selected = available.take(3).map { definition ->
            val nextRank = upgradeRanks.getOrDefault(definition.id, 0) + 1
            UpgradeOption(
                id = definition.id,
                title = definition.title,
                description = definition.description(nextRank),
                rank = nextRank,
                maxRank = definition.maxRank,
                accentColor = definition.accentColor,
            )
        }.toMutableList()

        while (selected.size < 3) {
            selected += UpgradeOption(
                id = if (player.hp < player.maxHp) "heal" else "bonus_exp",
                title = if (player.hp < player.maxHp) "Night Bloom" else "Soul Echo",
                description = if (player.hp < player.maxHp) {
                    "Recover 30 HP immediately"
                } else {
                    "Spawn bonus EXP shards around the player"
                },
                rank = 1,
                maxRank = 1,
                accentColor = if (player.hp < player.maxHp) {
                    Color.parseColor("#50FA7B")
                } else {
                    Color.parseColor("#8BE9FD")
                },
            )
        }

        return selected.take(3)
    }

    private fun applyUpgrade(option: UpgradeOption) {
        when (option.id) {
            "speed" -> {
                upgradeRanks.bump(option.id)
                player.increaseMoveSpeed(1.12f)
            }
            "power" -> {
                upgradeRanks.bump(option.id)
                player.increaseAttackPower(1)
            }
            "rapid" -> {
                upgradeRanks.bump(option.id)
                player.improveFireRate(0.88f, 0.14f)
            }
            "volley" -> {
                upgradeRanks.bump(option.id)
                player.addProjectileCount(1, 5)
            }
            "vitality" -> {
                upgradeRanks.bump(option.id)
                player.increaseMaxHp(20f)
            }
            "magnet" -> {
                upgradeRanks.bump(option.id)
                player.increasePickupRadius(40f)
            }
            "focus" -> {
                upgradeRanks.bump(option.id)
                player.increaseProjectileScale(1.5f, 0.2f)
            }
            "blade" -> {
                upgradeRanks.bump(option.id)
                player.upgradeBlades()
            }
            "aura" -> {
                upgradeRanks.bump(option.id)
                player.upgradeAura()
            }
            "heal" -> player.heal(30f)
            "bonus_exp" -> {
                repeat(6) {
                    obtainGem(
                        x = player.x + Random.nextFloat() * 80f - 40f,
                        y = player.y + Random.nextFloat() * 80f - 40f,
                        amount = 2,
                    )
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#08111F"))
        drawBackground(canvas)

        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        drawWorldFloor(canvas)
        drawAura(canvas)
        gems.forEach { it.draw(canvas, paint) }
        projectiles.forEach { it.draw(canvas, paint) }
        enemyProjectiles.forEach { it.draw(canvas, paint) }
        enemies.forEach { it.draw(canvas, paint) }
        boss?.takeIf { it.isActive }?.draw(canvas, paint)
        drawBlades(canvas)
        player.draw(canvas, paint)
        canvas.restore()

        drawHud(canvas)
        drawJoystick(canvas)
        drawBossIntro(canvas)
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
        val tileSize = 48f
        val startTileX = (cameraX / tileSize).toInt().coerceAtLeast(0)
        val startTileY = (cameraY / tileSize).toInt().coerceAtLeast(0)
        val endTileX = ((cameraX + width) / tileSize).toInt() + 2
        val endTileY = ((cameraY + height) / tileSize).toInt() + 2
        val tiles = SpriteAssets.floorTiles

        for (ty in startTileY..endTileY) {
            for (tx in startTileX..endTileX) {
                val left = tx * tileSize
                val top = ty * tileSize
                if (left >= worldWidth || top >= worldHeight) continue
                val bitmap = tiles[((tx * 13 + ty * 7) and Int.MAX_VALUE) % tiles.size]
                val right = (left + tileSize).coerceAtMost(worldWidth)
                val bottom = (top + tileSize).coerceAtMost(worldHeight)
                canvas.drawBitmap(bitmap, null, RectF(left, top, right, bottom), null)
            }
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = Color.parseColor("#335C81")
        canvas.drawRect(0f, 0f, worldWidth, worldHeight, paint)
    }

    private fun drawHud(canvas: Canvas) {
        val currentWave = currentWaveDefinition()
        val waveLabel = if (bossSpawned && !bossDefeated) "BOSS NIGHT" else currentWave.label
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 34f
        drawHearts(canvas, 28f, 24f)
        canvas.drawText("LV ${player.level}", 28f, 108f, paint)
        canvas.drawText("TIME ${elapsedTime.toInt()}s", 28f, 152f, paint)
        canvas.drawText("KILLS $defeatedEnemies", 28f, 196f, paint)
        canvas.drawText(waveLabel, 28f, 240f, paint)

        val barLeft = 28f
        val barTop = 264f
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
        canvas.drawText("Shots ${player.projectileCount}  Rate ${"%.2f".format(player.attackInterval)}", width - 24f, 108f, paint)
        canvas.drawText("Blade ${player.bladeLevel}  Aura ${player.auraLevel}", width - 24f, 140f, paint)
        canvas.drawText("Magnet ${player.pickupRadius.toInt()}", width - 24f, 172f, paint)
        canvas.drawText("Boss ETA ${maxOf(0f, bossSpawnTime - elapsedTime).toInt()}s", width - 24f, 204f, paint)

        boss?.takeIf { it.isActive }?.let { activeBoss ->
            val bossBarTop = height - 76f
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.parseColor("#FF7A90")
            paint.textSize = 30f
            canvas.drawText("Crimson Overlord", width * 0.5f, bossBarTop - 14f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#2A1020")
            canvas.drawRoundRect(24f, bossBarTop, width - 24f, bossBarTop + 22f, 11f, 11f, paint)
            paint.color = Color.parseColor("#FF7A90")
            canvas.drawRoundRect(24f, bossBarTop, 24f + (width - 48f) * activeBoss.hpRatio(), bossBarTop + 22f, 11f, 11f, paint)
        }
    }

    private fun drawHearts(canvas: Canvas, startX: Float, startY: Float) {
        val heartSpacing = 38f
        val totalHearts = 5
        val hpRatio = if (player.maxHp <= 0f) 0f else player.hp / player.maxHp
        val filledHearts = (hpRatio * totalHearts).toInt().coerceIn(0, totalHearts)
        repeat(totalHearts) { index ->
            val left = startX + heartSpacing * index
            val dest = RectF(left, startY, left + 28f, startY + 28f)
            val bitmap = if (index < filledHearts) SpriteAssets.heartFull else SpriteAssets.heartEmpty
            canvas.drawBitmap(bitmap, null, dest, null)
        }
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

    private fun drawAura(canvas: Canvas) {
        if (player.auraLevel <= 0 || player.auraRadius <= 0f) return

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(24, 128, 255, 234)
        canvas.drawCircle(player.x, player.y, player.auraRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(120, 128, 255, 234)
        canvas.drawCircle(player.x, player.y, player.auraRadius, paint)
    }

    private fun drawBlades(canvas: Canvas) {
        if (player.bladeLevel <= 0 || player.bladeCount <= 0) return

        paint.style = Paint.Style.FILL
        currentBladePositions().forEachIndexed { index, position ->
            paint.color = if (index % 2 == 0) Color.parseColor("#F6C85F") else Color.parseColor("#FFD87A")
            canvas.drawCircle(position.first, position.second, player.bladeHitRadius, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#8C5A1C")
            canvas.drawCircle(position.first, position.second, player.bladeHitRadius, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private fun drawBossIntro(canvas: Canvas) {
        if (bossIntroTimer <= 0f) return

        val alpha = (bossIntroTimer / 2.4f).coerceIn(0f, 1f)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((alpha * 160).toInt(), 18, 8, 18)
        canvas.drawRect(0f, height * 0.36f, width, height * 0.50f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.argb((alpha * 255).toInt(), 255, 122, 144)
        paint.textSize = 44f
        canvas.drawText("BOSS APPROACHING", width * 0.5f, height * 0.43f, paint)
    }

    private fun currentBladePositions(): List<Pair<Float, Float>> {
        val positions = mutableListOf<Pair<Float, Float>>()
        repeat(player.bladeCount) { index ->
            val angle = bladeSpinAngle + (Math.PI * 2.0 * index / player.bladeCount).toFloat()
            positions += (
                player.x + cos(angle) * player.bladeOrbitRadius to
                    player.y + sin(angle) * player.bladeOrbitRadius
                )
        }
        return positions
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

    private fun handleEnemyDefeat(enemy: Enemy) {
        defeatedEnemies += 1
        obtainGem(enemy.x, enemy.y, enemy.expReward)
    }

    private fun handleBossDefeat() {
        if (bossDefeated) return
        boss?.isActive = false
        boss = null
        bossDefeated = true
        defeatedEnemies += 12
    }

    private fun currentWaveDefinition(): WaveDefinition {
        return waveDefinitions.firstOrNull { elapsedTime >= it.startTime && elapsedTime < it.endTime }
            ?: waveDefinitions.last()
    }

    private fun obtainEnemy(
        x: Float,
        y: Float,
        type: EnemyType,
        moveSpeed: Float,
        hp: Float,
        damage: Float,
        expReward: Int,
    ) {
        val enemy = enemyPool.removeLastOrNull()
            ?: Enemy(x, y, type, moveSpeed, hp, damage, expReward)
        enemy.reset(x, y, type, moveSpeed, hp, damage, expReward)
        enemies += enemy
    }

    private fun obtainProjectile(
        x: Float,
        y: Float,
        velocityX: Float,
        velocityY: Float,
        damage: Float,
        spriteRadius: Float,
        lifeTime: Float,
    ) {
        val projectile = projectilePool.removeLastOrNull()
            ?: Projectile(x, y, velocityX, velocityY, damage, spriteRadius, lifeTime)
        projectile.reset(x, y, velocityX, velocityY, damage, spriteRadius, lifeTime)
        projectiles += projectile
    }

    private fun obtainEnemyProjectile(shot: EnemyShot) {
        val projectile = enemyProjectilePool.removeLastOrNull()
            ?: EnemyProjectile(shot.x, shot.y, shot.velocityX, shot.velocityY, shot.damage, shot.radius)
        projectile.reset(shot.x, shot.y, shot.velocityX, shot.velocityY, shot.damage, shot.radius, 3.2f)
        enemyProjectiles += projectile
    }

    private fun obtainGem(x: Float, y: Float, amount: Int) {
        val gem = gemPool.removeLastOrNull() ?: ExpGem(x, y, amount)
        gem.reset(x, y, amount)
        gems += gem
    }

    private fun recycleInactiveObjects() {
        // 비활성 객체를 프레임 끝에 한 번에 회수해 컬렉션 순회를 단순하게 유지합니다.
        recycleList(enemies, enemyPool)
        recycleList(projectiles, projectilePool)
        recycleList(enemyProjectiles, enemyProjectilePool)
        recycleList(gems, gemPool)
    }

    private fun <T : Any> recycleList(activeList: MutableList<T>, pool: ArrayDeque<T>) {
        val iterator = activeList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val isActive = when (item) {
                is Enemy -> item.isActive
                is Projectile -> item.isActive
                is EnemyProjectile -> item.isActive
                is ExpGem -> item.isActive
                else -> true
            }
            if (!isActive) {
                iterator.remove()
                pool.addLast(item)
            }
        }
    }

    private fun spawnBoss() {
        val spawnX = (player.x + width * 0.28f).coerceIn(120f, worldWidth - 120f)
        val spawnY = (player.y - height * 0.34f).coerceIn(120f, worldHeight - 120f)
        val nextBoss = boss ?: BossEnemy(spawnX, spawnY)
        nextBoss.reset(spawnX, spawnY)
        boss = nextBoss
        bossSpawned = true
        bossIntroTimer = 2.4f
    }

    private fun spawnBossMinions(count: Int) {
        val activeBoss = boss ?: return
        repeat(count) { index ->
            val angle = (Math.PI * 2.0 * index / count).toFloat() + elapsedTime * 0.3f
            val spawnX = (activeBoss.x + cos(angle) * 120f).coerceIn(40f, worldWidth - 40f)
            val spawnY = (activeBoss.y + sin(angle) * 120f).coerceIn(40f, worldHeight - 40f)
            val enemyType = if (index % 2 == 0) EnemyType.DASHER else EnemyType.TANK
            obtainEnemy(
                x = spawnX,
                y = spawnY,
                type = enemyType,
                moveSpeed = if (enemyType == EnemyType.DASHER) 150f else 92f,
                hp = if (enemyType == EnemyType.DASHER) 26f else 52f,
                damage = if (enemyType == EnemyType.DASHER) 15f else 18f,
                expReward = if (enemyType == EnemyType.DASHER) 2 else 3,
            )
        }
    }

    private fun MutableMap<String, Int>.bump(id: String) {
        this[id] = getOrDefault(id, 0) + 1
    }
}
