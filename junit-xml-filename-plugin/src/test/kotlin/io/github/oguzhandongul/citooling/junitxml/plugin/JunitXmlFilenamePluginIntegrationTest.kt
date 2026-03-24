package io.github.oguzhandongul.citooling.junitxml.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class JunitXmlFilenamePluginIntegrationTest {

    @TempDir
    lateinit var testProjectDir: Path

    @Test
    fun `should generate transformed junit xml with filename attributes`() {
        writeSettingsFile()
        writeBuildFile()
        writeTestSourceFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments(
                "test",
                "--stacktrace"
            )
            .forwardOutput()
            .build()

        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":enhanceTestJunitXml")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val transformedXml = testProjectDir.resolve(
            "build/custom-test-results/test/TEST-com.example.SampleTest.xml"
        )

        assertThat(Files.exists(transformedXml)).isTrue()

        val content = Files.readString(transformedXml)

        assertThat(content).contains("""filename="src/test/kotlin/com/example/SampleTest.kt"""")
        assertThat(content).contains("""classname="com.example.SampleTest"""")
    }

    private fun writeSettingsFile() {
        Files.writeString(
            testProjectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "test-project"
            """.trimIndent()
        )
    }

    private fun writeBuildFile() {
        Files.writeString(
            testProjectDir.resolve("build.gradle.kts"),
            """
            plugins {
                kotlin("jvm") version "2.2.0"
                id("io.github.oguzhandongul.junit-xml-filename")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation(kotlin("test"))
                testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }

            junitXmlFilename {
                outputDir.set(layout.buildDirectory.dir("custom-test-results"))
            }
            """.trimIndent()
        )
    }

    private fun writeTestSourceFile() {
        val sourceFile = testProjectDir.resolve("src/test/kotlin/com/example/SampleTest.kt")
        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package com.example

            import org.junit.jupiter.api.Test
            import kotlin.test.assertTrue

            class SampleTest {

                @Test
                fun testExample() {
                    assertTrue(true)
                }
            }
            """.trimIndent()
        )
    }
}