package io.github.oguzhandongul.citooling.filter.model

/**
 * Runtime representation of a named filter after configuration preprocessing.
 *
 * A filter in the YAML file contains a single ordered list of patterns where
 * exclusion rules are denoted by a leading `!`. For evaluation purposes, the
 * engine benefits from splitting that mixed list into explicit inclusion and
 * exclusion collections.
 *
 * This separation makes the matching logic simpler and easier to reason about:
 * a changed file must match at least one include pattern and no exclude pattern.
 *
 * @property name logical filter name that will also be used as the output key
 * @property includes compiled include patterns
 * @property excludes compiled exclude patterns
 */

data class CompiledFilter(
    val name: String,
    val includes: List<GlobPattern>,
    val excludes: List<GlobPattern>
)