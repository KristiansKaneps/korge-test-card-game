package ai.nodes

import ai.*

class DefenseNode(
    state: State,
    override val parent: INode?,
) : Node(state, parent) {
    override fun generateNodes() {
        // Player defended successfully => opponent can counter-attack or draw.
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
