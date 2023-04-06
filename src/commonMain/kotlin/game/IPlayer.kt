package game

import game.cards.*

interface IPlayer {
    val name: String
    val hand: CardHand

    fun setHand(slots: Int, cards: Collection<Card>)

    fun draw(getCards: (Int) -> Collection<Card>) =
        hand.take(getCards(hand.missing))

    fun copy(): IPlayer
}
