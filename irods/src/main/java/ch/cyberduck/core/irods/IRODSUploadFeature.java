package ch.cyberduck.core.irods;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
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

public class IRODSUploadFeature implements Upload<Checksum> {
    private static final Logger log = LogManager.getLogger(IRODSUploadFeature.class);

    private final IRODSSession session;
    private boolean truncate = true;
	private boolean append = false;
	private int numThread=3;
	private static final int BUFFER_SIZE = 4 * 1024 * 1024; 

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

            // Step 1: Open one primary output stream to get token and replica info
            IRODSDataObjectOutputStream primaryOut = new IRODSDataObjectOutputStream();
            primaryOut.open(primaryConn, file.getAbsolute(), truncate, append);
            final String replicaToken = primaryOut.getReplicaToken();
            final long replicaNumber = primaryOut.getReplicaNumber();
            primaryOut.close();

            // Step 2: Connection pool
            final IRODSConnectionPool pool = new IRODSConnectionPool(numThread);
            pool.start(
                session.getHost().getHostname(),
                session.getHost().getPort(),
                new QualifiedUsername(session.getHost().getCredentials().getUsername(), session.getRegion()),
                conn -> {
                    try {
                        IRODSApi.rcAuthenticateClient(conn, "native", session.getHost().getCredentials().getPassword());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

            // Step 3: Thread pool & chunking
            final ExecutorService executor = Executors.newFixedThreadPool(numThread);
            final long chunkSize = fileSize / numThread;
            final long remainChunkSize = fileSize % numThread;

            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < numThread; i++) {
                final long start = i * chunkSize;
                final PoolConnection conn = pool.getConnection();
                IRODSDataObjectOutputStream stream = new IRODSDataObjectOutputStream();
                stream.open(conn.getRcComm(), file.getAbsolute(),replicaToken,replicaNumber, truncate, append);
                ChunkWorker worker = new ChunkWorker(
                    stream,
                    local.getAbsolute(),
                    start,
                    (i == numThread - 1) ? chunkSize + remainChunkSize : chunkSize,
                    BUFFER_SIZE
                );
                tasks.add(executor.submit(worker));
            }

            for (Future<?> task : tasks) {
                task.get();
            }

            executor.shutdown();
            pool.close();

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
