/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import org.apache.commons.math3.util.FastMath;

import java.io.Serial;
import java.util.Optional;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble;

/**
 * This condition is valid if a selected biomolecule is present in the neighborhood of the node.
 */
public final class BiomolPresentInNeighbor extends AbstractNeighborCondition<Double> {

    @Serial
    private static final long serialVersionUID = 499903479123400111L;

    private final Biomolecule molecule;
    private final Double concentration;

    /**
     * @param molecule the molecule to check
     * @param concentration the minimum concentration
     * @param node the local node
     * @param environment the environment
     */
    public BiomolPresentInNeighbor(
        final Environment<Double, ?> environment,
        final Node<Double> node,
        final Biomolecule molecule,
        final Double concentration
    ) {
        super(environment, node);
        declareDependencyOn(molecule);
        this.molecule = molecule;
        this.concentration = concentration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isValid() {
        if (getValidNeighbors().isEmpty()) {
            return false;
        } else {
            final Neighborhood<Double> neighborhood = getEnvironment().getNeighborhood(getNode());
            return getValidNeighbors().entrySet().stream()
                .filter(n -> n.getKey().asPropertyOrNull(CellProperty.class) != null)
                .allMatch(n ->
                    neighborhood.contains(n.getKey()) && n.getKey().getConcentration(molecule) >= concentration
                );
        }
    }

    @Override
    public BiomolPresentInNeighbor cloneCondition(final Node<Double> node, final Reaction<Double> reaction) {
        return new BiomolPresentInNeighbor(getEnvironment(), node, molecule, concentration);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected double getNeighborPropensity(final Node<Double> neighbor) {
        // the neighbor is eligible, its propensity is computed using the concentration of the biomolecule
        return Optional.of(neighbor)
            .filter(it -> it.asPropertyOrNull(CellProperty.class) != null)
            .map(it -> it.getConcentration(molecule))
            .filter(it -> it >= concentration)
            .map(it -> binomialCoefficientDouble(it.intValue(), (int) FastMath.ceil(concentration)))
            .orElse(0d);
    }

    @Override
    public String toString() {
        return molecule.toString() + " >= " + concentration + " in neighbor";
    }

}
