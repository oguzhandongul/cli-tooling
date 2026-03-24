package io.github.oguzhandongul.citooling.filter.model

/**
 * Filters config.
 *
 * The value of each map entry is kept as raw pattern text at this stage.
 * Compilation into include/exclude matcher groups happens later in the loading pipeline.
 *
 * This type represents only the raw external configuration contract.
 * It does not embed evaluation semantics such as inclusion/exclusion splitting;
 * that transformation is handled by [io.github.oguzhandongul.citooling.filter.config.YamlConfigLoader].
 *
 * @property filters a map from logical filter name to ordered pattern definitions
 */

data class FiltersConfig(
    val filters: Map<String, List<String>> = emptyMap()
)