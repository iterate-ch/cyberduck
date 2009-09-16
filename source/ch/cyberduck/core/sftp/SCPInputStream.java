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
import java.io.OutputStream;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

/**
 * @author David Kocher, dkocher@cyberduck.ch
 * @version $Id$
 */
public class SCPInputStream extends BufferedInputStream
{

    private Session session;

    private SCPClient scp;

    /**
     * Bytes remaining to be read from the stream
     */
    private long remaining;

    public SCPInputStream(SCPClient client, Session session) throws IOException
    {
        super(session.getStdout(), 40000);

        this.scp = client;
        this.session = session;

        OutputStream os = new BufferedOutputStream(session.getStdin(), 512);

        os.write(0x0);
        os.flush();

        final SCPClient.LenNamePair lnp;

        while (true)
        {
            int c = session.getStdout().read();
            if (c < 0)
                throw new IOException("Remote scp terminated unexpectedly.");

            String line = client.receiveLine(session.getStdout());

            if (c == 'T')
            {
                /* Ignore modification times */
                continue;
            }

            if ((c == 1) || (c == 2))
                throw new IOException("Remote SCP error: " + line);

            if (c == 'C')
            {
                lnp = client.parseCLine(line);
                break;

            }
            throw new IOException("Remote SCP error: " + ((char) c) + line);
        }

        os.write(0x0);
        os.flush();

        this.remaining = lnp.length;
    }

    @Override
    public int read() throws IOException
    {
        if(!(remaining > 0)) {
            return -1;
        }

        int read = super.read();
        if (read < 0)
            throw new IOException("Remote scp terminated connection unexpectedly");

        remaining -= read;

        return read;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
        if(!(remaining > 0)) {
            return -1;
        }

        int trans = (int) remaining;
        if (remaining > len)
            trans = len;

        int read = super.read(b, off, trans);
        if (read < 0)
            throw new IOException("Remote scp terminated connection unexpectedly");

        remaining -= read;

        return read;
    }

    @Override
    public void close() throws IOException
    {
        try {
//            scp.readResponse(session.getStdout());
            
            session.getStdin().write(0x0);
            session.getStdin().flush();
        }
        finally {
            if(session != null)
                session.close();
        }
    }
}
