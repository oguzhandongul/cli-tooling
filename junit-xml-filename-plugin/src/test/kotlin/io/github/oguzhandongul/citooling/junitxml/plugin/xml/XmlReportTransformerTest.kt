package io.github.oguzhandongul.citooling.junitxml.plugin.xml

import io.github.oguzhandongul.citooling.junitxml.plugin.resolver.SourceFileResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class XmlReportTransformerTest {

    @Test
    fun `should add filename attribute when classname can be resolved`() {
        val projectRoot = Files.createTempDirectory("transformer-test")
        val sourceFile = projectRoot.resolve(
            "module-a/src/test/kotlin/com/example/filters/FiltersTest.kt"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package com.example.filters

            class FiltersTest
            """.trimIndent()
        )

        val inputXml = projectRoot.resolve("TEST-com.example.filters.FiltersTest.xml")
        Files.writeString(
            inputXml,
            """
            <testsuite name="com.example.filters.FiltersTest" tests="1" failures="0">
              <testcase
                  name="testExample"
                  classname="com.example.filters.FiltersTest"
                  time="0.123"/>
            </testsuite>
            """.trimIndent()
        )

        val outputXml = projectRoot.resolve("out/TEST-com.example.filters.FiltersTest.xml")

        val transformer = XmlReportTransformer(SourceFileResolver(projectRoot))
        transformer.transform(inputXml, outputXml)

        val content = Files.readString(outputXml)

        assertThat(content).contains("""filename="module-a/src/test/kotlin/com/example/filters/FiltersTest.kt"""")
    }

    @Test
    fun `should leave testcase unchanged when classname cannot be resolved`() {
        val projectRoot = Files.createTempDirectory("transformer-test")

        val inputXml = projectRoot.resolve("TEST-missing.xml")
        Files.writeString(
            inputXml,
            """
            <testsuite name="missing" tests="1" failures="0">
              <testcase
                  name="testMissing"
                  classname="com.example.missing.DoesNotExistTest"
                  time="0.123"/>
            </testsuite>
            """.trimIndent()
        )

        val outputXml = projectRoot.resolve("out/TEST-missing.xml")

        val transformer = XmlReportTransformer(SourceFileResolver(projectRoot))
        transformer.transform(inputXml, outputXml)

        val content = Files.readString(outputXml)

        assertThat(content).contains("""classname="com.example.missing.DoesNotExistTest"""")
        assertThat(content).doesNotContain("""filename=""")
    }

    @Test
    fun `should ignore testcase without classname attribute`() {
        val projectRoot = Files.createTempDirectory("transformer-test")

        val inputXml = projectRoot.resolve("TEST-no-classname.xml")
        Files.writeString(
            inputXml,
            """
            <testsuite name="no-classname" tests="1" failures="0">
              <testcase name="testAnonymous" time="0.123"/>
            </testsuite>
            """.trimIndent()
        )

        val outputXml = projectRoot.resolve("out/TEST-no-classname.xml")

        val transformer = XmlReportTransformer(SourceFileResolver(projectRoot))
        transformer.transform(inputXml, outputXml)

        val content = Files.readString(outputXml)

        assertThat(content).contains("""<testcase name="testAnonymous" time="0.123"/>""")
        assertThat(content).doesNotContain("""filename=""")
    }

    @Test
    fun `should create output parent directories`() {
        val projectRoot = Files.createTempDirectory("transformer-test")
        val sourceFile = projectRoot.resolve(
            "module-a/src/test/kotlin/com/example/filters/FiltersTest.kt"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(sourceFile, "class FiltersTest")

        val inputXml = projectRoot.resolve("TEST-create-dirs.xml")
        Files.writeString(
            inputXml,
            """
            <testsuite name="suite" tests="1" failures="0">
              <testcase
                  name="testExample"
                  classname="com.example.filters.FiltersTest"
                  time="0.123"/>
            </testsuite>
            """.trimIndent()
        )

        val outputXml = projectRoot.resolve("nested/output/TEST-create-dirs.xml")

        val transformer = XmlReportTransformer(SourceFileResolver(projectRoot))
        transformer.transform(inputXml, outputXml)

        assertThat(Files.exists(outputXml)).isTrue()
    }
}