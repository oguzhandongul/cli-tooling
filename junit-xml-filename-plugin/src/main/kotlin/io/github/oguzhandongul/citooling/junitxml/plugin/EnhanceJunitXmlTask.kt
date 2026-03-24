package io.github.oguzhandongul.citooling.junitxml.plugin

import io.github.oguzhandongul.citooling.junitxml.plugin.resolver.SourceFileResolver
import io.github.oguzhandongul.citooling.junitxml.plugin.xml.XmlReportTransformer
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * Task that reads Gradle-produced JUnit XML files, enriches test cases with source
 * file metadata, and writes the transformed XML to a dedicated output directory.
 *
 * The task is intentionally modeled as a separate step after the original [Test]
 * task instead of embedding post-processing logic directly inside the test task.
 * This keeps responsibilities clearer and makes inputs/outputs explicit.
 */
abstract class EnhanceJunitXmlTask @Inject constructor(
    private val layout: ProjectLayout
) : DefaultTask() {

    /**
     * Directory containing the original JUnit XML files produced by a Gradle [Test] task.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val originalXmlDirectory: DirectoryProperty

    /**
     * Directory where enriched XML files will be written.
     */
    @get:OutputDirectory
    abstract val transformedXmlDirectory: DirectoryProperty

    /**
     * Root directory of the Gradle project.
     *
     * This is used as the base path for producing repository-relative filenames.
     */
    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @TaskAction
    fun enhance() {
        val inputDir = originalXmlDirectory.get().asFile.toPath()
        val outputDir = transformedXmlDirectory.get().asFile.toPath()
        val projectRoot = projectRootDirectory.get().asFile.toPath()

        outputDir.createDirectories()

        val resolver = SourceFileResolver(projectRoot)
        val transformer = XmlReportTransformer(resolver)

        Files.walk(inputDir)
            .filter { it.isRegularFile() }
            .filter { it.extension.equals("xml", ignoreCase = true) }
            .forEach { inputFile ->
                val outputFile = outputDir.resolve(inputFile.name)
                transformer.transform(inputFile, outputFile)
            }
    }
}