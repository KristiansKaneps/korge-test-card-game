package game

import game.cards.*
import kotlinx.coroutines.*

/**
 * A game of cards.
 * @param player1 The first player (human).
 * @param player2 The second player (computer).
 * @param handSlots The number of slots in each player's hand.
 * @param deck The deck of cards to use.
 */
class Game(
    val player1: IPlayer,
    val player2: IPlayer,
    val handSlots: Int = 6,
    val deck: CardDeck = CardDeck.createSmallDeck(),
) {
    val playerActions = PlayerActions()

    val attacker: IPlayer?
        get() = when (turnPhase) {
            Phase.PLAYER1_ATTACK, Phase.PLAYER2_DEFEND, Phase.PLAYER1_COUNTERATTACK -> player1
            Phase.PLAYER2_ATTACK, Phase.PLAYER1_DEFEND, Phase.PLAYER2_COUNTERATTACK -> player2
            else -> null
        }
    val defender: IPlayer?
        get() = when (turnPhase) {
            Phase.PLAYER1_DEFEND, Phase.PLAYER2_ATTACK, Phase.PLAYER2_COUNTERATTACK -> player1
            Phase.PLAYER2_DEFEND, Phase.PLAYER1_ATTACK, Phase.PLAYER1_COUNTERATTACK -> player2
            else -> null
        }

    lateinit var table: Table
    lateinit var grave: CardGrave

    lateinit var turnPhase: Phase

    var turnNumber = 0
        private set

    init {
        randomize()
    }

    fun randomize() {
        table = Table(CardStack(deck))
        grave = CardGrave()
        turnPhase = setOf(Phase.PLAYER1_ATTACK, Phase.PLAYER2_ATTACK).shuffled().first()
        player1.setHand(handSlots, table.stack.draw(handSlots))
        player2.setHand(handSlots, table.stack.draw(handSlots))
    }

    suspend fun initPlayers() {
        // Generate possible game states tree.
        (player1 as? AIPlayer)?.tree?.generate(this)
        (player2 as? AIPlayer)?.tree?.generate(this)

        if (turnPhase == Phase.PLAYER1_ATTACK && player1 is AIPlayer) {
            player1.act(this)
            playerActions.done()
        } else if (turnPhase == Phase.PLAYER2_ATTACK && player2 is AIPlayer) {
            player2.act(this)
            playerActions.done()
        }
    }

    fun determineWinner() {
        if (player1.hand.size == 0 && player2.hand.size == 0) {
            println("Draw!")
            turnPhase = Phase.RESULT_PLAYERS_DRAW
            turnNumber++
        } else if (player1.hand.size == 0) {
            println("Winner is ${player1.name}!")
            turnPhase = Phase.RESULT_PLAYER1_WINNER
            turnNumber++
        } else if (player2.hand.size == 0) {
            println("Winner is ${player2.name}!")
            turnPhase = Phase.RESULT_PLAYER2_WINNER
            turnNumber++
        }
    }

    protected suspend fun nextTurn(nextPhase: Phase = turnPhase.next()) {
        println("Transitioning from $turnPhase to $nextPhase.")
        if (nextPhase == Phase.PLAYER1_ATTACK || nextPhase == Phase.PLAYER2_ATTACK) {
            if (nextPhase == Phase.PLAYER2_ATTACK) delay(1500L)
            table.clear(grave.cards)
            if (nextPhase == Phase.PLAYER2_ATTACK) {
                player1.draw(table.stack::draw)
                player2.draw(table.stack::draw)
            } else {
                player2.draw(table.stack::draw)
                player1.draw(table.stack::draw)
            }
        }
        turnPhase = nextPhase
        turnNumber++

        println("Next turn (#$turnNumber): $turnPhase")

        if (player2 is AIPlayer) {
            player2.resolve(this)
            if (turnPhase.isPlayer2Turn()) {
                player2.act(this)
                playerActions.done()
            }
        }
    }

    inner class PlayerActions {
        private var actionDone = false
        private var isTakeUpAction = false
        private var isPassAction = false

        suspend fun done(): Boolean {
            if (actionDone && isTakeUpAction && turnPhase == Phase.PLAYER1_DEFEND && table.canTransition(
                    turnPhase,
                    Phase.PLAYER2_ATTACK
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER2_ATTACK)
                return true
            }
            if (actionDone && isTakeUpAction && turnPhase == Phase.PLAYER2_DEFEND && table.canTransition(
                    turnPhase,
                    Phase.PLAYER1_ATTACK
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER1_ATTACK)
                return true
            }
            if (actionDone && table.canTransition(turnPhase, turnPhase.next())) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn()
                return true
            }
            if (!actionDone && turnPhase == Phase.PLAYER1_COUNTERATTACK && table.canTransition(
                    turnPhase,
                    Phase.PLAYER2_ATTACK
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER2_ATTACK)
                return true
            }
            if (actionDone && turnPhase == Phase.PLAYER1_COUNTERATTACK && table.canTransition(
                    turnPhase,
                    Phase.PLAYER2_DEFEND
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER2_DEFEND)
                return true
            }
            if (!actionDone && turnPhase == Phase.PLAYER2_COUNTERATTACK && table.canTransition(
                    turnPhase,
                    Phase.PLAYER1_ATTACK
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER1_ATTACK)
                return true
            }
            if (actionDone && turnPhase == Phase.PLAYER2_COUNTERATTACK && table.canTransition(
                    turnPhase,
                    Phase.PLAYER1_DEFEND
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER1_DEFEND)
                return true
            }
            if (actionDone && isPassAction && turnPhase == Phase.PLAYER1_DEFEND && table.canTransition(
                    turnPhase,
                    Phase.PLAYER2_DEFEND
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER2_DEFEND)
                return true
            }
            if (actionDone && isPassAction && turnPhase == Phase.PLAYER2_DEFEND && table.canTransition(
                    turnPhase,
                    Phase.PLAYER1_DEFEND
                )
            ) {
                actionDone = false
                isTakeUpAction = false
                isPassAction = false
                nextTurn(Phase.PLAYER1_DEFEND)
                return true
            }
            return false
        }

        fun canDraw(): Boolean =
            !actionDone && (
                turnPhase == Phase.PLAYER2_COUNTERATTACK
                    && table.canTransition(turnPhase, Phase.PLAYER1_ATTACK))
                || (
                turnPhase == Phase.PLAYER1_COUNTERATTACK
                    && table.canTransition(turnPhase, Phase.PLAYER2_ATTACK))

        fun canAttack(attackingCards: Collection<Card>): Boolean =
            table.canAttack(attacker!!, defender!!, attackingCards)

        fun attack(attackingCards: Collection<Card>): Boolean {
            println("${attacker!!.name} attacked.")
            if (table.attack(attacker!!, defender!!, attackingCards)) {
                actionDone = true
                return true
            }
            return false
        }

        fun canDefend(attackingCard: Card, defendingCard: Card): Boolean =
            table.canDefend(attacker!!, defender!!, attackingCard, defendingCard)

        fun defend(attackingCard: Card, defendingCard: Card): Boolean {
            println("${defender!!.name} defended against $attackingCard with $defendingCard.")
            if (table.defend(attacker!!, defender!!, attackingCard, defendingCard)) {
                actionDone = true
                return true
            }
            return false
        }

        fun canTakeUp(): Boolean = table.canTakeUp(attacker!!, defender!!)

        fun takeUp(): Boolean {
            println("${defender!!.name} took up.")
            if (table.takeUp(attacker!!, defender!!)) {
                actionDone = true
                isTakeUpAction = true
                return true
            }
            return false
        }

        fun canPass(passCards: Collection<Card>): Boolean =
            table.canPass(attacker!!, defender!!, passCards)

        fun pass(passCards: Collection<Card>): Boolean {
            println("${defender!!.name} passed back.")
            if (table.pass(attacker!!, defender!!, passCards)) {
                actionDone = true
                isPassAction = true
                return true
            }
            return false
        }
    }
}
