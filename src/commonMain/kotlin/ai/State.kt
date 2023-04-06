package ai

import game.*

class State(
    val table: Table,
    val player: IPlayer,
    val opponent: IPlayer,
) {
    fun resolvePlayer1(game: Game): IPlayer =
        if (game.player1.name == player.name) player else opponent
    fun resolvePlayer2(game: Game): IPlayer =
        if (game.player2.name == player.name) player else opponent

    fun copy() = State(
        table = table.copy(),
        player = player.copy(),
        opponent = opponent.copy(),
    )

    override fun toString(): String {
        return "State(board=${table.board}; player=$player; opponent=$opponent)"
    }
}
