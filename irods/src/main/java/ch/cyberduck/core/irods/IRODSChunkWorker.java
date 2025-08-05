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
import org.irods.irods4j.high_level.io.IRODSDataObjectStream.SeekDirection;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IRODSChunkWorker implements Runnable {
    private final Object stream;
    private final RandomAccessFile file;
    private final long offset;
    private final long chunkSize;
    private final byte[] buffer;

    public IRODSChunkWorker(Object stream, String localfilePath, long offset, long chunkSize, int bufferSize) throws IOException, IRODSException {
        this.stream = stream;
        this.file = new RandomAccessFile(localfilePath, "rw");
        this.offset = offset;
        this.chunkSize = chunkSize;
        this.buffer = new byte[bufferSize];


        file.seek(offset);
    }

    @Override
    public void run() {
        try {
            if(stream instanceof IRODSDataObjectInputStream) {
                IRODSDataObjectInputStream in = (IRODSDataObjectInputStream) (stream);
                in.seek((int) offset, SeekDirection.CURRENT);
                long remaining = chunkSize;
                while(remaining > 0) {
                    int readLength = (int) Math.min(buffer.length, remaining);
                    int read = in.read(buffer, 0, readLength);
                    file.write(buffer, 0, read);
                    remaining -= read;
                }

            }
            else if(stream instanceof IRODSDataObjectOutputStream) {
                IRODSDataObjectOutputStream out = (IRODSDataObjectOutputStream) (stream);
                out.seek((int) offset, SeekDirection.CURRENT);
                long remaining = chunkSize;
                while(remaining > 0) {
                    int writeLength = (int) Math.min(buffer.length, remaining);
                    int read = file.read(buffer, 0, writeLength);
                    if(read == -1) {
                        break;
                    }
                    out.write(buffer, 0, read);
                    remaining -= read;
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported stream type");
            }
        }
        catch(IOException | IRODSException e) {
            e.printStackTrace();
        }
        try {
            close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        file.close();
        if(stream instanceof IRODSDataObjectInputStream) {
            IRODSDataObjectInputStream in = (IRODSDataObjectInputStream) (stream);
            in.close();
        }
        else if(stream instanceof IRODSDataObjectOutputStream) {
            IRODSDataObjectOutputStream out = (IRODSDataObjectOutputStream) (stream);
            out.close();
        }
    }
}
