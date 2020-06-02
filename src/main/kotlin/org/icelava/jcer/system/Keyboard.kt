package org.icelava.jcer.system
import org.icelava.jcer.paint.*

val listeners: List<Listener> = mutableListOf()

object Keyboard {
    // Disable: private val sc = Scanner(System.`in`)

    fun key() = Painter.withPos(0, max_n - 1) { System.`in`.read() }
}

class Listener(val matcher: Regex, val callback: (String) -> Unit) {
    init {
        listeners.plus(this)
    }
}