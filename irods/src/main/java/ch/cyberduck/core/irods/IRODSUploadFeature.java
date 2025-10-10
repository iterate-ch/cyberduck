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
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IRODSUploadFeature implements Upload<Void> {

    private static final Logger log = LogManager.getLogger(IRODSUploadFeature.class);

    private final IRODSSession session;

    public IRODSUploadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle,
                       final ProgressListener progress, final StreamListener streamListener, final TransferStatus status,
                       final ConnectionCallback callback) throws BackgroundException {
        try {
            final PreferencesReader preferences = HostPreferencesFactory.get(session.getHost());

            final long fileSize = local.attributes().getSize();
            final String logicalPath = file.getAbsolute();
            final String dstRootResource = preferences.getProperty(IRODSProtocol.DESTINATION_RESOURCE);

            log.debug("status.getLength() = [{}]", status.getLength());
            log.debug("fileSize           = [{}]", fileSize);
            log.debug("local file         = [{}]", local.getAbsolute());
            log.debug("logicalPath        = [{}]", logicalPath);
            log.debug("dst root resource  = [{}]", dstRootResource);

            final long threshold = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_THRESHOLD), 1, Integer.MAX_VALUE);
            final int bufferSize = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_BUFFER_SIZE), 1, (int) (128 * TransferStatus.MEGA));

            // Transfer the bytes over multiple connections if the size of the local file
            // exceeds a certain number of bytes - e.g. 32MB.
            if(fileSize < threshold) {
                log.debug("local file does not exceed parallel transfer threshold. performing single-threaded transfer.");

                byte[] buffer = new byte[bufferSize];
                boolean truncate = true;
                boolean append = false;

                try(FileInputStream in = new FileInputStream(local.getAbsolute());
                    IRODSConnection conn = IRODSConnectionUtils.newAuthenticatedConnection(session)) {

                    IRODSDataObjectOutputStream out;
                    if(StringUtils.isNotBlank(dstRootResource)) {
                        out = new IRODSDataObjectOutputStream(conn.getRcComm(), logicalPath, dstRootResource, truncate, append);
                    }
                    else {
                        out = new IRODSDataObjectOutputStream(conn.getRcComm(), logicalPath, truncate, append);
                    }

                    try {
                        while(true) {
                            status.validate(); // Throws if transfer is cancelled.
                            int bytesRead = in.read(buffer);
                            if(bytesRead == -1) {
                                return null;
                            }
                            streamListener.recv(bytesRead);
                            out.write(buffer, 0, bytesRead);
                            streamListener.sent(bytesRead);
                        }
                    }
                    finally {
                        out.close();
                    }
                }
            }

            //
            // The data object is larger than the threshold so use parallel transfer.
            //

            log.debug("local file exceeds parallel transfer threshold. performing multi-threaded transfer.");

            final int threadCount = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_CONNECTIONS), 2, 10);
            log.debug("thread count = [{}]; starting thread pool.", threadCount);
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            final long chunkSize = fileSize / threadCount;
            final long remainingBytes = fileSize % threadCount;
            log.debug("chunk size      = [{}]", chunkSize);
            log.debug("remaining bytes = [{}]", remainingBytes);

            final List<InputStream> localFileStreams = new ArrayList<>();
            final List<IRODSDataObjectOutputStream> irodsStreams = new ArrayList<>();

            log.debug("launching connection pool with [{}] connections.", threadCount);
            try(IRODSConnectionPool pool = new IRODSConnectionPool(IRODSConnectionUtils.initConnectionOptions(session), threadCount)) {
                status.validate(); // Throws if transfer is cancelled.

                IRODSConnectionUtils.startIRODSConnectionPool(session, pool);
                log.debug("connection pool started.");

                try {
                    // Initialize streams.
                    String replicaToken = null;
                    long replicaNumber = -1;

                    for(int i = 0; i < threadCount; ++i) {
                        // We cannot use Files.newInputStream() because the type information
                        // is required to properly close the stream.
                        localFileStreams.add(new FileInputStream(local.getAbsolute()));

                        // The pooled connection will never be returned to the pool. This is
                        // okay because after the transfer, no connection is reused.
                        PoolConnection conn = pool.getConnection();

                        if(0 == i) {
                            log.debug("opened primary iRODS stream.");
                            // The first iRODS output stream is the primary stream. The opened
                            // replica is always truncated upon success.
                            if(StringUtils.isNotBlank(dstRootResource)) {
                                irodsStreams.add(new IRODSDataObjectOutputStream(
                                        conn.getRcComm(), logicalPath, dstRootResource, true, false));
                            }
                            else {
                                irodsStreams.add(new IRODSDataObjectOutputStream(
                                        conn.getRcComm(), logicalPath, true, false));
                            }
                            replicaToken = irodsStreams.get(0).getReplicaToken();
                            replicaNumber = irodsStreams.get(0).getReplicaNumber();
                            log.debug("replica token  = [{}]", replicaToken);
                            log.debug("replica number = [{}]", replicaNumber);
                        }
                        else {
                            log.debug("opened secondary iRODS stream.");
                            irodsStreams.add(new IRODSDataObjectOutputStream(
                                    conn.getRcComm(), replicaToken, logicalPath, replicaNumber, false, false));
                        }
                    }

                    status.validate(); // Throws if transfer is cancelled.

                    // Holds handles to tasks running on the thread pool. This allows us to wait for
                    // all tasks to complete before shutting down everything.
                    List<Future<?>> tasks = new ArrayList<>();

                    // Launch remaining IO tasks.
                    log.debug("launch parallel IO tasks.");
                    for(int i = 0; i < threadCount; ++i) {
                        tasks.add(executor.submit(new IRODSChunkWorker(
                                status,
                                streamListener,
                                localFileStreams.get(i),
                                irodsStreams.get(i),
                                i * chunkSize,
                                (threadCount - 1 == i) ? chunkSize + remainingBytes : chunkSize,
                                bufferSize
                        )));
                    }

                    waitForTasksToComplete(tasks);
                }
                finally {
                    closeOutputStreams(irodsStreams);
                    closeInputStreams(localFileStreams);
                }
            }

            log.debug("shutting down thread pool executor.");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            log.debug("done.");

            return null;
        }
        catch(Exception e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    private static void closeOutputStreams(List<IRODSDataObjectOutputStream> streams) {
        log.debug("closing output streams.");

        final IRODSDataObjectStream.OnCloseSuccess closeInstructions = new IRODSDataObjectStream.OnCloseSuccess();
        closeInstructions.updateSize = false;
        closeInstructions.updateStatus = false;
        closeInstructions.computeChecksum = false;
        closeInstructions.sendNotifications = false;

        for(int i = 1; i < streams.size(); ++i) {
            try {
                streams.get(i).close(closeInstructions);
            }
            catch(Exception e) {
                log.error(e.getMessage());
            }
        }

        try {
            streams.get(0).close();
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void closeInputStreams(List<InputStream> streams) {
        log.debug("closing input streams.");

        streams.forEach(out -> {
            try {
                out.close();
            }
            catch(Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private static void waitForTasksToComplete(List<Future<?>> tasks) {
        log.debug("waiting for parallel IO tasks to finish.");
        for(Future<?> task : tasks) {
            try {
                task.get();
            }
            catch(Exception e) {
                log.error(e.getMessage());
            }
        }
        log.debug("parallel IO tasks have finished.");
    }
}
