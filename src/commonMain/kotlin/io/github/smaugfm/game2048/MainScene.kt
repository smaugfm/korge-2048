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
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import io.github.smaugfm.game2048.ui.UiBoard
import io.github.smaugfm.game2048.ui.UiBoard.Companion.addBoard
import korlibs.io.async.ObservableProperty
import korlibs.io.async.launch
import korlibs.io.async.launchImmediately
import korlibs.io.concurrent.createFixedThreadDispatcher
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.storage
import korlibs.korge.view.SContainer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

@Suppress("RemoveEmptyPrimaryConstructor")
class MainScene() : Scene() {

    private var globalBoard = AnySizeBoard()
    private var expectimax = AnySizeExpectimax(NneonneoAnySizeHeuristics())
    private val aiDispatcher = Dispatchers.createFixedThreadDispatcher("ai", 2)
    private var isAnimationRunning = false
    private var isGameOverModal = false

    private lateinit var best: ObservableProperty<Int>
    private lateinit var score: ObservableProperty<Int>
    private lateinit var isAiPlaying: ObservableProperty<Boolean>

    private lateinit var history: History
    private lateinit var inputManager: KorgeInputManager

    private lateinit var staticUi: StaticUi
    private lateinit var uiBoard: UiBoard
    private lateinit var uiConstants: UIConstants

    override suspend fun SContainer.sceneInit() {
        with(injector) {
            best = get<GameState>().best
            score = get<GameState>().score
            isAiPlaying = get<GameState>().isAiPlaying
            inputManager = get()
            history = get()
            staticUi = get()
            uiConstants = get()
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
                        restoreField(history.undo())

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

    private fun SContainer.handleMoveBlocks(direction: Direction) {
        if (isAnimationRunning) {
            return
        }

        val (newBoard, moves) = globalBoard.moveGenerateMoves(direction)
        if (globalBoard != newBoard) {
            animateMoves(moves) {
                globalBoard = newBoard
                generateBlockAndSave()
                if (!globalBoard.hasAvailableMoves())
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
        launch(aiDispatcher) {
            var moveResultDeferred: Deferred<MoveBoardResult<AnySizeBoard>?>
            var moveResult = expectimax.findBestMove(globalBoard)
            while (true) {
                val waitForAnimation = CompletableDeferred<Unit>()

                if (moveResult == null) {
                    isAiPlaying.update(false)
                    gameOver()
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

    private fun restart() {
        isGameOverModal = false
        globalBoard = AnySizeBoard()
        uiBoard.clear()
        score.update(0)
        history.clear()
        generateBlockAndSave()
    }

    private fun generateBlockAndSave() {
        val (newBoard, power, index) = globalBoard.placeRandomTile() ?: return
        globalBoard = newBoard
        uiBoard.createNewBlock(power, index)
        history.add(globalBoard.tiles(), score.value)
    }

    private fun restoreField(historyElement: History.Element) {
        uiBoard.clear()
        globalBoard = AnySizeBoard(historyElement.tiles.map { it.power }.toIntArray())
        score.update(historyElement.score)
        globalBoard.tiles().forEachIndexed { i, tile ->
            if (tile.isNotEmpty) {
                uiBoard.createNewBlock(tile, i)
            }
        }
    }

}
