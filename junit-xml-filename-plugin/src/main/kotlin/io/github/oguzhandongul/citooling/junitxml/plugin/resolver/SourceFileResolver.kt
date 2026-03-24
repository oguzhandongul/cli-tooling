package io.github.oguzhandongul.citooling.junitxml.plugin.resolver

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * Resolves test class names from JUnit XML into source file paths relative to the
 * project root.
 *
 * The implementation intentionally uses a pragmatic heuristic rather than deep
 * language parsing. For typical Kotlin and Java test classes, the JUnit XML
 * `classname` attribute maps well to source files using package-to-path conversion.
 *
 * Supported conventions:
 * - `com.example.MyTest`       -> `.../com/example/MyTest.kt` or `.java`
 * - `com.example.MyTest$Inner` -> `.../com/example/MyTest.kt` or `.java`
 *
 * This is sufficient for the assignment and keeps the resolver easy to understand.
 */
class SourceFileResolver(
    private val projectRoot: Path
) {

    /**
     * Resolves a JUnit XML classname into a repository-relative source file path.
     *
     * Returns `null` if no matching source file can be found.
     */
    fun resolve(className: String): String? {
        val normalizedClassName = className.substringBefore('$')
        val packagePath = normalizedClassName.replace('.', '/')

        val candidateSuffixes = listOf(
            "src/test/kotlin/$packagePath.kt",
            "src/test/java/$packagePath.java"
        )

        candidateSuffixes.forEach { suffix ->
            val candidate = findUnderProjectRoot(suffix)
            if (candidate != null) {
                return projectRoot.relativize(candidate).invariantSeparatorsPathString
            }
        }

        val simpleName = normalizedClassName.substringAfterLast('.')
        return findByFileName(simpleName)?.let { found ->
            projectRoot.relativize(found).invariantSeparatorsPathString
        }
    }

    private fun findUnderProjectRoot(relativeSuffix: String): Path? {
        Files.walk(projectRoot).use { paths ->
            return paths
                .filter { it.isRegularFile() }
                .filter { path ->
                    path.invariantSeparatorsPathString.endsWith(relativeSuffix)
                }
                .findFirst()
                .orElse(null)
        }
    }

    private fun findByFileName(simpleName: String): Path? {
        val possibleNames = listOf("$simpleName.kt", "$simpleName.java")

        Files.walk(projectRoot).use { paths ->
            return paths
                .filter { it.isRegularFile() }
                .filter { path ->
                    path.name in possibleNames &&
                            path.invariantSeparatorsPathString.contains("/src/test/")
                }
                .findFirst()
                .orElse(null)
        }
    }
}