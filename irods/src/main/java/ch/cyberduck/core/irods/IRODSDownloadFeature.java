package ch.cyberduck.core.irods;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.irods.irods4j.high_level.connection.IRODSConnectionPool;
import org.irods.irods4j.high_level.connection.IRODSConnectionPool.PoolConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

public class IRODSDownloadFeature implements Download {

    private final IRODSSession session;
    private boolean truncate = true;
	private boolean append = false;
	private static final int BUFFER_SIZE = 4 * 1024 * 1024; 

    public IRODSDownloadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status,
                         final ConnectionCallback callback) throws BackgroundException {
    	final int numThread = 3;
        try {

        	 final RcComm primaryConn = session.getClient().getRcComm();
             final String logicalPath = file.getAbsolute();

             if (!IRODSFilesystem.exists(primaryConn, logicalPath)) {
                 throw new NotfoundException(logicalPath);
             }

             final long fileSize = IRODSFilesystem.dataObjectSize(primaryConn, logicalPath);

             // Step 1: Get replica token & number via primary stream
             try (IRODSDataObjectInputStream primary = new IRODSDataObjectInputStream(primaryConn, logicalPath)) {
                 final String replicaToken = primary.getReplicaToken();
                 final long replicaNumber = primary.getReplicaNumber();

                 // Step 2: Setup connection pool
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

                 final ExecutorService executor = Executors.newFixedThreadPool(numThread);
                 
                 //TODO:fileSize/
                 final long chunkSize = fileSize / numThread;
                 final long remainChunkSize = fileSize % numThread;


                 // Step 3: Create empty target file
                 try (RandomAccessFile out = new RandomAccessFile(local.getAbsolute(), "rw")) {
                     out.setLength(fileSize);
                 }

                 // Step 4: Parallel readers
                 List<Future<?>> tasks = new ArrayList<>();
                 for (int i = 0; i < numThread; i++) {
                     final long start = i * chunkSize;
                     final PoolConnection conn = pool.getConnection();
                     IRODSDataObjectInputStream stream = new IRODSDataObjectInputStream(conn.getRcComm(), replicaToken, replicaNumber);
                     ChunkWorker worker = new ChunkWorker(
                    	        stream,
                    	        local.getAbsolute(),
                    	        start,
                    	        (numThread - 1 == i) ? chunkSize + remainChunkSize : chunkSize,
                    	        BUFFER_SIZE
                    	    );
                     Future<?> task = executor.submit(worker);
                     tasks.add(task);
                 }

                 for (Future<?> task : tasks) {
                     task.get();
                 }


                 executor.shutdown();
                 pool.close();
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

    @Override
    public Download withReader(final Read reader) {
        return this;
    }
}
