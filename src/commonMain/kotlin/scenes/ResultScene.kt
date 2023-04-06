package scenes

import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korim.text.*
import com.soywiz.korio.async.*

class ResultScene(
    val text: String,
) : Scene() {
    lateinit var resultText: Text
    lateinit var backToMenu: UIButton

    override suspend fun SContainer.sceneMain() {
        resultText = text(text, textSize = 48.0) {
            position(width / 2, 64.0)
            centerXOn(this@sceneMain)
        }

        val buttonWidth = 200.0
        val buttonHeight = 50.0
        val buttonX = width / 2 - buttonWidth / 2
        val buttonY = height / 2 - buttonHeight / 2
        backToMenu = uiButton(label = "Exit to menu", width = buttonWidth, height = buttonHeight) {
            position(buttonX, buttonY)
            onClick {
                sceneContainer.changeTo({
                    MenuScene({
                        sceneContainer.views.gameWindow.close()
                    }) { game ->
                        launchImmediately {
                            sceneContainer.changeTo({ GameScene(game) })
                        }
                    }
                })
            }
        }
    }
}
