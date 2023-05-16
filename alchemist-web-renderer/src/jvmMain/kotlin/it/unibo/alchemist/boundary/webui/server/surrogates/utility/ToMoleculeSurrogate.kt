/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.surrogates.utility

import it.unibo.alchemist.boundary.webui.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.model.Molecule

/**
 * A function that maps a [it.unibo.alchemist.model.Molecule] to its surrogate class
 * [MoleculeSurrogate].
 *
 * @return the [MoleculeSurrogate] mapped starting from the
 * [Molecule].
 */
fun Molecule.toMoleculeSurrogate(): MoleculeSurrogate = MoleculeSurrogate(name)