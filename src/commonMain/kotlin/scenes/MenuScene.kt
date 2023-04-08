package scenes

import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import game.*
import game.cards.*

class MenuScene : Scene() {
    lateinit var titleText: Text
    lateinit var startGameButton: UIButton
    lateinit var startHumanButton: UIButton
    lateinit var startComputerButton: UIButton
    lateinit var exitButton: UIButton

    private suspend fun startGame(turnPhase: Phase? = null) {
        val game = Game(
            player1 = Player("Human Player"),
            player2 = AIPlayer("AI Player"),
            6,
            deck = CardDeck.createSmallDeck()
        ).apply {
            turnPhase?.let { this.turnPhase = it }
            initPlayers()
        }

        launchImmediately {
            sceneContainer.changeTo({ GameScene(game) })
        }
    }

    override suspend fun SContainer.sceneMain() {
        titleText = text("An unoptimized card game", textSize = 48.0) {
            position(width / 2, 64.0)
            centerXOn(this@sceneMain)
        }

        val buttonWidth = 200.0
        val buttonHeight = 50.0
        val buttonX = width / 2 - buttonWidth / 2
        val buttonY = height / 2 - 3 * buttonHeight / 2
        startGameButton = uiButton(label = "Start a random game", width = buttonWidth, height = buttonHeight) {
            position(buttonX, buttonY - 16.0)
            onClick {
                startGame()
            }
        }
        startHumanButton = uiButton(label = "You start", width = buttonWidth / 2 - 8.0, height = buttonHeight * 0.75) {
            textSize = 12.0
            position(buttonX, buttonY + buttonHeight)
            onClick {
                startGame(Phase.PLAYER1_ATTACK)
            }
        }
        startComputerButton = uiButton(label = "Computer starts", width = buttonWidth / 2 - 8.0, height = buttonHeight * 0.75) {
            textSize = 12.0
            position(buttonX + buttonWidth / 2 + 8.0, buttonY + buttonHeight)
            onClick {
                startGame(Phase.PLAYER2_ATTACK)
            }
        }
        exitButton = uiButton(label = "Exit", width = buttonWidth / 2, height = buttonHeight) {
            position(buttonX + width / 2, buttonY + 2 * height + 16.0)
            onClick {
                sceneContainer.views.gameWindow.close()
            }
        }
    }
}
