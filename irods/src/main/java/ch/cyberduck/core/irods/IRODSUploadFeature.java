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
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IRODSUploadFeature implements Upload<Checksum> {

    private final IRODSSession session;

    public IRODSUploadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Checksum upload(final Path file, final Local local, final BandwidthThrottle throttle,
                           final ProgressListener progress, final StreamListener streamListener, final TransferStatus status,
                           final ConnectionCallback callback) throws BackgroundException {
        try {
            final PreferencesReader preferences = HostPreferencesFactory.get(session.getHost());

            final RcComm primaryConn = session.getClient().getRcComm();
            final long fileSize = local.attributes().getSize();
            final String logicalPath = file.getAbsolute();

            // Transfer the bytes over multiple connections if the size of the local file
            // exceeds a certain threshold - e.g. 32MB.
            // TODO Consider making this configurable.
            if(fileSize < 32 * 1024 * 1024) { //preferences.getInteger("irods.parallel_transfer.size_threshold")) {
                byte[] buffer = new byte[4 * 1024 * 1024]; //preferences.getInteger("irods.parallel_transfer.rbuffer_size")];
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

            //
            // The data object is larger than the threshold so use parallel transfer.
            //

            // TODO Clamp the value so that users do not specify something ridiculous.
            final int threadCount = 3; //preferences.getInteger("irods.parallel_transfer.thread_count");
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            final long chunkSize = fileSize / threadCount;
            final long remainingBytes = fileSize % threadCount;

            final List<InputStream> localFileStreams = new ArrayList<>();
            final List<IRODSDataObjectOutputStream> secondaryIrodsStreams = new ArrayList<>();

            // Open the primary iRODS output stream.
            // TODO Needs to pass the target resource name if provided.
            boolean truncate = true;
            boolean append = false;
            try(IRODSDataObjectOutputStream primaryStream = new IRODSDataObjectOutputStream(primaryConn, logicalPath, truncate, append)) {
                // Capture the replica access token and replica number of the open replica.
                final String replicaToken = primaryStream.getReplicaToken();
                final long replicaNumber = primaryStream.getReplicaNumber();

                // Initialize connections for secondary streams.
                try(IRODSConnectionPool pool = new IRODSConnectionPool(threadCount - 1)) {
                    IRODSConnectionUtils.startIRODSConnectionPool(session, pool);

                    // Holds handles to tasks running on the thread pool. This allows us to wait for
                    // all tasks to complete before shutting down everything.
                    List<Future<?>> tasks = new ArrayList<>();

                    // Open the first input stream for the local file and store it for processing.
                    final java.nio.file.Path localFilePath = Paths.get(local.getAbsolute());
                    localFileStreams.add(Files.newInputStream(localFilePath));

                    // Launch the first IO task.
                    tasks.add(executor.submit(new IRODSChunkWorker(
                            localFileStreams.get(0),
                            primaryStream,
                            0,
                            chunkSize,
                            4 * 1024 * 1024//preferences.getInteger("irods.parallel_transfer.rbuffer_size")
                    )));

                    try {
                        truncate = false;

                        // Launch remaining IO tasks.
                        for(int i = 1; i < threadCount; ++i) {
                            localFileStreams.add(Files.newInputStream(localFilePath));
                            PoolConnection conn = pool.getConnection();
                            secondaryIrodsStreams.add(new IRODSDataObjectOutputStream(conn.getRcComm(), replicaToken, logicalPath, replicaNumber, truncate, append));
                            tasks.add(executor.submit(new IRODSChunkWorker(
                                    localFileStreams.get(localFileStreams.size() - 1),
                                    secondaryIrodsStreams.get(secondaryIrodsStreams.size() - 1),
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
                        closeOutputStreams(secondaryIrodsStreams);
                    }
                }
            }
            finally {
                closeInputStreams(localFileStreams);
            }

            executor.shutdown();
            // TODO Make this configurable.
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // TODO Make this configurable.
            final String fingerprintValue = IRODSFilesystem.dataObjectChecksum(primaryConn, logicalPath);
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

    private static void closeOutputStreams(List<IRODSDataObjectOutputStream> streams) {
        final IRODSDataObjectStream.OnCloseSuccess closeInstructions = new IRODSDataObjectStream.OnCloseSuccess();
        closeInstructions.updateSize = false;
        closeInstructions.updateStatus = false;
        closeInstructions.computeChecksum = false;
        closeInstructions.sendNotifications = false;

        streams.forEach(in -> {
            try {
                in.close(closeInstructions);
            }
            catch(Exception e) { /* Ignored */ }
        });
    }

    private static void closeInputStreams(List<InputStream> streams) {
        streams.forEach(out -> {
            try {
                out.close();
            }
            catch(Exception e) { /* Ignored */ }
        });
    }
}
