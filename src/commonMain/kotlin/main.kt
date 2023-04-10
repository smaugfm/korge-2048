import Block.Companion.block
import korlibs.event.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.io.async.*
import korlibs.io.async.ObservableProperty
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.animate.*
import korlibs.korge.input.*
import korlibs.korge.service.storage.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import korlibs.render.*
import korlibs.time.*
import kotlin.collections.set
import kotlin.properties.*
import kotlin.random.*

var cellSize: Double = 0.0
var btnSize: Double = 0.0
var fieldSize: Double = 0.0
var leftIndent: Double = 0.0
var topIndent: Double = 0.0
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

var map = PositionMap()
val blocks = mutableMapOf<Int, Block>()
var freeId = 0

suspend fun main() =
    Korge(
        KorgeConfig(
            windowSize = Size(480, 640),
            title = "2048",
            backgroundColor = RGBA(253, 247, 240),
            quality = GameWindow.Quality.PERFORMANCE
        )
    ) {
        println("vasa")
        setupProps()
        addUi()
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

fun columnX(number: Int) = leftIndent + 10 + (cellSize + 10) * number
fun rowY(number: Int) = topIndent + 10 + (cellSize + 10) * number

fun Stage.moveBlocksTo(direction: Direction) {
    if (isAnimationRunning) return

    if (!map.hasAvailableMoves()) {
        if (!isGameOver) {
            isGameOver = true
            showGameOver {
                restart()
            }
        }
    }

    val moves = mutableListOf<Pair<Int, Position>>()
    val merges = mutableListOf<Triple<Int, Int, Position>>()

    val newMap = calculateNewMap(map.copy(), direction, moves, merges)
    if (map != newMap) {
        isAnimationRunning = true
        showAnimation(moves, merges) {
            // when animation ends
            map = newMap
            generateBlockAndSave()
            isAnimationRunning = false
            // new code here
            val points = merges.sumOf { numberFor(it.first).value }
            score.update(score.value + points)
        }
    }
}

fun Stage.showAnimation(
    moves: List<Pair<Int, Position>>,
    merges: List<Triple<Int, Int, Position>>,
    onEnd: () -> Unit
) = launchImmediately {
    animate {
        parallel {
            moves.forEach { (id, pos) ->
                moveTo(blocks[id]!!, columnX(pos.x), rowY(pos.y), 0.15.seconds, Easing.LINEAR)
            }
            merges.forEach { (id1, id2, pos) ->
                sequence {
                    parallel {
                        moveTo(
                            blocks[id1]!!,
                            columnX(pos.x),
                            rowY(pos.y),
                            0.15.seconds,
                            Easing.LINEAR
                        )
                        moveTo(
                            blocks[id2]!!,
                            columnX(pos.x),
                            rowY(pos.y),
                            0.15.seconds,
                            Easing.LINEAR
                        )
                    }
                    block {
                        val nextNumber = numberFor(id1)
                            .next()
                        deleteBlock(id1)
                        deleteBlock(id2)
                        createNewBlockWithId(id1, nextNumber, pos)
                    }
                    sequenceLazy {
                        animateScale(blocks[id1]!!)
                    }
                }
            }
        }
        block {
            onEnd()
        }
    }
}

fun Animator.animateScale(block: Block) {
    val x = block.x
    val y = block.y
    val scale = block.scale
    tween(
        block::x[x - 4],
        block::y[y - 4],
        block::scale[scale + 0.1],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
    tween(
        block::x[x],
        block::y[y],
        block::scale[scale],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
}

private operator fun Scale.plus(d: Double): Scale =
    Scale(scaleX + d, scaleY + d)

fun calculateNewMap(
    map: PositionMap,
    direction: Direction,
    moves: MutableList<Pair<Int, Position>>,
    merges: MutableList<Triple<Int, Int, Position>>
): PositionMap {
    val newMap = PositionMap()
    val startIndex = when (direction) {
        Direction.LEFT, Direction.TOP -> 0
        Direction.RIGHT, Direction.BOTTOM -> 3
    }
    var columnRow = startIndex

    fun newPosition(line: Int) = when (direction) {
        Direction.LEFT -> Position(columnRow++, line)
        Direction.RIGHT -> Position(columnRow--, line)
        Direction.TOP -> Position(line, columnRow++)
        Direction.BOTTOM -> Position(line, columnRow--)
    }

    for (line in 0..3) {
        var curPos = map.getNotEmptyPositionFrom(direction, line)
        columnRow = startIndex
        while (curPos != null) {
            val newPos = newPosition(line)
            val curId = map[curPos.x, curPos.y]
            map[curPos.x, curPos.y] = -1

            val nextPos = map.getNotEmptyPositionFrom(direction, line)
            val nextId = nextPos?.let { map[it.x, it.y] }
            //two blocks are equal
            if (nextId != null && numberFor(curId) == numberFor(nextId)) {
                //merge these blocks
                map[nextPos.x, nextPos.y] = -1
                newMap[newPos.x, newPos.y] = curId
                merges += Triple(curId, nextId, newPos)
            } else {
                //add old block
                newMap[newPos.x, newPos.y] = curId
                moves += Pair(curId, newPos)
            }
            curPos = map.getNotEmptyPositionFrom(direction, line)
        }
    }
    return newMap
}


fun numberFor(blockId: Int) = blocks[blockId]!!.number
fun deleteBlock(blockId: Int) =
    blocks.remove(blockId)!!.removeFromParent()

fun Container.showGameOver(onRestart: () -> Unit) = container {
    fun restart() {
        this@container.removeFromParent()
        onRestart()
    }
    position(leftIndent, topIndent)
    roundRect(Size(fieldSize, fieldSize), rectCorners, Colors["#FFFFFF33"])
    text("Game Over", 60f, Colors.BLACK, font) {
        centerBetween(0.0, 0.0, fieldSize, fieldSize)
        y -= 60
    }
    text("Try again", 40f, Colors.BLACK, font) {
        centerBetween(0.0, 0.0, fieldSize, fieldSize)
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

fun Container.restart() {
    isGameOver = false
    map = PositionMap()
    blocks.values.forEach { it.removeFromParent() }
    blocks.clear()
    score.update(0)
    history.clear()
    generateBlockAndSave()
}

fun Container.generateBlockAndSave() {
    val position = map.getRandomFreePosition() ?: return
    val number = if (Random.nextDouble() < 0.9) Number.ZERO else Number.ONE
    val newId = createNewBlock(number, position)
    map[position.x, position.y] = newId
    history.add(map.toNumberIds(), score.value)
}

fun Container.createNewBlock(number: Number, position: Position): Int {
    val id = freeId++
    createNewBlockWithId(id, number, position)
    return id
}

fun Container.createNewBlockWithId(id: Int, number: Number, position: Position) {
    blocks[id] = block(number)
        .position(
            columnX(position.x),
            rowY(position.y)
        )
}

private fun Stage.addUi() {
    val bgLogo = addLogo()
    val bgField = addField()
    val bgBest = addStat("BEST", best) {
        alignRightToRightOf(bgField)
        alignTopToTopOf(bgLogo)
    }
    addStat("SCORE", score) {
        alignRightToLeftOf(bgBest, 24)
        alignTopToTopOf(bgBest)
    }

    addButtons(bgBest, bgField)
}

fun Container.restoreField(history: History.Element) {
    map.forEach { if (it != -1) deleteBlock(it) }
    map = PositionMap()
    score.update(history.score)
    freeId = 0
    val numbers = history.numberIds.map {
        if (it >= 0 && it < Number.values().size)
            Number.values()[it]
        else null
    }
    numbers.forEachIndexed { i, number ->
        if (number != null) {
            val newId = createNewBlock(number, Position(i % 4, i / 4))
            map[i % 4, i / 4] = newId
        }
    }
}

private suspend fun Stage.setupProps() {
    val storage = views.storage
    restartImg = resourcesVfs["restart.png"].readBitmap()
    undoImg = resourcesVfs["undo.png"].readBitmap()
    font = resourcesVfs["clear_sans.ttf"].readTtfFont()
    cellSize = views.virtualWidth / 5.0
    btnSize = cellSize * 0.3
    fieldSize = 50 + 4 * cellSize
    leftIndent = (views.virtualWidth - fieldSize) / 2
    topIndent = 150.0

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
