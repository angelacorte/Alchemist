/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction

/**
 * The condition is valid if all the other reactions having at least one condition can not execute.
 * This condition can be used only in a single reaction per node,
 * as multiple instances would lead to undecidable situations.
 */
class NoOtherReactionCanExecute<T>(node: Node<T>, private val myReaction: Reaction<T>) :
    AbstractNonPropensityContributingCondition<T>(node) {
    init {
        require(
            node.reactions
                .asSequence()
                .flatMap { it.conditions }
                .none { it is NoOtherReactionCanExecute<T> },
        ) {
            val className = this::class.simpleName
            "Violation of the $className contract. Only a single $className per node can get built. " +
                "Double creation at node $node, reaction $myReaction"
        }
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: Reaction<T>) =
        NoOtherReactionCanExecute(newNode, myReaction)

    override fun getContext() = Context.LOCAL

    override fun isValid() = node.reactions
        .asSequence()
        .filterNot { it == myReaction }
        .filter { it.conditions.isNotEmpty() }
        .none { it.canExecute() }
}
