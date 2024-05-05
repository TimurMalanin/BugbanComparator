import kotlin.system.exitProcess

fun main() {
    try {
        val firstFilePath = requestFilePath("Please enter the first file path:")
        val secondFilePath = requestFilePath("Please enter the second file path:")
        val outputOnlyInFirstFilePath = requestFilePath("Enter output path for problems only in the first analysis:")
        val outputOnlyInSecondFilePath = requestFilePath("Enter output path for problems only in the second analysis:")
        val outputInBothFilePath = requestFilePath("Enter output path for common problems:")

        AnalysisProcessor().process(
            firstFilePath,
            secondFilePath,
            outputOnlyInFirstFilePath,
            outputOnlyInSecondFilePath,
            outputInBothFilePath
        )
    } catch (e: Exception) {
        println(e.message)
        exitProcess(1)
    }
}

fun requestFilePath(prompt: String): String {
    println(prompt)
    return readlnOrNull()?.trim()?.takeUnless { it.isEmpty() } ?: run {
        println("Error: Invalid file path.")
        exitProcess(1)
    }
}
