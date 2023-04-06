package scenes

import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.Easing
import game.*
import game.cards.*
import graphics.*
import graphics.CardTextures.CARD_HEIGHT
import graphics.CardTextures.CARD_WIDTH
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.*

class GameScene(
    val game: Game,
) : Scene() {
    private val scope: CoroutineScope = this

    private var prevTurn = 0

    private var highCardImage: Image? = null
    private var stackImages = ArrayList<Image>()
    private var graveStackImages = ArrayList<Image>()
    private var rerenderStack = false
    private var rerenderGraveStack = false

    private val playerHandImages = HashMap<Card, Image>()
    private val opponentHandImages = HashMap<Card, Image>()
    private var rerenderPlayerHand = false
    private var rerenderOpponentHand = false

    private var table: RoundRect? = null
    private val tableImages = HashMap<Card, Image>()
    private var rerenderTable = false
    private lateinit var endTurnButton: UIButton
    private lateinit var takeUpButton: UIButton

    private enum class CardState {
        NONE,
        HOVER,
        DRAG,
    }

    private fun isCardNone(card: Card): Boolean =
        playerHandCardStates[card] == CardState.NONE

    private fun isCardHovered(card: Card): Boolean =
        playerHandCardStates[card] == CardState.HOVER

    private fun isCardDragged(card: Card): Boolean =
        playerHandCardStates[card] == CardState.DRAG

    private val cardAnimationContinuations: MutableMap<Card, CancellableContinuation<Unit>?> =
        HashMap()
    private val playerHandCardStates: MutableMap<Card, CardState> = HashMap()

    private fun SContainer.animateCardStates() {
        scope.launch {
            playerHandCardStates.forEach { (card, state) ->
                val image = playerHandImages[card]
                if (image == null) {
                    cardAnimationContinuations[card]?.cancel()
                    cardAnimationContinuations[card] = null
                    return@forEach
                }
                when (state) {
                    CardState.NONE -> {
                        cardAnimationContinuations[card]?.cancel()
                        val rotation = (image.getExtra("rotation") ?: 0.0) as Double
                        val scale = (image.getExtra("scale") ?: 1.0) as Double
                        val x = (image.getExtra("x") ?: 0.0) as Double
                        val y = (image.getExtra("y") ?: 0.0) as Double
                        image.zIndex = 2.0
                        cardAnimationContinuations[card] = image.tweenNoWait(
                            image::rotation[rotation.radians],
                            image::scale[scale],
                            image::x[x],
                            image::y[y],
                            time = 0.1.seconds,
                            easing = Easing.EASE_OUT
                        )?.c
                    }

                    CardState.HOVER -> {
                        cardAnimationContinuations[card]?.cancel()
                        val rotation = (image.getExtra("rotation") ?: 0.0) as Double
                        val scale = (image.getExtra("scale") ?: 1.0) as Double
                        val x = (image.getExtra("x") ?: 0.0) as Double
                        val y = (image.getExtra("y") ?: 0.0) as Double
                        val bx = sin(rotation) * image.width * image.scale
                        val by = -cos(rotation) * image.height * image.scale * 0.025
                        image.zIndex = 3.0
                        cardAnimationContinuations[card] = image.tweenNoWait(
                            image::rotation[rotation.radians],
                            image::scale[scale + 0.033],
                            image::x[x + bx],
                            image::y[y + by],
                            time = 0.25.seconds,
                            easing = Easing.EASE_OUT
                        )?.c
                    }
                    CardState.DRAG -> {
                        cardAnimationContinuations[card]?.cancel()
                        val scale = (image.getExtra("scale") ?: 1.0) as Double
                        image.zIndex = 3.0
                        cardAnimationContinuations[card] = image.tweenNoWait(
                            image::rotation[0.radians],
                            image::scale[scale + 0.033],
                            time = 0.05.seconds,
                            easing = Easing.EASE
                        )?.c
                    }
                    else -> {}
                }
            }
        }
    }

    override suspend fun SContainer.sceneMain() {
        val circleRadius = 4.0
        val circleRandMax = 1.0 - circleRadius / width
        val circleRandMin = circleRadius / width

        val circles = (1..25).map {
            circle(4.0, Colors.ANTIQUEWHITE).position(
                width * Random.nextDouble(
                    circleRandMin,
                    circleRandMax
                ), height * Random.nextDouble(circleRandMin, circleRandMax)
            )
        }

        playerHandCardStates.putAll(game.player1.hand.cards.map { it to CardState.NONE })

        val buttonWidth = 100.0
        val buttonHeight = 50.0
        val buttonX = width / 2 - buttonWidth / 2
        val buttonY = height * 0.75 - buttonHeight - 16.0
        endTurnButton = uiButton(label = "End Turn", width = buttonWidth, height = buttonHeight) {
            position(buttonX - buttonWidth / 2 - 8.0, buttonY)
            this.enabled = false
            onClick {
                this@uiButton.enabled = false
                val actionDone = game.playerActions.done()
                this@uiButton.enabled = !actionDone
            }
        }
        takeUpButton = uiButton(label = "Take Up", width = buttonWidth, height = buttonHeight) {
            position(buttonX + buttonWidth / 2 + 8.0, buttonY)
            this.enabled = false
            onClick {
                this@uiButton.enabled = false
                game.playerActions.takeUp()
                val actionDone = game.playerActions.done()
                this@uiButton.enabled = !actionDone
            }
        }

        renderTable()
        renderCardStack()
        renderCardGrave()
        renderPlayerHand()
        renderOpponentHand()

        addUpdater {
            launch {
                if (prevTurn != game.turnNumber) {
                    prevTurn = game.turnNumber
                    rerenderStack = true
                    rerenderGraveStack = true
                    rerenderTable = true
                    rerenderPlayerHand = true
                    rerenderOpponentHand = true

                    when (game.turnPhase) {
                        Phase.PLAYER1_COUNTERATTACK -> {
                            endTurnButton.enabled = true
                        }
                        Phase.PLAYER1_DEFEND -> {
                            takeUpButton.enabled = true
                        }
                        else -> {
                            endTurnButton.enabled = false
                            takeUpButton.enabled = false
                        }
                    }

                    if (game.turnPhase == Phase.RESULT_PLAYER1_WINNER) {
                        sceneContainer.changeTo({ ResultScene("${game.player1.name} wins!") })
                    } else if (game.turnPhase == Phase.RESULT_PLAYER2_WINNER) {
                        sceneContainer.changeTo({ ResultScene("${game.player2.name} wins!") })
                    } else if (game.turnPhase == Phase.RESULT_PLAYERS_DRAW) {
                        sceneContainer.changeTo({ ResultScene("It's a draw!") })
                    }
                }

                if (rerenderStack) {
                    rerenderStack = false
                    renderCardStack()
                }
                if (rerenderGraveStack) {
                    rerenderGraveStack = false
                    renderCardGrave()
                }
                if (rerenderTable) {
                    rerenderTable = false
                    renderTable()
                }
                if (rerenderPlayerHand) {
                    rerenderPlayerHand = false
                    renderPlayerHand()
                }
                if (rerenderOpponentHand) {
                    rerenderOpponentHand = false
                    renderOpponentHand()
                }
            }
            animateCardStates()
        }

        while (true) {
            animate {
                parallelLazy {
                    circles.forEach {
                        val rand = Random.nextFloat()
                        it.scaleTo(rand, rand, time = 2.seconds, easing = Easing.EASE_IN_OUT)
                    }
                }
            }
        }
    }

    private suspend fun SContainer.renderTable() {
        val tableX0 = width * 0.2
        val tableX1 = width * 0.8
        val tableY0 = height * 0.125
        val tableY1 = height * 0.75

        val tableWidth = tableX1 - tableX0
        val tableHeight = tableY1 - tableY0

        if (table != null) {
            table!!.removeFromParent()
            table = null
        }

        table = roundRect(
            tableWidth,
            tableHeight,
            8.0,
            8.0,
            Colors.TRANSPARENT_BLACK,
            Colors.DARKSLATEGRAY,
            3.0
        ) {
            position(tableX0, tableY0)
            zIndex = 1.0
        }

        tableImages.values.forEach {
            it.removeFromParent()
        }
        tableImages.clear()

        val cardsInOneRow = 6
        val cardWidth = ((tableWidth - (cardsInOneRow + 1) * 16.0) / cardsInOneRow)
        val scale = cardWidth / CARD_WIDTH
        val cardHeight = CARD_HEIGHT * scale

        game.table.getCards().forEachIndexed { outerIndex, cards ->
            val row = floor(outerIndex / cardsInOneRow.toFloat())
            val ox = tableX0 + 16.0 + (16.0 + cardWidth) * outerIndex
            val oy = tableY0 + 16.0 + row * (48.0 + cardHeight)
            cards.forEachIndexed { innerIndex, card ->
                tableImages.put(card, image(CardTextures[card]) {
                    position(ox, oy + 32.0 * innerIndex)
                    rotation(0.radians)
                    anchor(0.0, 0.0)
                    scale(scale)
                })
            }
        }
    }

    private suspend fun SContainer.renderCardStack() {
        val highCard = game.table.highCard
        val stackSize = max(0, game.table.stack.size - 1)

        val stackNextCardOffset = 2
        val x0 = 16.0 + stackNextCardOffset * stackSize
        val yc = height / 2

        val scale = (this@renderCardStack.width / 12) / CARD_WIDTH

        if (highCardImage != null) {
            highCardImage!!.removeFromParent()
            highCardImage = null
        }
        stackImages.forEach {
            it.removeFromParent()
        }
        stackImages.clear()

        if (game.table.stack.size > 0) {
            highCardImage = image(CardTextures[highCard]) {
                position(x0 + 16.0, yc)
                rotation(90.degrees)
                anchor(0.5, 1.0)
                scale(scale)
            }
        }

        for (i in 0 until stackSize) {
            stackImages.add(image(CardTextures.back) {
                position(x0 - i * stackNextCardOffset, yc - i * stackNextCardOffset / 2)
                anchor(0.0, .5)
                scale(scale)
                zIndex = 1.0
            })
        }
    }

    private var prevGraveSize: Int = 0
    private suspend fun SContainer.renderCardGrave() {
        val stackSize = game.grave.size
        if (prevGraveSize == stackSize) return
        prevGraveSize = stackSize

        val stackNextCardOffset = 2
        val x0 = width - 16.0 - stackNextCardOffset * stackSize
        val yc = height / 2

        val scale = (this@renderCardGrave.width / 12) / CARD_WIDTH

        graveStackImages.forEach {
            it.removeFromParent()
        }
        graveStackImages.clear()

        for (i in 0 until stackSize) {
            graveStackImages.add(image(CardTextures.back) {
                position(x0 + i * stackNextCardOffset, yc - i * stackNextCardOffset / 2)
                anchor(1.0, .5)
                rotation(((Random.nextDouble() - 0.5) * (PI / 18)).radians)
                scale(scale)
                zIndex = 1.0
            })
        }
    }

    private suspend fun SContainer.renderOpponentHand() {
        val player = game.player2

        val handSizeRatio = 0.15
        val cardSizeRatio = 1.15

        val R = width * 2
        val angleOffset = acos((width * handSizeRatio) / (2.0 * R))
        val viewableArc = PI - 2 * angleOffset
        val arcLength = viewableArc / (2 * PI) * PI * 2 * R
        val originX = width / 2
        val originY = -R * sin(angleOffset) + 16.0

        val cardWidth = min(100.0, arcLength / (player.hand.slots - 1) * cardSizeRatio)
        val cardAngle = if(player.hand.size < 2) 0.0 else viewableArc / (player.hand.size - 1)

        val maxPossibleCardAngle = viewableArc / 2

        val scale = cardWidth / CARD_WIDTH
        val yOffset = -sin(maxPossibleCardAngle) * (CARD_WIDTH * scale / 2) // due to mid anchor

        // Clear all images (temporary fix for card count change bbox).
        opponentHandImages.forEach { (_, image) -> image.removeFromParent() }.also { opponentHandImages.clear() }
        // Remove images only for cards that are no longer in hand (todo: fix card count change bbox).
        val iterator = opponentHandImages.iterator()
        while(iterator.hasNext()) {
            val (card, image) = iterator.next()
            if (!player.hand.cards.contains(card)) {
                image.removeFromParent()
                iterator.remove()
            }
        }

        player.hand.cards.forEachIndexed { indexAsc, card ->
            val index = player.hand.cards.size - 1 - indexAsc // reverse order
            val callback: (Image.() -> Unit) = {
                val rotation = PI - (viewableArc / 2 - cardAngle * index)
                val x = originX + R * cos(angleOffset + cardAngle * index)
                val y = originY + R * sin(angleOffset + cardAngle * index) + yOffset
                position(x, y)
                anchor(0.5, 1.0)
                scale(scale)
                this.rotation = rotation.radians
                this.zIndex = 2.0

                setExtra("x", x)
                setExtra("y", y)
                setExtra("scale", scale)
                setExtra("rotation", rotation)
            }

            if (opponentHandImages.contains(card)) {
                opponentHandImages[card]!!.callback()
            } else {
                opponentHandImages[card] = image(CardTextures.back) {
                    callback()
                }
            }
        }
    }

    private suspend fun SContainer.renderPlayerHand() {
        val player = game.player1

        game.player1.hand.cards.forEach { card ->
            if (!playerHandCardStates.containsKey(card)) {
                playerHandCardStates[card] = CardState.NONE
            }
        }

        val handSizeRatio = 0.25
        val cardSizeRatio = 1.25

        val R = width * 2
        val angleOffset = acos((width * handSizeRatio) / (2.0 * R))
        val viewableArc = PI - 2 * angleOffset
        val arcLength = viewableArc / (2 * PI) * PI * 2 * R
        val originX = width / 2
        val originY = height + R * sin(angleOffset) - 16.0

        val cardWidth = min(100.0, arcLength / (player.hand.slots - 1) * cardSizeRatio)
        val cardAngle = if(player.hand.size < 2) 0.0 else viewableArc / (player.hand.size - 1)

        val maxPossibleCardAngle = viewableArc / 2

        val scale = cardWidth / CARD_WIDTH
        val yOffset = -sin(maxPossibleCardAngle) * (CARD_WIDTH * scale / 2) // due to mid anchor

        // Clear all images (temporary fix for card count change bbox).
        playerHandImages.forEach { (_, image) -> image.removeFromParent() }.also { playerHandImages.clear() }
        // Remove images only for cards that are no longer in hand (todo: fix card count change bbox).
        val iterator = playerHandImages.iterator()
        while(iterator.hasNext()) {
            val (card, image) = iterator.next()
            if (!player.hand.cards.contains(card)) {
                image.removeFromParent()
                iterator.remove()
            }
        }

        player.hand.cards.forEachIndexed { indexAsc, card ->
            val index = player.hand.cards.size - 1 - indexAsc // reverse order
            val callback: (Image.() -> Unit) = {
                val image = this
                val rotation = -(cardAngle * index - viewableArc / 2)
                val x = originX + R * cos(angleOffset + cardAngle * index)
                val y = originY - R * sin(angleOffset + cardAngle * index) + yOffset
                position(x, y)
                anchor(0.5, 1.0)
                scale(scale)
                this.rotation = rotation.radians
                this.zIndex = 2.0

                setExtra("x", x)
                setExtra("y", y)
                setExtra("scale", scale)
                setExtra("rotation", rotation)

                var dragAnchorX = 0.0
                var dragAnchorY = 0.0
                var isDragging = false

                onMouseDrag {
                    if (it.end) {
                        isDragging = false
                        val mx = mouse.currentPosStage.x
                        val my = mouse.currentPosStage.y
                        if (table != null && (table!!.x < mx && table!!.y < my && table!!.x + table!!.width > mx && table!!.y + table!!.height > my)) {
                            if (game.turnPhase == Phase.PLAYER1_ATTACK || game.turnPhase == Phase.PLAYER1_COUNTERATTACK) {
                                if (game.playerActions.attack(setOf(card))) {
                                    rerenderPlayerHand = true
                                    rerenderTable = true
                                    endTurnButton.enabled = true
                                } else {
                                    playerHandCardStates[card] = CardState.NONE
                                }
                            } else if (game.turnPhase == Phase.PLAYER1_DEFEND) {
                                var cardUsed = false
                                val attackingCardImages = tableImages.entries.filter { (attackingCard, _) ->
                                    game.table.getAttackingCards().contains(attackingCard)
                                }
                                @Suppress("NAME_SHADOWING")
                                for ((attackingCard, image) in attackingCardImages) {
                                    // Check attacking card bounding boxes to determine against which card to defend [with current card].
                                    if (image.x < mx && image.y < my && image.x + image.width > mx && image.y + image.height > my) {
                                        if(game.playerActions.defend(attackingCard, card)) {
                                            cardUsed = true
                                            rerenderPlayerHand = true
                                            rerenderTable = true
                                        }
                                        break
                                    }
                                }
                                if (!cardUsed) {
                                    playerHandCardStates[card] = CardState.NONE
                                }
                                if (game.table.getAttackingCards().isEmpty()) {
                                    endTurnButton.enabled = true
                                }
                            } else {
                                playerHandCardStates[card] = CardState.NONE
                            }
                        } else {
                            playerHandCardStates[card] = CardState.NONE
                        }
                    } else if (it.start && !isDragging) {
                        isDragging = true
                        dragAnchorX = image.x - mouse.currentPosStage.x
                        dragAnchorY = image.y - mouse.currentPosStage.y
                        if (game.turnPhase.isPlayer1Turn())
                            playerHandCardStates[card] = CardState.DRAG
                    } else if (game.turnPhase.isPlayer1Turn()) {
                        val mx = mouse.currentPosStage.x + dragAnchorX
                        val my = mouse.currentPosStage.y + dragAnchorY
                        position(mx, my)
                    }
                }

                addUpdater {
                    if (mouse.isOver && isCardNone(card)) {
                        playerHandCardStates[card] = CardState.HOVER
                    }
                }
            }
            if (playerHandImages.contains(card)) {
                playerHandImages[card]!!.callback()
            } else {
                playerHandImages[card] = image(CardTextures[card]) {
                    callback()
                }
            }
        }

        addUpdater {
            playerHandCardStates.keys.forEach { card ->
                playerHandImages[card]?.let {
                    if (!it.mouse.isOver && isCardHovered(card)) {
                        playerHandCardStates[card] = CardState.NONE
                    }
                }
            }
        }
    }
}
