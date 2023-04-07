package game.cards

class Card(
    val type: CardType,
    val suit: CardSuit,
) {
    fun toPrettyString(): String = "${suit.symbol}${type.symbol}"
    override fun toString(): String = "${suit.initialChar}${type.symbol}"
    override fun equals(other: Any?): Boolean =
        this === other || (other is Card && type == other.type && suit == other.suit)

    override fun hashCode(): Int = type.ordinal shl 2 or suit.ordinal
}
