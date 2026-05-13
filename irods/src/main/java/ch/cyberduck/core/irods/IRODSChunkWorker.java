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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class IRODSChunkWorker implements Callable<Boolean> {

    private static final Logger log = LogManager.getLogger(IRODSChunkWorker.class);

    private final TransferStatus status;
    private final StreamListener streamListener;
    private final InputStream in;
    private final OutputStream out;
    private final long offset;
    private final long chunkSize;
    private final byte[] buffer;

    public IRODSChunkWorker(TransferStatus status, StreamListener streamListener, InputStream in, OutputStream out, long offset, long chunkSize, int bufferSize) {
        log.info("constructing iRODS chunk worker.");
        log.info("offset      = [{}]", offset);
        log.info("chunk size  = [{}]", chunkSize);
        log.info("buffer size = [{}]", bufferSize);
        this.status = status;
        this.streamListener = streamListener;
        this.in = in;
        this.out = out;
        this.offset = offset;
        this.chunkSize = chunkSize;
        this.buffer = new byte[bufferSize];
        log.info("iRODS chunk worker constructed.");
    }

    @Override
    public Boolean call() {
        try {
            IRODSStreamUtils.seek(in, offset);
            IRODSStreamUtils.seek(out, offset);

            long remaining = chunkSize;
            while(remaining > 0) {
                try {
                    status.validate();
                }
                catch(ConnectionCanceledException e) {
                    log.info("transfer cancelled.");
                    return false;
                }

                int count = (int) Math.min(buffer.length, remaining);

                int bytesRead = in.read(buffer, 0, count);
                log.info("read [{}] of [{}] requested bytes from input stream.", bytesRead, count);
                if(-1 == bytesRead) {
                    break;
                }

                streamListener.recv(bytesRead);
                out.write(buffer, 0, bytesRead);
                log.info("wrote [{}] bytes to output stream.", bytesRead);
                streamListener.sent(bytesRead);
                remaining -= bytesRead;
            }

            log.info("total bytes remaining = [{}]", remaining);
            log.info("done. wrote [{}] of [{}] bytes to the replica.", chunkSize - remaining, chunkSize);

            return true;
        }
        catch(IOException | IRODSException e) {
            log.error(e.getMessage());
        }

        return false;
    }

}
