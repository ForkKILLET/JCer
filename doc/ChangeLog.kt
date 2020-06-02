package doc

/*
 * JCer is not only a game to JC or anti-JC
 * Its full name is `Juicy CLI evil repo` :)
 * The commit info is written in Kotlin,
 * because ForkKILLET LOVES it.
 * FK may edit the log's format at any time.
 */

object ChangeLog {
    private val jcer = Package("jcer", null)
    private val doc = Package("doc", null)
    private val paint = Package("paint", jcer)
    private val system = Package("system", jcer)

    fun forever() {
        version("dev 2.0")
            .commit {
                (jcer / paint / "Basic.kt") {
                    add("basic methods to operator the terminal. (base on ANSI escape)")
                    add("nested painting area (like a tree)")
                    add(!"text bitmap (`.pic.txt` file) parsing and linking (to a painting area, then render it!)")
                }
                (jcer / system / "Keyboard.kt") {
                    add("a function waiting for an enter key. (useful when debugging)")
                }
            }
            .exec("jar")
            .doc(listOf(
                "IceLava Text Picture and Animation File Format",
                this
            ))
            .time("2020.05.24")

        version("dev 3.0")
            .commit {
                (doc / "Commit.kt") {
                    add(!"real kotlin support for kotlin-like commit info")
                    add(!"a parser for making kotlin commit info pithy and suitable for Git")
                    add("commit time record")
                }
            }
            .exec(null)
            .doc(listOf(this))
            .time("2020.06.02")
    }
}