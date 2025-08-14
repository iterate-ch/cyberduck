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
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IRODSDownloadFeature implements Download {

    private final IRODSSession session;

    public IRODSDownloadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status,
                         final ConnectionCallback callback) throws BackgroundException {
        try {
            final PreferencesReader preferences = HostPreferencesFactory.get(session.getHost());

            final RcComm primaryConn = session.getClient().getRcComm();
            final String logicalPath = file.getAbsolute();

            if(!IRODSFilesystem.exists(primaryConn, logicalPath)) {
                throw new NotfoundException(logicalPath);
            }

            final long dataObjectSize = IRODSFilesystem.dataObjectSize(primaryConn, logicalPath);

            // Transfer the bytes over multiple connections if the size of the data object
            // exceeds a certain threshold - e.g. 32MB.
            if(dataObjectSize < 32 * 1024 * 1024) { //preferences.getInteger("irods.parallel_transfer.size_threshold")) {
                byte[] buffer = new byte[4 * 1024 * 1024]; //preferences.getInteger("irods.parallel_transfer.rbuffer_size")];

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

            // TODO Clamp the value so that users do not specify something ridiculous.
            final int threadCount = 3; //preferences.getInteger("irods.parallel_transfer.thread_count");
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            final long chunkSize = dataObjectSize / threadCount;
            final long remainingBytes = dataObjectSize % threadCount;

            final List<IRODSDataObjectInputStream> secondaryIrodsStreams = new ArrayList<>();
            final List<OutputStream> localFileStreams = new ArrayList<>();

            // Open the primary iRODS input stream.
            // TODO Needs to pass the target resource name if provided.
            try(IRODSDataObjectInputStream primaryStream = new IRODSDataObjectInputStream(primaryConn, logicalPath)) {
                // Initialize connections for secondary streams.
                try(IRODSConnectionPool pool = new IRODSConnectionPool(threadCount - 1)) {
                    IRODSConnectionUtils.startIRODSConnectionPool(session, pool);

                    // Holds handles to tasks running on the thread pool. This allows us to wait for
                    // all tasks to complete before shutting down everything.
                    List<Future<?>> tasks = new ArrayList<>();

                    // Open the first output stream for the local file and store it for processing.
                    // This also guarantees the target file exists and is empty (i.e. truncated to zero
                    // if it exists).
                    final java.nio.file.Path localFilePath = Paths.get(local.getAbsolute());
                    localFileStreams.add(Files.newOutputStream(localFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

                    // Launch the first IO task.
                    tasks.add(executor.submit(new IRODSChunkWorker(
                            primaryStream,
                            localFileStreams.get(0),
                            0,
                            chunkSize,
                            4 * 1024 * 1024//preferences.getInteger("irods.parallel_transfer.rbuffer_size")
                    )));

                    try {
                        // Launch remaining IO tasks.
                        for(int i = 1; i < threadCount; ++i) {
                            PoolConnection conn = pool.getConnection();
                            secondaryIrodsStreams.add(new IRODSDataObjectInputStream(conn.getRcComm(), logicalPath));
                            localFileStreams.add(Files.newOutputStream(localFilePath, StandardOpenOption.WRITE));
                            tasks.add(executor.submit(new IRODSChunkWorker(
                                    secondaryIrodsStreams.get(secondaryIrodsStreams.size() - 1),
                                    localFileStreams.get(localFileStreams.size() - 1),
                                    i * chunkSize,
                                    (threadCount - 1 == i) ? chunkSize + remainingBytes : chunkSize,
                                    4 * 1024 * 1024//preferences.getInteger("irods.parallel_transfer.rbuffer_size")
                            )));
                        }

                        // Wait for all tasks on the thread pool to complete.
                        for(Future<?> task : tasks) {
                            try {
                                task.get();
                            }
                            catch(Exception e) { /* Ignored */ }
                        }
                    }
                    finally {
                        closeInputStreams(secondaryIrodsStreams);
                    }
                }
            }
            finally {
                closeOutputStreams(localFileStreams);
            }

            executor.shutdown();
            // TODO Make this configurable.
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(Exception e) {
            throw new IRODSExceptionMappingService().map("Download {0} failed", e);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return false;
    }

    @Override
    public Download withReader(final Read reader) {
        return this;
    }

    private static void closeInputStreams(List<IRODSDataObjectInputStream> streams) {
        streams.forEach(in -> {
            try {
                in.close();
            }
            catch(Exception e) { /* Ignored */ }
        });
    }

    private static void closeOutputStreams(List<OutputStream> streams) {
        streams.forEach(out -> {
            try {
                out.close();
            }
            catch(Exception e) { /* Ignored */ }
        });
    }
}
