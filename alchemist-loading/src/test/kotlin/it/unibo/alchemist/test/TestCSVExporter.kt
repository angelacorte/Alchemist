/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.regex.shouldMatch
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemist
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import java.io.File

class TestCSVExporter<T, P : Position<P>> :
    FreeSpec({
        "CSV files" - {
            val simulation = loadAlchemist<T, P>("testCSVExporter.yml")
            simulation.runInCurrentThread()
            val globalExporter =
                simulation.outputMonitors
                    .filterIsInstance<GlobalExporter<T, P>>()
                    .let {
                        it.size shouldBe 1
                        it.first()
                    }
            globalExporter.exporters.size shouldBe 2

            suspend fun CSVExporter<T, P>.dataFile(prefix: String): File = File(exportPath)
                .listFiles()
                ?.find { it.name.startsWith(prefix) && it.extension == fileExtension }
                .run { shouldNotBeNull() }
                .apply { "with prefix $prefix should exist" { shouldExist() } }
            val outputFile = simulation.csvExporters().first().dataFile("00-testing_csv_export_")
            "should exist when CSV export is enabled" {
                outputFile.shouldNotBeNull()
                outputFile.shouldExist()
            }
            "header should be like \"var1 = val1, var2 = val2\"" {
                val fileContents = outputFile.readText()
                val match = Regex("([^\\s=,]+)\\s*=\\s*([^,\\s]+)").findAll(fileContents).toList()
                require(match.isNotEmpty()) {
                    "Unmatched header regex in ${outputFile.absolutePath}:\n$fileContents"
                }
            }
            "should have limited-length decimals" {
                val limitedDecimalsFile = simulation.csvExporters()[1].dataFile("fixed-decimals_")
                val precision2 = """(0\.0*\d\d|\d\.0*\d|\d\.\d|\d\d)(e(-|\+)\d+)?"""
                val lineRegex = Regex("""^$precision2(\s($precision2))+$""")
                limitedDecimalsFile.useLines { lines ->
                    lines
                        .filterNot { it.startsWith("#") }
                        .forEach {
                            lineRegex shouldMatch it
                        }
                }
            }
        }
        "column order should replicate" {
            val simulation: Simulation<T, P> = loadAlchemist("testCSVExportColumnAlignment.yml")
            simulation.runInCurrentThread()
            val exporter = simulation.csvExporters().first()
            // Get the first line of the output produce by CSVExporter
            val exporterFirstLine =
                File(exporter.exportPath)
                    .listFiles()
                    ?.first { it.name.startsWith("column-alignment") }
                    ?.readLines()
                    ?.dropWhile { !it.contains("d c b a") } // remove the lines before the column names
                    ?.drop(1) // I am not interested in column head
                    ?.first()
            exporterFirstLine.shouldNotBeNull()
            exporterFirstLine.shouldNotBeEmpty()
            exporterFirstLine.shouldContain("0 1 2 3")
        }
    }) {
    // common utility functions
    companion object {
        fun <T, P : Position<P>> Simulation<T, P>.csvExporters(): List<CSVExporter<T, P>> = outputMonitors
            .filterIsInstance<GlobalExporter<T, P>>()
            .also { check(it.size == 1) }
            .first()
            .exporters
            .filterIsInstance<CSVExporter<T, P>>()
    }
}
