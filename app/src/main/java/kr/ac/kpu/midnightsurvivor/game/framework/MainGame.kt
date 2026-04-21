package kr.ac.kpu.midnightsurvivor.game.framework

import android.graphics.Canvas
import android.view.MotionEvent
import kr.ac.kpu.midnightsurvivor.game.scene.TitleScene

class MainGame {
    private val sceneStack = mutableListOf<Scene>()
    private var viewportWidth = 0f
    private var viewportHeight = 0f

    init {
        replaceScene(TitleScene(this))
    }

    fun onResize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
        sceneStack.forEach { it.onResize(width, height) }
    }

    fun replaceScene(scene: Scene) {
        while (sceneStack.isNotEmpty()) {
            sceneStack.removeAt(sceneStack.lastIndex).onExit()
        }
        sceneStack += scene
        scene.onResize(viewportWidth, viewportHeight)
        scene.onEnter()
    }

    fun pushScene(scene: Scene) {
        sceneStack += scene
        scene.onResize(viewportWidth, viewportHeight)
        scene.onEnter()
    }

    fun popScene() {
        if (sceneStack.size <= 1) return
        sceneStack.removeAt(sceneStack.lastIndex).onExit()
    }

    fun update(deltaTime: Float) {
        sceneStack.lastOrNull()?.update(deltaTime)
    }

    fun draw(canvas: Canvas) {
        if (sceneStack.isEmpty()) return

        var startIndex = sceneStack.lastIndex
        while (startIndex > 0 && sceneStack[startIndex].isTransparent) {
            startIndex--
        }
        for (index in startIndex..sceneStack.lastIndex) {
            sceneStack[index].draw(canvas)
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return sceneStack.lastOrNull()?.onTouchEvent(event) ?: false
    }
}
