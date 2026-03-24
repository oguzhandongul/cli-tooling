package io.github.oguzhandongul.citooling.filter.output

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * Serializes filter evaluation results into a simple `.env`-style file.
 *
 * The generated format is intentionally minimal because it is designed for CI/CD
 * systems that consume line-based `key=value` pairs.
 *
 * Example output:
 * ```text
 * backend=true
 * frontend=false
 * docs=true
 * ```
 */
class EnvFileWriter {

    /**
     * Writes all results to the target file, creating parent directories when needed.
     *
     * The writer does not attempt partial updates; it always rewrites the full file.
     * For this use case that is preferable because:
     * - output generation is cheap
     * - atomic full regeneration is easier to reason about
     * - the CLI is the single source of truth for the file contents
     */
    fun write(outputPath: Path, results: Map<String, Boolean>) {
        outputPath.parent
            ?.takeIf { !it.exists() }
            ?.createDirectories()

        val content = buildString {
            results.forEach { (name, matched) ->
                append(name)
                append("=")
                append(matched.toString())
                appendLine()
            }
        }

        Files.writeString(outputPath, content)
    }
}