package io.github.oguzhandongul.citooling.filter.service

import io.github.oguzhandongul.citooling.filter.matcher.GlobPathMatcherFactory
import io.github.oguzhandongul.citooling.filter.model.CompiledFilter
import io.github.oguzhandongul.citooling.filter.model.GlobPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilterEvaluationServiceTest {

    private val factory = GlobPathMatcherFactory()
    private val service = FilterEvaluationService(factory)

    @Test
    fun `should return true when file matches include and not exclude`() {
        val filter = compiledFilter(
            name = "backend",
            includes = listOf("app/src/**/*.kt"),
            excludes = listOf("app/src/test/**")
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = listOf("app/src/main/kotlin/App.kt")
        )

        assertThat(result).containsEntry("backend", true)
    }

    @Test
    fun `should return false when file matches exclusion`() {
        val filter = compiledFilter(
            name = "backend",
            includes = listOf("app/src/**/*.kt"),
            excludes = listOf("app/src/test/**")
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = listOf("app/src/test/kotlin/AppTest.kt")
        )

        assertThat(result).containsEntry("backend", false)
    }

    @Test
    fun `should return true when at least one file is valid`() {
        val filter = compiledFilter(
            name = "backend",
            includes = listOf("app/src/**/*.kt"),
            excludes = listOf("app/src/test/**")
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = listOf(
                "app/src/test/kotlin/AppTest.kt",
                "app/src/main/kotlin/App.kt"
            )
        )

        assertThat(result).containsEntry("backend", true)
    }

    @Test
    fun `should evaluate multiple filters independently`() {
        val backend = compiledFilter(
            name = "backend",
            includes = listOf("app/src/**/*.kt"),
            excludes = listOf("app/src/test/**")
        )

        val docs = compiledFilter(
            name = "docs",
            includes = listOf("README.md", "docs/**/*.md"),
            excludes = emptyList()
        )

        val result = service.evaluate(
            filters = listOf(backend, docs),
            changedFiles = listOf(
                "README.md",
                "app/src/test/kotlin/AppTest.kt"
            )
        )

        assertThat(result).containsEntry("backend", false)
        assertThat(result).containsEntry("docs", true)
    }

    @Test
    fun `should return false when includes are empty`() {
        val filter = CompiledFilter(
            name = "empty",
            includes = emptyList(),
            excludes = emptyList()
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = listOf("README.md")
        )

        assertThat(result).containsEntry("empty", false)
    }

    @Test
    fun `should return false for empty changed files`() {
        val filter = compiledFilter(
            name = "docs",
            includes = listOf("docs/**"),
            excludes = emptyList()
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = emptyList()
        )

        assertThat(result).containsEntry("docs", false)
    }

    @Test
    fun `should normalize changed file paths before matching`() {
        val filter = compiledFilter(
            name = "backend",
            includes = listOf("app/src/**/*.kt"),
            excludes = emptyList()
        )

        val result = service.evaluate(
            filters = listOf(filter),
            changedFiles = listOf("app\\src\\main\\kotlin\\App.kt")
        )

        assertThat(result).containsEntry("backend", true)
    }

    private fun compiledFilter(
        name: String,
        includes: List<String>,
        excludes: List<String>
    ): CompiledFilter {
        return CompiledFilter(
            name = name,
            includes = includes.map(::pattern),
            excludes = excludes.map(::pattern)
        )
    }

    private fun pattern(raw: String): GlobPattern {
        return GlobPattern(
            raw = raw,
            matcher = factory.create(raw)
        )
    }
}