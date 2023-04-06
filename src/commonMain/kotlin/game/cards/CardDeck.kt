package game.cards

class CardDeck(
    val cards: Set<Card>,
) {
    companion object {
        fun createLargeDeck(): CardDeck {
            val cards = CardType.values().flatMap { type ->
                CardSuit.values().map { suit ->
                    Card(type, suit)
                }
            }.toSet()
            return CardDeck(cards)
        }

        fun createSmallDeck(): CardDeck {
            val cards = CardType.values().slice(4 until CardType.values().size).flatMap { type ->
                CardSuit.values().map { suit ->
                    Card(type, suit)
                }
            }.toSet()
            return CardDeck(cards)
        }

        fun createMiniDeck(): CardDeck {
            val cards = CardType.values().slice(8 until CardType.values().size).flatMap { type ->
                CardSuit.values().map { suit ->
                    Card(type, suit)
                }
            }.toSet()
            return CardDeck(cards)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CardDeck && cards == other.cards)
    }
}
