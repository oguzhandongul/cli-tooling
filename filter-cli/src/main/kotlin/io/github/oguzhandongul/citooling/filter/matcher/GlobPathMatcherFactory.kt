package io.github.oguzhandongul.citooling.filter.matcher

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * Factory responsible for creating path matchers based on glob patterns.
 *
 * This implementation delegates matching to the JDK's built-in [PathMatcher]
 * using the `glob:` syntax, ensuring compatibility with standard glob semantics
 * and avoiding the need for custom pattern parsing.
 *
 */
class GlobPathMatcherFactory {

    /**
     * Creates a [PathMatcher] for the given glob expression.
     *
     * For most patterns, the returned matcher is a direct JDK glob matcher. For patterns
     * of the form a composite matcher is returned so root-level files are also
     * matched as expected by CI filtering semantics.
     */
    fun create(glob: String): PathMatcher {
        val normalizedGlob = normalize(glob)

        val primaryMatcher = createMatcher(normalizedGlob)
        val fallbackMatcher = createFallbackMatcherIfNeeded(normalizedGlob)

        return PathMatcher { path ->
            primaryMatcher.matches(path) || (fallbackMatcher?.matches(path) == true)
        }
    }

    /**
     * Normalizes input paths and patterns by:
     * - replacing Windows-style separators (`\`) with `/`
     * - trimming surrounding whitespace
     */
    fun normalize(path: String): String =
        path.replace('\\', '/').trim()

    /**
     * Converts a normalized repository-style path string into a [Path] that can be
     * consumed by the JDK matcher.
     */
    fun toPath(path: String): Path =
        Path.of(normalize(path))

    /**
     * Creates a JDK [PathMatcher] for the given glob pattern.
     */
    private fun createMatcher(pattern: String): PathMatcher {
        return FileSystems.getDefault().getPathMatcher("glob:$pattern")
    }

    /**
     * Creates a fallback matcher for given patterns such as root-level files
     * like `App.kt` are also matched.
     *
     * Returns `null` when no fallback is needed.
     */
    private fun createFallbackMatcherIfNeeded(pattern: String): PathMatcher? {
        val match = ROOT_LEVEL_DOUBLE_STAR_PATTERN.matchEntire(pattern) ?: return null
        val extension = match.groupValues[1]
        return createMatcher("*.$extension")
    }

    private companion object {
        val ROOT_LEVEL_DOUBLE_STAR_PATTERN = Regex("""^\*\*/\*\.(.+)$""")
    }
}