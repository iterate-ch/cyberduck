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

import java.awt.Dimension;

import javax.swing.JSeparator;
import javax.swing.JToolBar;


/**
 * Toolbar separator that shows an etched line. The orientation will change if
 * the toolbar is dragged around a <code>BorderLayout</code>
 *
 * @author Brett Smith
 * @version $Id$
 */
public class ToolBarSeparator
    extends JSeparator {
    /**
     * Constructor for the ToolBarSeparator object
     */
    public ToolBarSeparator() {
        super(JSeparator.VERTICAL);
    }

    /**
     * Gets the maximumSize attribute of the ToolBarSeparator object
     *
     * @return The maximumSize value
     */
    public Dimension getMaximumSize() {
        return (((JToolBar) getParent()).getOrientation()==JToolBar.HORIZONTAL)
               ? new Dimension(4, super.getMaximumSize().height)
               : new Dimension(super.getMaximumSize().width, 4);
    }

    /**
     * Description of the Method
     */
    public void doLayout() {
        setOrientation((((JToolBar) getParent()).getOrientation()==JToolBar.HORIZONTAL)
                       ? JSeparator.VERTICAL : JSeparator.HORIZONTAL);
    }
}
