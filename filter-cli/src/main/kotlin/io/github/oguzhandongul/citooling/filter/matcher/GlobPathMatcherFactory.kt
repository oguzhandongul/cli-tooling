package io.github.oguzhandongul.citooling.filter.matcher

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * Creates JDK [PathMatcher] instances for repository-style glob expressions.
 *
 * This implementation standardizes all input to forward-slash repository-relative
 * paths before delegating to the built-in `glob:` syntax supported by the JDK.
 *
 * The assignment only requires `*`, `**`, and exclusion markers handled outside
 * the matcher itself. Using [PathMatcher] avoids maintaining a custom glob engine
 * while still keeping the matching contract small and explicit.
 */
class GlobPathMatcherFactory {

    /**
     * Compiles a normalized glob expression into a [PathMatcher].
     */
    fun create(glob: String): PathMatcher {
        val normalizedGlob = normalize(glob)
        return FileSystems.getDefault().getPathMatcher("glob:$normalizedGlob")
    }

    /**
     * Normalizes an incoming path or glob to a repository-style representation.
     *
     * The CLI reasons about repository-relative paths, so using forward slashes
     * consistently makes the intent clearer and avoids leaking host OS separators
     * into matching logic.
     */
    fun normalize(path: String): String =
        path.replace('\\', '/').trim()

    /**
     * Converts a normalized repository-style path string into a [Path] that can be
     * consumed by the JDK matcher.
     */
    fun toPath(path: String): Path =
        Path.of(normalize(path))
}