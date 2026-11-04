package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import com.github.sardine.impl.SardineException;

/**
 * Chunked upload using the Nextcloud chunking v2 protocol. Chunks are uploaded to a temporary collection below
 * <code>remote.php/dav/uploads/USER</code> and assembled on the server with a final MOVE request to the target.
 *
 * @see <a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/WebDAV/chunking.html">Chunked file upload</a>
 */
public class NextcloudChunkedUploadFeature extends HttpUploadFeature<Void> {
    private static final Logger log = LogManager.getLogger(NextcloudChunkedUploadFeature.class);

    /**
     * Maximum number of chunks allowed by the server
     */
    private static final long MAX_CHUNKS = 10000L;

    private final DAVSession session;
    private final long chunksize;
    private final int concurrency;

    public NextcloudChunkedUploadFeature(final DAVSession session) {
        super(EnumSet.of(Flags.checksumOptional), HashAlgorithm.sha1);
        this.session = session;
        this.chunksize = HostPreferencesFactory.get(session.getHost()).getLong("nextcloud.upload.multipart.size");
        this.concurrency = HostPreferencesFactory.get(session.getHost()).getInteger("nextcloud.upload.multipart.concurrency");
    }

    @Override
    public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle,
                       final ProgressListener progress, final StreamListener listener, final TransferStatus status,
                       final ConnectionCallback callback) throws BackgroundException {
        final Path collection = new Path(String.format("%s/%s",
                new NextcloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.uploads).getAbsolute(),
                new UUIDRandomStringService().random()), EnumSet.of(Path.Type.directory));
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            log.debug("Create upload collection {} for {}", collection, file);
            session.getClient().createDirectory(new DAVPathEncoder(session).encode(collection));
            final long size = Math.max(chunksize, (status.getLength() + MAX_CHUNKS - 1) / MAX_CHUNKS);
            final List<Future<Void>> chunks = new ArrayList<>();
            long offset = 0;
            long remaining = status.getLength();
            for(int number = 1; remaining > 0; number++) {
                final long length = Math.min(size, remaining);
                chunks.add(this.submit(pool, file, collection, local, throttle, listener, status, number, offset, length, callback));
                remaining -= length;
                offset += length;
            }
            Interruptibles.awaitAll(chunks);
            progress.message(MessageFormat.format(LocaleFactory.localizedString("Finalize {0}", "Status"), file.getName()));
            this.assemble(file, collection, status);
            status.setComplete();
            return null;
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
            this.cleanup(collection);
        }
    }

    private Future<Void> submit(final ThreadPool pool, final Path file, final Path collection, final Local local,
                                final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus overall, final int number, final long offset, final long length,
                                final ConnectionCallback callback) {
        log.info("Submit chunk {} of {} to queue with offset {} and length {}", number, file, offset, length);
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .setSegment(true)
                        .setParent(overall)
                        .setOffset(offset)
                        .setLength(length);
                status.setPart(number);
                // Chunks are sorted by name when assembled
                final Path chunk = new Path(collection, String.format("%05d", number), EnumSet.of(Path.Type.file));
                NextcloudChunkedUploadFeature.super.upload(new DAVWriteFeature(session), chunk, local, throttle,
                        ProgressListener.noop, counter, status, overall, status, callback);
                return null;
            }
        }, overall, counter));
    }

    private void assemble(final Path file, final Path collection, final TransferStatus status) throws IOException {
        final Map<String, String> headers = new HashMap<>();
        headers.put("OC-Total-Length", String.valueOf(status.getLength()));
        if(null != status.getModified()) {
            headers.put("X-OC-Mtime", String.valueOf(status.getModified() / 1000));
        }
        if(null != status.getCreated()) {
            headers.put("X-OC-CTime", String.valueOf(status.getCreated() / 1000));
        }
        final Checksum checksum = status.getChecksum();
        if(Checksum.NONE != checksum) {
            switch(checksum.algorithm) {
                case sha1:
                case md5:
                    headers.put("OC-Checksum", String.format("%s:%s", checksum.algorithm, checksum.hash));
                    break;
            }
        }
        final String target = String.format("%s%s", new HostUrlProvider().withUsername(false).get(session.getHost()),
                new DAVPathEncoder(session).encode(file));
        final String source = String.format("%s/.file", new DAVPathEncoder(session).encode(collection));
        log.debug("Assemble {} to {} with headers {}", source, target, headers);
        session.getClient().move(source, target, true, headers);
    }

    private void cleanup(final Path collection) {
        try {
            session.getClient().delete(new DAVPathEncoder(session).encode(collection));
        }
        catch(IOException e) {
            // Collection is removed by the server when assembled
            log.debug("Failure removing upload collection {}: {}", collection, e.getMessage());
        }
    }
}
