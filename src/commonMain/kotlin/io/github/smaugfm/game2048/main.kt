package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.MoveBoardResult
import io.github.smaugfm.game2048.board.solve.AnySizeExpectimax
import io.github.smaugfm.game2048.board.solve.NneonneoAnySizeHeuristics
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
import korlibs.io.async.async
import korlibs.io.async.launch
import korlibs.io.async.launchImmediately
import korlibs.io.concurrent.createFixedThreadDispatcher
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
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.time.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlin.properties.Delegates

const val cellPadding = 10
const val rectRadius = 5.0
const val boardSize = 4
const val boardArraySize = boardSize * boardSize
const val maxAiDepth = 3
var btnSize: Double = 0.0
var cellSize: Double = 0.0

//val moveAnimationDuration = 0.0375.seconds
//val scaleAnimationDuration = 0.05.seconds
//val moveAnimationDuration = 0.075.seconds
//val scaleAnimationDuration = 0.1.seconds
val moveAnimationDuration = 0.15.seconds
val scaleAnimationDuration = 0.2.seconds
val accentColor = Colors["#EDC403"]
val backgroundColor = Colors["#BBAE9E"]
val backgroundColorLight = Colors["#CEC0B2"]
val labelBackgroundColor = Colors["#47413B"]
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
var isAiPlaying = ObservableProperty(false)

var globalBoard = AnySizeBoard()
var expectimax = AnySizeExpectimax(NneonneoAnySizeHeuristics())
private val aiDispatcher = Dispatchers.createFixedThreadDispatcher("ai", 2)

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
                Key.LEFT -> handleMoveBlocks(Direction.LEFT)
                Key.RIGHT -> handleMoveBlocks(Direction.RIGHT)
                Key.UP -> handleMoveBlocks(Direction.TOP)
                Key.DOWN -> handleMoveBlocks(Direction.BOTTOM)
                else -> Unit
            }
        }
    }
    onSwipe(20.0) {
        if (isAiPlaying.value)
            return@onSwipe
        when (it.direction) {
            SwipeDirection.LEFT -> handleMoveBlocks(Direction.LEFT)
            SwipeDirection.RIGHT -> handleMoveBlocks(Direction.RIGHT)
            SwipeDirection.TOP -> handleMoveBlocks(Direction.TOP)
            SwipeDirection.BOTTOM -> handleMoveBlocks(Direction.BOTTOM)
        }
    }
    isAiPlaying.observe {
        if (it)
            startAiPlay()
    }
    if (isAiPlaying.value)
        startAiPlay()
}

fun Stage.startAiPlay() {
    launch(aiDispatcher) {
        var moveResultDeferred: Deferred<MoveBoardResult<AnySizeBoard>?>
        var moveResult = expectimax.findBestMove(globalBoard)
        while (true) {
            val waitForAnimation = CompletableDeferred<Unit>()

            if (moveResult == null) {
                isAiPlaying.update(false)
                showGameOverIfNoMoves(false)
                break
            }
            var (newBoard, moves) = moveResult
            animateMoves(moves) {
                waitForAnimation.complete(Unit)
            }
            val newTile = newBoard.placeRandomTile() ?: break
            newBoard = newTile.newBoard

            history.add(globalBoard.tiles(), score.value)

            if (!isAiPlaying.value) {
                break
            }
            moveResultDeferred = async(aiDispatcher) {
                expectimax.findBestMove(newBoard)
            }
            waitForAnimation.await()
            uiBoard.createNewBlock(newTile.tile, newTile.index)

            globalBoard = newBoard
            moveResult = moveResultDeferred.await()
        }
    }
}

fun Stage.handleMoveBlocks(direction: Direction) {
    if (isAnimationRunning) {
        return
    }

    val (newBoard, moves) = globalBoard.moveGenerateMoves(direction)
    if (globalBoard != newBoard) {
        animateMoves(moves) {
            globalBoard = newBoard
            generateBlockAndSave()
            showGameOverIfNoMoves(globalBoard.hasAvailableMoves())
        }
    }

}

private fun Stage.showGameOverIfNoMoves(hasMoves: Boolean): Boolean {
    if (!hasMoves) {
        if (!isGameOver) {
            isGameOver = true
            showGameOver {
                restart()
            }
            return false
        }
    }
    return true
}

private fun Stage.animateMoves(
    moves: List<BoardMove>,
    onEnd: () -> Unit = {}
) {
    isAnimationRunning = true
    launchImmediately {
        uiBoard.animate(moves) {
            updateScore(moves)
            isAnimationRunning = false
            onEnd()
        }
    }
}

private fun updateScore(moves: List<BoardMove>) {
    val points = moves
        .filterIsInstance<BoardMove.Merge>()
        .sumOf { it.newTile.score }
    score.update(score.value + points)
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
    globalBoard = AnySizeBoard()
    uiBoard.clear()
    score.update(0)
    history.clear()
    generateBlockAndSave()
}

fun generateBlockAndSave() {
    val (newBoard, power, index) = globalBoard.placeRandomTile() ?: return
    globalBoard = newBoard
    uiBoard.createNewBlock(power, index)
    history.add(globalBoard.tiles(), score.value)
}

fun restoreField(historyElement: History.Element) {
    uiBoard.clear()
    globalBoard = AnySizeBoard(historyElement.tiles.map { it.power }.toIntArray())
    score.update(historyElement.score)
    globalBoard.tiles().forEachIndexed { i, tile ->
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
