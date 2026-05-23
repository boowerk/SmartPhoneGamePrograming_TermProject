package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.audio.GameAudio
import kr.ac.kpu.midnightsurvivor.game.audio.GameSfx
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene

class ResultScene(
    game: MainGame,
    private val summary: RunSummary,
) : Scene(game) {
    private val restartButton = RectF()

    override fun onEnter() {
        // 결과 화면에서는 승패에 맞는 짧은 마무리 사운드만 재생합니다.
        GameAudio.play(if (summary.victory) GameSfx.VICTORY else GameSfx.DEFEAT)
    }

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        val buttonWidth = width * 0.6f
        val left = (width - buttonWidth) * 0.5f
        restartButton.set(left, height * 0.72f, left + buttonWidth, height * 0.72f + 110f)
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0F172A"))

        paint.textAlign = Paint.Align.CENTER
        paint.color = if (summary.victory) Color.parseColor("#F1FA8C") else Color.parseColor("#FF6B6B")
        paint.textSize = 72f
        canvas.drawText(if (summary.victory) "SURVIVED" else "DEFEATED", width * 0.5f, height * 0.20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 34f
        canvas.drawText("Time  ${"%.1f".format(summary.survivedTime)} sec", width * 0.5f, height * 0.34f, paint)
        canvas.drawText("Defeated  ${summary.defeatedEnemies}", width * 0.5f, height * 0.40f, paint)
        canvas.drawText("Level  ${summary.reachedLevel}", width * 0.5f, height * 0.46f, paint)

        // 결과 화면에서 빌드와 진행도를 한 번에 확인할 수 있게 요약 통계를 함께 노출합니다.
        paint.textSize = 24f
        paint.color = Color.parseColor("#C9D1D9")
        canvas.drawText("Phase  ${summary.deepestPhase}", width * 0.5f, height * 0.54f, paint)
        canvas.drawText("Shots  ${summary.projectilesFired}", width * 0.5f, height * 0.59f, paint)
        canvas.drawText("Pickups  ${summary.pickupsCollected}", width * 0.5f, height * 0.64f, paint)
        canvas.drawText("Upgrades  ${summary.selectedUpgrades}", width * 0.5f, height * 0.69f, paint)
        canvas.drawText(
            "Boss  ${if (summary.bossDefeated) "Defeated" else if (summary.bossEncountered) "Reached" else "Not Spawned"}",
            width * 0.5f,
            height * 0.74f,
            paint,
        )
        canvas.drawText(summary.weaponLoadout, width * 0.5f, height * 0.79f, paint)

        paint.color = Color.parseColor("#1F4068")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(restartButton, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("BACK TO TITLE", restartButton.centerX(), restartButton.centerY() + 14f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && restartButton.contains(event.x, event.y)) {
            game.replaceScene(TitleScene(game))
            return true
        }
        return restartButton.contains(event.x, event.y)
    }
}
