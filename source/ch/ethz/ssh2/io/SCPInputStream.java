package ch.ethz.ssh2.io;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.SCPClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;

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
        super(session.getStdout(), 512);
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

    public void close() throws IOException
    {
        try {
            scp.readResponse(session.getStdout());
            
            session.getStdin().write(0x0);
            session.getStdin().flush();
        }
        finally {
            if(session != null)
                session.close();
        }
    }
}
