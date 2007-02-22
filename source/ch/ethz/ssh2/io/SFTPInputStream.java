package ch.ethz.ssh2.io;

import ch.ethz.ssh2.SFTPv3FileHandle;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Kocher, dkocher@cyberduck.ch
 * @version $Id$
 */
public class SFTPInputStream extends InputStream
{

    private SFTPv3FileHandle handle;

    /**
     * Offset (in bytes) in the file to read
     */
    private int readOffset = 0;

    public SFTPInputStream(SFTPv3FileHandle handle)
    {
        if(null == handle)
            throw new IllegalArgumentException("Cannot accept null argument!");
        if(null == handle.getClient())
            throw new IllegalArgumentException("Cannot accept null client!");
        this.handle = handle;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * @see ch.ethz.ssh2.SFTPv3Client#read(SFTPv3FileHandle,long,byte[],int,int)
     */
    public int read(byte[] buffer, int offset, int len)
            throws IOException
    {
        int read = handle.getClient().download(handle,
                readOffset, buffer, offset, len);
        if(read > 0)
        {
            readOffset += read;
        }
        return read;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     * <p/>
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read()
            throws IOException
    {
        byte[] buffer = new byte[1];
        int read = handle.getClient().download(handle,
                readOffset, buffer, 0, 1);
        if(read > 0)
        {
            readOffset += read;
        }
        return read;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    public long skip(long n)
    {
        readOffset += n;
        return n;
    }

    public void close()
            throws IOException
    {
        handle.getClient().closeFile(handle);
    }
}