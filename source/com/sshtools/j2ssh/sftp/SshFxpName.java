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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshFxpName extends SubsystemMessage implements MessageRequestId {
    /**  */
    public static final int SSH_FXP_NAME = 104;
    private UnsignedInteger32 id;
    private SftpFile[] files;

    /**
     * Creates a new SshFxpName object.
     *
     * @param id
     * @param files
     */
    public SshFxpName(UnsignedInteger32 id, SftpFile[] files) {
        super(SSH_FXP_NAME);
        this.id = id;
        this.files = files;
    }

    /**
     * Creates a new SshFxpName object.
     */
    public SshFxpName() {
        super(SSH_FXP_NAME);
    }

    /**
     * @return
     */
    public UnsignedInteger32 getId() {
        return id;
    }

    /**
     * @return
     */
    public SftpFile[] getFiles() {
        return files;
    }

    /**
     * @param bar
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException
     *                             DOCUMENT
     *                             ME!
     */
    public void constructMessage(ByteArrayReader bar)
            throws java.io.IOException,
            com.sshtools.j2ssh.transport.InvalidMessageException {
        id = bar.readUINT32();

        UnsignedInteger32 count = bar.readUINT32();
        files = new SftpFile[count.intValue()];

        String shortname;
        String longname;

        for (int i = 0; i < files.length; i++) {
            shortname = bar.readString();
            longname = bar.readString();
            files[i] = new SftpFile(shortname, new FileAttributes(bar));
        }
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_FXP_NAME";
    }

    /**
     * @param baw
     * @throws java.io.IOException
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException
     *                             DOCUMENT
     *                             ME!
     */
    public void constructByteArray(ByteArrayWriter baw)
            throws java.io.IOException,
            com.sshtools.j2ssh.transport.InvalidMessageException {
        baw.writeUINT32(id);
        baw.writeUINT32(new UnsignedInteger32(files.length));

        for (int i = 0; i < files.length; i++) {
			baw.writeString(files[i].getAbsolutePath());
            baw.writeString(files[i].getLongname());
            baw.write(files[i].getAttributes().toByteArray());
        }
    }
}
