import com.soywiz.korge.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import modules.*

suspend fun main() = Korge(Korge.Config(
    windowSize = ISizeInt(1280, 720),
    bgcolor = Colors["#2b2b2b"],
    title = "Card Game",
    quality = GameWindow.Quality.QUALITY,
    icon = "icon.png",
    module = MainModule,
))
