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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JPanel;


/**
 * A base panel for displaying images
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class ImagePanel
    extends JPanel {
    ResourceIcon icon;

    /**
     * Constructor for the ImagePanel object
     *
     * @param imageName the path to the resource image
     */
    public ImagePanel(String imageName) {
        icon = new ResourceIcon(imageName);
    }

    /**
     * Constructor for the ImagePanel object
     */
    public ImagePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the preferedSize attribute of the ImagePanel object
     *
     * @return The preferedSize value
     */
    public Dimension getPreferedSize() {
        Insets insets = getInsets();

        return new Dimension(icon.getIconWidth() + insets.left + insets.right,
                             icon.getIconHeight() + insets.top + insets.bottom);
    }

    /**
     * Paints the component
     *
     * @param g The graphics to paint to
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Insets insets = getInsets();

        icon.paintIcon(this, g, insets.left, insets.top);
    }

    /**
     * initiates the panel
     *
     * @exception Exception if an error occurs
     */
    private void jbInit()
                 throws Exception {
        this.setBackground(Color.white);
    }
}
