/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.util.BugReporting
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets
import org.jgrapht.graph.DefaultDirectedGraph

private typealias Edge<T> = Pair<Actionable<T>, Actionable<T>>

/**
 * This class offers an implementation of a dependency graph, namely a
 * data structure which can address in an efficient way the problem of
 * finding those reactions affected by the execution of another
 * reaction. This class relies heavily on the ReactionHandler
 * interface.
 *
 * @param <T> concentration type
 */
class JGraphTDependencyGraph<T>(private val environment: Environment<T, *>) : DependencyGraph<T> {
    private val inGlobals = ArrayListSet<Actionable<T>>()
    private val outGlobals = ArrayListSet<Actionable<T>>()
    private val graph: DefaultDirectedGraph<Actionable<T>, Edge<T>> = DefaultDirectedGraph(null, null, false)
    private val runtimeRemovalCache = mutableSetOf<Actionable<T>>()

    override fun createDependencies(newReaction: Actionable<T>) {
        val allReactions = graph.vertexSet()
        val neighborhood by lazy {
            if (newReaction is Reaction) {
                newReaction.node.neighborhood
            } else {
                ListSets.emptyListSet()
            }
        }
        val localReactions by lazy {
            if (newReaction is Reaction) {
                newReaction.node.reactions
                    .filter { allReactions.contains(it) }
                    .asSequence()
            } else {
                emptySequence()
            }
        }
        val neighborhoodReactions by lazy {
            neighborhood
                .asSequence()
                .flatMap { it.reactions.asSequence() }
                .filter { allReactions.contains(it) }
                .toList()
                .asSequence()
        }
        val extendedNeighborhoodReactions by lazy {
            neighborhood
                .asSequence()
                // Neighbors of neighbors
                .flatMap { it.neighborhood.asSequence() }
                // No duplicates
                .distinct()
                // Exclude self and direct neighbors
                .filterNot { it == newReaction.node || it in neighborhood }
                .flatMap { it.reactions.asSequence() }
                .filter { allReactions.contains(it) }
                .toList()
                .asSequence()
        }

        fun Context.candidates(
            oppositeGlobal: Sequence<Actionable<T>>,
            oppositeContext: Actionable<T>.() -> Context,
        ): Sequence<Actionable<T>> = when (this) {
            Context.LOCAL ->
                oppositeGlobal +
                    localReactions +
                    neighborhoodReactions.filter { it.oppositeContext() == Context.NEIGHBORHOOD }
            Context.NEIGHBORHOOD ->
                oppositeGlobal +
                    localReactions +
                    neighborhoodReactions +
                    extendedNeighborhoodReactions.filter { it.oppositeContext() == Context.NEIGHBORHOOD }
            Context.GLOBAL ->
                allReactions.asSequence()
        }
        val inboundCandidates: Sequence<Actionable<T>> =
            newReaction.inputContext.candidates(outGlobals.asSequence()) { outputContext }
        val outboundCandidates: Sequence<Actionable<T>> =
            newReaction.outputContext.candidates(inGlobals.asSequence()) { inputContext }
        check(graph.addVertex(newReaction)) {
            "$newReaction was already in the dependency graph"
        }
        inboundCandidates
            .filter { newReaction.dependsOn(it) }
            .forEach { graph.addEdge(it, newReaction, Edge(it, newReaction)) }
        outboundCandidates
            .filter { it.dependsOn(newReaction) }
            .forEach { graph.addEdge(newReaction, it, Edge(newReaction, it)) }
        if (newReaction.inputContext == Context.GLOBAL) {
            inGlobals.add(newReaction)
        }
        if (newReaction.outputContext == Context.GLOBAL) {
            outGlobals.add(newReaction)
        }
    }

    override fun removeDependencies(reaction: Actionable<T>) {
        fun bugInfo() = mapOf(
            "reaction" to reaction,
            "graph" to graph,
            "incarnation" to environment.incarnation,
            "environment" to environment,
        )

        fun bug(message: String): Nothing = BugReporting.reportBug(message, bugInfo())
        if (!graph.removeVertex(reaction)) {
            bug("Reaction does not exists in the dependency graph.")
        }
        if (reaction.inputContext == Context.GLOBAL && !inGlobals.remove(reaction)) {
            bug("Inconsistent state: $reaction, with global input context, was not in the appropriate pool.")
        }
        if (reaction.outputContext == Context.GLOBAL && !outGlobals.remove(reaction)) {
            bug("Inconsistent state: $reaction, with global input context, was not in the appropriate pool.")
        }
        runtimeRemovalCache += reaction
    }

