package io.github.smaugfm.game2048.ui

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.Font
import korlibs.image.font.readTtfFont
import korlibs.image.format.readBitmap
import korlibs.inject.Injector
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.Views
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlinx.coroutines.runBlocking

class UIConstants private constructor(
    views: Views,
    val font: Font,
    val fontBold: Font,
    val restartImg: Bitmap,
    val undoImg: Bitmap,
) {
    val tileSize = views.virtualWidth / 5.0
    val tilePadding = tileSize / 12
    val rectRadius = tileSize / 18
    val rectCorners = RectCorners(rectRadius)
    val buttonSize: Double = tileSize * 0.45
    val statHeight = tileSize * 0.8
    val padding = tileSize / 10
    val smallPadding = padding / 2

    companion object {

        operator fun invoke(injector: Injector) {
            injector.mapSingleton {
                runBlocking {
                    UIConstants(
                        get(),
                        resourcesVfs["clear_sans.ttf"].readTtfFont(),
                        resourcesVfs["clear_sans_bold.ttf"].readTtfFont(),
                        resourcesVfs["restart.png"].readBitmap(),
                        resourcesVfs["undo.png"].readBitmap()
                    )
                }
            }
        }

        val accentColor = Colors["#EDC403"]
        val backgroundColor = Colors["#BBAE9E"]
        val backgroundColorLight = Colors["#CEC0B2"]
        val labelBackgroundColor = Colors["#47413B"]
        val labelColor = RGBA(239, 226, 210)
        val textColor = Colors.WHITE
        val underboardLabelColor = backgroundColor
        val gameOverTextColor = Colors.BLACK

        val windowSize = Size(480, 640)
        val virtualSize = windowSize * 2
    }
}
