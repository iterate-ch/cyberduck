package ch.cyberduck.core.ctera.directio;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;

import java.io.IOException;
import java.io.InputStream;

public class DirectIOInputStream extends ProxyInputStream {

    private InputStream decryptedInputStream;
    private final DecryptData decryptor;
    private final EncryptInfo encryptInfo;


    public DirectIOInputStream(final InputStream proxy, final EncryptInfo encryptInfo) {
        super(proxy);
        this.decryptor = new DecryptData();
        this.encryptInfo = encryptInfo;
        this.decryptedInputStream = null;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        return this.read(b) == IOUtils.EOF ? IOUtils.EOF : b[0];
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] dst, final int off, final int len) throws IOException {
        initStream();
        return decryptedInputStream.read(dst, off, len);
    }

    private void initStream() throws IOException {
        if(decryptedInputStream == null) {
            this.readNextChunk();
        }
    }

    @Override
    public long skip(final long len) throws IOException {
        return IOUtils.skip(this, len);
    }

    private void readNextChunk() throws IOException {
        try {
            decryptedInputStream = decryptor.decryptData(this.in, encryptInfo);
        }
        catch(Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}

