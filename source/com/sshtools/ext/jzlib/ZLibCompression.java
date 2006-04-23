package com.sshtools.ext.jzlib;

import com.sshtools.j2ssh.transport.compression.SshCompression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ZLibCompression implements SshCompression {

    static private final int BUF_SIZE = 65535;

    ByteArrayOutputStream compressStream;
    ByteArrayOutputStream uncompressStream;
    private ZStream zstream;
    private byte tmpbuf[];

    public ZLibCompression() {
        compressStream = new ByteArrayOutputStream(BUF_SIZE);
        uncompressStream = new ByteArrayOutputStream(BUF_SIZE);
        tmpbuf = new byte[BUF_SIZE];
        zstream = new ZStream();
    }

    public void init(int type, int level) {
        if (type == SshCompression.DEFLATER) {
            zstream.deflateInit(level);
        }
        if (type == SshCompression.INFLATER) {
//            inflated_buf = new byte[BUF_SIZE];
            zstream.inflateInit();
        }
    }

    public byte[] compress(byte buf[], int start, int length)
        throws IOException
	{
        compressStream.reset();
        zstream.next_in = tmpbuf;
        zstream.next_in_index = start;
        zstream.avail_in = length - start;
        do {
            zstream.next_out = tmpbuf;
            zstream.next_out_index = 0;
            zstream.avail_out = BUF_SIZE;
            int status = zstream.deflate(JZlib.Z_PARTIAL_FLUSH);
            switch(status) {
				case JZlib.Z_OK:
					compressStream.write(tmpbuf, zstream.next_out_index, BUF_SIZE - zstream.avail_out);
					break;
				default:
					throw new IOException("compress: deflate returned " + status);
            }
        } while(zstream.avail_out == 0);

        return compressStream.toByteArray();
//        zstream.next_in = buf;
//        zstream.next_in_index = start;
//        zstream.avail_in = length - start;
//        int status;
//        int outputlen = start;
//
//        do {
//            zstream.next_out = tmpbuf;
//            zstream.next_out_index = 0;
//            zstream.avail_out = BUF_SIZE;
//            status = zstream.deflate(JZlib.Z_PARTIAL_FLUSH);
//            switch (status) {
//                case JZlib.Z_OK:
//                    // Resize the buffer and re-allocate
//                    byte[] foo = new byte[start + outputlen + BUF_SIZE - zstream.avail_out];
//                    if (start + outputlen > 0) {
//                        System.arraycopy(buf, 0, foo, start + outputlen,
//                                BUF_SIZE - zstream.avail_out);
//                    }
//                    buf = foo;
//                    System.arraycopy(tmpbuf, 0,
//                            buf, outputlen,
//                            BUF_SIZE - zstream.avail_out);
//                    outputlen += (BUF_SIZE - zstream.avail_out);
//                    break;
//                default:
//                    throw new IOException("Compress: deflate returned " + status);
//            }
//        }
//        while (zstream.avail_out == 0);
//
//        return buf;
    }

//    private byte[] inflated_buf;

    public byte[] uncompress(byte buf[], int start, int length)
        throws IOException
    {
        uncompressStream.reset();
        zstream.next_in = buf;
        zstream.next_in_index = start;
        zstream.avail_in = length;
        do {
            zstream.next_out = tmpbuf;
            zstream.next_out_index = 0;
            zstream.avail_out = BUF_SIZE;
            int status = zstream.inflate(JZlib.Z_PARTIAL_FLUSH);
            switch(status) {
				case JZlib.Z_OK:
					uncompressStream.write(tmpbuf, zstream.next_out_index, zstream.avail_out);
					break;
				case JZlib.Z_BUF_ERROR:
					return uncompressStream.toByteArray();
				default:
					throw new IOException("uncompress: inflate returned " + status);
            }
        } while(true);
//        int inflated_end = 0;
//
//        zstream.next_in = tmpbuf;
//        zstream.next_in_index = start;
//        zstream.avail_in = length;
//
//        while (true) {
//            zstream.next_out = tmpbuf;
//            zstream.next_out_index = 0;
//            zstream.avail_out = BUF_SIZE;
//            int status = zstream.inflate(JZlib.Z_PARTIAL_FLUSH);
//            switch (status) {
//                case JZlib.Z_OK:
//                    if (inflated_buf.length < inflated_end + BUF_SIZE - zstream.avail_out) {
//                        byte[] foo = new byte[inflated_end + BUF_SIZE - zstream.avail_out];
//                        System.arraycopy(inflated_buf, 0, foo, 0, inflated_end);
//                        inflated_buf = foo;
//                    }
//                    System.arraycopy(tmpbuf, 0,
//                            inflated_buf, inflated_end,
//                            BUF_SIZE - zstream.avail_out);
//                    inflated_end += (BUF_SIZE - zstream.avail_out);
//                    length = inflated_end;
//                    break;
//                case JZlib.Z_BUF_ERROR:
//                    if (inflated_end > buf.length - start) {
//                        byte[] foo = new byte[inflated_end + start];
//                        System.arraycopy(buf, 0, foo, 0, start);
//                        System.arraycopy(inflated_buf, 0, foo, start, inflated_end);
//                        buf = foo;
//                    }
//                    else {
//                        System.arraycopy(inflated_buf, 0, buf, start, inflated_end);
//                    }
//                    length = inflated_end;
//                    return buf;
//                default:
//                    throw new IOException("Uncompress: inflate returned " + status);
//            }
//        }
    }
}