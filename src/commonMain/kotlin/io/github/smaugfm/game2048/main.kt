package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.ai.Ai
import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveGenerator
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.UiBoard
import io.github.smaugfm.game2048.ui.UiBoard.Companion.addBoard
import io.github.smaugfm.game2048.ui.addStaticUi
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.Font
import korlibs.image.font.readTtfFont
import korlibs.image.format.readBitmap
import korlibs.io.async.ObservableProperty
import korlibs.io.async.launchAsap
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.korge.input.*
import korlibs.korge.service.storage.storage
import korlibs.korge.view.*
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.time.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlin.properties.Delegates

const val cellPadding = 10
const val rectRadius = 5.0
const val boardSize = 4
const val boardArraySize = boardSize * boardSize
var btnSize: Double = 0.0
var cellSize: Double = 0.0
val moveAnimationDuration = 0.15.seconds
val scaleAnimationDuration = 0.2.seconds
val accentColor = Colors["#edc403"]
val backgroundColor = Colors["#bbae9e"]
val backgroundColorLight = Colors["#cec0b2"]
val labelColor = RGBA(239, 226, 210)
val textColor = Colors.WHITE
val gameOverTextColor = Colors.BLACK
val rectCorners = RectCorners(rectRadius)
var font: Font by Delegates.notNull()
var fontBold: Font by Delegates.notNull()
var restartImg: Bitmap by Delegates.notNull()
var undoImg: Bitmap by Delegates.notNull()
var history: History by Delegates.notNull()
var isAnimationRunning = false
var isGameOver = false
val score = ObservableProperty(0)
val best = ObservableProperty(0)
var uiBoard: UiBoard by Delegates.notNull()
var board = Board()
var isAiPlaying = ObservableProperty(false)

suspend fun main() = Korge(
    KorgeConfig(
        windowSize = Size(480, 640),
        title = "2048",
        backgroundColor = RGBA(253, 247, 240),
    )
) {
    setupGame()
    uiBoard = addBoard()
    addStaticUi()
    if (!history.isEmpty()) {
        restoreField(history.currentElement)
    } else {
        generateBlockAndSave()
    }

    keys {
        down {
            if (isAiPlaying.value)
                return@down
            when (it.key) {
                Key.LEFT -> moveBlocksTo(Direction.LEFT)
                Key.RIGHT -> moveBlocksTo(Direction.RIGHT)
                Key.UP -> moveBlocksTo(Direction.TOP)
                Key.DOWN -> moveBlocksTo(Direction.BOTTOM)
                else -> Unit
            }
        }
    }
    onSwipe(20.0) {
        if (isAiPlaying.value)
            return@onSwipe
        when (it.direction) {
            SwipeDirection.LEFT -> moveBlocksTo(Direction.LEFT)
            SwipeDirection.RIGHT -> moveBlocksTo(Direction.RIGHT)
            SwipeDirection.TOP -> moveBlocksTo(Direction.TOP)
            SwipeDirection.BOTTOM -> moveBlocksTo(Direction.BOTTOM)
        }
    }
    isAiPlaying.observe {
        if (it)
            startAiPlay()
    }
}

fun Stage.startAiPlay() {
    launchAsap {
        while (isAiPlaying.value) {
            val waitForAnimation = CompletableDeferred<Unit>()
            val dir = Ai.bestNextMove(board)
            moveBlocksTo(dir) {
                waitForAnimation.complete(Unit)
            }
            waitForAnimation.await()
        }
    }
}

fun Stage.moveBlocksTo(direction: Direction, onEnd: () -> Unit = {}) {
    if (isAnimationRunning) {
        println("should not be here")
        return
    }

    if (!MoveGenerator.hasAvailableMoves(board)) {
        if (!isGameOver) {
            isGameOver = true
            showGameOver {
                restart()
            }
        }
    }

    val (newBoard, moves) = MoveGenerator.moveBoard(board, direction)
    if (board != newBoard) {
        isAnimationRunning = true
        launchImmediately {
            uiBoard.animate(moves) {
                board = newBoard
                val points = moves
                    .filterIsInstance<MoveGenerator.BoardMove.Move>()
                    .sumOf { board[it.to].score }
                score.update(score.value + points)
                generateBlockAndSave()
                isAnimationRunning = false
            }
            onEnd()
        }
    } else {
        onEnd()
    }
}

fun Container.showGameOver(onRestart: () -> Unit) = container {
    fun restart() {
        this@container.removeFromParent()
        onRestart()
    }
    position(uiBoard.pos)
    roundRect(uiBoard.size, rectCorners, Colors["#FFFFFF33"])
    text("Game Over", 60f, textColor, font) {
        centerOn(uiBoard)
        y -= 60
    }
    text("Try again", 40f, gameOverTextColor, font) {
        centerOn(uiBoard)
        y += 20
        onOver {
            onOver { color = RGBA(90, 90, 90) }
            onOut { color = RGBA(0, 0, 0) }
            onDown { color = RGBA(120, 120, 120) }
            onUp { color = RGBA(120, 120, 120) }
            onClick { restart() }
        }
    }
    keys.down {
        when (it.key) {
            Key.ENTER, Key.SPACE -> restart()
            else -> Unit
        }
    }
}

fun restart() {
    isGameOver = false
    board = Board()
    uiBoard.clear()
    score.update(0)
    history.clear()
    generateBlockAndSave()
}

fun generateBlockAndSave() {
    val (power, index) = MoveGenerator.placeRandomBlock(board) ?: return
    uiBoard.createNewBlock(power, index)
    history.add(board.powers(), score.value)
}

fun restoreField(historyElement: History.Element) {
    uiBoard.clear()
    board = Board(historyElement.powers.map { it.power }.toIntArray())
    score.update(historyElement.score)
    historyElement.powers.forEachIndexed { i, tile ->
        if (tile.isNotEmpty) {
            uiBoard.createNewBlock(tile, i)
        }
    }
}

private suspend fun Stage.setupGame() {
    val storage = views.storage
    restartImg = resourcesVfs["restart.png"].readBitmap()
    undoImg = resourcesVfs["undo.png"].readBitmap()
    font = resourcesVfs["clear_sans.ttf"].readTtfFont()
    fontBold = resourcesVfs["clear_sans_bold.ttf"].readTtfFont()
    cellSize = views.virtualWidth / 5.0
    btnSize = cellSize * 0.3

    history = History(storage.getOrNull("history")) {
        storage["history"] = it.toString()
    }
    best.update(storage.getOrNull("best")?.toInt() ?: 0)
    score.observe {
        if (it > best.value) best.update(it)
    }
    best.observe {
        storage["best"] = it.toString()
    }
}
