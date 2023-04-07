package ai.nodes

import ai.*

class AttackNode(
    state: State,
    override val parent: INode?,
) : Node(state, parent) {
    override fun generateNodes() {
        // Player attacked successfully => opponent can defend or take up.
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
