package game.cards

enum class CardSuit(
    val symbol: Char,
    val initialChar: Char,
    val color: CardColor,
) {
    DIAMONDS('♦', 'D', CardColor.RED),
    HEARTS('♥', 'H', CardColor.RED),
    SPADES('♠', 'S', CardColor.BLACK),
    CLUBS('♣', 'C', CardColor.BLACK);

    override fun toString(): String = initialChar.toString()
}
