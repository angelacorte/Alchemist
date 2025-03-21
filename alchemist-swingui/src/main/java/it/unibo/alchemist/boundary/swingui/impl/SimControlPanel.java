/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.impl;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.swingui.tape.impl.AbstractJTapeSection;
import it.unibo.alchemist.boundary.swingui.tape.impl.JTapeFeatureStack;
import it.unibo.alchemist.boundary.swingui.tape.impl.JTapeGroup;
import it.unibo.alchemist.boundary.swingui.tape.impl.JTapeMainFeature;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.core.Status;

import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class maintains multiple control panels for controlling a simulation,
 * ensuring that they are coherently updated.
 *
 * @deprecated The entire Swing UI is deprecated and is set to be replaced with a modern UI
 */
@Deprecated
@SuppressFBWarnings
public final class SimControlPanel extends JTapeGroup {

    @Serial
    private static final long serialVersionUID = 8245609434257107323L;
    private static final Map<Simulation<?, ?>, Set<SimControlPanel>> SIMCONTROLMAP = new MapMaker()
            .weakKeys().makeMap();
    private boolean down;
    private final Map<SimControlCommand, SimControlButton> map = new EnumMap<>(SimControlCommand.class);
    private Simulation<?, ?> simulation;

    /**
     * Builds a new BaseSimControlPanel.
     */
    private SimControlPanel() {
        super(LocalizedResourceBundle.getString("controls"));
        // setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        final AbstractJTapeSection mfplay = new JTapeMainFeature();
        final AbstractJTapeSection s = new JTapeFeatureStack();
        final AbstractJTapeSection mfstop = new JTapeMainFeature();
        for (final SimControlCommand scc : SimControlCommand.values()) {
            final SimControlButton but = scc.createButton();
            map.put(scc, but);
            // add(but);
            if (scc == SimControlCommand.PLAY) {
                mfplay.registerFeature(but);
                // playButt = but;
            } else if (scc == SimControlCommand.STOP) {
                mfstop.registerFeature(but);
            } else {
                s.registerFeature(but);
            }
        }
        registerSection(mfplay);
        registerSection(mfstop);
        registerSection(s);
    }

    private SimControlPanel(final Simulation<?, ?> sim) {
        this();
        setSimulation(sim);
    }

    private static synchronized void checkOldAndRemove() {
        final Set<Simulation<?, ?>> toRemove = new HashSet<>();
        for (final Simulation<?, ?> sim : SIMCONTROLMAP.keySet()) {
            if (sim.getStatus().equals(Status.TERMINATED) || SIMCONTROLMAP.get(sim).isEmpty()) {
                toRemove.add(sim);
            }
        }
        for (final Simulation<?, ?> sim : toRemove) {
            SIMCONTROLMAP.remove(sim);
        }
    }

    /**
     * @param sim the simulation. `null` values allowed.
     * @return a new SimControlPanel
     */
    public static SimControlPanel createControlPanel(final Simulation<?, ?> sim) {
        if (sim == null) {
            return new SimControlPanel();
        }
        return new SimControlPanel(sim);
    }

    private static synchronized Set<SimControlPanel> getSiblings(final SimControlPanel scp) {
        if (scp.simulation != null) {
            final Set<SimControlPanel> result = SIMCONTROLMAP.get(scp.simulation);
            return result == null ? new HashSet<>() : result;
        }
        return Sets.newHashSet(scp);
    }

    private static synchronized void removeAllActionListeners(final SimControlPanel scp) {
        for (final SimControlButton but : scp.map.values()) {
            while (but.getActionListeners().length > 0) {
                but.removeActionListener(but.getActionListeners()[0]);
            }
        }
    }

    /**
     * @param cmd
     *            the command corresponding to the button
     * @param enabled
     *            true if you want the button to be enabled, false otherwise
     */
    public void setButtonEnabled(final SimControlCommand cmd, final boolean enabled) {
        setButtonEnabled(this, cmd, enabled);
    }

    private static synchronized void setButtonEnabled(
        final SimControlPanel pan,
        final SimControlCommand cmd,
        final boolean enabled
    ) {
        for (final SimControlPanel scp : getSiblings(pan)) {
            scp.map.get(cmd).setEnabled(enabled);
        }
    }

    /**
     * @param sim
     *            the simulation to set
     */
    public void setSimulation(final Simulation<?, ?> sim) {
        setSimulation(this, sim);
    }

    private static synchronized void setSimulation(final SimControlPanel scp, final Simulation<?, ?> sim) {
        if (sim != scp.simulation) { // NOPMD: this comparison is intentional
            if (scp.simulation != null) {
                /*
                 * Remove from the previous set
                 */
                getSiblings(scp).remove(scp);
            }
            checkOldAndRemove();
            scp.simulation = sim;
            Set<SimControlPanel> l = SIMCONTROLMAP.get(sim);
            if (l == null) {
                l = new HashSet<>();
                SIMCONTROLMAP.put(sim, l);
            } else {
                /*
                 * Remove all the listeners
                 */
                removeAllActionListeners(scp);
                /*
                 * Clone one of the existing elements. Ensures consistency.
                 */
                if (!l.isEmpty()) {
                    final SimControlPanel clone = l.iterator().next();
                    for (final Entry<SimControlCommand, SimControlButton> entry : clone.map.entrySet()) {
                        final SimControlCommand cmd = entry.getKey();
                        final SimControlButton but = entry.getValue();
                        final SimControlButton dbut = scp.map.get(cmd);
                        setButtonEnabled(scp, cmd, but.isEnabled());
                        for (final ActionListener al : but.getActionListeners()) {
                            dbut.addActionListener(al);
                        }
                    }
                }
            }
            l.add(scp);
        }
    }

    /**
     * To be called when this control panel will be no longer useful.
     */
    public void shutdown() {
        shutdown(this);
    }

    private static synchronized void shutdown(final SimControlPanel scp) {
        if (scp.simulation != null) {
            final Set<SimControlPanel> scset = SIMCONTROLMAP.get(scp.simulation);
            if (scset != null) {
                scset.remove(scp);
            }
        }
        scp.simulation = null;
        scp.down = true;
        removeAllActionListeners(scp);
    }

    private static synchronized void addActionListener(final SimControlPanel cmd, final ActionListener l) {
        for (final SimControlPanel scp : getSiblings(cmd)) {
            for (final SimControlButton b : scp.map.values()) {
                b.addActionListener(l);
            }
        }
    }

    /**
     * See {@link SimControlButton#addActionListener(ActionListener)}.
     *
     * @param l
     *            the {@link ActionListener} to add
     */
    public void addActionListener(final ActionListener l) {
        addActionListener(this, l);
    }

    /**
     * @param <T>
     *            concentrations
     * @return the simulation
     */
    @SuppressWarnings("unchecked")
    public <T> Simulation<T, ?> getSimulation() {
        return (Simulation<T, ?>) simulation;
    }

    /**
     * @return the isDown
     */
    public boolean isDown() {
        return down;
    }

    @Override
    public synchronized void setEnabled(final boolean e) {
        super.setEnabled(e);
        for (final SimControlButton b : map.values()) {
            b.setEnabled(e);
        }
        checkOldAndRemove();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " controlling " + simulation;
    }

}
