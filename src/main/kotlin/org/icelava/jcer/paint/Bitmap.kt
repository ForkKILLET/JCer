package org.icelava.jcer.paint
import org.icelava.jcer.*
import org.icelava.jcer.system.*
import java.io.File
import java.nio.charset.Charset

abstract class UnexpectedFormatWhenParsingSomethingException(
    parsingType: String, context: String? = null, expectStructure: String? = null, nowStructure: String? = null,
    extraInfo: String? = null
) : Throwable(
    "$parsingType parser met ${ if (context == null) "(U/A)" else "`$context`" }" +
    "${ if (nowStructure == null) "" else ": $nowStructure" }, " +
    "but expect ${expectStructure}." + if (extraInfo == null) "" else " $extraInfo"
)

class UnexpectedFormatWhenParsingBitmapException(
    context: String? = null, expectStructure: String? = null, nowStructure: String? = null, extraInfo: String? = null
) : UnexpectedFormatWhenParsingSomethingException(
    "Bitmap (`.pic.txt`)", context, expectStructure, nowStructure, extraInfo
)

abstract class WrongWhenLinkSomethingException(
    linkType: String, path: String, linkTarget: Any, step: String, info: String = ""
) : Throwable("Failed when linking $linkType at `$path` to $linkTarget: $step. $info")

class WrongWhenLinkBitmapException(
    path: String, linkTarget: PaintArea, step: String, info: String = ""
) : WrongWhenLinkSomethingException(
    "Bitmap (`.pic.txt`)", path, "PaintArea (at ${linkTarget.x}, ${linkTarget.y})", step, info
)

@ExperimentalStdlibApi object Bitmap {
    const val insNames = "mncCesubE\\" // Note: "fDCdrR"

    val insCharParamTables = mapOf(
        "NoSpace" to blankTable,
        "ColorHex" to hexTable + "=",
        "UnicodeHex" to hexTable
    )
    val insParamsTypes = mapOf(
        'm' to listOf("Int"),
        'n' to listOf("Int"),
        'c' to listOf("Chars:ColorHex*2"),
        'C' to listOf("Chars:NoSpace", "Chars:ColorHex*2"),
        'e' to listOf(),
        's' to listOf(),
        'u' to listOf("Chars:UnicodeHex*4"),
        'b' to listOf(),
        'E' to listOf(),
        '\\' to listOf()
    )

    data class PosIter(var v: Int, var autoMoving: Boolean = false, val jRecNum: Int = 10) {
        private val jRec = mutableListOf<Int>()
        private var jPos = 1

        operator fun compareTo(int: Int) = v.compareTo(int)
        operator fun inc() = this m 1
        operator fun plus(int: Int) = PosIter(v + int)
        operator fun plusAssign(int: Int) { this j v + int }
        operator fun dec() = this m -1
        operator fun minus(int: Int) = PosIter(v - int)
        operator fun minusAssign(int: Int) { this j v - int }
        operator fun rangeTo(int: Int) = v..int
        operator fun rangeTo(it: PosIter) = v..it.v

        infix fun j(t: Int): PosIter {
            v = t
            if (jRec.size == jRecNum) jRec.removeFirst()
            jRec.add(t)
            return this
        }
        infix fun m(d: Int) = j(v + d)
        infix fun u(m: Int) {
            if (m < 0 || jRec.size - jPos - m < 0)
                throw WrongParamRangeException("Bitmap.Matcher.u", "l", 0, jRec.size + jPos, m)
            jPos += m
            this j jRec[jRec.size - jPos]
        }
        infix fun r(m: Int) {
            if (m < 0 || m > jPos)
                throw WrongParamRangeException("Bitmap.Matcher.r", "l", 0, jPos, m)
            jPos -= m
            this j jRec[jRec.size - jPos]
        }

        fun me() = if (autoMoving) this else copy()
    }
    operator fun String.get(idx: PosIter) = this[idx.v]
    operator fun String.set(idx: PosIter, value: Char) { this[idx] = value }
    private fun String.substring(startIt: PosIter, endIt: PosIter) = substring(startIt.v, endIt.v)
    operator fun Int.plus(it: PosIter) = this + it.v
    operator fun Int.minus(it: PosIter) = this - it.v
    operator fun Int.rangeTo(it: PosIter) = this..it.v

