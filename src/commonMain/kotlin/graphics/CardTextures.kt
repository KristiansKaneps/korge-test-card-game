package graphics

import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import game.cards.*
import kotlin.properties.*

object CardTextures {
    const val CARD_WIDTH = 500
    const val CARD_HEIGHT = 726

    var back by Delegates.notNull<Bitmap>()
        private set

    private val textures = HashMap<Card, Bitmap>()

    operator fun get(card: Card) = textures[card] ?: error("No texture for card $card")

    private suspend fun generateTexture(card: Card): Bitmap {
        val suffix = if (card.type.ordinal > 8 && card.type !== CardType.ACE) "2" else ""
        val prefix = if (card.type.ordinal <= 8) card.type.symbol else card.type.name.lowercase()
        val file = resourcesVfs["cards/${prefix}_of_${card.suit.name.lowercase()}$suffix.png"]
        return file.readBitmap().mipmaps(true)
    }

    suspend fun generate() {
        back = resourcesVfs["cards/back.png"].readBitmap().mipmaps(true)
        CardDeck.createLargeDeck().cards.forEach {
            textures[it] = generateTexture(it)
        }
    }
}
