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
package com.sshtools.j2ssh.agent;

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;


/**
 * @author $author$
 * @version $Revision$
 */
public class KeyConstraints {
    /**  */
    public static final long NO_TIMEOUT = 0;

    /**  */
    public static final long NO_LIMIT = 0xffffffffL;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_TIMEOUT = 50;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_USE_LIMIT = 51;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_FORWARDING_STEPS = 52;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_FORWARDING_PATH = 100;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_SSH1_COMPAT = 150;

    /**  */
    protected static final int SSH_AGENT_CONSTRAINT_NEED_USER_VERIFICATION = 151;
    private UnsignedInteger32 timeout = new UnsignedInteger32(NO_TIMEOUT);
    private UnsignedInteger32 uselimit = new UnsignedInteger32(NO_LIMIT);
    private UnsignedInteger32 maxsteps = new UnsignedInteger32(NO_LIMIT);
    private String forwardingpath = "";
    private boolean userverify = false;
    private boolean compat = false;
    private long keyadded = System.currentTimeMillis();
    private long usedcount = 0;

    /**
     * Creates a new KeyConstraints object.
     */
    public KeyConstraints() {
    }

    /**
     * Creates a new KeyConstraints object.
     *
     * @param bar
     * @throws IOException
     */
    public KeyConstraints(ByteArrayReader bar) throws IOException {
        while (bar.available() > 0) {
            switch (bar.read() & 0xFF) {
                case SSH_AGENT_CONSTRAINT_TIMEOUT:
                    timeout = bar.readUINT32();

                    break;

                case SSH_AGENT_CONSTRAINT_USE_LIMIT:
                    uselimit = bar.readUINT32();

                    break;

                case SSH_AGENT_CONSTRAINT_FORWARDING_STEPS:
                    maxsteps = bar.readUINT32();

                    break;

                case SSH_AGENT_CONSTRAINT_FORWARDING_PATH:
                    forwardingpath = bar.readString();

                    break;

                case SSH_AGENT_CONSTRAINT_SSH1_COMPAT:
                    compat = (bar.read() != 0);

                    break;

                case SSH_AGENT_CONSTRAINT_NEED_USER_VERIFICATION:
                    userverify = (bar.read() != 0);

                    break;
            }
        }
    }

    /**
     * @param timeout
     */
    public void setKeyTimeout(UnsignedInteger32 timeout) {
        this.timeout = timeout;
    }

    /**
     * @param uselimit
     */
    public void setKeyUseLimit(int uselimit) {
        this.uselimit = new UnsignedInteger32(uselimit);
    }

    /**
     * @param maxsteps
     */
    public void setMaximumForwardingSteps(int maxsteps) {
        this.maxsteps = new UnsignedInteger32(maxsteps);
    }

    /**
     * @param forwardingpath
     */
    public void setForwardingPath(String forwardingpath) {
        this.forwardingpath = forwardingpath;
    }

    /**
     * @param userverify
     */
    public void setRequiresUserVerification(boolean userverify) {
        this.userverify = userverify;
    }

    /**
     * @param compat
     */
    public void setSSH1Compatible(boolean compat) {
        this.compat = compat;
    }

    /**
     * @return
     */
    public long getKeyTimeout() {
        return timeout.longValue();
    }

    /**
     * @return
     */
    public long getKeyUseLimit() {
        return uselimit.longValue();
    }

    /**
     * @return
     */
    public long getMaximumForwardingSteps() {
        return maxsteps.longValue();
    }

    /**
     * @return
     */
    public long getUsedCount() {
        return usedcount;
    }

    /**
     * @return
     */
    public boolean hasTimedOut() {
        return (timeout.longValue() != 0)
                ? (((System.currentTimeMillis() - keyadded) / 1000) > timeout.longValue())
                : false;
    }

    /**
     * @return
     */
    public boolean canUse() {
        return (uselimit.longValue() != 0) ? (usedcount < uselimit.longValue())
                : true;
    }

    /**
     *
     */
    public void use() {
        usedcount++;
    }

    /**
     * @return
     */
    public String getForwardingPath() {
        return forwardingpath;
    }

    /**
     * @return
     */
    public boolean requiresUserVerification() {
        return userverify;
    }

    /**
     * @return
     */
    public boolean isSSH1Compatible() {
        return compat;
    }

    /**
     * @return
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayWriter baw = new ByteArrayWriter();
        baw.write(SSH_AGENT_CONSTRAINT_TIMEOUT);
        baw.writeUINT32(timeout);
        baw.write(SSH_AGENT_CONSTRAINT_USE_LIMIT);
        baw.writeUINT32(uselimit);
        baw.write(SSH_AGENT_CONSTRAINT_FORWARDING_STEPS);
        baw.writeUINT32(maxsteps);
        baw.write(SSH_AGENT_CONSTRAINT_FORWARDING_PATH);
        baw.writeString(forwardingpath);
        baw.write(SSH_AGENT_CONSTRAINT_SSH1_COMPAT);
        baw.write(compat ? 0 : 1);
        baw.write(SSH_AGENT_CONSTRAINT_NEED_USER_VERIFICATION);
        baw.write(userverify ? 0 : 1);

        return baw.toByteArray();
    }
}
