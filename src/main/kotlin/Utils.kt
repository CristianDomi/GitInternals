package gitinternals

import java.io.File

fun hashToPathString(hash: String): String {
    val dir = hash.substring(0..1)
    val gitObject = hash.substring(2..hash.lastIndex)
    return "$dir/$gitObject"
}

fun getPath(): String = "${System.getProperty("user.dir")}/Git Internals/task/src/gitinternals"

fun getFile(path: String, local: Boolean = false): File {
    return if (local) File("${getPath()}/$path") else File(path)
}
