package buildsrc.utils

fun String.splitNonAlphanumeric(): List<String> =
  split(Regex("[^\\p{Alnum}]"))

fun String.toCamelCase(): String =
  splitNonAlphanumeric().joinToString("") { it.replaceFirstChar { it.uppercase() } }
