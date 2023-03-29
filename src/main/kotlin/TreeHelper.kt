package gitinternals

private const val SHA_SIZE = 40
private const val SHA_INDEX = 1
private const val SHA_RAW_SIZE = 20
private const val LINE_SEPARATOR_SUBSTITUTE_CODE = 256
private const val ZERO_PAD_HEX_TEN = "0a"
private const val TREE_HEADER_SIZE = 8
private val SHA_RAW_RANGE = 0..19

//Line separator substitute = Ā

object TreeHelper {

    fun getTreeContent(hash: String): List<String> {
        val bytes = FileUtils.getFileBytesFromHash(hash)
        val treeContent = bytes.drop(TREE_HEADER_SIZE).decodeToString(onlyNullSeparator = false)
        return decodeTreeContent(treeContent.lines().filter {it != ""})
    }

    fun decodeTreeContent(content: List<String>): List<String> {
        val shaList = treeContentShaList(content)
        val mutableContent = content.toMutableList()
        mutableContent.removeLast()
        mutableContent[0] = treeContentFormat(content.first(), shaList.first())
        for (index in 1..shaList.lastIndex) {
            val currentContent = mutableContent[index].removeRange(SHA_RAW_RANGE)
            mutableContent[index] = treeContentFormat(currentContent, shaList[index])
        }
        return mutableContent.filter { it.length > SHA_SIZE }
    }

    private fun treeContentShaList(content: List<String>): List<String> {
        val sha1List = mutableListOf<String>()
        for (line in 1..content.lastIndex) {
            val sha1 = content[line].take(SHA_RAW_SIZE)
            val stringBuilder = StringBuilder()
            for (char in sha1.toCharArray()) {
                if (char.code == LINE_SEPARATOR_SUBSTITUTE_CODE) {
                    stringBuilder.append(ZERO_PAD_HEX_TEN)
                    continue
                }
                stringBuilder.append(zeroPadHex(Integer.toHexString(char.code)))
            }
            sha1List.add(stringBuilder.toString())
        }
        return sha1List
    }

    private fun treeContentFormat(string: String, sha: String): String {
        val stringArray = string.split(" ").toMutableList()
        stringArray.add(SHA_INDEX, sha)
        return stringArray.joinToString(" ")
    }

    private fun zeroPadHex(string: String): String {
        if (string.length != 1) return string
        return "0$string"
    }

}