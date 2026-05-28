package kr.ac.kpu.midnightsurvivor.game.scene

data class RunSummary(
    val survivedTime: Float,
    val defeatedEnemies: Int,
    val reachedLevel: Int,
    val victory: Boolean,
    val bossEncountered: Boolean,
    val bossDefeated: Boolean,
    val bossesDefeatedCount: Int,
    val deepestPhase: String,
    val projectilesFired: Int,
    val pickupsCollected: Int,
    val selectedUpgrades: Int,
    val weaponLoadout: String,
)
