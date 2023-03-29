package gitinternals

private const val HEX_TEN_SUBSTITUTE = "Ā"
private const val NULL_CODE = 0
private const val LINE_SEPARATOR_CODE = 10

fun String.lines() = this.split("\n")

fun List<Int>.decodeToString(onlyNullSeparator: Boolean = true): String {
    val stringBuilder = StringBuilder()
    for (item in this) {
        when {
            item == NULL_CODE -> stringBuilder.append(System.getProperty("line.separator"))
            item == LINE_SEPARATOR_CODE && onlyNullSeparator.not() -> stringBuilder.append(HEX_TEN_SUBSTITUTE)
            else -> stringBuilder.append(item.toChar())
        }
    }
    return stringBuilder.toString()
}