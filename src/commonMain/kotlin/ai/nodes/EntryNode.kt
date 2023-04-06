package ai.nodes

import ai.*

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
