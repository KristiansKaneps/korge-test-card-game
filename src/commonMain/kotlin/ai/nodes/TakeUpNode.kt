package ai.nodes

import ai.*

class TakeUpNode(
    state: State,
    override val parent: INode?,
) : Node(state, parent) {
    override fun generateNodes() {
        // Player took up the cards => switch turns.
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
