package io.github.oguzhandongul.citooling.filter.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class EnvFileWriterTest {

    private val writer = EnvFileWriter()

    @Test
    fun `should write env file content`() {
        val tempFile = Files.createTempFile("filter-results", ".env")

        writer.write(
            outputPath = tempFile,
            results = linkedMapOf(
                "tests" to true,
                "backend" to false,
                "frontend" to true
            )
        )

        val content = Files.readString(tempFile)

        assertThat(content).isEqualTo(
            """
            tests=true
            backend=false
            frontend=true

            """.trimIndent()
        )
    }

    @Test
    fun `should create parent directories when needed`() {
        val tempDir = Files.createTempDirectory("env-writer")
        val nestedFile = tempDir.resolve("nested/output/result.env")

        writer.write(
            outputPath = nestedFile,
            results = linkedMapOf("backend" to true)
        )

        assertThat(Files.exists(nestedFile)).isTrue()
        assertThat(Files.readString(nestedFile)).isEqualTo(
            """
            backend=true

            """.trimIndent()
        )
    }
}