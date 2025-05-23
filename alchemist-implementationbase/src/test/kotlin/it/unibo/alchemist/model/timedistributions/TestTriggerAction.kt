/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractLocalAction

class TestTriggerAction<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    node: Node<T>,
) : AbstractLocalAction<T>(node) {
    private var executed = false

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): Action<T> = TestTriggerAction(environment, node)

    override fun execute() {
        when (executed) {
            true -> error("Reaction already executed")
            false -> executed = true
        }
    }
}
