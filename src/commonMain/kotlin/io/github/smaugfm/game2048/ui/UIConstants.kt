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
import korlibs.time.seconds

class UIConstants private constructor(
    views: Views,
    val font: Font,
    val fontBold: Font,
    val restartImg: Bitmap,
    val undoImg: Bitmap
) {
    var cellSize = views.virtualWidth / 5.0
    var btnSize: Double = cellSize * 0.3

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

        internal val moveAnimationDuration = 0.0375.seconds
        internal val scaleAnimationDuration = 0.05.seconds
//        internal val moveAnimationDuration = 0.075.seconds
//        internal val scaleAnimationDuration = 0.1.seconds
//        internal val moveAnimationDuration = 0.15.seconds
//        internal val scaleAnimationDuration = 0.2.seconds


        internal val accentColor = Colors["#EDC403"]
        internal val backgroundColor = Colors["#BBAE9E"]
        internal val backgroundColorLight = Colors["#CEC0B2"]
        internal val labelBackgroundColor = Colors["#47413B"]
        internal val labelColor = RGBA(239, 226, 210)
        internal val textColor = Colors.WHITE
        internal val gameOverTextColor = Colors.BLACK


        internal const val cellPadding = 10.0
        internal const val rectRadius = 5.0
        internal val rectCorners = RectCorners(rectRadius)
    }
}
