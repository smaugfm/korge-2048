import Number.EIGHT
import Number.ELEVEN
import Number.FIFTEEN
import Number.FIVE
import Number.FOUR
import Number.FOURTEEN
import Number.NINE
import Number.ONE
import Number.SEVEN
import Number.SIX
import Number.SIXTEEN
import Number.TEN
import Number.THIRTEEN
import Number.THREE
import Number.TWELVE
import Number.TWO
import Number.ZERO
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.addTo
import korlibs.korge.view.centerBetween
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.Size

class Block(val number: Number) : Container() {
    init {
        roundRect(Size(cellSize, cellSize), rectCorners, fill = number.color)
        val textColor =
            when (number) {
                ZERO, Number.ONE -> Colors.BLACK
                else -> Colors.WHITE
            }
        text(
            number.value.toString(),
            textSizeFor(number),
            textColor,
            font
        ) {
            centerBetween(0.0, 0.0, cellSize, cellSize)
        }
    }

    private fun textSizeFor(number: Number) =
        when (number) {
            ZERO, ONE, TWO, THREE, FOUR, FIVE -> cellSize / 2
            SIX, SEVEN, EIGHT -> cellSize * 4 / 9
            NINE, TEN, ELEVEN, TWELVE -> cellSize * 2 / 5
            THIRTEEN, FOURTEEN, FIFTEEN -> cellSize * 7 / 20
            SIXTEEN -> cellSize * 3 / 10
        }.toFloat()

    companion object {
        fun Container.block(number: Number) =
            Block(number).addTo(this)

    }
}
