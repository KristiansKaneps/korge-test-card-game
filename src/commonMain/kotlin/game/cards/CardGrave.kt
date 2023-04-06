package game.cards

class CardGrave(
    val cards: MutableSet<Card> = mutableSetOf(),
) {
    val size: Int get() = cards.size

    fun add(vararg cards: Card) = this.cards.addAll(cards)
    fun add(cards: Collection<Card>) = this.cards.addAll(cards)

    fun copy(): CardGrave = CardGrave(HashSet(cards))

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CardGrave && cards == other.cards)
    }
}
