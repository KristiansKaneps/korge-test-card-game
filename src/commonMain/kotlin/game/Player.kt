package game

import game.cards.*

open class Player(
    override val name: String,
) : IPlayer {
    override lateinit var hand: CardHand

    override fun setHand(slots: Int, cards: Collection<Card>) {
        hand = CardHand(slots)
        hand.take(cards)
    }

    override fun copy(): IPlayer {
        return Player(name).apply {
            hand = this@Player.hand.copy()
        }
    }

    override fun toString(): String {
        return "Player(name='$name'; hand=$hand)"
    }
}
