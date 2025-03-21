/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.tape.impl;

import javax.swing.JTabbedPane;
import java.io.Serial;

/**
 * JTape is a container for a commands bar that should expose the whole set of
 * features of an application that may concern the user.
 *
 * @deprecated The entire Swing UI is deprecated and is scheduled to be replaced with a modern UI.
 */
@Deprecated
public class JTape extends JTabbedPane {

    @Serial
    private static final long serialVersionUID = -2711040476982254056L;

    /**
     * Adds a tab to the JTape instance.
     *
     * @param tab
     *            is the {@link JTapeTab} to add
     * @return <code>true</code>
     */
    public boolean registerTab(final JTapeTab tab) {
        addTab(tab.getTitle(), tab);
        return true;
    }

    /**
     * Removes a tab.
     *
     * @param tab the tab
     * @return true if the tab was present and got removed
     */
    public boolean deregisterTab(final JTapeTab tab) {
        final int idx = indexOfTab(tab.getTitle());
        if (idx >= 0) {
            removeTabAt(idx);
            return true;
        }
        return false;
    }

}
