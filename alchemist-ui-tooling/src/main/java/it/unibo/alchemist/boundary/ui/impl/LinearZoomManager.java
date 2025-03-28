/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.ui.impl;

import it.unibo.alchemist.boundary.ui.api.ZoomManager;

/**
 * A {@link LinearZoomManager} converts the sliding of any physical/virtual
 * device/control into a zoom rate through a linear function.<br>
 * Zoom = number of slides * rate.
 */
public final class LinearZoomManager extends BaseSlideInputManager implements ZoomManager {

    private final double rate;

    /**
     * Same of {@link #LinearZoomManager(double, double, double, double)} but
     * rate is 1, and minimum and maximum are +/- {@link Double#MAX_VALUE}.
     *
     * @param z
     *            is the desired initial zoom
     */
    public LinearZoomManager(final double z) {
        this(z, 1d, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    /**
     * Initialize a new {@link LinearZoomManager} instance with the parameters
     * in input.
     *
     * @param zoom
     *            is the desired initial zoom
     * @param rate
     *            is the linear factor
     * @param min
     *            minimum allowed zoom
     * @param max
     *            maximum allowed zoom
     */
    public LinearZoomManager(final double zoom, final double rate, final double min, final double max) {
        super(zoom / rate, min, max);
        if (rate < Double.MIN_NORMAL) {
            throw new IllegalStateException();
        }
        this.rate = rate;
    }

    @Override
    public double getZoom() {
        return rate * getValue();
    }

    @Override
    public void setZoom(final double z) {
        setValue(z / rate);
    }
}
