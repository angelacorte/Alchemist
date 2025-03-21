/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere;

import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;

import java.util.List;
import java.util.Map;

/**
 */
public interface ILsaCondition extends Condition<List<ILsaMolecule>> {

    /**
     * When this method is called, the condition must filter the current matches
     * and allowed nodes.
     *
     * @param matches
     *            current matches. This map may be modified
     * @param validNodes
     *            the list of the valid neighbors. This list may be modified
     * @param retrieved
     *            the list of molecules removed for each node for each
     *            possible binding
     * @return true if the condition is valid, false otherwise
     */
    boolean filter(
        List<Map<HashString, ITreeNode<?>>> matches,
        List<ILsaNode> validNodes,
        List<Map<ILsaNode, List<ILsaMolecule>>> retrieved
    );

    @Override
    ILsaCondition cloneCondition(Node<List<ILsaMolecule>> node, Reaction<List<ILsaMolecule>> reaction);

    @Override
    ILsaNode getNode();

}
