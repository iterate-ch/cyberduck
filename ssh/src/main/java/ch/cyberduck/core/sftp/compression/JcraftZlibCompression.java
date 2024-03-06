package ch.cyberduck.core.sftp.compression;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZStream;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SSHRuntimeException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.compression.Compression;

public class JcraftZlibCompression implements Compression {


    /**
     * Named factory for the ZLib Compression.
     */
    public static class Factory
            implements net.schmizz.sshj.common.Factory.Named<Compression> {

        @Override
        public Compression create() {
            return new JcraftZlibCompression();
        }

        @Override
        public String getName() {
            return "zlib";
        }
    }

    private static final int BUF_SIZE = 4096;

    private final byte[] tempBuf = new byte[BUF_SIZE];

    private ZStream stream;

    @Override
    public void init(Mode mode) {
        stream = new ZStream();
        switch(mode) {
            case DEFLATE:
                stream.deflateInit(JZlib.Z_DEFAULT_COMPRESSION);
                break;
            case INFLATE:
                stream.inflateInit();
                break;
            default:
                assert false;
        }
    }

    @Override
    public boolean isDelayed() {
        return false;
    }

    @Override
    public void compress(Buffer buffer) {
        stream.next_in = buffer.array();
        stream.next_in_index = buffer.rpos();
        stream.avail_in = buffer.available();
        buffer.wpos(buffer.rpos());
        do {
            stream.next_out = tempBuf;
            stream.next_out_index = 0;
            stream.avail_out = BUF_SIZE;
            final int status = stream.deflate(JZlib.Z_PARTIAL_FLUSH);
            if(status == JZlib.Z_OK) {
                buffer.putRawBytes(tempBuf, 0, BUF_SIZE - stream.avail_out);
            }
            else {
                throw new SSHRuntimeException("compress: deflate returned " + status);
            }
        }
        while(stream.avail_out == 0);
    }


    @Override
    public void uncompress(Buffer from, Buffer to)
            throws TransportException {
        stream.next_in = from.array();
        stream.next_in_index = from.rpos();
        stream.avail_in = from.available();
        while(true) {
            stream.next_out = tempBuf;
            stream.next_out_index = 0;
            stream.avail_out = BUF_SIZE;
            final int status = stream.inflate(JZlib.Z_PARTIAL_FLUSH);
            switch(status) {
                case JZlib.Z_OK:
                    to.putRawBytes(tempBuf, 0, BUF_SIZE - stream.avail_out);
                    break;
                case JZlib.Z_BUF_ERROR:
                    return;
                default:
                    throw new TransportException(DisconnectReason.COMPRESSION_ERROR, "uncompress: inflate returned "
                            + status);
            }
        }
    }
}
