package kr.ac.kpu.midnightsurvivor.game.graphics

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kr.ac.kpu.midnightsurvivor.R

object SpriteAssets {
    private lateinit var resources: Resources

    lateinit var playerIdle: List<Bitmap>
        private set
    lateinit var playerRun: List<Bitmap>
        private set
    lateinit var goblinRun: List<Bitmap>
        private set
    lateinit var impRun: List<Bitmap>
        private set
    lateinit var floorTiles: List<Bitmap>
        private set
    lateinit var buttonBlue: Bitmap
        private set
    lateinit var buttonRed: Bitmap
        private set
    lateinit var doorFrameTop: Bitmap
        private set
    lateinit var bannerBlue: Bitmap
        private set
    lateinit var heartFull: Bitmap
        private set
    lateinit var heartEmpty: Bitmap
        private set

    fun initialize(resources: Resources) {
        if (this::resources.isInitialized) return
        this.resources = resources

        playerIdle = listOf(
            load(R.drawable.knight_f_idle_anim_f0),
            load(R.drawable.knight_f_idle_anim_f1),
            load(R.drawable.knight_f_idle_anim_f2),
            load(R.drawable.knight_f_idle_anim_f3),
        )
        playerRun = listOf(
            load(R.drawable.knight_f_run_anim_f0),
            load(R.drawable.knight_f_run_anim_f1),
            load(R.drawable.knight_f_run_anim_f2),
            load(R.drawable.knight_f_run_anim_f3),
        )
        goblinRun = listOf(
            load(R.drawable.goblin_run_anim_f0),
            load(R.drawable.goblin_run_anim_f1),
            load(R.drawable.goblin_run_anim_f2),
            load(R.drawable.goblin_run_anim_f3),
        )
        impRun = listOf(
            load(R.drawable.imp_run_anim_f0),
            load(R.drawable.imp_run_anim_f1),
            load(R.drawable.imp_run_anim_f2),
            load(R.drawable.imp_run_anim_f3),
        )
        floorTiles = listOf(
            load(R.drawable.floor_1),
            load(R.drawable.floor_2),
            load(R.drawable.floor_3),
            load(R.drawable.floor_4),
            load(R.drawable.floor_5),
            load(R.drawable.floor_6),
            load(R.drawable.floor_7),
            load(R.drawable.floor_8),
        )
        buttonBlue = load(R.drawable.button_blue_up)
        buttonRed = load(R.drawable.button_red_up)
        doorFrameTop = load(R.drawable.doors_frame_top)
        bannerBlue = load(R.drawable.wall_banner_blue)
        heartFull = load(R.drawable.ui_heart_full)
        heartEmpty = load(R.drawable.ui_heart_empty)
    }

    private fun load(resId: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        return BitmapFactory.decodeResource(resources, resId, options)
    }
}
