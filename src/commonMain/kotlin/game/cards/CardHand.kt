package game.cards

class CardHand(
    val slots: Int,
    val cards: HashSet<Card> = HashSet(slots),
) {
    val size: Int get() = cards.size

    val missing: Int get() = slots - size

    operator fun get(index: Int): Card? = cards.elementAtOrNull(index)

    fun take(card: Card) {
        cards.add(card)
    }

    fun take(cards: Collection<Card>) {
        this.cards.addAll(cards)
    }

    fun play(card: Card) {
        cards.remove(card)
    }

    fun play(cards: Collection<Card>) {
        this.cards.removeAll(cards)
    }

    fun copy() = CardHand(slots, HashSet<Card>(slots).apply { addAll(cards) })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardHand) return false
        if (slots != other.slots) return false
        return cards == other.cards
    }

    override fun toString(): String {
        return "CardHand(slots=$slots; cards=$cards)"
    }
}
