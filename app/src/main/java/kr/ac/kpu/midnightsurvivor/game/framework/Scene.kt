package kr.ac.kpu.midnightsurvivor.game.framework

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent

abstract class Scene(
    protected val game: MainGame,
) {
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected var width = 0f
    protected var height = 0f

    open val isTransparent: Boolean = false

    open fun onEnter() = Unit
    open fun onExit() = Unit

    open fun onResize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas)

    open fun onTouchEvent(event: MotionEvent): Boolean = false
}
