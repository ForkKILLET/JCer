package org.icelava.jcer.system
import org.icelava.jcer.*
import java.io.File

object Path {
    val jarFile: String = File(Main::class.java.protectionDomain.codeSource.location.toURI()).path
    val jarPath =
            if (jarFile.endsWith("JCer.jar")) jarFile.substring(0 until jarFile.length - 8)
            else "/Volumes/ForkKILLET/Project/JCer/exec/"
    val project = jarPath.substring(0 until jarPath.length - 5)
    val resource = project + "res/"
}