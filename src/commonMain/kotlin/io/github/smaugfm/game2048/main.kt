package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.MoveBoardResult
import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.heuristics.impl.NneonneoAnySizeHeuristics
import io.github.smaugfm.game2048.input.InputEvent
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.UIConstants
import io.github.smaugfm.game2048.ui.UiBoard
import io.github.smaugfm.game2048.ui.UiBoard.Companion.addBoard
import io.github.smaugfm.game2048.ui.addStaticUi
import io.github.smaugfm.game2048.ui.showGameOver
import korlibs.image.color.RGBA
import korlibs.io.async.ObservableProperty
import korlibs.io.async.async
import korlibs.io.async.launch
import korlibs.io.async.launchImmediately
import korlibs.io.concurrent.createFixedThreadDispatcher
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Container
import korlibs.korge.view.Stage
import korlibs.math.geom.Size
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlin.properties.Delegates

const val boardSize = 4
const val boardArraySize = boardSize * boardSize
const val maxAiDepth = 3

var uiConstants: UIConstants by Delegates.notNull()
var uiBoard: UiBoard by Delegates.notNull()

var globalBoard = AnySizeBoard()
var expectimax = AnySizeExpectimax(NneonneoAnySizeHeuristics())
private val aiDispatcher = Dispatchers.createFixedThreadDispatcher("ai", 2)

var isAnimationRunning = false
var isGameOverModal = false
val score = ObservableProperty(0)
val best = ObservableProperty(0)
var isAiPlaying = ObservableProperty(false)
var inputManager: KorgeInputManager by Delegates.notNull()

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
    if (!uiConstants.history.isEmpty()) {
        restoreField(uiConstants.history.currentElement)
    } else {
        generateBlockAndSave()
    }

    inputManager.eventsFlow().collect { inputEvent ->
        when (inputEvent) {
            InputEvent.ClickInput.AiToggleClick ->
                isAiPlaying.update(!isAiPlaying.value)

            InputEvent.ClickInput.RestartClick ->
                restart()

            InputEvent.ClickInput.TryAgainClick ->
                restart()

            InputEvent.ClickInput.UndoClick ->
                if (!isAiPlaying.value)
                    restoreField(uiConstants.history.undo())

            is InputEvent.DirectionInput.AiDirection -> TODO()
            is InputEvent.DirectionInput.UserDirection ->
                handleMoveBlocks(inputEvent.dir)
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
                gameOver(this)
                break
            }
            var (newBoard, moves) = moveResult
            animateMoves(moves) {
                waitForAnimation.complete(Unit)
            }
            val newTile = newBoard.placeRandomTile() ?: break
            newBoard = newTile.newBoard

            uiConstants.history.add(globalBoard.tiles(), score.value)

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
            if (!globalBoard.hasAvailableMoves())
                gameOver(this)
        }
    }

}

private fun gameOver(container: Container) {
    if (!isGameOverModal) {
        isGameOverModal = true
        container.showGameOver {
            restart()
        }
    }
}

private fun CoroutineScope.animateMoves(
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

fun restart() {
    isGameOverModal = false
    globalBoard = AnySizeBoard()
    uiBoard.clear()
    score.update(0)
    uiConstants.history.clear()
    generateBlockAndSave()
}

fun generateBlockAndSave() {
    val (newBoard, power, index) = globalBoard.placeRandomTile() ?: return
    globalBoard = newBoard
    uiBoard.createNewBlock(power, index)
    uiConstants.history.add(globalBoard.tiles(), score.value)
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
    inputManager = KorgeInputManager(this)
    uiConstants = UIConstants.create(views)

    best.update(storage.getOrNull("best")?.toInt() ?: 0)
    score.observe {
        if (it > best.value) best.update(it)
    }
    best.observe {
        storage["best"] = it.toString()
    }
}
