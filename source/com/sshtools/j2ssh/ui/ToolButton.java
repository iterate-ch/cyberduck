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

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;


/**
 * <p>
 * An extension of <code>JButton</code> that looks nicer on the tool bar
 * </p>
 *
 * @author Brett Smith
 * @version $Id$
 *
 * @created 20 December 2002
 */
public class ToolButton
    extends JButton {
    //
    private final static Insets INSETS = new Insets(0, 0, 0, 0);
    private boolean hideText;

    /**
     * Construct a new <code>IconPanel</code> given an icon and a component
     *
     * @param action icon
     */
    public ToolButton(Action action) {
        super(action);
        setMargin(INSETS);
        setRequestFocusEnabled(false);
        setFocusPainted(false);
        setHideText(true);
    }

    /**
     * Determines if the button can retrieve focus
     *
     * @return always returns <tt>false</tt>
     */
    public boolean isFocusable() {
        return false;
    }

    /**
     * Sets the hide text property of the buttin
     *
     * @param hideText <tt>true</tt> if the text is to be hidden otherwies
     *        <tt>false</tt>
     */
    public void setHideText(boolean hideText) {
        if (this.hideText!=hideText) {
            firePropertyChange("hideText", this.hideText, hideText);
        }

        this.hideText = hideText;
        repaint();
    }

    /**
     * Gets the text for the button
     *
     * @return the button text if not hidden otherwise <tt>null</tt>
     */
    public String getText() {
        return hideText ? null : super.getText();
    }
}
