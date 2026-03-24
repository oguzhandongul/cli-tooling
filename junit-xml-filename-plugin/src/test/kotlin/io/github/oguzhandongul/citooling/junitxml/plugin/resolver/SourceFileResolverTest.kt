package io.github.oguzhandongul.citooling.junitxml.plugin.resolver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class SourceFileResolverTest {

    @Test
    fun `should resolve kotlin test file from fully qualified classname`() {
        val projectRoot = Files.createTempDirectory("resolver-test")
        val sourceFile = projectRoot.resolve(
            "module-a/src/test/kotlin/com/example/filters/FiltersTest.kt"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package com.example.filters

            class FiltersTest
            """.trimIndent()
        )

        val resolver = SourceFileResolver(projectRoot)

        val resolved = resolver.resolve("com.example.filters.FiltersTest")

        assertThat(resolved)
            .isEqualTo("module-a/src/test/kotlin/com/example/filters/FiltersTest.kt")
    }

    @Test
    fun `should resolve java test file from fully qualified classname`() {
        val projectRoot = Files.createTempDirectory("resolver-test")
        val sourceFile = projectRoot.resolve(
            "module-b/src/test/java/com/example/filters/FiltersJavaTest.java"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package com.example.filters;

            class FiltersJavaTest {}
            """.trimIndent()
        )

        val resolver = SourceFileResolver(projectRoot)

        val resolved = resolver.resolve("com.example.filters.FiltersJavaTest")

        assertThat(resolved)
            .isEqualTo("module-b/src/test/java/com/example/filters/FiltersJavaTest.java")
    }

    @Test
    fun `should resolve outer class file for nested classname`() {
        val projectRoot = Files.createTempDirectory("resolver-test")
        val sourceFile = projectRoot.resolve(
            "module-a/src/test/kotlin/com/example/filters/FiltersTest.kt"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package com.example.filters

            class FiltersTest
            """.trimIndent()
        )

        val resolver = SourceFileResolver(projectRoot)

        val resolved = resolver.resolve("com.example.filters.FiltersTest\$Nested")

        assertThat(resolved)
            .isEqualTo("module-a/src/test/kotlin/com/example/filters/FiltersTest.kt")
    }

    @Test
    fun `should return null when no matching source file is found`() {
        val projectRoot = Files.createTempDirectory("resolver-test")
        val resolver = SourceFileResolver(projectRoot)

        val resolved = resolver.resolve("com.example.missing.DoesNotExistTest")

        assertThat(resolved).isNull()
    }

    @Test
    fun `should fall back to filename search when direct suffix match is not found`() {
        val projectRoot = Files.createTempDirectory("resolver-test")
        val sourceFile = projectRoot.resolve(
            "custom-module/src/test/kotlin/odd/layout/FiltersTest.kt"
        )

        Files.createDirectories(sourceFile.parent)
        Files.writeString(
            sourceFile,
            """
            package odd.layout

            class FiltersTest
            """.trimIndent()
        )

        val resolver = SourceFileResolver(projectRoot)

        val resolved = resolver.resolve("com.example.unexpected.FiltersTest")

        assertThat(resolved)
            .isEqualTo("custom-module/src/test/kotlin/odd/layout/FiltersTest.kt")
    }
}