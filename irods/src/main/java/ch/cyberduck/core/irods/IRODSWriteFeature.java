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
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.io.OutputStream;

public class IRODSWriteFeature implements Write<Void> {

    private static final Logger log = LogManager.getLogger(IRODSWriteFeature.class);

    private final IRODSSession session;

    public IRODSWriteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            boolean append = status.isAppend();
            boolean truncate = !append;
            // Instruct the server to compute and register a checksum for the replica on close
            final boolean computeChecksum = IRODSUploadFeature.hasChecksum(status);
            final OutputStream out = new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(), truncate, append) {
                @Override
                public void close() {
                    final IRODSDataObjectStream.OnCloseSuccess instructions = new IRODSDataObjectStream.OnCloseSuccess();
                    instructions.computeChecksum = computeChecksum;
                    this.close(instructions);
                }
            };
            return new VoidStatusOutputStream(out);
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
