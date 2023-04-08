package modules

import com.soywiz.korge.scene.*
import com.soywiz.korinject.*
import graphics.*
import scenes.*

object MainModule : Module() {
    override val mainScene = MenuScene::class

    override suspend fun AsyncInjector.configure() {
        CardTextures.generate()

        mapPrototype { MenuScene() }
    }
}
