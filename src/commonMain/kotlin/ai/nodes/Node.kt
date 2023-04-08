package ai.nodes

import ai.*
import game.*
import game.cards.*
import kotlin.math.*

abstract class Node(
    state: State,
    override val parent: INode?,
    override val children: ArrayList<INode> = ArrayList(),
    override val depth: Int = parent?.depth?.plus(1) ?: 0,
) : INode {
    protected val state: State = state.copy()
    override fun currentState(): State = state

    override var evaluation: Float = 0.0f
    override var isEvaluated: Boolean = false
    override var isExpanded: Boolean = false

    /**
     * Evaluates this node.
     */
    override fun evaluate(game: Game): Float {
        // If the evaluation has already been calculated and is final, return it.
        if (isEvaluated && isExpanded) return evaluation

        // If the evaluation was not final and the children have not been generated,
        // heuristically evaluate this node.
        if (!isExpanded) {
            evaluation = heuristicEvaluation(game)
            return evaluation
        }

        // If the evaluation was not final and the children have been generated,
        // evaluate this node based on the evaluations of its children.
        // If any of the children evaluations are not final, this node's evaluation is not final.
        evaluation = (children.sumOf { it.evaluate(game).toDouble() } / children.size).toFloat()
        isEvaluated = children.all { it.isEvaluated }
        return evaluation
    }

    /**
     * Heuristically evaluates the given player's hand.
     */
    private fun heuristicHandEval(hand: CardHand): Float {
        val playerHandEval = (hand.cards.sumOf {
            if (it.suit == state.table.highSuit) {
                100 + it.type.ordinal + 1
            } else {
                it.type.ordinal + 1
            }
        } + (hand.slots - hand.cards.size) * MAX_CARD_RANK)
        return playerHandEval.toFloat() / (max(hand.slots, hand.cards.size) * MAX_CARD_RANK)
    }

    /**
     * Heuristically evaluates this node.
     */
    private fun heuristicEvaluation(game: Game): Float {
        val playerHandEval = heuristicHandEval(state.player.hand)
        val opponentHandEval = heuristicHandEval(state.opponent.hand)
        evaluation = if (game.player1.name == state.player.name) {
            opponentHandEval - playerHandEval
        } else {
            playerHandEval - opponentHandEval
        }
        return evaluation
    }

    /**
     * Expands this node.
     */
    override fun generate(game: Game) {
        if (isExpanded) return
        isExpanded = true

        // If the game is over, set this node's evaluation to the final evaluation and return.
        if (state.table.stack.size == 0 && state.player.hand.size == 0 && state.opponent.hand.size == 0) {
            evaluation = 0.0f
            isEvaluated = true
            return
        }
        if (state.table.stack.size == 0 && state.player.hand.size > 0 && state.opponent.hand.size == 0) {
            evaluation = if (game.player1.name == state.player.name) 1.0f else -1.0f
            isEvaluated = true
            return
        }
        if (state.table.stack.size == 0 && state.player.hand.size == 0 && state.opponent.hand.size > 0) {
            evaluation = if (game.player1.name == state.player.name) -1.0f else 1.0f
            isEvaluated = true
            return
        }

        // If the game is not over, expand this node.
        generateNodes()
    }

    /**
     * Generates the children of this node.
     * This method should be overridden by subclasses.
     */
    abstract fun generateNodes()

    override fun toString(): String {
        return "Node(depth=$depth; state=$state)"
    }

    companion object {
        /**
         * High card rank is [100 + (the ordinal value + 1) of the card type].
         */
        private val MAX_CARD_RANK = 100 + (CardType.ACE.ordinal + 1)
    }
}
