package kr.ac.kpu.midnightsurvivor.game.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.framework.MainGame
import kr.ac.kpu.midnightsurvivor.game.framework.Scene
import kr.ac.kpu.midnightsurvivor.game.graphics.SpriteAssets

class LevelUpScene(
    game: MainGame,
    private val options: List<UpgradeOption>,
    private val onSelect: (UpgradeOption) -> Unit,
) : Scene(game) {
    override val isTransparent: Boolean = true

    private val panelRect = RectF()
    private val cards = mutableListOf<RectF>()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        cards.clear()
        panelRect.set(width * 0.08f, height * 0.12f, width * 0.92f, height * 0.90f)
        val margin = panelRect.left + 28f
        val cardWidth = panelRect.width() - 56f
        val cardHeight = 150f
        val startY = panelRect.top + 160f
        for (index in options.indices) {
            val top = startY + index * (cardHeight + 28f)
            cards += RectF(margin, top, margin + cardWidth, top + cardHeight)
        }
    }

    override fun update(deltaTime: Float) = Unit

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(180, 7, 10, 20)
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width, height, paint)

        drawTiledRect(canvas, panelRect, 36f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.parseColor("#6FA8DC")
        canvas.drawRoundRect(panelRect, 28f, 28f, paint)

        val bannerWidth = panelRect.width() * 0.36f
        val bannerLeft = panelRect.centerX() - bannerWidth * 0.5f
        val bannerTop = panelRect.top + 24f
        canvas.drawBitmap(
            SpriteAssets.bannerBlue,
            null,
            RectF(bannerLeft, bannerTop, bannerLeft + bannerWidth, bannerTop + 110f),
            null,
        )
        canvas.drawBitmap(
            SpriteAssets.doorFrameTop,
            null,
            RectF(panelRect.left + 32f, panelRect.top + 116f, panelRect.right - 32f, panelRect.top + 142f),
            null,
        )

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        paint.textSize = 58f
        canvas.drawText("LEVEL UP", width * 0.5f, panelRect.top + 88f, paint)

        for (index in options.indices) {
            val option = options[index]
            val card = cards[index]

            drawTiledRect(canvas, card, 28f)
            paint.color = Color.parseColor("#12243A")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas.drawRoundRect(card, 24f, 24f, paint)

            paint.color = option.accentColor
            canvas.drawRoundRect(card.left, card.top, card.left + 18f, card.bottom, 24f, 24f, paint)

            val titleBadge = RectF(card.left + 26f, card.top + 20f, card.left + 150f, card.top + 62f)
            canvas.drawBitmap(SpriteAssets.buttonBlue, null, titleBadge, null)

            val rankBadge = RectF(card.right - 140f, card.top + 18f, card.right - 26f, card.top + 60f)
            canvas.drawBitmap(SpriteAssets.buttonRed, null, rankBadge, null)

            paint.color = option.accentColor
            paint.textSize = 36f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(option.title, card.left + 40f, card.top + 52f, paint)

            paint.color = Color.WHITE
            paint.textSize = 26f
            canvas.drawText(option.description, card.left + 28f, card.top + 102f, paint)

            paint.color = Color.parseColor("#C9D1D9")
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 22f
            canvas.drawText("Rank ${option.rank}/${option.maxRank}", card.right - 28f, card.top + 48f, paint)
        }
    }

    private fun drawTiledRect(canvas: Canvas, rect: RectF, tileSize: Float) {
        val tiles = SpriteAssets.floorTiles
        var tileY = rect.top
        var row = 0
        while (tileY < rect.bottom) {
            var tileX = rect.left
            var col = 0
            while (tileX < rect.right) {
                val bitmap = tiles[(row * 7 + col * 3) % tiles.size]
                val right = (tileX + tileSize).coerceAtMost(rect.right)
                val bottom = (tileY + tileSize).coerceAtMost(rect.bottom)
                canvas.drawBitmap(bitmap, null, RectF(tileX, tileY, right, bottom), null)
                tileX += tileSize
                col++
            }
            tileY += tileSize
            row++
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        for (index in cards.indices) {
            if (cards[index].contains(event.x, event.y)) {
                onSelect(options[index])
                game.popScene()
                return true
            }
        }
        return true
    }
}
