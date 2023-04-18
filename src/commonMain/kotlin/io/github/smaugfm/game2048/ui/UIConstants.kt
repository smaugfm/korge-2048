package io.github.smaugfm.game2048.ui

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.Font
import korlibs.image.font.readTtfFont
import korlibs.image.format.readBitmap
import korlibs.inject.AsyncInjector
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.Views
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.time.seconds

class UIConstants private constructor(
    views: Views,
    val font: Font,
    val fontBold: Font,
    val restartImg: Bitmap,
    val undoImg: Bitmap
) {
    val tileSize = views.virtualWidth / 5.0
    val tilePadding = tileSize / 12
    val rectRadius = tileSize / 18
    val rectCorners = RectCorners(rectRadius)

    companion object {

        suspend operator fun invoke(injector: AsyncInjector) {
            injector.mapSingleton {
                UIConstants(
                    get(),
                    resourcesVfs["clear_sans.ttf"].readTtfFont(),
                    resourcesVfs["clear_sans_bold.ttf"].readTtfFont(),
                    resourcesVfs["restart.png"].readBitmap(),
                    resourcesVfs["undo.png"].readBitmap()
                )
            }
        }

//        val moveAnimationDuration = 0.0375.seconds
//        val scaleAnimationDuration = 0.05.seconds
//        val moveAnimationDuration = 0.075.seconds
//        val scaleAnimationDuration = 0.1.seconds
        val moveAnimationDuration = 0.15.seconds
        val scaleAnimationDuration = 0.2.seconds

        val accentColor = Colors["#EDC403"]
        val backgroundColor = Colors["#BBAE9E"]
        val backgroundColorLight = Colors["#CEC0B2"]
        val labelBackgroundColor = Colors["#47413B"]
        val labelColor = RGBA(239, 226, 210)
        val textColor = Colors.WHITE
        val gameOverTextColor = Colors.BLACK

        val windowSize = Size(480, 640)
        val virtualSize = windowSize * 2
    }
}
