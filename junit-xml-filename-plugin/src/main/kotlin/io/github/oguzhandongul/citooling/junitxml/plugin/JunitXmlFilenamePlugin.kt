package io.github.oguzhandongul.citooling.junitxml.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import kotlin.jvm.java

/**
 * Gradle plugin that post-processes JUnit XML reports and enriches test cases
 * with source file metadata.
 *
 * The plugin hooks into all Gradle [Test] tasks and registers a dedicated follow-up
 * task for each one. That follow-up task reads the original XML output, enriches
 * it, and writes the transformed reports to a separate directory.
 *
 * The design intentionally avoids mutating Gradle's original XML output in place.
 * Keeping the transformed files separate is safer and makes the plugin easier to
 * reason about during local debugging and CI integration.
 */
class JunitXmlFilenamePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            JunitXmlFilenameExtension::class.java
        )

        extension.outputDir.convention(
            project.layout.buildDirectory.dir(DEFAULT_OUTPUT_DIRECTORY)
        )

        project.tasks.withType(Test::class.java).configureEach { testTask ->
            registerEnhancementTask(project, testTask, extension)
        }
    }

    private fun registerEnhancementTask(
        project: Project,
        testTask: Test,
        extension: JunitXmlFilenameExtension
    ) {
        val taskName = "enhance${testTask.name.replaceFirstChar(Char::uppercaseChar)}JunitXml"

        project.tasks.register(taskName, EnhanceJunitXmlTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description =
                "Enhances JUnit XML reports produced by the '${testTask.name}' test task."

            task.dependsOn(testTask)

            task.originalXmlDirectory.set(testTask.reports.junitXml.outputLocation)
            task.transformedXmlDirectory.set(
                extension.outputDir.map { outputDir ->
                    outputDir.dir(testTask.name)
                }
            )

            task.projectRootDirectory.set(project.layout.projectDirectory)
        }
    }

    private companion object {
        const val EXTENSION_NAME = "junitXmlFilename"
        const val DEFAULT_OUTPUT_DIRECTORY = "test-results/junit-xml-with-filenames"
        const val TASK_GROUP = "verification"
    }
}