/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.SwingConstants;


/**
 * <p>
 * Useful UI utilities
 * </p>
 *
 * @author Brett Smith
 * @version $Id$
 */
public class UIUtil
    implements SwingConstants {
    /**
     * Utility common in use with GridBagLayout. Adds a component to the
     * specified grid width position
     *
     * @param parent container
     * @param componentToAdd Description of the Parameter
     * @param constraints object
     * @param pos Description of the Parameter
     *
     * @throws IllegalArgumentException if parent is not a JComponent
     */
    public static void jGridBagAdd(JComponent parent,
                                   JComponent componentToAdd,
                                   GridBagConstraints constraints, int pos) {
        if (!(parent.getLayout() instanceof GridBagLayout)) {
            throw new IllegalArgumentException("parent must have a GridBagLayout");
        }

        //
        GridBagLayout layout = (GridBagLayout) parent.getLayout();

        //
        constraints.gridwidth = pos;
        layout.setConstraints(componentToAdd, constraints);
        parent.add(componentToAdd);
    }

    /**
     * <p>
     * Position a component on the screen using one of ..
     * </p>
     *
     * <p>
     * SwingConstants.CENTER<br> SwingConstants.NORTH<br>
     * SwingConstants.SOUTH<br>
     * SwingConstants.EAST<br>
     * SwingConstants.WEST<br>
     * SwingConstants.NORTH_EAST<br>
     * SwingConstants.NORTH_WEST<br>
     * SwingConstants.SOUTH_EAST<br>
     * SwingConstants.SOUTH_WEST<br>
     * </p>
     *
     * @param p position of component
     * @param c component to postion
     */
    public static void positionComponent(int p, Component c) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        switch (p) {
            case NORTH_WEST:
                c.setLocation(0, 0);

                break;

            case NORTH:
                c.setLocation((d.width - c.getSize().width) / 2, 0);

                break;

            case NORTH_EAST:
                c.setLocation((d.width - c.getSize().width), 0);

                break;

            case WEST:
                c.setLocation(0, (d.height - c.getSize().height) / 2);

                break;

            case SOUTH_WEST:
                c.setLocation(0, (d.height - c.getSize().height));

                break;

            case EAST:
                c.setLocation(d.width - c.getSize().width,
                              (d.height - c.getSize().height) / 2);

                break;

            case SOUTH_EAST:
                c.setLocation((d.width - c.getSize().width),
                              (d.height - c.getSize().height) - 30);

                break;

            case CENTER:
                c.setLocation((d.width - c.getSize().width) / 2,
                              (d.height - c.getSize().height) / 2);

                break;
        }
    }
}
