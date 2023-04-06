package game.cards

enum class CardType(
    val symbol: CharSequence,
    val rank: Byte
) {
    TWO('2', 1),
    THREE('3', 2),
    FOUR('4', 3),
    FIVE('5', 4),
    SIX('6', 5),
    SEVEN('7', 6),
    EIGHT('8', 7),
    NINE('9', 8),
    TEN("10", 9),
    JACK('J', 10),
    QUEEN('Q', 11),
    KING('K', 12),
    ACE('A', 13);

    constructor(symbol: Char, rank: Byte) : this(symbol.toString(), rank)
}
