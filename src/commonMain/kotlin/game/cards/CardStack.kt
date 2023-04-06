package game.cards

class CardStack(
    val deck: CardDeck,
    val cards: ArrayList<Card> = ArrayList(deck.cards.shuffled()),
) {
    val size: Int get() = cards.size

    var drawn: Int = 0
        private set

    operator fun get(index: Int): Card? = cards.elementAtOrNull(index)

    fun getLast(): Card = cards.last()

    protected fun draw(): Card? {
        val card = cards.firstOrNull()
        if (card != null) {
            cards.remove(card)
            drawn++
        }
        return card
    }

    fun draw(count: Int = 1): Collection<Card> {
        val cards = mutableSetOf<Card>()
        for (i in 0 until count)
            draw()?.let(cards::add) ?: break
        return cards
    }

    fun copy(): CardStack = CardStack(deck, ArrayList(cards)).apply { drawn = this@CardStack.drawn }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CardStack && cards == other.cards && deck == other.deck)
    }
}
