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

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Defines the interface for a session data provider. Each session instance
 * requires an input stream and output stream for the session data.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface SessionDataProvider {
    /**
     * Data is received from the session using the Inputstream returned and
     * sent to the remote computer
     *
     * @return the providers InputStream
     */
    public InputStream getInputStream();

    /**
     * Data is recieved from the remote computer and outputed to the session
     * using the outputstream returned.
     *
     * @return the providers OutputStream
     */
    public OutputStream getOutputStream();
}
