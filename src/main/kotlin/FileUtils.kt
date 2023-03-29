package gitinternals

import java.io.FileInputStream
import java.util.zip.InflaterInputStream

private const val UNSIGNED_BYTE = 255

object FileUtils {

    private var directory = ""
    private val ignoreDirectories = listOf("info","pack","objects",".DS_Store")
    private val ignoreFiles = listOf(".DS_Store","")

    fun setDirectory(directory: String) {
        this.directory = directory
    }

    fun getFileBytesFromHash(hash: String): List<Int> {
        if (directory.isEmpty()) throw DirectoryNotSetException()
        val file = getFile("$directory/objects")
        for (directories in file.walk()) {
            if (directories.isDirectory && directories.name !in ignoreDirectories) {
                val objectFile = directories.listFiles()?.first()?.name ?: ""
                if (objectFile in ignoreFiles || hash.startsWith(directories.name).not()) continue
                val objectHash = "${directories.name}$objectFile"
                if (hash == objectHash) {
                    return getBytesFromFile("$directory/objects/${directories.name}/$objectFile")
                }
            }
        }
        return emptyList()
    }

    fun getFileLines(fileName: String): List<String> {
        if (directory.isEmpty()) throw DirectoryNotSetException()
        val refsPath = "$directory/refs/heads"
        val file = getFile(refsPath)
        for (fileWalk in file.walk()){
            if (fileWalk.isFile) {
                if (fileWalk.name == fileName) {
                    return fileWalk.readLines()
                }
            }
        }
        return emptyList()
    }

    fun getBytesFromFile(objectPath: String): List<Int> {
        val file = FileInputStream(getFile(objectPath))
        val inflater = InflaterInputStream(file)
        return inflater.readAllBytes().map { it.toInt().and(UNSIGNED_BYTE) }
    }

    fun getBytesBeforeNull(bytes: List<Int>, direction: NullPredicateDirection): List<Int> {
        return with(bytes) {
            when (direction) {
                NullPredicateDirection.LEFT_TO_RIGHT -> takeWhile(::nullSplitPredicate)
                NullPredicateDirection.RIGHT_TO_LEFT -> takeLastWhile(::nullSplitPredicate)
            }
        }
    }

    private fun nullSplitPredicate(int: Int) = int != 0

}