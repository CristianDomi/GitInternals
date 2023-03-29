package gitinternals

private const val TREE_HEADER_SIZE = 8

class GitObject(bytes: List<Int>) {

    companion object {
        fun prepareGitObjectForPrint(gitObject: GitObject) {
            gitObject.content = when (gitObject.type) {
                GitObjectType.BLOB -> gitObject.content
                GitObjectType.TREE -> TreeHelper.decodeTreeContent(gitObject.content.filter {it != ""})
                GitObjectType.COMMIT -> CommitHelper.commitSemicolonFormatter(gitObject.content)
            }
        }
    }

    val type: GitObjectType
    var content: List<String>

    init {
        type = getType(FileUtils.getBytesBeforeNull(bytes, NullPredicateDirection.LEFT_TO_RIGHT))
         content =  when (type) {
             GitObjectType.BLOB -> {
                 FileUtils.getBytesBeforeNull(bytes, NullPredicateDirection.RIGHT_TO_LEFT).decodeToString().lines()
             }
             GitObjectType.TREE -> {
                 bytes.drop(TREE_HEADER_SIZE).decodeToString(onlyNullSeparator = false).lines()
             }
             GitObjectType.COMMIT -> {
                 FileUtils.getBytesBeforeNull(bytes, NullPredicateDirection.RIGHT_TO_LEFT).decodeToString().lines()
             }
         }

    }

    private fun getType(bytes: List<Int>): GitObjectType {
        val typeBuilder = StringBuilder()
        for (byte in bytes) {
            if (byte == 32) break
            typeBuilder.append(Char(byte))
        }
        return GitObjectType.valueOf(typeBuilder.toString().uppercase())
    }

}