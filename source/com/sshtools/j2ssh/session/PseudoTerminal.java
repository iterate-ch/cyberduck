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
package com.sshtools.j2ssh.session;

/**
 * Describes the basic display features of a pseudo terminal for the
 * SessionChannel
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface PseudoTerminal {
    /**
     * Get the number of columns for the terminal
     *
     * @return the number of columns
     */
    public int getColumns();

    /**
     * Get the terminal modes
     *
     * @return a string of encoded terminal modes
     */
    public String getEncodedTerminalModes();

    /**
     * Get the height in pixels of the terminal
     *
     * @return the height of the terminal
     */
    public int getHeight();

    /**
     * Get the rows for the terminal
     *
     * @return the number of rows
     */
    public int getRows();

    /**
     * Get the Terminal type (i.e VT100)
     *
     * @return the terminal type
     */
    public String getTerm();

    /**
     * Get the width in pixels of the terminal
     *
     * @return the width of the terminal
     */
    public int getWidth();
}
