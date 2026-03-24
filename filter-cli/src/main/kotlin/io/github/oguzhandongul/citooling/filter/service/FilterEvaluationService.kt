package io.github.oguzhandongul.citooling.filter.service

import io.github.oguzhandongul.citooling.filter.matcher.GlobPathMatcherFactory
import io.github.oguzhandongul.citooling.filter.model.CompiledFilter

/**
 * Evaluates named filters against a set of changed file paths.
 *
 * A filter is considered matched when at least one changed file:
 * - matches any include pattern
 * - and matches no exclude pattern
 */
class FilterEvaluationService(
    private val pathMatcherFactory: GlobPathMatcherFactory = GlobPathMatcherFactory()
) {

    fun evaluate(
        filters: List<CompiledFilter>,
        changedFiles: List<String>
    ): Map<String, Boolean> {
        val normalizedFiles = changedFiles.map(pathMatcherFactory::toPath)

        return filters.associate { filter ->
            filter.name to matchesFilter(filter, normalizedFiles)
        }
    }

    private fun matchesFilter(
        filter: CompiledFilter,
        changedFiles: List<java.nio.file.Path>
    ): Boolean {
        if (filter.includes.isEmpty()) return false

        return changedFiles.any { file ->
            val matchesInclude = filter.includes.any { it.matcher.matches(file) }
            val matchesExclude = filter.excludes.any { it.matcher.matches(file) }
            matchesInclude && !matchesExclude
        }
    }
}