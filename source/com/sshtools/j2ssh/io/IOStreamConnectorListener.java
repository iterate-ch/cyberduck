/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
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
package com.sshtools.j2ssh.io;

import java.util.EventListener;

/**
 *  <p>This interface should be implemented by objects interested in listening
 *  for data being taken from an <code>InputStream</code> and passed on to
 *  an <code>OutputStream</code> using the <code>IOStreamConnection</code>
 *  </p>
 *  <p><strong>Implementing classes should return from these methods as
 *  quickly as possible, in particular on the dataSent and dataReceived methods
 *  so as to not affect the performance of the forwarding</string></p>
 *
 *@author     <A HREF="mailto:t_magicthize@uses.sourceforge.net">Brett Smith</A>
 *@created    11 January 2003
 *@version    $Id: ForwardingConfiguration.java,v 1.2 2002/12/10 00:07:30
 *      martianx Exp $
 */
public interface IOStreamConnectorListener extends EventListener {
    /**
     *  Implement to be informed when data is sent from the <code>InputStream</code>
     *  to the <code>OutputStream</code>
     *
     * @param data the data sent
     * @param count the number of bytes in <code>data</code> that were sent
     */
    public void data(byte[] data, int count);
}
