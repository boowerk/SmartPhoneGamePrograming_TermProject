package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Paint
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject

class CombatEffect(
    x: Float,
    y: Float,
    color: Int,
    startRadius: Float,
    endRadius: Float,
    lifeTime: Float,
    strokeWidth: Float = 4f,
) : GameObject(x, y) {
    private var color: Int = color
    private var startRadius: Float = startRadius
    private var endRadius: Float = endRadius
    private var lifeTime: Float = lifeTime
    private var duration: Float = lifeTime
    private var strokeWidth: Float = strokeWidth

    fun reset(
        x: Float,
        y: Float,
        color: Int,
        startRadius: Float,
        endRadius: Float,
        lifeTime: Float,
        strokeWidth: Float,
    ) {
        // 효과도 재사용해서 타격, 레벨업, 보스 연출을 가볍게 쌓습니다.
        this.x = x
        this.y = y
        this.color = color
        this.startRadius = startRadius
        this.endRadius = endRadius
        this.lifeTime = lifeTime
        this.duration = lifeTime
        this.strokeWidth = strokeWidth
        isActive = true
    }

    override fun update(deltaTime: Float) {
        lifeTime -= deltaTime
        if (lifeTime <= 0f) {
            isActive = false
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val ratio = 1f - (lifeTime / duration).coerceIn(0f, 1f)
        val radius = startRadius + (endRadius - startRadius) * ratio
        val alpha = ((1f - ratio) * 255f).toInt().coerceIn(0, 255)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = (color and 0x00FFFFFF) or (alpha shl 24)
        canvas.drawCircle(x, y, radius, paint)
    }
}
