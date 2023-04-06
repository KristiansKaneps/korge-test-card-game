package ai

import ai.nodes.*
import game.*

class Tree(
    var deltaDepth: Int = 10,
) {
    lateinit var root: INode

    fun generate(game: Game) {
        val player: IPlayer
        val opponent: IPlayer
        if (game.turnPhase.isPlayer2Turn()) {
            player = game.player2
            opponent = game.player1
        } else {
            player = game.player1
            opponent = game.player2
        }

        root = EntryNode(
            state = State(
                table = game.table,
                player = player,
                opponent = opponent,
            ),
        )

        generateUpToDepth(game, root.depth + deltaDepth)

        println("Generated ${sumTotalNodes()} nodes.")
        println("Dead end nodes: ${getDeadEndNodes().size}")
        println("Root node eval: ${root.evaluate(game)}; depth=1 eval: ${root.children.map { it.evaluate(game) }.joinToString(", ")}")
    }

    private fun generateUpToDepth(game: Game, depth: Int) {
        var currentDepth = root.depth
        val waiting = mutableListOf(root)
        while (waiting.size > 0) {
            val node = waiting.removeFirst()
            if (node.depth > depth) continue
            node.generate(game)
            if (node.depth != currentDepth) {
                currentDepth = node.depth
                println("Currently generating at depth=$currentDepth")
            }
            waiting.addAll(node.children)
//            println("Generated ${node.type.name}, remaining: ${waiting.size}, depth: ${node.depth},\t children: ${node.children.size},\t card count: ${node.currentState().table.stack.size}, ${node.currentState().player.hand.size}, ${node.currentState().opponent.hand.size}")
        }

        // Finally, evaluate all the nodes by evaluating the root node.
        root.evaluate(game)
    }

    fun getNextBestNode(): INode? {
        return root.children.maxByOrNull { it.evaluation }
    }

    fun updateRoot(game: Game, newRoot: INode = root) {
        root = newRoot
        generateUpToDepth(game, root.depth + deltaDepth)
        println("Updating root to @${root::class.simpleName}, evaluation=${root.evaluation}")
    }

    fun getDeadEndNodes(parent: INode = root): List<INode> {
        val deadEnds = mutableListOf<INode>()
        for (child in parent.children) {
            if (child.isExpanded && child.children.isEmpty())
                deadEnds.add(child)
            else
                deadEnds.addAll(getDeadEndNodes(child))
        }
        return deadEnds
    }

    fun sumTotalNodes(parent: INode = root, depthRange: IntRange? = null): Int {
        var total = 0
        for (child in parent.children) {
            if (depthRange == null || child.depth in depthRange)
                total += sumTotalNodes(child, depthRange)
            else if (child.depth !in depthRange)
                break
        }
        return total + 1
    }
}
