package org.icelava.jcer.paint
import org.icelava.jcer.*

const val max_m = 80
const val max_n = 25

object Painter {
    init {
        clearScreen()
        with(Coc) {
            hl = true
            fg = ColorT.Black
            bg = ColorT.White
        }
    }

    object Cur : Pos(max_m, max_n) {
        override var x: Int = 0
            set(value) {
                if (value < 0 || value >= max_m)
                    throw WrongVarRangeException("Painter.Pos.x", 0, max_m - 1, value)
                print("\u001b[${y + 1};${value + 1}H")
                field = value
            }
        override var y: Int = 0
            set(value) {
                if (value < 0 || value >= max_n)
                    throw WrongVarRangeException("Painter.Pos.y", 0, max_n - 1, value)
                print("\u001b[${value + 1};${x + 1}H")
                field = value
            }
    }

    object Coc: Color() {
        override var fg = ColorT.Black
            set(value) {
                print("\u001b[${value.ordinal + 30 + if (hl) 60 else 0}m")
                field = value
            }
        override var bg = ColorT.White
            set (value) {
                print("\u001b[${value.ordinal + 40 + if (hl) 60 else 0}m")
                field = value
            }
        override var hl = true
            set(value) {
                field = value
                fg = fg
            }
    }

    fun clearScreen() {
        print("\u001b[2J")
        Cur.home()
    }

    fun fillScreen(bg: ColorT = ColorT.White) {
        Cur.home()
        withColor(bg = bg) {
            print("\n")
            repeat(max_n) { print(" " * max_m + if (it == max_n - 1) "" else "\n") }
        }
        Cur.home()
    }

    fun toDefault() {
        print("\u001b[0m")
        clearScreen()
    }

    fun withColor(fg: ColorT = Coc.fg, bg: ColorT = Coc.bg, hl: Boolean = false, callback: SimpleFun) {
        val orgF = Coc.fg; val orgB = Coc.bg; val orgH = Coc.hl
        Coc.fg = fg; Coc.bg = bg; Coc.hl = hl
        callback()
        Coc.fg = orgF; Coc.bg = orgB; Coc.hl = orgH
    }

    fun withPos(x: Int = Cur.x, y: Int = Cur.y, callback: SimpleFun) {
        val orgX = Cur.x; val orgY = Cur.y
        Cur.x = x; Cur.y = y
        callback()
        Cur.x = orgX; Cur.y = orgY
    }

    @Test @TestPass("2020.05.17") fun testOverwriteText() {
        clearScreen()
        withPos(0, 0) { print("Some Text!") }
        withPos(0, 0) { withColor(bg = ColorT.White) { print("          ") } }
    }

    @Test @TestPass("2020.05.17") fun testPosChanging() {
        withPos(2, 2) { print("I moved!") }
    }

    @Test @TestPass("2020.05.17") fun testWithColor() {
        withColor(fg = ColorT.Cyan) { print("My color changed!") }
    }

    @Test @TestPass("2020.05.17") fun testCliFormats() {
        print("Some textextextextextextextextextextextextextextextextextextextextextextextextextextext" +
                "extextextextextextextextextextextextextextextextextextextextextextextextextextextextext")
        Coc.bg = ColorT.White
        fillScreen()
        Cur.home()
        withColor(fg = ColorT.Cyan) { print("[]") }
        withPos(2, 1) { withColor(fg = ColorT.Magenta) { print("[]") } }
        withPos(4, 2) { withColor(fg = ColorT.Yellow) { print("[]") } }
    }

    @Test @TestPass("2020.05.17") fun testPaintArea() {
        Coc.bg = ColorT.White
        fillScreen()

        val testArea = PaintArea(windowArea, 0, 0, 10, 5)
        with(testArea) {
            fill(arrayOf(
                "+--------+",
                "|        |",
                "|  JCer  |",
                "|        |",
                "+--------+"
            ))
            paint()
        }
    }
}

var windowAreaCreated = false
val windowArea = PaintArea(null, 0, 0, max_m, max_n)

typealias PaintPoint = PaintArea.Point
class PaintArea(var parentArea: PaintArea? = null, val x: Int, val y: Int, val m: Int, val n: Int) {
    data class Point(
        var cA: PaintArea? = null,
        var fg: ColorT = ColorT.Black,
        var bg: ColorT = ColorT.White,
        var hl: Boolean = false,
        var ch: Char = ' '
    ) {
        constructor(cA: PaintArea? = null, color: Color, ch: Char) : this(cA, color.fg, color.bg, color.hl, ch)
    }

    private var childrenAreas: List<PaintArea> = mutableListOf()
    var board: Array<Array<Point>> = Array(n) { Array(m) { Point() } }

    init {
        parentArea?:run {
            if (windowAreaCreated) parentArea = windowArea
            else windowAreaCreated = true
        }
        parentArea?.run {
            childrenAreas += this
            board[y][x].cA = this
        }
    }

    fun paint(callback: (childArea: PaintArea) -> Unit = {}) {
        var nowCA: PaintArea? = null
        for ((yi, line) in board.withIndex()) for ((xi, dot) in line.withIndex()) {
            dot.cA?.run {
                nowCA = this
                callback(this)
            }
            dot.cA?: run {
                if (nowCA != null) with(nowCA) {
                    if (xi in x..x + m || yi in y..y + n) return@run
                }
                Painter.withPos(x + xi, x + yi) {
                    Painter.withColor(dot.fg, dot.bg, dot.hl) { print(dot.ch) }
                }
            }
        }
    }

    fun refresh() {
        var callback: (PaintArea) -> Unit = {}
        callback = { cA: PaintArea -> cA.paint(callback) }
        this.paint(callback)
    }

    fun fill(
        lines: Array<String>,
        fg: ColorT = ColorT.Black, bg: ColorT = ColorT.White, hl: Boolean = false
    ) {
        var lineEnough = false
        for (i in board.indices) {
            fillLine(i, lines[i], fg, bg, hl)
            if (i == n - 1) lineEnough = true
            if (i == n)
                throw WrongParamPropertyException("PaintArea.fill", "lines", "size", n, lines.size, "Too many rows.")
        }
        if (!lineEnough)
            throw WrongParamPropertyException("PaintArea.fill", "lines", "size", n, lines.size, "Too few rows.")
    }

    fun fillLine(
        idx: Int, line: String,
        fg_: ColorT = ColorT.Black, bg_: ColorT = ColorT.White, hl_: Boolean = false
    ) {
        if (line.length != m) throw WrongParamPropertyException(
            "PaintArea.fillLine", "line", "length", m, line.length,
            "Too ${if (line.length < m) "few" else "many"} cols."
        )
        for ((i, ch_) in line.withIndex()) {
            with(board[idx][i]) {
                ch = ch_; fg = fg_; bg = bg_; hl = hl_
            }
        }
    }
}