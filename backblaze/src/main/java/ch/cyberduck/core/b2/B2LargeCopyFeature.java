package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2StartLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;

import static ch.cyberduck.core.b2.B2LargeUploadService.X_BZ_INFO_LARGE_FILE_SHA1;
import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_CREATION_DATE_MILLIS;
import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2LargeCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(B2LargeCopyFeature.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    private final Long partSize;
    private final Integer concurrency;

    public B2LargeCopyFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getLong("b2.copy.largeobject.size"),
                new HostPreferences(session.getHost()).getInteger("b2.upload.largeobject.concurrency"));
    }

    public B2LargeCopyFeature(final B2Session session, final B2VersionIdProvider fileid, final Long partSize, final Integer concurrency) {
        this.session = session;
        this.fileid = fileid;
        this.partSize = partSize;
        this.concurrency = concurrency;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("largeupload", concurrency);
        try {
            final Map<String, String> fileinfo = new HashMap<>(status.getMetadata());
            if(null != status.getModified()) {
                fileinfo.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(status.getModified()));
            }
            if(null != status.getCreated()) {
                fileinfo.put(X_BZ_INFO_SRC_CREATION_DATE_MILLIS, String.valueOf(status.getCreated()));
            }
            final Checksum checksum = status.getChecksum();
            if(Checksum.NONE != checksum) {
                switch(checksum.algorithm) {
                    case sha1:
                        fileinfo.put(X_BZ_INFO_LARGE_FILE_SHA1, status.getChecksum().hash);
                        break;
                }
            }
            final B2StartLargeFileResponse response = session.getClient().startLargeFileUpload(fileid.getVersionId(containerService.getContainer(target)),
                    containerService.getKey(target), status.getMime(), fileinfo);
            final long size = status.getLength();
            // Submit file segments for concurrent upload
            final List<Future<B2UploadPartResponse>> parts = new ArrayList<Future<B2UploadPartResponse>>();
            long remaining = status.getLength();
            long offset = 0;
            final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
            for(int partNumber = 1; remaining > 0; partNumber++) {
                final Long length = Math.min(Math.max((size / B2LargeUploadService.MAXIMUM_UPLOAD_PARTS), partSize), remaining);
                // Submit to queue
                parts.add(this.submit(pool, source, response.getFileId(), status, partNumber, offset, length, callback));
                if(log.isDebugEnabled()) {
                    log.debug("Part {} submitted with size {} and offset {}", partNumber, length, offset);
                }
                remaining -= length;
                offset += length;
            }
            for(Future<B2UploadPartResponse> f : parts) {
                final B2UploadPartResponse part = Interruptibles.await(f);
                completed.add(part);
                listener.sent(part.getContentLength());
            }
            completed.sort(new Comparator<B2UploadPartResponse>() {
                @Override
                public int compare(final B2UploadPartResponse o1, final B2UploadPartResponse o2) {
                    return o1.getPartNumber().compareTo(o2.getPartNumber());
                }
            });
            final List<String> checksums = new ArrayList<String>();
            for(B2UploadPartResponse part : completed) {
                checksums.add(part.getContentSha1());
            }
            session.getClient().finishLargeFileUpload(response.getFileId(), checksums.toArray(new String[checksums.size()]));
            if(log.isInfoEnabled()) {
                log.info("Finished large file upload {} with {} parts", target, completed.size());
            }
            fileid.cache(target, response.getFileId());
            return target.withAttributes(new PathAttributes(source.attributes()).withVersionId(response.getFileId()));
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Cannot copy {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        finally {
            pool.shutdown(false);
        }
    }

    private Future<B2UploadPartResponse> submit(final ThreadPool pool, final Path file, final String largeFileId,
                                                final TransferStatus overall,
                                                final int partNumber, final Long offset, final Long length,
                                                final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info("Submit part {} of {} to queue with offset {} and length {}", partNumber, file, offset, length);
        }
        return pool.execute(new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<B2UploadPartResponse>() {
            @Override
            public B2UploadPartResponse call() throws BackgroundException {
                overall.validate();
                try {
                    HttpRange range = HttpRange.byLength(offset, length);
                    return session.getClient().copyLargePart(fileid.getVersionId(file), largeFileId, partNumber,
                            String.format("bytes=%d-%d", range.getStart(), range.getEnd()));
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(fileid).map("Cannot copy {0}", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }
        }, overall));
    }
}
