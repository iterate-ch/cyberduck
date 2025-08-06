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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IRODSUploadFeature implements Upload<Checksum> {
    private static final Logger log = LogManager.getLogger(IRODSUploadFeature.class);

    private final IRODSSession session;
    private int numThread = 3; // TODO Make configurable
    private static final int BUFFER_SIZE = 4 * 1024 * 1024; // TODO Make configurable

    public IRODSUploadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Checksum upload(final Path file, final Local local, final BandwidthThrottle throttle,
                           final ProgressListener progress, final StreamListener streamListener, final TransferStatus status,
                           final ConnectionCallback callback) throws BackgroundException {
        try {
            final RcComm primaryConn = session.getClient().getRcComm();
            final long fileSize = local.attributes().getSize();
            final String logicalPath = file.getAbsolute();

            // Transfer the bytes over multiple connections if the size of the local file
            // exceeds a certain threshold - e.g. 32MB.
            // TODO Consider making this configurable.
            if(fileSize < 32 * 1024 * 1024) {
                byte[] buffer = new byte[4 * 1024 * 1024];
                boolean truncate = true;
                boolean append = false;

                try(FileInputStream in = new FileInputStream(local.getAbsolute());
                    IRODSDataObjectOutputStream out = new IRODSDataObjectOutputStream(primaryConn, logicalPath, truncate, append)) {
                    while(true) {
                        int bytesRead = in.read(buffer);
                        if(bytesRead == -1) {
                            break;
                        }
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }

            // Step 1: Open one primary output stream to get token and replica info
            try(IRODSDataObjectOutputStream primaryOut = new IRODSDataObjectOutputStream()) {
                boolean truncate = true;
                boolean append = false;
                primaryOut.open(primaryConn, logicalPath, truncate, append);
                final String replicaToken = primaryOut.getReplicaToken();
                final long replicaNumber = primaryOut.getReplicaNumber();

                // Step 2: Connection pool
                try(final IRODSConnectionPool pool = new IRODSConnectionPool(numThread)) {
                    pool.start(
                            session.getHost().getHostname(),
                            session.getHost().getPort(),
                            new QualifiedUsername(session.getHost().getCredentials().getUsername(), session.getRegion()),
                            conn -> {
                                try {
                                    // TODO Replace "native" with the scheme defined by the Authorization config property.
                                    IRODSApi.rcAuthenticateClient(conn, "native", session.getHost().getCredentials().getPassword());
                                    return true;
                                }
                                catch(Exception e) {
                                    return false;
                                }
                            });

                    // Step 3: Thread pool & chunking
                    final long chunkSize = fileSize / numThread;
                    final long remainChunkSize = fileSize % numThread;
                    truncate = false;

                    final ExecutorService executor = Executors.newFixedThreadPool(numThread);
                    List<Future<?>> tasks = new ArrayList<>();
                    for(int i = 0; i < numThread; i++) {
                        final long start = i * chunkSize;
                        final PoolConnection conn = pool.getConnection();
                        tasks.add(executor.submit(new IRODSChunkWorker(
                                conn,
                                new FileInputStream(local.getAbsolute()),
                                new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(), replicaToken, replicaNumber, truncate, append),
                                start,
                                (i == numThread - 1) ? chunkSize + remainChunkSize : chunkSize,
                                BUFFER_SIZE
                        )));
                    }

                    for(Future<?> task : tasks) {
                        task.get();
                    }

                    executor.shutdown();
                }
            }

            final String fingerprintValue = IRODSFilesystem.dataObjectChecksum(primaryConn, file.getAbsolute());
            return Checksum.parse(fingerprintValue);
        }
        catch(Exception e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Write.Append(status.isExists()).withStatus(status);
    }

    @Override
    public Upload<Checksum> withWriter(final Write<Checksum> writer) {
        return this;
    }
}
