import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

class AnalysisProcessorTest {
    private lateinit var processor: AnalysisProcessor
    private val gson = Gson()
    private val inputPath1 = javaClass.classLoader.getResource("input1.json")?.file ?: throw AssertionError("File not found: input1.json")
    private val inputPath2 = javaClass.classLoader.getResource("input2.json")?.file ?: throw AssertionError("File not found: input2.json")

    private val writeAnalysisResult = getAccessibleMethod("writeAnalysisResult")
    private val readAnalysisResult = getAccessibleMethod("readAnalysisResult")
    private val matchAnalysis = getAccessibleMethod("matchAnalysis")
    private val diffAnalysis = getAccessibleMethod("diffAnalysis")
    private fun getAccessibleMethod(methodName: String) = AnalysisProcessor::class.declaredMemberFunctions
        .firstOrNull { it.name == methodName }?.apply { isAccessible = true }
        ?: throw AssertionError("Method not found: $methodName")

    @BeforeEach
    fun setup() {
        processor = AnalysisProcessor()
    }

    @Test
    fun `test readAnalysisResult`() {
        val result = readAnalysisResult.call(processor, inputPath1) as AnalysisResult
        assertEquals(2, result.problems.size, "Incorrect number of problems in the analysis result")
        assertEquals("123abc", result.problems[0].hash, "Incorrect hash in the first problem")
    }

    @Test
    fun `test writeAnalysisResult`() {
        val fileMock = File.createTempFile("output", ".json")
        val method = AnalysisProcessor::class.declaredMemberFunctions.first { it.name == "writeAnalysisResult" }
        method.isAccessible = true

        writeAnalysisResult.call(processor, AnalysisResult(listOf(Problem("hash1", listOf("data1")))), fileMock.absolutePath)

        val content = fileMock.readText()
        val result = gson.fromJson(content, AnalysisResult::class.java)

        assertEquals(1, result.problems.size, "Incorrect number of problems in the written analysis result")
        assertEquals("hash1", result.problems[0].hash, "Incorrect hash in the written problem")
    }

    @Test
    fun `test diffAnalysis`() {
        val firstAnalysis = readAnalysisResult.call(processor, inputPath1) as AnalysisResult
        val secondAnalysis = readAnalysisResult.call(processor, inputPath2) as AnalysisResult

        val uniqueProblemsFirst = diffAnalysis.call(processor, firstAnalysis, secondAnalysis) as List<Problem>
        val uniqueProblemsSecond = diffAnalysis.call(processor, secondAnalysis, firstAnalysis) as List<Problem>

        assertEquals(1, uniqueProblemsFirst.size, "Incorrect number of unique problems in the first analysis")
        assertEquals(1, uniqueProblemsSecond.size, "Incorrect number of unique problems in the second analysis")
        assertEquals("456def", uniqueProblemsFirst[0].hash, "Incorrect hash of unique problem in the first analysis")
        assertEquals("789ghi", uniqueProblemsSecond[0].hash, "Incorrect hash of unique problem in the second analysis")
    }

    @Test
    fun `test matchAnalysis`() {
        val firstAnalysis = readAnalysisResult.call(processor, inputPath1) as AnalysisResult
        val secondAnalysis = readAnalysisResult.call(processor, inputPath2) as AnalysisResult

        val commonProblems = matchAnalysis.call(processor, firstAnalysis, secondAnalysis) as List<Problem>

        assertEquals(1, commonProblems.size, "Incorrect number of common problems")
        assertEquals("123abc", commonProblems[0].hash, "Incorrect hash of common problem")
    }
}
