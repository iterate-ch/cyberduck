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
package com.sshtools.j2ssh.forwarding;

import java.util.EventListener;

/**
 *  <p>This interface should be implemented by objects interested in
 *  port forwarding events such as when a forwarded connection is opened and
 *  closed.
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
public interface ForwardingConfigurationListener extends EventListener {
    /**
     *  Implement to be informed when a forwarded connection is opened
     *
     * @param channel the channel that was opened
     */
    public void opened(ForwardingChannel channel);
    /**
     *  Implement to be informed when a forwarded connection is closed
     *
     * @param channel the channel that was opened
     */
    public void closed(ForwardingChannel channel);
    /**
     *  Implement to be informed when data is received on a forwarded connection
     *
     * @param channel the channel where the data was received
     * @param bytes number of bytes received
     */
    public void dataReceived(ForwardingChannel channel, int bytes);
    /**
     *  Implement to be informed when data is sent on a forwarded connection
     *
     * @param channel the channel where the data was sent
     * @param bytes number of bytes sent
     */
    public void dataSent(ForwardingChannel channel, int bytes);
}