    object Matcher {
        interface MatchRes {
            var finishPos: Int
            infix fun ji(idx: PosIter): MatchRes {
                idx j finishPos
                return this
            }
        }
        data class InsRes(
            var insName: Char? = null, var paramList: List<Any> = listOf(), var realPos: Int = 0,
            override var finishPos: Int = 0
        ) : MatchRes {
            override infix fun ji(idx: PosIter): InsRes {
                idx j finishPos
                return this
            }
            infix fun p(idx: Int) = paramList[idx]
        }
        data class BlaRes(var matchNum: Int = 0, override var finishPos: Int = 0) : MatchRes {
            override infix fun ji(idx: PosIter): BlaRes {
                idx j finishPos
                return this
            }
            fun assert(
                assertInfo: String = "at least 1 blank char", failInfo: String = "",
                callback: (Int) -> Boolean = { it > 0 }
            ): BlaRes {
                if (!callback(matchNum)) throw UnexpectedFormatWhenParsingBitmapException(
                    null, assertInfo, "other", failInfo
                )
                return this
            }
        }
        data class ChrRes(var chars: String? = "", override var finishPos: Int = 0) : MatchRes {
            override infix fun ji(idx: PosIter): ChrRes {
                idx j finishPos
                return this
            }
        }
        data class IntRes(var int: Int? = 0, override var finishPos: Int = 0) : MatchRes {
            override infix fun ji(idx: PosIter): IntRes {
                idx j finishPos
                return this
            }
        }

        var afterNlAtEol = false
        var afterBla = true // Note: Begin of file
        var inStr = false
        fun freshTmp() {
            afterNlAtEol = false
            afterBla = true
            inStr = false
        }

        private fun ci(str: String, idx: PosIter) {
            if (idx < 0 || idx >= str.length)
                throw WrongParamRangeException("Bitmap.checkIdx", "idx", 0, str.length - 1, idx)
        }
        fun bla(str: String, idx: PosIter): BlaRes {
            ci(str, idx)

            var i = 0
            val it = idx.me()

            while (str[it + i] in blankTable) i++
            it m i

            return BlaRes(i, it.v)
        }
        fun bla(str: String, idx: PosIter, num: Int): BlaRes {
            ci(str, idx)

            var i = 0
            val it = idx.me()

            while (it + i < str.length && str[it + i] in blankTable && i < num) i++
            if (i < num) throw UnexpectedFormatWhenParsingBitmapException(
                str.substring(idx..idx + i), "$num blank char(s)", "other char"
            )
            it m i

            return BlaRes(i, it.v)
        }
        fun chr(
            str: String, idx: PosIter, num: Int? = null,
            charTable: CharTable = letterNumberTable, nullable: Boolean = false
        ): ChrRes {
            ci(str, idx)

            var i = 0
            val it = idx.me()
            val res = ChrRes("")

            while (str[idx + i] in charTable && i < num?:Int.MAX_VALUE) {
                res.chars += str[idx + i]
                i++
            }

            if (num != null)
                if (i == 0 && nullable) res.chars = null
                else if (i < num - 1)
                    throw UnexpectedFormatWhenParsingBitmapException(str.substring(idx..idx + i), "$num char(s)", "only $i char(s)", "Char table: $charTable")
            bla(str, it m i, 1)
            afterBla = true
            res.finishPos = it.v

            return res
        }
        fun int(str: String, idx: PosIter, nullable: Boolean = false): IntRes {
            val charRes = chr(str, idx, null, numberTable)
            return IntRes(if (nullable) charRes.chars?.toInt() else charRes.chars!!.toInt(), charRes.finishPos)
        }
        fun ins(str: String, idx: PosIter, want: Char? = null): InsRes? {
            ci(str, idx)
            if (want != null && want !in insNames)
                throw WrongParamPropertyException("Bitmap.matchInstruction", "want", "\\in", insNames, want,
                    "Enabled instructions: `$insNames`"
                )

            val it = idx.me()
            val startPos = idx.copy()
            val res = InsRes()
            val cb = { if (!afterBla) bla(str, it, 1) }

            cb()
            if (str[it] != '\\')
                if (want != null) throw UnexpectedFormatWhenParsingBitmapException(
                    str.substring(startPos..it), "slash (`\\`)", "other"
                )
                else return null
            res.insName = str[it m 1]
            if (want != null && res.insName != want) throw UnexpectedFormatWhenParsingBitmapException(
                str.substring(startPos..it), "instruction (`\\$want`)", "other instruction"
            )

            it m 1; bla(str, it, 1)

            val pList = mutableListOf<Any>()
            res.paramList = pList
            for (pInfo in insParamsTypes[res.insName as Char]!!) {
                var charTable: CharTable? = null
                var charNum: Int? = null
                var pType: String
                if (pInfo.startsWith("Chars:")) {
                    pType = "Chars"
                    val charInfo = pInfo.substring(6)
                    var charTableFinish = charInfo.indexOf('*')
                    if (charTableFinish == -1) charTableFinish = charInfo.lastIndex
                    else charNum = charInfo.substring(charTableFinish + 1).toInt()
                    charTable = insCharParamTables[charInfo.substring(0, charTableFinish)]!!
                }
                else pType = pInfo
                when (pType) {
                    "Int" -> pList.add((int(str, it) ji it).int!!)
                    "Int?" -> (int(str, it, true) ji it).int?.run { pList.add(this) }
                    "Chars" -> pList.add((chr(str, it, charNum, charTable!!) ji it).chars!!)
                    "Chars?" -> (chr(str, it, charNum, charTable!!) ji it).chars?.run { pList.add(this) }
                }
                cb()
            }
            res.finishPos = it.v

            afterBla = true
            return res
        }
    }

