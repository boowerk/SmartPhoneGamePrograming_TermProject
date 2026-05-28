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
    private val summaryPanel = RectF()

    override fun onEnter() {
        // 결과 화면에서는 승패에 맞는 짧은 마무리 사운드만 재생합니다.
        GameAudio.play(if (summary.victory) GameSfx.VICTORY else GameSfx.DEFEAT)
    }

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        val buttonWidth = width * 0.6f
        val left = (width - buttonWidth) * 0.5f
        summaryPanel.set(width * 0.11f, height * 0.28f, width * 0.89f, height * 0.80f)
        restartButton.set(left, height * 0.84f, left + buttonWidth, height * 0.84f + 96f)
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0F172A"))

        paint.textAlign = Paint.Align.CENTER
        paint.color = if (summary.victory) Color.parseColor("#F1FA8C") else Color.parseColor("#FF6B6B")
        paint.textSize = 72f
        canvas.drawText(if (summary.victory) "SURVIVED" else "DEFEATED", width * 0.5f, height * 0.20f, paint)

        // 결과 수치를 한 패널 안에 모아 발표 화면에서 읽기 쉽게 정리합니다.
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#172235")
        canvas.drawRoundRect(summaryPanel, 28f, 28f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.parseColor("#335C81")
        canvas.drawRoundRect(summaryPanel, 28f, 28f, paint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 30f
        val textLeft = summaryPanel.left + 26f
        var lineY = summaryPanel.top + 54f
        canvas.drawText("Time  ${"%.1f".format(summary.survivedTime)} sec", textLeft, lineY, paint)
        lineY += 46f
        canvas.drawText("Defeated  ${summary.defeatedEnemies}", textLeft, lineY, paint)
        lineY += 46f
        canvas.drawText("Level  ${summary.reachedLevel}", textLeft, lineY, paint)
        lineY += 48f

        // 결과 화면에서 빌드와 진행도를 한 번에 확인할 수 있게 요약 통계를 함께 노출합니다.
        paint.textSize = 22f
        paint.color = Color.parseColor("#C9D1D9")
        canvas.drawText("Phase  ${summary.deepestPhase}", textLeft, lineY, paint)
        lineY += 38f
        canvas.drawText("Shots  ${summary.projectilesFired}", textLeft, lineY, paint)
        lineY += 38f
        canvas.drawText("Pickups  ${summary.pickupsCollected}", textLeft, lineY, paint)
        lineY += 38f
        canvas.drawText("Upgrades  ${summary.selectedUpgrades}", textLeft, lineY, paint)
        lineY += 38f
        canvas.drawText("Bosses  ${summary.bossesDefeatedCount}", textLeft, lineY, paint)
        lineY += 38f
        canvas.drawText(
            "Boss  ${if (summary.bossDefeated) "Defeated" else if (summary.bossEncountered) "Reached" else "Not Spawned"}",
            textLeft,
            lineY,
            paint,
        )
        lineY += 42f
        canvas.drawText(summary.weaponLoadout, textLeft, lineY, paint)

        paint.color = Color.parseColor("#1F4068")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(restartButton, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
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
