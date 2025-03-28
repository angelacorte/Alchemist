/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.times;

import it.unibo.alchemist.model.Time;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 */
class TestDoubleTime {

    /**
     *
     */
    @Test
    void testDoubleTime() {
        final Time t = new DoubleTime(1);
        assertNotNull(t);
        assertEquals(new DoubleTime(1), t);
        assertNotEquals(new DoubleTime(), t);
        assertEquals(new DoubleTime(1).hashCode(), t.hashCode());
    }

}
