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
 * The current implementation enriches test cases with a `filename` attribute when
 * the `classname` attribute can be resolved to a source file under the project root.
 *
 * DOM is used intentionally here because the assignment requires a relatively small,
 * structure-aware XML rewrite rather than streaming-scale processing.
 */
class XmlReportTransformer(
    private val sourceFileResolver: SourceFileResolver
) {

    /**
     * Reads the input XML report, enriches test cases with `filename` attributes,
     * and writes the result to the target location.
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

        val filename = sourceFileResolver.resolve(className) ?: return
        testCase.setAttribute("filename", filename)
    }
}