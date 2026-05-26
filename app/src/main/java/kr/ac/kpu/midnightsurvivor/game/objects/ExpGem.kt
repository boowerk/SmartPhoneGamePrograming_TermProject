package kr.ac.kpu.midnightsurvivor.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.hypot
import kr.ac.kpu.midnightsurvivor.game.framework.GameObject
import kr.ac.kpu.midnightsurvivor.game.framework.Sprite

enum class PickupKind {
    EXP,
    HEAL,
}

class ExpGem(
    x: Float,
    y: Float,
    amount: Int = 1,
    kind: PickupKind = PickupKind.EXP,
) : GameObject(x, y) {
    var amount: Int = amount
        private set
    var kind: PickupKind = kind
        private set
    private var sprite = Sprite(Color.parseColor("#50FA7B"), 12f)

    val radius: Float
        get() = sprite.radius

    override fun update(deltaTime: Float) = Unit

    fun reset(x: Float, y: Float, amount: Int, kind: PickupKind) {
        // 회복 오브까지 함께 재사용할 수 있게 종류와 색상을 같이 되살립니다.
        this.x = x
        this.y = y
        this.amount = amount
        this.kind = kind
        this.sprite = when (kind) {
            PickupKind.EXP -> Sprite(Color.parseColor("#50FA7B"), 12f)
            PickupKind.HEAL -> Sprite(Color.parseColor("#FF79C6"), 14f)
        }
        isActive = true
    }

    fun updateToward(targetX: Float, targetY: Float, attractRadius: Float, deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val distance = hypot(dx, dy)
        if (distance in 0f..attractRadius) {
            x += (dx / distance) * 220f * deltaTime
            y += (dy / distance) * 220f * deltaTime
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = sprite.color
        canvas.drawCircle(x, y, sprite.radius, paint)

        if (kind == PickupKind.HEAL) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.WHITE
            canvas.drawLine(x - 6f, y, x + 6f, y, paint)
            canvas.drawLine(x, y - 6f, x, y + 6f, paint)
        }
    }
}
