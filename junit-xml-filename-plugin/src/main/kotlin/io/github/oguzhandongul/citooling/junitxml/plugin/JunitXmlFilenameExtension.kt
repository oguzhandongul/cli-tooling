package io.github.oguzhandongul.citooling.junitxml.plugin

import org.gradle.api.file.DirectoryProperty

/**
 * Extension for configuring the JUnit XML post-processing plugin.
 *
 * The plugin writes enriched test result XML files to a separate output directory
 * instead of overwriting Gradle's original JUnit XML reports. Keeping the output
 * separate makes the behavior safer, easier to debug, and more aligned with the
 * assignment requirements.
 */
abstract class JunitXmlFilenameExtension {

    /**
     * Directory where transformed JUnit XML files will be written.
     *
     * The path is expected to be configured relative to the project layout, usually
     * via `layout.buildDirectory.dir(...)`.
     */
    abstract val outputDir: DirectoryProperty
}