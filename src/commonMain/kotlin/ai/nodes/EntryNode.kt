package ai.nodes

import ai.*

/**
 * The entry node of the game tree.
 */
class EntryNode(
    state: State,
) : Node(state, null) {
    override fun generateNodes() {
        children.add(PlayerNode(
            state = state,
            parent = this,
        ))
    }
}