    private fun fetch(path: String): String = File(path).readText(Charset.forName("UTF-8"))

    private fun parse(str: String): Array<Array<PaintPoint>> {
        val bitmap: Array<Array<PaintPoint>>
        val it = PosIter(0, true)
        var lastInsIt = it.copy()
        var t: Matcher.InsRes?
        val s = str + ' ' // Note: Then we needn't add blank chars for the first and the last instructions.

        t = Matcher.ins(s, it, 'm')!!
        val m: Int = (t p 0) as Int
        t = Matcher.ins(s, it, 'n')!!
        val n: Int = (t p 0) as Int
        bitmap = Array(n) { Array(m) { PaintPoint() } }

        var pos = Pos(m, n)
        val color = Color()

        insLoop@ while (true) {
            t = Matcher.ins(s, it)
            if (t == null) {
                if (Matcher.inStr) {
                    while (str[it] != '\\') {
                        Matcher.afterBla = s[it] in blankTable
                        if (s[it] == '\n') {
                            if (pos.x == 0)
                                if (Matcher.afterNlAtEol && pos.y != n - 1) {
                                    pos.y++
                                    pos.x = 0
                                } else Matcher.afterNlAtEol = true
                        }
                        else if (!Matcher.afterBla || s[it + 1] != '\\') {
                            Matcher.afterNlAtEol = false
                            bitmap[pos] = PaintPoint(null, color, s[it])
                            if (pos.x != m - 1 || pos.y != n - 1) pos++
                        }
                        it m 1
                    }
                }
                else throw UnexpectedFormatWhenParsingBitmapException(
                    s.substring(lastInsIt, it), "a slash (`/`) to start a instruction", "another char",
                    "If this char is a part of Bitmap, make sure it's in the scope of a `\\s` instruction."
                )
            }
            else {
                lastInsIt = it.copy()
                when (t.insName) {
                    'c' -> ((t p 0) as String).run { color.toHex(this[0], this[1]) }
                    'e' -> while (pos.x < pos.m) bitmap[pos] = PaintPoint(null, color, ' ')
                    's' -> Matcher.inStr = true
                    'E' -> break@insLoop
                }
            }
        }

        Matcher.freshTmp()
        return bitmap
    }

    fun link(path: String, area: PaintArea, dx: Int, dy: Int) {
        val bitmap = parse(fetch(path))
        if (dx < 0 || dx >= area.m) throw WrongParamRangeException("Bitmap.link",
            "dx", 0, area.m - 1, dx, "Delta x must in the target area."
        )
        if (dy < 0 || dy >= area.n) throw WrongParamRangeException("Bitmap.link",
            "dy", 0, area.n - 1, dx, "Delta y must in the target area."
        )

        val bm = bitmap[0].size
        val bn = bitmap.size
        if (dx + bm >= area.m || dy + bn >= area.n) throw WrongWhenLinkBitmapException(
            path, area, "Locating",
            "Bitmap width: $bm, Bitmap height: $bn, Bitmap linking pos based on the target area: ($dx, $dy)"
        )

        for (yi in 0 until bn) for (xi in 0 until bm) area.board[dy + yi][dx + xi] = bitmap[yi][xi]
    }

    @TestPass("2020.05.24") @Test fun testBitmapPainting() {
        Painter.Coc.bg = ColorT.White
        Painter.fillScreen()
        link(Path.resource + "test.pic.txt", windowArea, 0, 0)
        // TODO: Fix the fucking relative path
        windowArea.paint()
    }
}