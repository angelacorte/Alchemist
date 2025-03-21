/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import kotlin.reflect.jvm.jvmName

/**
 *
 */
abstract class AbstractNodeProperty<T>(override val node: Node<T>) : NodeProperty<T> {
    override fun toString(): String = with(this::class) { simpleName ?: qualifiedName ?: jvmName }
}
