/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.test.loadYamlSimulation
import java.io.File

object TestingEnvironments {
    fun <T, P> graphqlTestEnvironmnets(): Set<EuclideanEnvironment<T, P>> where P : Position<P>, P : Vector<P> =
        this::class.java.classLoader.getResource("yaml")?.path?.let { path ->
            File(path).listFiles()?.map {
                loadYamlSimulation<T, P>("yaml/${it.name}")
            }?.toSet()
        }.orEmpty()
}

class TestingEnvironmentTest<T, P> : StringSpec({
    "TestingEnvironments should load all the environments" {
        TestingEnvironments.graphqlTestEnvironmnets<T, P>().size shouldNotBe 0
    }
}) where T : Any, P : Position<P>, P : Vector<P>