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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.List;

public class IRODSWriteFeature implements Write<List<String>> {

    private static final Logger log = LogManager.getLogger(IRODSWriteFeature.class);

    private final IRODSSession session;

    public IRODSWriteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<List<String>> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            boolean append = status.isAppend();
            boolean truncate = !append;
            final IRODSDataObjectOutputStream stream = new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(), truncate, append);
            return new StatusOutputStream<List<String>>(stream) {
                private List<String> reply;

                @Override
                public void close() throws IOException {
                    // Instruct the server to compute and register a checksum for the replica on close
                    stream.close(IRODSUploadFeature.closeInstructions(IRODSUploadFeature.hasChecksum(status)));
                    try {
                        // Set the latest attributes of the data object registered by the server
                        reply = IRODSAttributesFinderFeature.query(conn.getRcComm(), file.getAbsolute());
                    }
                    catch(IRODSException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                    if(reply.isEmpty()) {
                        log.warn("no replica found for [{}] after closing stream.", file);
                        return;
                    }
                    log.debug("closed stream for [{}] with reply [{}].", file, reply);
                    status.setResponse(new IRODSAttributesFinderFeature(session).toAttributes(reply));
                }

                @Override
                public List<String> getStatus() {
                    return reply;
                }
            };
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map("Uploading {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Uploading {0} failed", e, file);
        }
    }

    /**
     * Compute local checksum with the default hash scheme of iRODS to allow verification against the
     * checksum registered by the server after the upload
     */
    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.sha256);
    }
}
