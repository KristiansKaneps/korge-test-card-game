import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import graphics.*
import scenes.*

suspend fun main() = Korge(
    width = 1280,
    height = 720,
    bgcolor = Colors["#2b2b2b"],
    title = "Card Game",
    quality = GameWindow.Quality.QUALITY,
    iconPath = "icon.png"
) {
    val sceneContainer = sceneContainer()

    CardTextures.generate()

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
