package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.input.InputEvent
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.search.Search
import io.github.smaugfm.game2048.search.Search.Companion.FindBestMoveResult
import io.github.smaugfm.game2048.ui.AnimationSpeed
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UiBoard
import korlibs.io.async.launchAsap
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class MainScene(
    private var inputManager: KorgeInputManager,
    private val gs: GameState,
    private val history: History,
    private val boardFactory: BoardFactory<Board4>,
    private val search: Search,
    private val uiBoard: UiBoard,
    private val staticUi: StaticUi,
) : Scene() {
    private var isAnimationRunning = false
    private var isGameOverModal = false

    private var board: Board4 = boardFactory.createEmpty()

    override suspend fun SContainer.sceneMain() {
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
                        InputEvent.ClickInput.AiButtonClick       -> {
                            if (isGameOverModal) return@collect
                            gs.isAiPlaying.update(!gs.isAiPlaying.value)
                        }

                        InputEvent.ClickInput.RestartClick        ->
                            if (!gs.isAiPlaying.value)
                                restart()

                        InputEvent.ClickInput.TryAgainClick       ->
                            if (!gs.isAiPlaying.value)
                                restart()

                        InputEvent.ClickInput.UndoClick           -> {
                            if (isGameOverModal || gs.isAiPlaying.value) return@collect
                            restoreField(history.undo())
                        }

                        InputEvent.ClickInput.AnimationSpeedClick -> {
                            if (isGameOverModal || !gs.isAiPlaying.value) return@collect

                            gs.animationSpeed =
                                when (gs.animationSpeed) {
                                    AnimationSpeed.Normal      -> AnimationSpeed.Fast
                                    AnimationSpeed.Fast        -> AnimationSpeed.NoAnimation
                                    AnimationSpeed.NoAnimation -> AnimationSpeed.Normal
                                }
                        }

                        is InputEvent.DirectionInput              -> {
                            if (isGameOverModal) return@collect
                            if (!gs.isAiPlaying.value)
                                handleUserDirectionInput(inputEvent.dir)
                        }
                    }
                }
        }

        gs.isAiPlaying.observe {
            if (it) {
                launchAsap {
                    startAiPlay()
                }
            }
        }
        if (gs.isAiPlaying.value)
            startAiPlay()
    }

    private suspend fun startAiPlay() {
        var bestMoveResult: FindBestMoveResult? =
            search.findBestMove(board)
        while (bestMoveResult != null && gs.isAiPlaying.value) {
            var (newBoard, moves) = board.moveGenerateMoves(bestMoveResult.direction)

            val prevBestMove = bestMoveResult
            val animationDeferred = animateMoves(moves)

            val tilePlacementRes = newBoard.placeRandomTile() ?: break
            newBoard = tilePlacementRes.newBoard

            bestMoveResult = search.findBestMove(newBoard)
            animationDeferred.await()

            gs.aiDepth.update(prevBestMove.maxDepth)
            gs.aiElapsedMs.update(prevBestMove.elapsedMs)
            gs.moveNumber.update(gs.moveNumber.value + 1)

            uiBoard.createNewBlock(tilePlacementRes.tile, tilePlacementRes.index)

            board = newBoard
            history.add(board.tiles(), gs.score.value)
        }

        gs.isAiPlaying.update(false)
        if (bestMoveResult == null) {
            gameOver()
        }
    }

    private suspend fun handleUserDirectionInput(direction: Direction) {
        if (isAnimationRunning) {
            return
        }
        hideAiStats()

        val (newBoard, moves) = board.moveGenerateMoves(direction)
        if (board != newBoard) {
            animateMoves(moves).await()
            board = newBoard
            gs.moveNumber.update(gs.moveNumber.value + 1)
            generateBlockAndSave()
            if (!board.hasAvailableMoves())
                gameOver()
        }

    }

    private fun hideAiStats() {
        gs.showAiStats.update(false)
        gs.aiDepth.update(0)
        gs.aiElapsedMs.update(0f)
    }

    private fun animateMoves(
        moves: List<BoardMove>,
    ): Deferred<Unit> {
        val cd = CompletableDeferred<Unit>()
        isAnimationRunning = true
        launchImmediately {
            uiBoard.animate(moves) {
                updateScore(moves)
                isAnimationRunning = false
                cd.complete(Unit)
            }
        }
        return cd
    }

    private fun updateScore(moves: List<BoardMove>) {
        val points = moves
            .filterIsInstance<BoardMove.Merge>()
            .sumOf { it.newTile.score }
        gs.score.update(gs.score.value + points)
    }

    private suspend fun gameOver() {
        if (!isGameOverModal) {
            isGameOverModal = true
            staticUi.showGameOver(uiBoard)
        }
    }

    private fun restart() {
        hideAiStats()
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
        hideAiStats()
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
