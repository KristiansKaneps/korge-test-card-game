package game.cards

enum class CardColor(
    val hexCode: String,
) {
    RED("#FF0000"),
    BLACK("#000000");

    override fun toString(): String = hexCode
}
