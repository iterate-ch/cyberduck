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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IRODSDownloadFeature implements Download {

    private final IRODSSession session;
    private final int numThread = 3; // TODO Make this configurable
    private static final int BUFFER_SIZE = 4 * 1024 * 1024; // TODO Make configurable

    public IRODSDownloadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void download(final Read read, final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status,
                         final ConnectionCallback callback) throws BackgroundException {
        try {
            final RcComm primaryConn = session.getClient().getRcComm();
            final String logicalPath = file.getAbsolute();

            if(!IRODSFilesystem.exists(primaryConn, logicalPath)) {
                throw new NotfoundException(logicalPath);
            }

            final long dataObjectSize = IRODSFilesystem.dataObjectSize(primaryConn, logicalPath);

            // Transfer the bytes over multiple connections if the size of the data object
            // exceeds a certain threshold - e.g. 32MB.
            // TODO Consider making this configurable.
            if(dataObjectSize < 32 * 1024 * 1024) {
                byte[] buffer = new byte[4 * 1024 * 1024];

                try(IRODSDataObjectInputStream in = new IRODSDataObjectInputStream(primaryConn, logicalPath);
                    FileOutputStream out = new FileOutputStream(local.getAbsolute())) {
                    while(true) {
                        int bytesRead = in.read(buffer);
                        if(bytesRead == -1) {
                            break;
                        }
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }

            //
            // The data object is larger than the threshold so use parallel transfer.
            //

            // Step 1: Get replica token & number via primary stream
            try(IRODSDataObjectInputStream primary = new IRODSDataObjectInputStream(primaryConn, logicalPath)) {
                final String replicaToken = primary.getReplicaToken();
                final long replicaNumber = primary.getReplicaNumber();

                // Step 2: Setup connection pool
                try(final IRODSConnectionPool pool = new IRODSConnectionPool(numThread)) {
                    pool.start(
                            session.getHost().getHostname(),
                            session.getHost().getPort(),
                            new QualifiedUsername(session.getHost().getCredentials().getUsername(), session.getRegion()),
                            conn -> {
                                try {
                                    // TODO Needs to take the value of the Authorization profile value.
                                    IRODSApi.rcAuthenticateClient(conn, "native", session.getHost().getCredentials().getPassword());
                                    return true;
                                }
                                catch(Exception e) {
                                    return false;
                                }
                            });

                    final long chunkSize = dataObjectSize / numThread;
                    final long remainChunkSize = dataObjectSize % numThread;

                    // Step 3: Create empty target file
                    try(RandomAccessFile out = new RandomAccessFile(local.getAbsolute(), "rw")) {
                        out.setLength(dataObjectSize);
                    }

                    // Step 4: Parallel readers
                    final ExecutorService executor = Executors.newFixedThreadPool(numThread);
                    List<Future<?>> tasks = new ArrayList<>();
                    for(int i = 0; i < numThread; i++) {
                        final PoolConnection conn = pool.getConnection();
                        tasks.add(executor.submit(new IRODSChunkWorker(
                                conn,
                                new IRODSDataObjectInputStream(conn.getRcComm(), replicaToken, replicaNumber),
                                new FileOutputStream(local.getAbsolute()),
                                i * chunkSize,
                                (numThread - 1 == i) ? chunkSize + remainChunkSize : chunkSize,
                                BUFFER_SIZE
                        )));
                    }

                    for(Future<?> task : tasks) {
                        task.get();
                    }

                    executor.shutdown();
                }
            }
        }
        catch(Exception e) {
            throw new IRODSExceptionMappingService().map("Download {0} failed", e);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return false;
    }
}
