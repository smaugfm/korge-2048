package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.FindBestMove
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.FindBestMoveResult
import io.github.smaugfm.game2048.input.InputEvent
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.AnimationSpeed
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UiBoard
import io.github.smaugfm.game2048.ui.UiBoard.Companion.addBoard
import korlibs.io.async.ObservableProperty
import korlibs.io.async.launchAsap
import korlibs.io.async.launchImmediately
import korlibs.io.concurrent.createFixedThreadDispatcher
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainScene(
    private var inputManager: KorgeInputManager,
    private val gs: GameState,
    private val history: History,
    private val boardFactory: BoardFactory<Board4>,
    private val expectimax: FindBestMove,
    private val staticUi: StaticUi
) : Scene() {
    private val aiDispatcher =
        Dispatchers.createFixedThreadDispatcher("ai", 2)
    private var isAnimationRunning = false
    private var isGameOverModal = false

    private val isAiStopping: ObservableProperty<Boolean> = ObservableProperty(false)
    private var board: Board4 = boardFactory.createEmpty()

    private lateinit var uiBoard: UiBoard

    override suspend fun SContainer.sceneMain() {
        uiBoard = addBoard(injector)
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
                        InputEvent.ClickInput.AiButtonClick -> {
                            if (isGameOverModal) return@collect
                            if (!gs.isAiPlaying.value)
                                gs.isAiPlaying.update(true)
                            else
                                isAiStopping.update(true)
                        }

                        InputEvent.ClickInput.RestartClick ->
                            if (!gs.isAiPlaying.value)
                                restart()

                        InputEvent.ClickInput.TryAgainClick ->
                            if (!gs.isAiPlaying.value)
                                restart()

                        InputEvent.ClickInput.UndoClick -> {
                            if (isGameOverModal || gs.isAiPlaying.value) return@collect
                            restoreField(history.undo())
                        }

                        InputEvent.ClickInput.AnimationSpeedClick -> {
                            if (isGameOverModal || !gs.isAiPlaying.value) return@collect

                            gs.aiAnimationSpeed.update(
                                when (gs.aiAnimationSpeed.value) {
                                    AnimationSpeed.Normal -> AnimationSpeed.Fast
                                    AnimationSpeed.Fast -> AnimationSpeed.Faster
                                    AnimationSpeed.Faster -> AnimationSpeed.Normal
                                }
                            )
                        }

                        is InputEvent.DirectionInput -> {
                            if (isGameOverModal) return@collect
                            if (!gs.isAiPlaying.value)
                                handleMoveBlocks(inputEvent.dir)
                        }
                    }
                }
        }

        gs.isAiPlaying.observe {
            if (it)
                startAiPlay()
        }
        if (gs.isAiPlaying.value)
            startAiPlay()
    }

    private fun SContainer.startAiPlay() {
        launchAsap(aiDispatcher) {
            var bestMoveResultDeferred: Deferred<FindBestMoveResult?> =
                CompletableDeferred(null)
            var bestMoveResult = expectimax.findBestMove(board)
            isAiStopping.observe {
                if (it)
                    bestMoveResultDeferred.cancel()
            }
            while (!isAiStopping.value) {
                val waitForAnimation = CompletableDeferred<Unit>()

                if (bestMoveResult == null) {
                    gs.isAiPlaying.update(false)
                    gameOver()
                    break
                }
                var (newBoard, moves) = board.moveGenerateMoves(bestMoveResult.direction)
                gs.aiDepth.update(bestMoveResult.maxDepth)
                gs.aiElapsedMs.update(bestMoveResult.elapsedMs)
                animateMoves(moves) {
                    waitForAnimation.complete(Unit)
                }
                val newTile = newBoard.placeRandomTile() ?: break
                newBoard = newTile.newBoard

                history.add(board.tiles(), gs.score.value)

                if (!gs.isAiPlaying.value) {
                    break
                }
                bestMoveResultDeferred = async(aiDispatcher) {
                    expectimax.findBestMove(newBoard)
                }
                waitForAnimation.await()

                gs.moveNumber.update(gs.moveNumber.value + 1)
                uiBoard.createNewBlock(newTile.tile, newTile.index)

                board = newBoard
                try {
                    bestMoveResult = bestMoveResultDeferred.await()
                } catch (e: CancellationException) {
                    break
                }
            }

            isAiStopping.update(false)
            gs.isAiPlaying.update(false)
        }
    }

    private fun SContainer.handleMoveBlocks(direction: Direction) {
        if (isAnimationRunning) {
            return
        }
        gs.aiDepth.update(0)
        gs.aiElapsedMs.update(0f)

        val (newBoard, moves) = board.moveGenerateMoves(direction)
        if (board != newBoard) {
            animateMoves(moves) {
                board = newBoard
                gs.moveNumber.update(gs.moveNumber.value + 1)
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
        gs.score.update(gs.score.value + points)
    }

    private fun SContainer.gameOver() {
        if (!isGameOverModal) {
            isGameOverModal = true
            staticUi.showGameOver(uiBoard, this)
        }
    }

    private fun restart() {
        isGameOverModal = false
        board = boardFactory.createEmpty()
        uiBoard.clear()
        gs.moveNumber.update(0)
        gs.score.update(0)
        history.clear()
        generateBlockAndSave()
    }

    private fun generateBlockAndSave() {
        val (newBoard, power, index) = board.placeRandomTile() ?: return
        board = newBoard
        uiBoard.createNewBlock(power, index)
        history.add(board.tiles(), gs.score.value)
    }

    private fun restoreField(historyElement: History.Element) {
        uiBoard.clear()
        board = boardFactory.fromTiles(historyElement.tiles)
        gs.score.update(historyElement.score)
        board.tiles().forEachIndexed { i, tile ->
            if (tile.isNotEmpty) {
                uiBoard.createNewBlock(tile, i)
            }
        }
    }
}
