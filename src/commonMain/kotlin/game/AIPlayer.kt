package game

import ai.*
import ai.nodes.*
import game.cards.*

class AIPlayer(
    override val name: String = "AI Player",
    val tree: Tree = Tree(),
) : Player(name) {
    fun resolve(game: Game) {
        if (tree.root.children.isEmpty()) {
            game.determineWinner()
            return
        }

        println("AI is resolving current game state...")
        val possibleNodes = tree.root.children + tree.root.children.flatMap { it.children }
        possibleNodes.find {
            it.currentState().table == game.table
                && it.currentState().resolvePlayer1(game).hand == game.player1.hand
                && it.currentState().resolvePlayer2(game).hand == game.player2.hand
        }?.let { node ->
            println("AI found a matching node in tree (@${node::class.simpleName}): $node")
            tree.root = node
        } ?: run {
            throw Exception("Could not find child node with matching game state.")
        }
    }

    fun act(game: Game) {
        println("AI is acting on game state...")
        val node = tree.root
        if (node.children.isEmpty()) {
            game.determineWinner()
            return
        }

        val nextNode = tree.getNextBestNode()!!
        println("Next node: ${nextNode::class.simpleName} (descendant of ${nextNode.parent!!::class.simpleName})")

        if (nextNode is PlayerNode && nextNode.currentState().player is AIPlayer) {
            tree.updateRoot(game, nextNode)
            act(game)
        }

        if (node.currentState().player is AIPlayer) {
            if (nextNode is DefenseNode) {
                @Suppress("UNCHECKED_CAST")
                nextNode.currentState().table.board.entries.forEach { (attackingCard, defendingCard) ->
                    if (game.table.board[attackingCard] == null && !game.playerActions.defend(attackingCard, defendingCard!!))
                        throw Exception("@${nextNode::class.simpleName}: AI tried to illegally defend against $attackingCard with $defendingCard.")
                }
            } else if (nextNode is AttackNode) {
                if (!game.playerActions.attack(nextNode.currentState().table.getAttackingCards()))
                    throw Exception("@${nextNode::class.simpleName}: AI tried to illegally attack with ${nextNode.currentState().table.getAttackingCards()}.")
            } else if (nextNode is TakeUpNode) {
                if (!game.playerActions.takeUp())
                    throw Exception("@${nextNode::class.simpleName}: AI tried to illegally take up.")
            } else if (nextNode is DrawNode) {
                if (!game.playerActions.canDraw())
                    throw Exception("@${nextNode::class.simpleName}: AI tried to illegally draw.")
            } else if (nextNode is PlayerNode) {
            }
            tree.updateRoot(game, nextNode)
        }
    }

    override fun copy(): IPlayer {
        return AIPlayer(name, tree).apply {
            hand = this@AIPlayer.hand.copy()
        }
    }

    override fun toString(): String {
        return "AIPlayer(name='$name'; hand=$hand)"
    }
}
