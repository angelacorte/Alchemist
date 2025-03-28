/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.alignments;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.boundary.gps.GPSTimeAlignment;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.maps.GPSTrace;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 */
public abstract class AbstractGPSTimeAlignment implements GPSTimeAlignment {

    private final SinglePointBehavior policy;

    /**
     * Define the behavior for trace with a single point.
     */
    protected enum SinglePointBehavior {
        /**
         * retain traces with a single point.
         */
        RETAIN_SINGLE_POINTS,
        /**
         * discard traces with a single point.
         */
        DISCARD_SINGLE_POINTS,
        /**
         * throw exception for traces with a single point.
         */
        THROW_EXCEPTION_ON_SINGLE_POINTS
    }

    /**
     * @param policy define policy for traces with a single point
     */
    protected AbstractGPSTimeAlignment(final SinglePointBehavior policy) {
        this.policy = policy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutableList<GPSTrace> alignTime(final List<GPSTrace> traces) {
        Stream<GPSTrace> stream = traces.stream().map(trace -> trace.startAt(computeStartTime(traces, trace)));
        if (policy == SinglePointBehavior.DISCARD_SINGLE_POINTS) {
            stream = stream.filter(trace -> trace.size() > 1);
        }
        if (policy == SinglePointBehavior.THROW_EXCEPTION_ON_SINGLE_POINTS) {
            final ImmutableList<GPSTrace> reified = stream.collect(ImmutableList.toImmutableList());
            final Optional<GPSTrace> single = reified.stream().filter(t -> t.size() <= 1).findAny();
            if (single.isPresent()) {
                throw new IllegalStateException("Time alignment produced a trace with a single point: " + single.get());
            }
            stream = reified.stream();
        }
        return stream.collect(ImmutableList.toImmutableList());
    }

    /**
     * @param allTraces all {@link GPSTrace} to normalize
     * @param currentTrace current {@link GPSTrace} to normalize
     * @return the time from which the trace should begin
     */
    protected abstract Time computeStartTime(List<GPSTrace> allTraces, GPSTrace currentTrace);

}
