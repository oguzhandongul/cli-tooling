package io.github.oguzhandongul.citooling.filter.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oguzhandongul.citooling.filter.model.CompiledFilter
import io.github.oguzhandongul.citooling.filter.model.FiltersConfig
import io.github.oguzhandongul.citooling.filter.model.GlobPattern
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class YamlConfigLoader {

    private val mapper = YAMLMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

    fun load(configPath: Path): FiltersConfig {
        require(configPath.exists()) {
            "Configuration file does not exist: $configPath"
        }
        require(configPath.isRegularFile()) {
            "Configuration path is not a file: $configPath"
        }

        val config = mapper.readValue(configPath.toFile(), FiltersConfig::class.java)
        validate(config)
        return config
    }

    fun loadCompiled(
        configPath: Path,
        compiler: (String) -> PathMatcher
    ): List<CompiledFilter> {
        val config = load(configPath)

        return config.filters.map { (filterName, patterns) ->
            val includes = mutableListOf<GlobPattern>()
            val excludes = mutableListOf<GlobPattern>()

            patterns.forEach { rawPattern ->
                val isExclude = rawPattern.startsWith("!")
                val normalizedPattern = if (isExclude) rawPattern.drop(1) else rawPattern

                val compiled = GlobPattern(
                    raw = normalizedPattern,
                    matcher = compiler(normalizedPattern)
                )

                if (isExclude) {
                    excludes += compiled
                } else {
                    includes += compiled
                }
            }

            CompiledFilter(
                name = filterName,
                includes = includes,
                excludes = excludes
            )
        }
    }

    private fun validate(config: FiltersConfig) {
        require(config.filters.isNotEmpty()) {
            "Configuration must define at least one filter under 'filters'."
        }

        config.filters.forEach { (filterName, patterns) ->
            require(filterName.isNotBlank()) {
                "Filter names must not be blank."
            }

            require(patterns.isNotEmpty()) {
                "Filter '$filterName' must define at least one pattern."
            }

            patterns.forEach { pattern ->
                require(pattern.isNotBlank()) {
                    "Filter '$filterName' contains a blank pattern."
                }

                require(pattern != "!") {
                    "Filter '$filterName' contains an invalid exclusion pattern '!'."
                }
            }
        }
    }
}