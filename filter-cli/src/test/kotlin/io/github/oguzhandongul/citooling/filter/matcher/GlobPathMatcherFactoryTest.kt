package io.github.oguzhandongul.citooling.filter.matcher

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GlobPathMatcherFactoryTest {

    private val factory = GlobPathMatcherFactory()

    @Test
    fun `should match exact filename`() {
        val matcher = factory.create("README.md")

        assertThat(matcher.matches(factory.toPath("README.md"))).isTrue()
        assertThat(matcher.matches(factory.toPath("docs/README.md"))).isFalse()
    }

    @Test
    fun `should match single segment wildcard`() {
        val matcher = factory.create("src/*.kt")

        assertThat(matcher.matches(factory.toPath("src/Main.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("src/Util.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("src/nested/Main.kt"))).isFalse()
    }

    @Test
    fun `should match multi segment wildcard`() {
        val matcher = factory.create("src/**/*.kt")

        assertThat(matcher.matches(factory.toPath("src/main/Main.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("src/main/kotlin/App.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("lib/main/kotlin/App.kt"))).isFalse()
    }

    @Test
    fun `should match docs double star pattern`() {
        val matcher = factory.create("docs/**")

        assertThat(matcher.matches(factory.toPath("docs/index.md"))).isTrue()
        assertThat(matcher.matches(factory.toPath("docs/guides/setup.md"))).isTrue()
        assertThat(matcher.matches(factory.toPath("other/docs/index.md"))).isFalse()
    }

    @Test
    fun `should match kotlin files in nested paths`() {
        val matcher = factory.create("**/*.kt")

        assertThat(matcher.matches(factory.toPath("src/main/kotlin/App.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("src/test/kotlin/AppTest.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("README.md"))).isFalse()
    }

    @Test
    fun `should normalize windows style path separators`() {
        val matcher = factory.create("src/**/*.kt")

        assertThat(matcher.matches(factory.toPath("src\\main\\kotlin\\App.kt"))).isTrue()
    }

    @Test
    fun `should trim whitespace during normalization`() {
        assertThat(factory.normalize("  src\\main\\App.kt  "))
            .isEqualTo("src/main/App.kt")
    }

    @Test
    fun `should match root level file with double star pattern`() {
        val matcher = factory.create("**/*.kt")

        assertThat(matcher.matches(factory.toPath("App.kt"))).isTrue()
        assertThat(matcher.matches(factory.toPath("README.md"))).isFalse()
    }
}