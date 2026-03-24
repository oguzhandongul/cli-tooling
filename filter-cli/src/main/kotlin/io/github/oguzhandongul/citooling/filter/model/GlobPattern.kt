package io.github.oguzhandongul.citooling.filter.model

import java.nio.file.PathMatcher

/**
 * A compiled glob pattern backed by the JDK's [PathMatcher].
 *
 * The tool keeps the original pattern string for readability and diagnostics, while
 * delegating actual matching behavior to the platform's built-in glob engine.
 *
 * @property raw the normalized glob expression
 * @property matcher the compiled JDK path matcher
 */
data class GlobPattern(
    val raw: String,
    val matcher: PathMatcher
)