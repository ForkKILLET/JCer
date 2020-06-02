package doc

fun Throw(msg: String): Nothing = throw Throwable(msg)

class Package(val name: String, val pa: Package?) {
    operator fun contains(child: Package) = child.pa === this
    operator fun div(child: Package): Package {
        if (child !in this) Throw("Package structure error: `${child.name}` isn't `$name`'s child.")
        return child
    }
    operator fun div(file: String) = File(file, this)
    val path: String = (pa?.path?:"org.icelava") + ".$name"
}

class File(name: String, pa: Package) {
    var info = ""

    operator fun invoke(modify: File.() -> Unit) {
        this.modify()
        cNow!!.fileModify += "\n$path:" + ("\n" + info).replace("\n", "\n    ")
    }
    val path = pa.path + "/$name"
    fun add(what: String): File {
        this.info += "add: $what\n"
        return this
    }
}

operator fun String.not() = "(important) $this"

class Commit {
    lateinit var version: String
    var fileModify: String = ""
    var execType: String? = null
    lateinit var documents: String
    lateinit var createTime: String

    fun commit(actions: Commit.() -> Unit): Commit {
        this.actions()
        return this
    }
    fun exec(type: String? = "jar"): Commit {
        execType = type
        return this
    }
    fun doc(what: List<Any>): Commit {
        documents = what.joinToString(separator = "\n") { if (it === ChangeLog) "this" else it.toString() }
        return this
    }
    fun time(t: String): Commit {
        createTime = t
        return this
    }
}


fun version(name: String): Commit {
    val c = Commit()
    cNow = c
    c.version = name
    commits.plusAssign(c)
    return c
}

var commits = mutableListOf<Commit>()
var cNow: Commit? = null

fun ChangeLog.format(c: Commit): String {
    var info: String
    with(c) { info = """
VER @$version

MODIFY
$fileModify
RELEASE $execType

DOCUMENTS
$documents

@$createTime
""" }
    return info
}

fun ChangeLog.select(version: String): Commit? {
    for (c in commits) if (c.version == version) return c
    return null
}