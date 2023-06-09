package ai.nodes

import ai.*
import game.cards.*
import kotlin.math.*

class PlayerNode(
    state: State,
    override val parent: INode,
) : Node(state, parent) {
    override fun generateNodes() {
        if (state.table.getAttackingCards().isEmpty()) {
            // If there are no attacking cards, the player can either attack, counterattack or draw.
            if (state.table.board.isEmpty()) {
                // If there are no cards, the player can only attack.
                generateAttackNodes()
            } else {
                // If there are cards, the player can counter-attack.
                generateCounterAttackNodes()
                // If player doesn't want to or doesn't have any cards to counter-attack with,
                // opponent defended successfully => switch turns.
                if (parent is DefenseNode)
                    generateDrawNode()
            }
        } else {
            // If there are attacking cards, the player can either defend or take up.
            generateTakeUpNode()
            generateDefenseNodes()
        }
    }

    private fun generateDrawNode() {
        val table = state.table.copy().apply { clear() }
        val player = state.player.copy()
        val opponent = state.opponent.copy()

        // Opponent defended successfully.
        player.draw(table.stack::draw)
        opponent.draw(table.stack::draw)
        children.add(DrawNode(
            state = State(
                table = table,
                player = player,
                opponent = opponent,
            ),
            parent = this,
        ))
    }

    private fun generateCounterAttackNodes() {
        val boardCardTypes = state.table.getCardsFlat().map { it.type }.toHashSet()

        // Counter-attack with the same type of cards that of which are on the board.
        @Suppress("NAME_SHADOWING")
        Combinatorics.combinationsUpToK(state.player.hand.cards.filter { boardCardTypes.contains(it.type) }, min(state.player.hand.size, state.opponent.hand.size))
            .reversed().forEach { move ->
                val player = state.player.copy()
                val opponent = state.opponent.copy()
                val table = state.table.copy().apply { attack(player, opponent, move) }

                children.add(AttackNode(
                    state = State(
                        table = table,
                        player = player,
                        opponent = opponent,
                    ),
                    parent = this,
                ))
            }
    }

    private fun generateAttackNodes() {
        val player = state.player.copy()

        // Attack with any combination of cards.
        @Suppress("NAME_SHADOWING")
        Combinatorics.groupByPossibleCardTypeMoves(state.player.hand.cards, min(player.hand.size, state.opponent.hand.size)).reversed().forEach { move ->
            val player = player.copy()
            children.add(AttackNode(
                state = State(
                    table = state.table.copy().apply { attack(player, state.opponent, move) },
                    player = player,
                    opponent = state.opponent,
                ),
                parent = this,
            ))
        }
    }

    private fun generateTakeUpNode() {
        val table = state.table.copy()
        val player = state.player.copy()
        val opponent = state.opponent.copy()

        // Take up cards and draw a new one.
        table.takeUp(opponent, player)
        opponent.draw(table.stack::draw)

        children.add(TakeUpNode(
            state = State(
                table = table,
                player = player,
                opponent = opponent,
            ),
            parent = this,
        ))
    }

    private fun generateDefenseNodes() {
        // To preserve the order of the attacking cards, we need to convert the set to a list.
        val attackingCards = state.table.getAttackingCards().toList()

        // Generate all possible scenarios where the player defends with the cards in his hand.
        // Maybe could be replaced with a smarter algorithm in the future.
        val variations = Combinatorics.variations(state.player.hand.cards, attackingCards.size)
        variation@for (move in variations) {
            for ((index, card) in move.withIndex()) {
                if (!state.table.canDefend(
                        state.opponent,
                        state.player,
                        attackingCards[index],
                        card
                    )
                )
                    continue@variation
            }

            val player = state.player.copy()
            val opponent = state.opponent.copy()
            val table = state.table.copy().apply {
                move.forEachIndexed { index, card ->
                    defend(opponent, player, attackingCards[index], card)
                }
            }

            children.add(DefenseNode(
                state = State(
                    table = table,
                    player = player,
                    opponent = opponent,
                ),
                parent = this,
            ))
        }
    }
}
