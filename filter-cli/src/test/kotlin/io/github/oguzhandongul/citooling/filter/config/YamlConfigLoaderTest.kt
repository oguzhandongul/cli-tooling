package io.github.oguzhandongul.citooling.filter.config

import io.github.oguzhandongul.citooling.filter.matcher.GlobPathMatcherFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files

class YamlConfigLoaderTest {

    private val loader = YamlConfigLoader()
    private val factory = GlobPathMatcherFactory()

    @Test
    fun `should load yaml config`() {
        val tempFile = Files.createTempFile("filters", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters:
              backend:
                - "app/src/**/*.kt"
                - "!app/src/test/**"
              docs:
                - "README.md"
                - "docs/**/*.md"
            """.trimIndent()
        )

        val config = loader.load(tempFile)

        assertThat(config.filters).containsKeys("backend", "docs")
        assertThat(config.filters["backend"]).containsExactly(
            "app/src/**/*.kt",
            "!app/src/test/**"
        )
        assertThat(config.filters["docs"]).containsExactly(
            "README.md",
            "docs/**/*.md"
        )
    }

    @Test
    fun `should compile includes and excludes`() {
        val tempFile = Files.createTempFile("filters", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters:
              backend:
                - "app/src/**/*.kt"
                - "!app/src/test/**"
            """.trimIndent()
        )

        val compiled = loader.loadCompiled(tempFile, factory::create)

        assertThat(compiled).hasSize(1)

        val filter = compiled.first()
        assertThat(filter.name).isEqualTo("backend")
        assertThat(filter.includes).hasSize(1)
        assertThat(filter.excludes).hasSize(1)
        assertThat(filter.includes.first().raw).isEqualTo("app/src/**/*.kt")
        assertThat(filter.excludes.first().raw).isEqualTo("app/src/test/**")
    }

    @Test
    fun `should fail when config file does not exist`() {
        val missingPath = Files.createTempDirectory("missing")
            .resolve("nope.yaml")

        val exception = assertThrows<IllegalArgumentException> {
            loader.load(missingPath)
        }

        assertThat(exception.message).contains("Configuration file does not exist")
    }

    @Test
    fun `should fail when filters section is empty`() {
        val tempFile = Files.createTempFile("filters-empty", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters: {}
            """.trimIndent()
        )

        val exception = assertThrows<IllegalArgumentException> {
            loader.load(tempFile)
        }

        assertThat(exception.message).contains("at least one filter")
    }

    @Test
    fun `should fail when filter has no patterns`() {
        val tempFile = Files.createTempFile("filters-empty-patterns", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters:
              backend: []
            """.trimIndent()
        )

        val exception = assertThrows<IllegalArgumentException> {
            loader.load(tempFile)
        }

        assertThat(exception.message).contains("must define at least one pattern")
    }

    @Test
    fun `should fail when filter contains blank pattern`() {
        val tempFile = Files.createTempFile("filters-blank-pattern", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters:
              backend:
                - " "
            """.trimIndent()
        )

        val exception = assertThrows<IllegalArgumentException> {
            loader.load(tempFile)
        }

        assertThat(exception.message).contains("blank pattern")
    }

    @Test
    fun `should fail when filter contains invalid exclusion marker only`() {
        val tempFile = Files.createTempFile("filters-invalid-exclusion", ".yaml")
        Files.writeString(
            tempFile,
            """
            filters:
              backend:
                - "!"
            """.trimIndent()
        )

        val exception = assertThrows<IllegalArgumentException> {
            loader.load(tempFile)
        }

        assertThat(exception.message).contains("invalid exclusion pattern")
    }
}