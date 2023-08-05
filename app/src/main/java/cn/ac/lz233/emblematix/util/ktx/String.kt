package cn.ac.lz233.emblematix.util.ktx

fun String.safeSubString(startIndex: Int, endIndex: Int): String {
    if (startIndex >= endIndex) return ""
    return substring(
        if (startIndex < 0) {
            0
        } else if (startIndex > lastIndex) {
            lastIndex
        } else {
            startIndex
        },
        if (endIndex < 0) {
            0
        } else if (endIndex > lastIndex) {
            lastIndex
        } else {
            endIndex
        }
    )
}