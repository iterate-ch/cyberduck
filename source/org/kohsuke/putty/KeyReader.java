package org.kohsuke.putty;

import ch.ethz.ssh2.crypto.PEMDecryptException;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Parses the putty key bit vector, which is an encoded sequence
 * of {@link BigInteger}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class KeyReader {
    private final DataInput di;

    public KeyReader(byte[] key) {
        this.di = new DataInputStream(new ByteArrayInputStream(key));
    }

    /**
     * Skips an integer without reading it.
     */
    public void skip() {
        try {
            di.skipBytes(di.readInt());
        }
        catch(IOException e) {
            throw new AssertionError(e);
        }
    }

    private byte[] read() throws PEMDecryptException {
        try {
            int len = di.readInt();
            if(len <= 0 || len > 512) {
                throw new PEMDecryptException();
            }
            byte[] r = new byte[len];
            di.readFully(r);
            return r;
        }
        catch(IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Reads the next integer.
     */
    public BigInteger readInt() throws PEMDecryptException {
        return new BigInteger(read());
    }
}
