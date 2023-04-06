package ai.nodes

import ai.*
import game.*

interface INode {
    val depth: Int

    val parent: INode?
    val children: List<INode>

    /**
     * Extra data that can be used to understand the node.
     */
    var extra: Any?

    /**
     * The state of the game at this node.
     */
    fun currentState(): State

    /**
     * Whether the children of this node have been generated.
     */
    var isExpanded: Boolean

    /**
     * Whether the evaluation of this node has been calculated and is final.
     */
    var isEvaluated: Boolean

    /**
     * The last evaluation of this node.
     */
    var evaluation: Float

    /**
     * Evaluates this node and returns the evaluation.
     */
    fun evaluate(game: Game): Float

    /**
     * Expands this node by generating its children.
     */
    fun generate(game: Game)
}
