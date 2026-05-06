package kr.ac.kpu.midnightsurvivor.game.scene

data class UpgradeOption(
    val id: String,
    val title: String,
    val description: String,
    val rank: Int,
    val maxRank: Int,
    val accentColor: Int,
)
