package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IRODSChunkWorker implements Runnable {

    private final InputStream in;
    private final OutputStream out;
    private final long offset;
    private final long chunkSize;
    private final byte[] buffer;

    public IRODSChunkWorker(InputStream in, OutputStream out, long offset, long chunkSize, int bufferSize) {
        this.in = in;
        this.out = out;
        this.offset = offset;
        this.chunkSize = chunkSize;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public void run() {
        try {
            seek(in);
            seek(out);

            long remaining = chunkSize;
            while(remaining > 0) {
                int count = (int) Math.min(buffer.length, remaining);

                int bytesRead = in.read(buffer, 0, count);
                if(-1 == bytesRead) {
                    break;
                }

                out.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        catch(IOException | IRODSException e) {
            // TODO Log error
        }
    }

    private void seek(InputStream in) throws IRODSException, IOException {
        if(in instanceof IRODSDataObjectInputStream) {
            IRODSDataObjectInputStream stream = (IRODSDataObjectInputStream) in;
            long totalOffset = offset;
            while(totalOffset > 0) {
                if(totalOffset >= Integer.MAX_VALUE) {
                    totalOffset -= Integer.MAX_VALUE;
                    stream.seek(Integer.MAX_VALUE, IRODSDataObjectStream.SeekDirection.CURRENT);
                }
                else {
                    stream.seek((int) totalOffset, IRODSDataObjectStream.SeekDirection.CURRENT);
                }
            }
        }
        else if(in instanceof FileInputStream) {
            ((FileInputStream) in).getChannel().position(offset);
        }
    }

    private void seek(OutputStream out) throws IRODSException, IOException {
        if(out instanceof IRODSDataObjectOutputStream) {
            IRODSDataObjectOutputStream stream = (IRODSDataObjectOutputStream) out;
            long totalOffset = offset;
            while(totalOffset > 0) {
                if(totalOffset >= Integer.MAX_VALUE) {
                    totalOffset -= Integer.MAX_VALUE;
                    stream.seek(Integer.MAX_VALUE, IRODSDataObjectStream.SeekDirection.CURRENT);
                }
                else {
                    stream.seek((int) totalOffset, IRODSDataObjectStream.SeekDirection.CURRENT);
                }
            }
        }
        else if(out instanceof FileOutputStream) {
            ((FileOutputStream) out).getChannel().position(offset);
        }
    }
}
