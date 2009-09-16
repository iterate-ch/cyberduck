package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

/**
 * @author David Kocher, dkocher@cyberduck.ch
 * @version $Id$
 */
public class SCPOutputStream extends BufferedOutputStream {

    private Session session;

    private SCPClient scp;

    public SCPOutputStream(SCPClient client, Session session,
                           final String remoteFile, long length, String mode) throws IOException {
        super(session.getStdin(), 40000);
        this.session = session;
        this.scp = client;

        InputStream is = new BufferedInputStream(session.getStdout(), 512);

        scp.readResponse(is);

        String cline = "C" + mode + " " + length + " " + remoteFile + "\n";

        super.write(cline.getBytes());
        this.flush();

        scp.readResponse(is);
    }

    @Override
    public void close() throws IOException
    {
        try {
            this.write(0);
            this.flush();

            scp.readResponse(session.getStdout());

            this.write("E\n".getBytes());
            this.flush();
        }
        finally {
            if(session != null)
                session.close();
        }
    }
}
