package gitinternals

enum class GitObjectType { BLOB, TREE, COMMIT }

enum class TreeContentEntry { MODE, HASH, FILENAME }

enum class NullPredicateDirection { LEFT_TO_RIGHT, RIGHT_TO_LEFT }

enum class CommitContent(val identifier: String) {
    TREE("tree"),
    PARENT("parent"),
    AUTHOR("author"),
    COMMITTER("committer"),
    COMMIT_MESSAGE("")
}