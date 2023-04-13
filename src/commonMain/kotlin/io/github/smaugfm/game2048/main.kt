package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveGenerator
import io.github.smaugfm.game2048.core.PowerOfTwo
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
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.keys
import korlibs.korge.input.onClick
import korlibs.korge.input.onDown
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.input.onSwipe
import korlibs.korge.input.onUp
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Container
import korlibs.korge.view.Stage
import korlibs.korge.view.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.properties.Delegates

const val cellPadding = 10
const val rectRadius = 5.0
const val boardSize = 4
const val boardArraySize = boardSize * boardSize
var btnSize: Double = 0.0
var cellSize: Double = 0.0
val rectCorners = RectCorners(rectRadius)
var font: Font by Delegates.notNull()
var restartImg: Bitmap by Delegates.notNull()
var undoImg: Bitmap by Delegates.notNull()
var history: History by Delegates.notNull()
var isAnimationRunning = false
var isGameOver = false
val score = ObservableProperty(0)
val best = ObservableProperty(0)
var uiBoard: UiBoard by Delegates.notNull()
var board = Board()

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
        when (it.direction) {
            SwipeDirection.LEFT -> moveBlocksTo(Direction.LEFT)
            SwipeDirection.RIGHT -> moveBlocksTo(Direction.RIGHT)
            SwipeDirection.TOP -> moveBlocksTo(Direction.TOP)
            SwipeDirection.BOTTOM -> moveBlocksTo(Direction.BOTTOM)
        }
    }
}

fun Stage.moveBlocksTo(direction: Direction) {
    if (isAnimationRunning) return

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
                    .sumOf { board.power(it.to).score }
                score.update(score.value + points)
                generateBlockAndSave()
                isAnimationRunning = false
            }
        }
    }
}

fun Container.showGameOver(onRestart: () -> Unit) = container {
    fun restart() {
        this@container.removeFromParent()
        onRestart()
    }
    position(uiBoard.pos)
    roundRect(uiBoard.size, rectCorners, Colors["#FFFFFF33"])
    text("Game Over", 60f, Colors.BLACK, font) {
        centerOn(uiBoard)
        y -= 60
    }
    text("Try again", 40f, Colors.BLACK, font) {
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
    board = Board(historyElement.powers)
    score.update(historyElement.score)
    historyElement.powers.forEachIndexed { i, power ->
        if (power > 0) {
            uiBoard.createNewBlock(PowerOfTwo(power), i)
        }
    }
}

private suspend fun Stage.setupGame() {
    val storage = views.storage
    restartImg = resourcesVfs["restart.png"].readBitmap()
    undoImg = resourcesVfs["undo.png"].readBitmap()
    font = resourcesVfs["clear_sans.ttf"].readTtfFont()
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
