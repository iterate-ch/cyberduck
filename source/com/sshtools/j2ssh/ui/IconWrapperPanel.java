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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A panel that takes an icon and another component. The icon is aligned to the
 * left of the component
 *
 * @author Brett Smith
 * @version $Id$
 */
public class IconWrapperPanel
    extends JPanel {
    /**
     * Construct a new <code>IconPanel</code> given an icon and a component
     *
     * @param icon icon
     * @param component component
     */
    public IconWrapperPanel(Icon icon, JComponent component) {
        super(new BorderLayout());

        //  Create the west panel with the icon in it
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        westPanel.add(new JLabel(icon), BorderLayout.NORTH);

        //  Build this panel
        add(westPanel, BorderLayout.WEST);
        add(component, BorderLayout.CENTER);
    }
}
