/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.sftp;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.cyberduck.core.Transcript;
import ch.cyberduck.core.TranscriptFactory;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.subsystem.SubsystemMessageStore;
import com.sshtools.j2ssh.util.OpenClosedState;

class SftpMessageStore extends SubsystemMessageStore {
    /**  */
    public static Log log = LogFactory.getLog(SftpMessageStore.class);

    private Transcript transcript;

    /**
     * Creates a new SftpMessageStore object.
     */
    public SftpMessageStore() {
        this.transcript = TranscriptFactory.getImpl(this.toString()); //@todo get proper log
    }

    /**
     * @param requestId
     * @return
     * @throws InterruptedException
     */
    public synchronized SubsystemMessage getMessage(UnsignedInteger32 requestId)
            throws InterruptedException {
        Iterator it;
        SubsystemMessage msg;

        // If there are no messages available then wait untill there are.
        while (getState().getValue() == OpenClosedState.OPEN) {
            if (messages.size() > 0) {
                it = messages.iterator();

                while (it.hasNext()) {
                    msg = (SubsystemMessage)it.next();

                    if (msg instanceof MessageRequestId) {
                        if (((MessageRequestId)msg).getId().equals(requestId)) {
                            messages.remove(msg);
                            this.transcript.log("< " + msg.getMessageName());
                            return msg;
                        }
                    }
                }
            }

            log.debug("Waiting for new messages");
            wait(5000);
        }

        return null;
    }
}
