package io.github.oguzhandongul.citooling.filter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.github.oguzhandongul.citooling.filter.config.YamlConfigLoader
import io.github.oguzhandongul.citooling.filter.matcher.GlobPathMatcherFactory
import io.github.oguzhandongul.citooling.filter.output.EnvFileWriter
import io.github.oguzhandongul.citooling.filter.service.FilterEvaluationService

fun main(args: Array<String>) = FilterCliCommand().main(args)

class FilterCliCommand : CliktCommand(name = "filter-cli") {

    override fun help(context: Context): String =
        "Evaluates changed file paths against YAML-defined CI/CD filters."

    private val config by option(
        "--config",
        help = "Path to the YAML filter configuration file."
    ).file(
        mustExist = true,
        canBeDir = false
    )

    private val output by option(
        "--output",
        help = "Path to the output .env file."
    ).file(
        canBeDir = false
    )

    private val changedFiles by option(
        "--changed",
        help = "Changed file path. This option may be repeated."
    ).multiple(required = true)

    override fun run() {
        val pathMatcherFactory = GlobPathMatcherFactory()
        val loader = YamlConfigLoader()
        val evaluator = FilterEvaluationService(pathMatcherFactory)
        val writer = EnvFileWriter()

        val filters = loader.loadCompiled(
            configPath = requireNotNull(config).toPath(),
            compiler = pathMatcherFactory::create
        )

        val results = evaluator.evaluate(
            filters = filters,
            changedFiles = changedFiles
        )

        writer.write(
            outputPath = requireNotNull(output).toPath(),
            results = results
        )
    }
}