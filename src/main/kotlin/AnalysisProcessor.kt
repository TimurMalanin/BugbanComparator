import com.google.gson.Gson
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

data class Problem(
    val hash: String, val data: List<String>
)

data class AnalysisResult(
    val problems: List<Problem>
)

class AnalysisProcessor {
    fun process(firstFilePath: String, secondFilePath: String, outputOnlyInFirstFilePath: String, outputOnlyInSecondFilePath: String, outputInBothFilePath: String) {
        try {
            val firstAnalysis = readAnalysisResult(firstFilePath)
            val secondAnalysis = readAnalysisResult(secondFilePath)

            val onlyInFirst = diffAnalysis(firstAnalysis, secondAnalysis)
            val onlyInSecond = diffAnalysis(secondAnalysis, firstAnalysis)
            val inBoth = matchAnalysis(firstAnalysis, secondAnalysis)

            writeAnalysisResult(AnalysisResult(onlyInFirst), outputOnlyInFirstFilePath)
            writeAnalysisResult(AnalysisResult(onlyInSecond), outputOnlyInSecondFilePath)
            writeAnalysisResult(AnalysisResult(inBoth), outputInBothFilePath)

        } catch (e: Exception) {
            println("Error processing files: ${e.message}")
            exitProcess(2)
        }
    }

    private fun readAnalysisResult(filePath: String): AnalysisResult {
        try {
            val content = File(filePath).readText()
            return Gson().fromJson(content, AnalysisResult::class.java)
        } catch (e: IOException) {
            throw Exception("Failed to read or parse file at $filePath", e)
        }
    }

    private fun writeAnalysisResult(analysisResult: AnalysisResult, filePath: String) {
        try {
            File(filePath).writeText(Gson().toJson(analysisResult))
        } catch (e: IOException) {
            throw Exception("Failed to write to file at $filePath", e)
        }
    }

    private fun diffAnalysis(firstAnalysis: AnalysisResult, secondAnalysis: AnalysisResult): MutableList<Problem> {
        val secondHashes = secondAnalysis.problems.map { it.hash }.toSet()
        return firstAnalysis.problems.filter { it.hash !in secondHashes }.filter { it.data.isNotEmpty() }.toMutableList()
    }

    private fun matchAnalysis(firstAnalysis: AnalysisResult, secondAnalysis: AnalysisResult): MutableList<Problem> {
        val secondProblemsByHash = secondAnalysis.problems.associateBy { it.hash }
        return firstAnalysis.problems.filter {
            it.hash in secondProblemsByHash.keys && it.data.sorted() == secondProblemsByHash[it.hash]?.data?.sorted()
        }.filter { it.data.isNotEmpty() }.toMutableList()
    }
}
