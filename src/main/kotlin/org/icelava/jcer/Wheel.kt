package org.icelava.jcer
import java.io.File

// Zone: annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Testing

// Disabled: The whole project doesn't supported Windows now, then this annotation becomes useless.
// @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
// annotation class WindowsNotSupported

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Deprecated

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Test

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TestPass(val date: String)

// Zone: basic exceptions

class WrongVarRangeException(
    varName: String, varRangeLeft: Any?, varRangeRight: Any?, varValue: Any, extraInfo: String? = null
) : Throwable("$varName must be in ${varRangeLeft ?: "."}..${varRangeRight ?: "."} but it's now $varValue." +
              if (extraInfo == null) "" else " $extraInfo"
)

class WrongParamPropertyException(
    funName: String, paramName: String, paramPropertyName: String, paramPropertyCorrectValue: Any,
    paramPropertyNowValue: Any, extraInfo: String? = null
) : Throwable("$funName:$paramName.$paramPropertyName must be $paramPropertyCorrectValue but it's now " +
              "$paramPropertyNowValue." + if (extraInfo == null) "" else " $extraInfo")

class WrongParamRangeException(
    funName: String, paramName: String, paramRangeLeft: Any?, paramRangeRight: Any?,
    paramValue: Any, extraInfo: String? = null
) : Throwable("$funName:$paramName must be in ${paramRangeLeft ?: "."}..${paramRangeRight ?: "."} " +
              "but it's now $paramValue." + if (extraInfo == null) "" else " $extraInfo")

// Zone: constants, typealias and toolkit classes / obejects

typealias SimpleFun = () -> Unit // Xyx: CokeCola and crisps

data class CharTable(val chars: String, val symbol: Boolean = true) {
    operator fun contains(char: Char) = (char in chars) % symbol
    operator fun plus(newChars: String) = CharTable(chars + newChars, symbol)
    operator fun plus(charTable: CharTable): CharTable {
        if (charTable.symbol != symbol)
            throw WrongParamPropertyException(
                "Bitmap.CharTable.\\+", "charTable", "opposite", symbol, charTable.symbol,
                "Can't merge CharTables in different symbol."
            )
        return CharTable(chars + charTable.chars, symbol)
    }
    operator fun unaryMinus() = CharTable(chars, !symbol)
}

val lowercaseLetterTable = CharTable("abcdefghijklmnopqrstuvwxyz")
val uppercaseLetterTable = CharTable("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
val letterTable = lowercaseLetterTable + uppercaseLetterTable
val numberTable = CharTable("0123456789")
val letterNumberTable = letterTable + numberTable
val hexTable = numberTable + "abcdef" + "ABCDEF"
var blankTable = CharTable(" \n\t")

open class Pos(val m: Int, val n: Int) {
    open var x: Int = 0
        set(value) {
            if (value < 0 || value >= m)
                throw WrongVarRangeException("Pos.x", 0, m - 1, value)
            field = value
        }
    open var y: Int = 0
        set(value) {
            if (value < 0 || value >= n)
                throw WrongVarRangeException("Pos.y", 0, n - 1, value)
            field = value
        }

    init {
        home()
    }

    operator fun inc(): Pos {
        if (x == m - 1) {
            y++; x = 0
        }
        else x++
        return this
    }
    operator fun dec(): Pos {
        if (x == 0) {
            y--; x = m - 1
        }
        else x--
        return this
    }

    fun home() {
        x = 0; y = 0
    }
}
operator fun <T> Array<Array<T>>.get(pos: Pos) = this[pos.x][pos.y]
operator fun <T> Array<Array<T>>.set(pos: Pos, value: T) { this[pos.y][pos.x] = value }

typealias ColorT = Color.ColorType
open class Color {
    enum class ColorType {
        Black, Red, Green, Yellow, Blue, Magenta, Cyan, White;
    }

    open var fg = ColorT.Black
    open var bg = ColorT.White
    open var hl = false

    fun toHex(fg_: Char, bg_: Char) {
        val fgV: Int? = if (fg_ == '=') null else String(charArrayOf(fg_)).toInt(16)
        val bgV: Int? = if (bg_ == '=') null else String(charArrayOf(bg_)).toInt(16)
        fgV?.run { hl = fgV > 7 }
        val fromInt = { hex: Int ->
            when (if (hex > 7) hex - 8 else hex) {
                0 -> ColorT.Black
                1 -> ColorT.Red
                2 -> ColorT.Green
                3 -> ColorT.Yellow
                4 -> ColorT.Blue
                5 -> ColorT.Magenta
                6 -> ColorT.Cyan
                7 -> ColorT.White
                else -> null
            }!!
        }
        fgV?.run { fg = fromInt(fgV) }
        bgV?.run { bg = fromInt(bgV) }
    }

    companion object {
        fun fromHex(fg_: Char, bg_: Char): Color {
            val color = Color()
            color.toHex(fg_, bg_)
            return color
        }
    }
}

// Zone: built-in extensions

operator fun String.times(time: Int): String = this.repeat(time)
operator fun Boolean.rem(bool: Boolean) = (this && bool) || !(this || bool) // Note: Xor