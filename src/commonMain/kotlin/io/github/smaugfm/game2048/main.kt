package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.Board.Companion.addBoard
import io.github.smaugfm.game2048.PositionMap.Companion.positionMap
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
import kotlin.random.Random

var cellSize: Double = 0.0
const val cellPadding = 10
var btnSize: Double = 0.0
const val rectRadius = 5.0
val rectCorners = RectCorners(rectRadius)
var font: Font by Delegates.notNull()
var restartImg: Bitmap by Delegates.notNull()
var undoImg: Bitmap by Delegates.notNull()
var history: History by Delegates.notNull()
var isAnimationRunning = false
var isGameOver = false
val score = ObservableProperty(0)
val best = ObservableProperty(0)
var board: Board by Delegates.notNull()
const val boardSize = 4
const val boardArraySize = boardSize * boardSize

var map = positionMap()

suspend fun main() = Korge(
    KorgeConfig(
        windowSize = Size(480, 640),
        title = "2048",
        backgroundColor = RGBA(253, 247, 240),
    )
) {
    setupGame()
    board = addBoard()
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

    if (!MoveGenerator.hasAvailableMoves(map)) {
        if (!isGameOver) {
            isGameOver = true
            showGameOver {
                restart()
            }
        }
    }

    val (newMap, moves) = MoveGenerator.moveMap(map, direction)
    if (map != newMap) {
        isAnimationRunning = true
        launchImmediately {
            board.animate(moves) {
                map = newMap
                val points = moves.filter { it.merge }.sumOf { map.power(it.to).score }
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
    position(board.pos)
    roundRect(board.size, rectCorners, Colors["#FFFFFF33"])
    text("Game Over", 60f, Colors.BLACK, font) {
        centerOn(board)
        y -= 60
    }
    text("Try again", 40f, Colors.BLACK, font) {
        centerOn(board)
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
    map = positionMap()
    board.clear()
    score.update(0)
    history.clear()
    generateBlockAndSave()
}

fun generateBlockAndSave() {
    val index = map.getRandomFreeIndex() ?: return
    val power = if (Random.nextDouble() < 0.9) PowerOfTwo(1) else PowerOfTwo(2)
    board.createNewBlock(power, index)
    map[index] = power.power
    history.add(map.powers(), score.value)
}

fun restoreField(historyElement: History.Element) {
    board.clear()
    map = positionMap()
    score.update(historyElement.score)
    historyElement.powers.forEachIndexed { i, power ->
        if (power > 0) {
            board.createNewBlock(PowerOfTwo(power), i)
            map[i] = power
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
