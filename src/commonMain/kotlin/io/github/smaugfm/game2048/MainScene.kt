package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.Board
import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.input.InputEvent
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import io.github.smaugfm.game2048.ui.UiBoard
import io.github.smaugfm.game2048.ui.UiBoard.Companion.addBoard
import korlibs.io.async.ObservableProperty
import korlibs.io.async.launchAsap
import korlibs.io.async.launchImmediately
import korlibs.io.concurrent.createFixedThreadDispatcher
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.storage
import korlibs.korge.view.SContainer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainScene<T: Board<T>> : Scene() {
    private val aiDispatcher = Dispatchers.createFixedThreadDispatcher("ai", 2)
    private var isAnimationRunning = false
    private var isGameOverModal = false

    private lateinit var best: ObservableProperty<Int>
    private lateinit var score: ObservableProperty<Int>
    private lateinit var isAiPlaying: ObservableProperty<Boolean>

    private lateinit var history: History
    private lateinit var inputManager: KorgeInputManager

    private lateinit var boardFactory: BoardFactory<T>
    private lateinit var board: T
    private lateinit var expectimax: Expectimax<T>

    private lateinit var staticUi: StaticUi
    private lateinit var uiConstants: UIConstants

    private lateinit var uiBoard: UiBoard


    override suspend fun SContainer.sceneInit() {
        with(injector) {
            best = get<GameState>().best
            score = get<GameState>().score
            isAiPlaying = get<GameState>().isAiPlaying

            inputManager = get()
            history = get()

            boardFactory = get()
            board = boardFactory.createEmpty()
            staticUi = get()
            uiConstants = get()

            expectimax = get()
        }

        val storage = views.storage

        best.update(storage.getOrNull("best")?.toInt() ?: 0)
        score.observe {
            if (it > best.value) best.update(it)
        }
        best.observe {
            storage["best"] = it.toString()
        }
    }

    override suspend fun SContainer.sceneMain() {
        uiBoard = addBoard(views, uiConstants)
        staticUi.addStaticUi(this, uiBoard)
        if (!history.isEmpty()) {
            restoreField(history.currentElement)
        } else {
            generateBlockAndSave()
        }

        launchImmediately {
            inputManager
                .eventsFlow()
                .collect { inputEvent ->
                    when (inputEvent) {
                        InputEvent.ClickInput.AiToggleClick ->
                            isAiPlaying.update(!isAiPlaying.value)

                        InputEvent.ClickInput.RestartClick ->
                            restart()

                        InputEvent.ClickInput.TryAgainClick ->
                            restart()

                        InputEvent.ClickInput.UndoClick ->
                            if (!isAiPlaying.value)
                                restoreField(history.undo())

                        is InputEvent.DirectionInput.AiDirection -> {
                            TODO()
                        }

                        is InputEvent.DirectionInput.UserDirection ->
                            handleMoveBlocks(inputEvent.dir)
                    }
                }
        }

        isAiPlaying.observe {
            if (it)
                startAiPlay()
        }
        if (isAiPlaying.value)
            startAiPlay()
    }

    private fun SContainer.handleMoveBlocks(direction: Direction) {
        if (isAnimationRunning) {
            return
        }

        val (newBoard, moves) = board.moveGenerateMoves(direction)
        if (board != newBoard) {
            animateMoves(moves) {
                board = newBoard
                generateBlockAndSave()
                if (!board.hasAvailableMoves())
                    gameOver()
            }
        }

    }

    private fun animateMoves(
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

    private fun SContainer.gameOver() {
        if (!isGameOverModal) {
            isGameOverModal = true
            staticUi.showGameOver(uiBoard, this)
        }
    }

    private fun SContainer.startAiPlay() {
        launchAsap(aiDispatcher) {
            var bestDirectionDeferred: Deferred<Direction?>
            var bestDirection = expectimax.findBestDirection(board)
            while (true) {
                val waitForAnimation = CompletableDeferred<Unit>()

                if (bestDirection == null) {
                    isAiPlaying.update(false)
                    gameOver()
                    break
                }
                var (newBoard, moves) = board.moveGenerateMoves(bestDirection)
                animateMoves(moves) {
                    waitForAnimation.complete(Unit)
                }
                val newTile = newBoard.placeRandomTile() ?: break
                newBoard = newTile.newBoard

                history.add(board.tiles(), score.value)

                if (!isAiPlaying.value) {
                    break
                }
                bestDirectionDeferred = async(aiDispatcher) {
                    expectimax.findBestDirection(newBoard)
                }
                waitForAnimation.await()
                uiBoard.createNewBlock(newTile.tile, newTile.index)

                board = newBoard
                bestDirection = bestDirectionDeferred.await()
            }
        }
    }

    private fun restart() {
        isGameOverModal = false
        board = boardFactory.createEmpty()
        uiBoard.clear()
        score.update(0)
        history.clear()
        generateBlockAndSave()
    }

    private fun generateBlockAndSave() {
        val (newBoard, power, index) = board.placeRandomTile() ?: return
        board = newBoard
        uiBoard.createNewBlock(power, index)
        history.add(board.tiles(), score.value)
    }

    private fun restoreField(historyElement: History.Element) {
        uiBoard.clear()
        board = boardFactory.fromTiles(historyElement.tiles)
        score.update(historyElement.score)
        board.tiles().forEachIndexed { i, tile ->
            if (tile.isNotEmpty) {
                uiBoard.createNewBlock(tile, i)
            }
        }
    }
}
