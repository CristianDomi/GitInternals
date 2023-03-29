package gitinternals

import java.time.Instant

private const val MINUTE = 60
private const val HOUR = 3600

object CommitHelper {

    private var parentFormatted = false

    fun getCommitContent(hash: String): String {
        val bytes = FileUtils.getFileBytesFromHash(hash)
        return commitBytesToString(bytes)
    }

    fun getLineContent(line: String) = line.dropWhile { it != ' ' }.trim()

    fun getCommitMessage(commitContent: List<String>): String {
        val indices = arrayOf<Int?>(0, 0)
        commitContent.forEachIndexed { index, string ->
            if (string == "") if (indices.first() == null) indices[0] = index + 1 else indices[1] = index
        }
        return commitContent.subList(1, commitContent.lastIndex).joinToString("\n")
    }

    private fun getParentDiffs(commit: Commit): Pair<Boolean, String> {
        val parentDiff = commit.hasParentDiff()
        val parentsText = if (parentDiff) parentsDivFormatter(commit) else ""
        return Pair(parentDiff, parentsText)
    }

    private fun parentsDivFormatter(commit: Commit): String {
        val stringBuilder = StringBuilder()
        for (parent in commit.parents) {
            if (stringBuilder.isEmpty()) stringBuilder.append(parent) else stringBuilder.append(" | $parent")
        }
        val result = stringBuilder.toString()
        return if (result.contains("|")) result else ""
    }

    fun formatCommitContent(content: List<String>): List<String> {
        val commit = Commit(content)
        val formattedContent: MutableList<String> = mutableListOf()
        for (contentText in content) {
            formattedContent.add(commitLineFormatter(contentText, getParentDiffs(commit)) ?: "")
        }
        return formattedContent.filter { it != "" }
    }

    /**
     * Formatea una linea de contenido de un COMMIT.
     *
     * [content]: El contenido del commit.
     * [parentDiffs]: A - Si hay padres distintos, B - Formato del texto de padres en caso de haber mas de uno.
     */
    private fun commitLineFormatter(content: String, parentDiffs: Pair<Boolean, String>): String? {
        return when (content.takeWhile { it != ':' }) {
            CommitContent.AUTHOR.identifier -> authorLineFormatter(content, "original")
            CommitContent.COMMITTER.identifier -> authorLineFormatter(content, "commit")
            CommitContent.PARENT.identifier -> {
                if (parentFormatted.not()) {
                    parentFormatted = true
                    formatParentLine(content, parentDiffs)
                } else {
                    null
                }
            }

            else -> content
        }
    }

    fun commitSemicolonFormatter(commitContent: List<String>): List<String> {
        val list = mutableListOf<String>()
        var commitMessage = false
        for (content in commitContent) {
            if (content.isEmpty() && commitMessage.not()) {
                list.add("commit message:")
                commitMessage = true
                continue
            }
            if (commitMessage.not()) list.add(content.replaceFirst(" ", ": ")) else list.add(content)
        }
        return list
    }

    private fun formatParentLine(content: String, parentDiffs: Pair<Boolean, String>): String {
        var newContent = ""
        newContent = if (parentDiffs.first) content.replace("parent", "parents") else content
        if (parentDiffs.second.isNotEmpty()) {
            newContent = newContent.replace(content.split(" ").last(), parentDiffs.second)
        }
        return newContent
    }

    fun authorLineFormatter(text: String, timeStampText: String): String {
        var resultString = text
        val timeStamp = text.split(" ").takeLast(2)
        val (date, timeZone) = unixEpochTimeStampToDateFormat(
            timeStamp.first().toLong(),
            timeStamp.last(),
            timeStampText
        )
        resultString = resultString.replace(timeStamp.first(), date)
        resultString = resultString.replace(timeStamp.last(), timeZone)
        return resultString.replace("[<>]".toRegex(), "")
    }

    fun getContentFromCommit(commitContent: List<String>, contentToGet: CommitContent): String {
        if (contentToGet == CommitContent.COMMIT_MESSAGE) return getCommitMessage(commitContent)
        for (content in commitContent) {
            if (content.startsWith(contentToGet.identifier)) {
                return content.dropWhile { it != ' ' }.trim()
            }
        }
        return ""
    }

    private fun unixEpochTimeStampToDateFormat(timeStamp: Long, timeZone: String, text: String): Pair<String, String> {
        val hour = timeZone.take(3)
        val minutes = timeZone.takeLast(2)
        val utc = "$hour:$minutes"
        val date = Instant.ofEpochSecond(timeStamp).plusSeconds(hour.toLong() * HOUR)
            .plusSeconds(minutes.toLong() * MINUTE)
            .toString().replace("Z", "").replace("T", " ")
        return Pair("$text timestamp: $date", utc)
    }

    private fun commitBytesToString(bytes: List<Int>) =
        FileUtils.getBytesBeforeNull(bytes, NullPredicateDirection.RIGHT_TO_LEFT).decodeToString()

}