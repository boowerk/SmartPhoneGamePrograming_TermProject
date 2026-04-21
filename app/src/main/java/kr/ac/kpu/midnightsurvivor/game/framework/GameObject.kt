package kr.ac.kpu.midnightsurvivor.game.framework

import android.graphics.Canvas
import android.graphics.Paint

abstract class GameObject(
    var x: Float,
    var y: Float,
) {
    var isActive = true

    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas, paint: Paint)
}
