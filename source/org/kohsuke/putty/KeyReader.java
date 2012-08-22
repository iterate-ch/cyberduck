package org.kohsuke.putty;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import ch.ethz.ssh2.crypto.PEMDecryptException;

/**
 * Parses the putty key bit vector, which is an encoded sequence
 * of {@link BigInteger}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class KeyReader {
    private final DataInput di;

    public KeyReader(byte[] key) {
        this.di = new DataInputStream(new ByteArrayInputStream(Arrays.copyOf(key, key.length)));
    }

    /**
     * Skips an integer without reading it.
     */
    public void skip() throws IOException {
        final int n = di.readInt();
        final int skipped = di.skipBytes(n);
        if(skipped != n) {
            throw new IOException(String.format("Skipped %d bytes instead of %d", skipped, n));
        }
    }

    private byte[] read() throws PEMDecryptException {
        try {
            int len = di.readInt();
            if(len <= 0 || len > 512) {
                throw new PEMDecryptException("Invalid length " + len);
            }
            byte[] r = new byte[len];
            di.readFully(r);
            return r;
        }
        catch(IOException e) {
            throw new AssertionError(e);
        }
    }

    public BigInteger readInt() throws PEMDecryptException {
        return new BigInteger(read());
    }
}