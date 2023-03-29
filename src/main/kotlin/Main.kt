package gitinternals

import gitinternals.CommitHelper.authorLineFormatter
import gitinternals.CommitHelper.formatCommitContent
import gitinternals.CommitHelper.getContentFromCommit
import gitinternals.FileUtils.getBytesFromFile
import gitinternals.GitObject.Companion.prepareGitObjectForPrint

private const val LIST_BRANCHES = "list-branches"
private const val CAT_FILE = "cat-file"
private const val LOG = "log"
private const val COMMIT_TREE = "commit-tree"

/**
 * Opciones disponibles.
 *
 * list-branches: Lista las ramas existentes.
 * cat-file: Muestra el contenido de un objeto
 * log: Muestra la secuencia de un commit.
 * commit-tree: Muestra el arbol de objetos de un commit
 */
fun main() {
    println("Enter .git directory location:")
    val gitDirectoryLocation = readln()
    FileUtils.setDirectory(gitDirectoryLocation)

    println("Enter command:")
    when (readln()) {
        LIST_BRANCHES -> listBranches(directory = gitDirectoryLocation)
        CAT_FILE -> catFile(directory = gitDirectoryLocation)
        LOG -> log()
        COMMIT_TREE -> commitTree()
    }
}

fun commitTree() {
    println("Enter commit-hash:")
    val commitHash = readln()
    val commitLines = CommitHelper.getCommitContent(commitHash).lines()
    val treeHash = getContentFromCommit(commitLines, CommitContent.TREE)
    val initialTree = getTree(treeHash)
    initialTree.printContent(TreeContentEntry.FILENAME)
}

fun getTree(treeHash: String): Tree {
    val tree = Tree(TreeHelper.getTreeContent(treeHash), treeHash)
    for (hash in tree.hashes) { tree.subTrees.add(getTree(hash)) }
    return tree
}

fun log() {
    println("Enter branch name:")
    val branchName = readln()
    val fileLines = FileUtils.getFileLines(branchName)
    if (fileLines.isEmpty()) throw NotSuchBranchException(branchName)
    val branchHash = fileLines.first()
    printSubsequentCommit(branchHash)
}

fun printSubsequentCommit(commitHash: String, merge: Boolean = false) {
    val commitLines = CommitHelper.getCommitContent(commitHash).lines()
    val commit = Commit(commitLines)
    val merged = if (merge) " (merged)" else ""
    println("Commit: $commitHash$merged")
    val parent = commit.getFirstParent()
    println(authorLineFormatter(commit.committer, "commit"))
    println("${commit.commitMessage}\n")
    if (commit.hasMultipleParents() && merge.not()) {
        printSubsequentCommit(commit.getParent(1), merge = true)
    }
    if (parent.isNotEmpty() && merge.not()) printSubsequentCommit(parent)
}

fun listBranches(directory: String) {
    val refsPath = "$directory/refs/heads"
    val file = getFile(refsPath)
    val headName = getHeadName(directory)
    val heads = mutableListOf<String>()
    for (fileWalk in file.walk()) {
        if (fileWalk.isFile) {
            val pointer = if (fileWalk.name == headName) "*" else " "
            heads.add("$pointer ${fileWalk.name}")
        }
    }
    heads.sort()
    heads.forEach { println(it) }
}

fun getHeadName(directory: String): String {
    return getFile("$directory/HEAD").readLines().first().takeLastWhile { it != '/' }
}

fun catFile(directory: String) {
    println("Enter git object hash:")
    val gitObjectLocation = readln()
    val objectPath = "$directory/objects/${hashToPathString(gitObjectLocation)}"
    val objectBytes = getBytesFromFile(objectPath)

    val gitObject = GitObject(objectBytes)

    println("*${gitObject.type}*")

    prepareGitObjectForPrint(gitObject)

    if (gitObject.type == GitObjectType.COMMIT) gitObject.content = formatCommitContent(gitObject.content)

    for (contentText in gitObject.content) { println(contentText) }

}