    private fun addNeighborDirected(n1: Node<T>, n2: Node<T>) {
        val n2NonGlobalReactions: Iterable<Reaction<T>> by lazy {
            n2.reactions.filterNot { it.outputContext == Context.GLOBAL }
        }
        val n2NeighborhoodReactions: Iterable<Reaction<T>> by lazy {
            n2NonGlobalReactions.filter { it.outputContext == Context.NEIGHBORHOOD }
        }
        val neighborInputInfluencers: Iterable<Reaction<T>> by lazy {
            // All the non-global reactions of the new neighbor
            n2NonGlobalReactions +
                // Plus all the reactions of the new neighbor's neighbors with neighborhood output
                (n2.neighborhood - setOf(n1) - n1.neighborhood)
                    .asSequence()
                    .flatMap { it.reactions.asSequence() }
                    .filter { it.outputContext == Context.NEIGHBORHOOD }
        }
        n1.reactions.forEach { reaction ->
            when (reaction.inputContext) {
                // Local-reading reactions can be only influenced by the new neighbor's neighborhood reactions
                Context.LOCAL -> n2NeighborhoodReactions
                Context.NEIGHBORHOOD -> neighborInputInfluencers
                else -> emptyList()
            }.asSequence()
                .filter { reaction.dependsOn(it) }
                .forEach { graph.addEdge(it, reaction, Edge(it, reaction)) }
        }
    }

    /** @see [DependencyGraph.addNeighbor] */
    override fun addNeighbor(n1: Node<T>, n2: Node<T>) {
        addNeighborDirected(n1, n2)
        addNeighborDirected(n2, n1)
    }

    /**
     * Remove edges linking reactions in [n1] that could have influenced reactions in [n2].
     * - reactions of n1 with output local may have influenced those with input neighborhood in [n2]
     * - reactions of n1 with output neighborhood may have influenced those with input local or neighborhood in [n2]
     *   plus those with input neighborhood in the n2's neighborhood that is no longer part of [n1] neighborhood
     * - reactions with global output are unmodified
     */
    private fun removeNeighborDirected(n1: Node<T>, n2: Node<T>) {
        val n2NonGlobalReactions by lazy { n2.reactions.filterNot { it.inputContext == Context.GLOBAL } }
        val n2NeighborhoodReactions by lazy { n2NonGlobalReactions.filter { it.inputContext == Context.NEIGHBORHOOD } }
        val neighborOutputInfluencers by lazy {
            // All the non-global reactions of the old neighbor
            n2NonGlobalReactions +
                // Plus all the reactions of the new neighbor's neighbors with neighborhood output
                (n2.neighborhood - setOf(n1) - n1.neighborhood - n1.neighborhood.flatMap { it.neighborhood }.toSet())
                    .asSequence()
                    .flatMap { it.reactions.asSequence() }
                    .filter { it.inputContext == Context.NEIGHBORHOOD }
                    .toList()
        }
        n1.reactions.forEach { reaction ->
            when (reaction.outputContext) {
                // Local-reading reactions may have been influenced only by the ex neighbor neigh-writing reactions
                Context.LOCAL -> n2NeighborhoodReactions
                Context.NEIGHBORHOOD -> neighborOutputInfluencers
                else -> emptyList()
            }.asSequence()
                .filter { reaction.dependsOn(it) }
                .forEach { graph.removeEdge(it, reaction) }
        }
    }

    override fun removeNeighbor(n1: Node<T>, n2: Node<T>) {
        removeNeighborDirected(n1, n2)
        removeNeighborDirected(n2, n1)
    }

    override fun outboundDependencies(reaction: Actionable<T>?): ListSet<Actionable<T>> {
        if (graph.containsVertex(reaction)) {
            return graph.outgoingEdgesOf(reaction).let { edges ->
                edges.mapTo(ArrayListSet(edges.size)) { it.second }
            }
        }
        require(runtimeRemovalCache.remove(reaction)) {
            BugReporting.reportBug(
                "A reaction that is being updated does not exists in the dependency graph, " +
                    "nor has been scheduled for removal.",
                mapOf(
                    "graph" to graph,
                    "incarnation" to environment.incarnation,
                    "environment" to environment,
                    "reaction" to reaction,
                ),
            )
        }
        return ListSets.emptyListSet()
    }

    override fun toString() = graph.toString()

    override fun globalInputContextReactions(): ListSet<Actionable<T>> = ListSets.unmodifiableListSet(inGlobals)

    private val Actionable<T>.node: Node<T> get() = checkNotNull(this as? Reaction).node

    private fun Actionable<T>.dependsOn(other: Actionable<T>) = inboundDependencies.any { inbound ->
        other.outboundDependencies.any { outbound ->
            inbound.dependsOn(outbound) || outbound.makesDependent(inbound)
        }
    }

    private val Node<T>.neighborhood get() = environment.getNeighborhood(this).neighbors

    private companion object {
        private val Actionable<*>.inputContext get() =
            when (this) {
                is Reaction -> inputContext
                else -> Context.GLOBAL
            }

        private val Actionable<*>.outputContext get() =
            when (this) {
                is Reaction -> outputContext
                else -> Context.GLOBAL
            }
    }
}
