package game

enum class Phase {
    PLAYER1_ATTACK,
    PLAYER2_ATTACK,
    PLAYER1_DEFEND,
    PLAYER2_DEFEND,
    PLAYER1_COUNTERATTACK,
    PLAYER2_COUNTERATTACK,

    RESULT_PLAYER1_WINNER,
    RESULT_PLAYER2_WINNER,
    RESULT_PLAYERS_DRAW;

    fun next(): Phase = when (this) {
        PLAYER1_ATTACK -> PLAYER2_DEFEND
        PLAYER2_DEFEND -> PLAYER1_COUNTERATTACK
        PLAYER1_COUNTERATTACK -> PLAYER2_DEFEND
        PLAYER2_ATTACK -> PLAYER1_DEFEND
        PLAYER1_DEFEND -> PLAYER2_COUNTERATTACK
        PLAYER2_COUNTERATTACK -> PLAYER1_DEFEND
        else -> this
    }

    fun inverse(): Phase {
        if (ordinal >= RESULT_PLAYER1_WINNER.ordinal) return this
        if (ordinal % 2 == 0) return values()[ordinal + 1]
        return values()[ordinal - 1]
    }

    fun isPlayer1Turn() = this == PLAYER1_ATTACK || this == PLAYER1_DEFEND || this == PLAYER1_COUNTERATTACK
    fun isPlayer2Turn() = this == PLAYER2_ATTACK || this == PLAYER2_DEFEND || this == PLAYER2_COUNTERATTACK
}
