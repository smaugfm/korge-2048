package io.github.smaugfm.game2048.util

inline fun fastForLoop(start: Int, to: Int, block: (i: Int) -> Unit) {
    var i = start
    while (i < to) {
        block(i)
        i++
    }
}

inline fun fastRepeat(times: Int, block: (i: Int) -> Unit) {
    fastForLoop(0, times, block)
}
