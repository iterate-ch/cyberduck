/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;


/**
 * An empty icon
 *
 * @author Brett Smith
 */
public class EmptyIcon implements Icon {
    private int w;
    private int h;

    /**
     * Creates a new EmptyIcon
     *
     * @param w width
     * @param h height
     */
    public EmptyIcon(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
    }

    public int getIconWidth() {
        return w;
    }

    public int getIconHeight() {
        return h;
    }
}
