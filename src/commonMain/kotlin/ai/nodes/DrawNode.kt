package ai.nodes

import ai.*

class DrawNode(
    state: State,
    override val parent: INode?,
) : Node(state, parent) {
    override fun generateNodes() {
        children.add(PlayerNode(
            state = State(
                table = state.table,
                player = state.opponent,
                opponent = state.player,
            ),
            parent = this,
        ))
    }
}
