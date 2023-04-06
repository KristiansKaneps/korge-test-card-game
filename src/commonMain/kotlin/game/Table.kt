package game

import game.cards.*

class Table(
    val stack: CardStack,
    val board: HashMap<Card, Card?> = HashMap(),
    val highCard: Card = stack.getLast(),
) {
    val highSuit: CardSuit get() = highCard.suit

    fun clear() = board.clear()

    fun clear(dest: MutableCollection<Card>) {
        board.entries.fold(dest) { acc, (attackingCard, defendingCard) ->
            acc.add(attackingCard)
            defendingCard?.let(acc::add)
            acc
        }
        board.clear()
    }

    fun getAttackingCards(): Set<Card> =
        board.entries.filter { it.value == null }.map { it.key }.toSet()

    fun getDefendingCards(): Set<Card> =
        board.entries.filter { it.value != null }.map { it.value!! }.toSet()

    fun areAllCardsDefended(): Boolean =
        board.values.all { it != null }

    fun getCards(): Set<Array<Card>> =
        board.entries.fold(HashSet()) { acc, (attackingCard, defendingCard) ->
            acc.add(
                if (defendingCard == null) arrayOf(attackingCard)
                else arrayOf(attackingCard, defendingCard)
            )
            acc
        }

    fun getCardsFlat(): Set<Card> = board.entries.fold(HashSet()) { acc, (card1, card2) ->
        acc.add(card1)
        card2?.let { acc.add(it) }
        acc
    }

    fun getCardTypesOnBoard(): Set<CardType> = getCardsFlat().map(Card::type).toSet()

    fun areCardsTheSameType(cards: Collection<Card>): Boolean =
        cards.map(Card::type).toSet().size == 1

    fun canTransition(currPhase: Phase, nextPhase: Phase): Boolean {
        return when (currPhase) {
            Phase.PLAYER1_ATTACK -> nextPhase == Phase.PLAYER2_DEFEND && getAttackingCards().isNotEmpty()
            Phase.PLAYER2_DEFEND -> ((nextPhase == Phase.PLAYER1_COUNTERATTACK || nextPhase == Phase.PLAYER1_ATTACK) && areAllCardsDefended()) || (nextPhase == Phase.PLAYER1_DEFEND && getAttackingCards().isNotEmpty())
            Phase.PLAYER1_COUNTERATTACK -> (nextPhase == Phase.PLAYER2_DEFEND && getAttackingCards().isNotEmpty()) || (nextPhase == Phase.PLAYER2_ATTACK && areAllCardsDefended())
            Phase.PLAYER2_ATTACK -> nextPhase == Phase.PLAYER1_DEFEND && getAttackingCards().isNotEmpty()
            Phase.PLAYER1_DEFEND -> ((nextPhase == Phase.PLAYER2_COUNTERATTACK || nextPhase == Phase.PLAYER2_ATTACK) && areAllCardsDefended()) || (nextPhase == Phase.PLAYER2_DEFEND && getAttackingCards().isNotEmpty())
            Phase.PLAYER2_COUNTERATTACK -> (nextPhase == Phase.PLAYER1_DEFEND && getAttackingCards().isNotEmpty()) || (nextPhase == Phase.PLAYER1_ATTACK && areAllCardsDefended())
            else -> false
        }
    }

    fun canTakeUp(attacker: IPlayer, defender: IPlayer): Boolean = !areAllCardsDefended()

    fun canPass(attacker: IPlayer, defender: IPlayer, passCards: Collection<Card>): Boolean {
        val boardCardTypes = board.keys.map(Card::type).toSet()
        val passCardTypes = passCards.map(Card::type).toSet()
        return board.isNotEmpty()
            && attacker.hand.size >= (board.size + passCards.size)
            && board.values.all { it == null }
            && boardCardTypes.size == 1
            && passCardTypes.size == 1
            && boardCardTypes.first() == passCardTypes.first()
    }

    fun canDefend(
        attacker: IPlayer,
        defender: IPlayer,
        attackingCard: Card,
        defendingCard: Card,
    ): Boolean {
        val attHigh = attackingCard.suit == highSuit
        val defHigh = defendingCard.suit == highSuit
        val attSuit = attackingCard.suit
        val defSuit = defendingCard.suit
        val attRank = attackingCard.type.rank
        val defRank = defendingCard.type.rank
        return !(((attSuit != defSuit && !defHigh) || (attHigh && !defHigh))
            || (attRank >= defRank && ((!attHigh && !defHigh) || (attHigh && defHigh))))
    }

    fun canAttack(attacker: IPlayer, defender: IPlayer, attackingCards: Collection<Card>): Boolean {
        val cardTypesOnBoard = getCardTypesOnBoard()
        return defender.hand.size >= (attackingCards.size + getAttackingCards().size)
            && (
                areCardsTheSameType(getAttackingCards() + attackingCards)
                    || attackingCards.all { cardTypesOnBoard.contains(it.type) }
                )
    }

    fun takeUp(attacker: IPlayer, defender: IPlayer): Boolean {
        clear(defender.hand.cards)
        return true
    }

    fun pass(attacker: IPlayer, defender: IPlayer, passCards: Collection<Card>): Boolean {
        if (!canPass(attacker, defender, passCards)) return false
        defender.hand.play(passCards)
        passCards.forEach { board[it] = null }
        return true
    }

    fun defend(
        attacker: IPlayer,
        defender: IPlayer,
        attackingCard: Card,
        defendingCard: Card,
    ): Boolean {
        if (!canDefend(attacker, defender, attackingCard, defendingCard)) return false
        defender.hand.play(defendingCard)
        board[attackingCard] = defendingCard
        return true
    }

    fun attack(
        attacker: IPlayer,
        defender: IPlayer,
        attackingCards: Collection<Card>,
    ): Boolean {
        if (!canAttack(attacker, defender, attackingCards)) return false
        attacker.hand.play(attackingCards)
        attackingCards.forEach { board[it] = null }
        return true
    }

    fun copy(board: HashMap<Card, Card?> = HashMap(this.board)): Table =
        Table(stack.copy(), board, highCard)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table) return false

        if (highCard != other.highCard) return false
        if (stack.size != other.stack.size) return false
        if (stack != other.stack) return false
        if (board.size != other.board.size) return false
        for(entry in board.entries) {
            var found = false
            for(otherEntry in other.board.entries) {
                if (entry.key == otherEntry.key && entry.value == otherEntry.value) {
                    found = true
                    break
                }
            }
            if (!found) return false
        }

        return true
    }
}
