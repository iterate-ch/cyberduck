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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

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
            final String dstRootResource = getResource(preferences).orElse(StringUtils.EMPTY);

            log.debug("status.getLength() = [{}]", status.getLength());
            log.debug("fileSize           = [{}]", fileSize);
            log.debug("local file         = [{}]", local.getAbsolute());
            log.debug("logicalPath        = [{}]", logicalPath);
            log.debug("dst root resource  = [{}]", dstRootResource);

            // Signals whether the completion flag should be set following the transfer.
            // An upload is considered to be successful if and only if no errors occurred.
            // That includes waiting for background threads to terminate and closing output
            // streams. This is important for certain features (e.g. mtime preservation).
            boolean setCompletionFlag = false;

            final long threshold = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_THRESHOLD), 1, Integer.MAX_VALUE);
            final int bufferSize = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_BUFFER_SIZE), 1, (int) (128 * TransferStatus.MEGA));

            // Transfer the bytes over multiple connections if the size of the local file
            // exceeds a certain number of bytes - e.g. 32MB.
            if(fileSize < threshold) {
                log.debug("local file does not exceed parallel transfer threshold [{}]. performing single-threaded transfer.", threshold);

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
                            try {
                                status.validate(); // Throws if transfer is cancelled.
                            }
                            catch(ConnectionCanceledException e) {
                                log.info("transfer cancelled.");
                                return null;
                            }

                            int bytesRead = in.read(buffer);
                            if(bytesRead == -1) {
                                setCompletionFlag = true;
                                return null;
                            }
                            streamListener.recv(bytesRead);
                            out.write(buffer, 0, bytesRead);
                            streamListener.sent(bytesRead);
                        }
                    }
                    finally {
                        out.close();

                        if(setCompletionFlag) {
                            status.setComplete();
                        }
                    }
                }
            }

            //
            // The data object is larger than the threshold so use parallel transfer.
            //

            log.debug("local file exceeds parallel transfer threshold [{}]. performing multi-threaded transfer.", threshold);

            final int threadCount = IRODSIntegerUtils.clamp(
                    preferences.getInteger(IRODSProtocol.PARALLEL_TRANSFER_CONNECTIONS), 2, 10);
            log.debug("thread count = [{}]; starting thread pool.", threadCount);
            final ThreadPool threadPool = ThreadPoolFactory.get("multipart-iRODS", threadCount);

            final long chunkSize = fileSize / threadCount;
            final long remainingBytes = fileSize % threadCount;
            log.debug("chunk size      = [{}]", chunkSize);
            log.debug("remaining bytes = [{}]", remainingBytes);

            final List<InputStream> localFileStreams = new ArrayList<>();
            final List<IRODSDataObjectOutputStream> irodsStreams = new ArrayList<>();

            log.debug("launching connection pool with [{}] connections.", threadCount);
            try(IRODSConnectionPool pool = new IRODSConnectionPool(IRODSConnectionUtils.initConnectionOptions(session), threadCount)) {
                try {
                    status.validate(); // Throws if transfer is cancelled.
                }
                catch(ConnectionCanceledException e) {
                    log.info("transfer cancelled.");
                    return null;
                }

                IRODSConnectionUtils.startIRODSConnectionPool(session, pool);
                log.debug("connection pool started.");

                try {
                    String replicaToken = null;
                    long replicaNumber = -1;

                    for(int i = 0; i < threadCount; ++i) {
                        // We cannot use Files.newInputStream() does not report the concrete
                        // type of the stream. The concrete type is needed for seek operations.
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

                    try {
                        status.validate(); // Throws if transfer is cancelled.
                    }
                    catch(ConnectionCanceledException e) {
                        log.info("transfer cancelled.");
                        return null;
                    }

                    // Holds handles to tasks running on the thread pool. This allows us to wait for
                    // all tasks to complete before shutting down everything.
                    List<Future<Boolean>> tasks = new ArrayList<>();

                    // Launch remaining IO tasks.
                    log.debug("launch parallel IO tasks.");
                    for(int i = 0; i < threadCount; ++i) {
                        tasks.add(threadPool.execute(new IRODSChunkWorker(
                                status,
                                streamListener,
                                localFileStreams.get(i),
                                irodsStreams.get(i),
                                i * chunkSize,
                                (threadCount - 1 == i) ? chunkSize + remainingBytes : chunkSize,
                                bufferSize
                        )));
                    }

                    setCompletionFlag = waitForTasksToComplete(tasks);
                }
                finally {
                    final boolean closedOutputStreams = closeOutputStreams(irodsStreams);
                    if(setCompletionFlag && closedOutputStreams) {
                        status.setComplete();
                    }

                    closeInputStreams(localFileStreams);
                }
            }

            log.debug("shutting down thread pool.");
            threadPool.shutdown(false);
            log.debug("done.");

            return null;
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(Exception e) {
            throw new DefaultExceptionMappingService().map(e);
        }
    }

    @Override
    public Write.Append append(Path file, TransferStatus status) throws BackgroundException {
        return new Write.Append(status.isExists()).withStatus(status);
    }

    private static boolean closeOutputStreams(List<IRODSDataObjectOutputStream> streams) {
        log.debug("closing output streams.");

        final IRODSDataObjectStream.OnCloseSuccess closeInstructions = new IRODSDataObjectStream.OnCloseSuccess();
        closeInstructions.updateSize = false;
        closeInstructions.updateStatus = false;
        closeInstructions.computeChecksum = false;
        closeInstructions.sendNotifications = false;

        boolean success = true;

        for(int i = 1; i < streams.size(); ++i) {
            try {
                streams.get(i).close(closeInstructions);
            }
            catch(Exception e) {
                log.error(e.getMessage());
                success = false;
            }
        }

        try {
            streams.get(0).close();
        }
        catch(Exception e) {
            log.error(e.getMessage());
            success = false;
        }

        return success;
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

    private static boolean waitForTasksToComplete(List<Future<Boolean>> tasks) {
        log.debug("waiting for parallel IO tasks to finish.");
        boolean success = true;

        for(Future<Boolean> task : tasks) {
            try {
                if(!task.get()) {
                    success = false;
                }
            }
            catch(Exception e) {
                success = false;
                log.error(e.getMessage());
            }
        }

        log.debug("parallel IO tasks have finished.");
        return success;
    }

    private Optional<String> getResource(PreferencesReader prefs) {
        final String resc = prefs.getProperty(IRODSProtocol.DESTINATION_RESOURCE);
        if(StringUtils.isNotBlank(resc)) {
            return Optional.of(resc);
        }

        final String region = session.getHost().getRegion();
        int index = region.indexOf(':');
        if(index != -1 && ++index < region.length()) {
            return Optional.of(region.substring(index));
        }

        return Optional.empty();
    }
}
