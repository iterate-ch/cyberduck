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
package com.sshtools.j2ssh.transport.publickey;

/**
 * Defines a public key file format
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface SshPublicKeyFormat
    extends SshKeyFormatConversion {
    /**
     * Provides some additional information about the key
     *
     * @param comment the comment to apply
     */
    public void setComment(String comment);

    /**
     * Gets the additonal information about the key
     *
     * @return the comment
     */
    public String getComment();

    /**
     * Determines if the algorithm is supported
     *
     * @param algorithm The public key algorithm
     *
     * @return <tt>true</tt> if the algorithm is supported otherwise
     *         <tt>false</tt>
     */
    public boolean supportsAlgorithm(String algorithm);
}
