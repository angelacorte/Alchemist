/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positionfilters

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Check if only one between [positionBasedFilterA] and [positionBasedFilterB] is satisfied.
 * @param positionBasedFilterA the first filter.
 * @param positionBasedFilterB the second filter.
 */
data class Xor<P : Position<P>>(
    val positionBasedFilterA: PositionBasedFilter<P>,
    val positionBasedFilterB: PositionBasedFilter<P>,
) : PositionBasedFilter<P> {
    /**
     * Returns true if only one [positionBasedFilterA] and [positionBasedFilterB] is satisfied.
     */
    override operator fun contains(position: P) = position in positionBasedFilterA != position in positionBasedFilterB
}
