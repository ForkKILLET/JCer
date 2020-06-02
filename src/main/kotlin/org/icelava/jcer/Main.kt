package org.icelava.jcer
import doc.*
import org.icelava.jcer.system.*

class Main {
    companion object {
        @ExperimentalStdlibApi @JvmStatic fun main(args: Array<String>) {
            ChangeLog.forever()
            print(ChangeLog.format(ChangeLog.select("dev 3.0")!!))
            Keyboard.key()
        }
    }
}