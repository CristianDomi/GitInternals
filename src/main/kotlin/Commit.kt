package gitinternals

import gitinternals.CommitHelper.getLineContent
import gitinternals.CommitHelper.getCommitMessage

class Commit(content: List<String>) {

    var tree: String = ""
    var parents: MutableList<String> = mutableListOf()
    var author: String = ""
    var committer: String = ""
    var commitMessage: String = ""

    init {
        for (index in content.indices) {
            val line = content[index]
            when {
                line.startsWith(CommitContent.TREE.identifier) -> tree = getLineContent(line)
                line.startsWith(CommitContent.PARENT.identifier) -> parents.add(getLineContent(line))
                line.startsWith(CommitContent.AUTHOR.identifier) -> author = getLineContent(line)
                line.startsWith(CommitContent.COMMITTER.identifier) -> committer = getLineContent(line)
                line.startsWith(CommitContent.COMMIT_MESSAGE.identifier) -> {
                    commitMessage = getCommitMessage(content.slice(index..content.lastIndex))
                    break
                }
            }
        }
    }

    fun getFirstParent(): String {
        return parents.firstOrNull() ?: ""
    }

    fun getParent(index: Int): String {
        return parents.getOrElse(index) {""}
    }

    fun hasParentDiff(): Boolean {
        return hasDiffTreeAndParent() || hasDiffAuthorAndCommitter() || hasMultipleParents()
    }

    fun hasMultipleParents() = parents.size > 1

    private fun hasDiffAuthorAndCommitter() = author != committer

    private fun hasDiffTreeAndParent() = tree != (parents.firstOrNull() ?: "")

}