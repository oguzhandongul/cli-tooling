package io.github.oguzhandongul.citooling.junitxml.plugin.xml

import io.github.oguzhandongul.citooling.junitxml.plugin.resolver.SourceFileResolver
import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.createDirectories

/**
 * Transforms JUnit XML reports by adding source file metadata to each `<testcase>`.
 *
 * The implementation enriches test cases with a `filename` attribute when
 * the `classname` attribute can be resolved to a source file under the project root.
 *
 * As a bonus feature, if a test case fails, it parses the stacktrace to extract
 * the exact line number of the failure and appends it as a `line` attribute.
 *
 * DOM is used intentionally here because the assignment requires a relatively small,
 * structure-aware XML rewrite rather than streaming-scale processing.
 */
class XmlReportTransformer(
    private val sourceFileResolver: SourceFileResolver
) {

    /**
     * Reads the input XML report, enriches test cases with `filename` and `line` attributes,
     * and writes the transformed result to the target location.
     */
    fun transform(inputFile: Path, outputFile: Path) {
        val documentBuilder = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
        }.newDocumentBuilder()

        val document = documentBuilder.parse(inputFile.toFile())
        val testCases = document.getElementsByTagName("testcase")

        for (index in 0 until testCases.length) {
            val node = testCases.item(index)
            if (node is Element) {
                enrichTestCase(node)
            }
        }

        outputFile.parent?.createDirectories()

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
        }

        transformer.transform(
            DOMSource(document),
            StreamResult(outputFile.toFile())
        )
    }

    private fun enrichTestCase(testCase: Element) {
        val className = testCase.getAttribute("classname")
        if (className.isBlank()) return

        // Original Requirement: Append the resolved source filename
        val filename = sourceFileResolver.resolve(className)
        if (filename != null) {
            testCase.setAttribute("filename", filename)
        }

        // BONUS Requirement: Append the exact line number of the failure/error
        val lineNumber = extractLineNumber(testCase, className)
        if (lineNumber != null) {
            testCase.setAttribute("line", lineNumber)
        }
    }

    /**
     * Parses the stacktrace from `<failure>` or `<error>` nodes to locate
     * the exact line number where the assertion or exception occurred.
     *
     * @return The line number as a String if found, or null otherwise.
     */
    private fun extractLineNumber(testCase: Element, className: String): String? {
        val failures = testCase.getElementsByTagName("failure")
        val errors = testCase.getElementsByTagName("error")

        // If the test passed, there are no error/failure nodes, so we skip parsing.
        val errorNode = when {
            failures.length > 0 -> failures.item(0)
            errors.length > 0 -> errors.item(0)
            else -> return null
        }

        val stackTrace = errorNode.textContent ?: return null

        // Extract the simple class name (e.g., com.example.SampleTest -> SampleTest)
        val simpleClassName = className.substringAfterLast('.')

        // Regex: Matches (SampleTest.kt:42) or (SampleTest.java:42) and captures '42'
        val regex = Regex("""\(${Regex.escape(simpleClassName)}\.(?:kt|java):(\d+)\)""")
        val match = regex.find(stackTrace)

        return match?.groupValues?.get(1)
    }
}