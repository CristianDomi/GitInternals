package gitinternals

private const val FOLDER_MODE = "40000"

class Tree(content: List<String>, private val treeHash: String) {

    private val treeContent = mutableListOf<TreeContent>()
    val hashes = mutableListOf<String>()
    val subTrees = mutableListOf<Tree>()

    init {
        for (line in content) {
            val (mode, hash, fileName) = line.split(" ")
            treeContent.add(TreeContent(mode, hash, fileName))
            if (isFolder(mode)) hashes.add(hash)
        }
    }

    fun printContent(vararg treeContentEntry: TreeContentEntry) {
        val contentBuilder = StringBuilder()
        for (content in treeContent) {
            if (TreeContentEntry.MODE in treeContentEntry) contentBuilder.append("${content.mode} ")
            if (TreeContentEntry.HASH in treeContentEntry) contentBuilder.append("${content.hash} ")
            if (TreeContentEntry.FILENAME in treeContentEntry) {
                if (hasFolders(content.mode)) {
                    contentBuilder.append("${content.fileName}/")
                } else {
                    contentBuilder.append(content.fileName)
                }
            }
            if (hasFolders(content.mode)) {
                print(contentBuilder.toString().trim())
                subTrees.find { it.treeHash == content.hash } ?.printContent(TreeContentEntry.FILENAME)
            } else {
                println(contentBuilder.toString().trim())
            }
            contentBuilder.clear()
        }
    }

    private fun isFolder(string: String) = string == FOLDER_MODE

    private fun hasFolders(mode: String) = isFolder(mode) && subTrees.isNotEmpty()

    inner class TreeContent(
        val mode: String,
        val hash: String,
        val fileName: String
    )
}