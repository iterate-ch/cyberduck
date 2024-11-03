package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;
import synapticloop.b2.response.BaseB2Response;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_CREATION_DATE_MILLIS;
import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2LargeUploadService extends HttpUploadFeature<BaseB2Response, MessageDigest> {
    private static final Logger log = LogManager.getLogger(B2LargeUploadService.class);

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;
    public static final String X_BZ_INFO_LARGE_FILE_SHA1 = "large_file_sha1";

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    private final Long partSize;
    private final Integer concurrency;

    private Write<BaseB2Response> writer;

    public B2LargeUploadService(final B2Session session, final B2VersionIdProvider fileid, final Write<BaseB2Response> writer) {
        this(session, fileid, writer, new HostPreferences(session.getHost()).getLong("b2.upload.largeobject.size"),
                new HostPreferences(session.getHost()).getInteger("b2.upload.largeobject.concurrency"));
    }

    public B2LargeUploadService(final B2Session session, final B2VersionIdProvider fileid, final Write<BaseB2Response> writer, final Long partSize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.fileid = fileid;
        this.writer = writer;
        this.partSize = partSize;
        this.concurrency = concurrency;
    }

    @Override
    public BaseB2Response upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                 final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final long partSize;
        if(file.getType().contains(Path.Type.encrypted)) {
            // For uploads to vault part size must be a multiple of 32 * 1024. Recommended partsize from B2 API may not meet that requirement.
            partSize = PreferencesFactory.get().getLong("b2.upload.largeobject.size");
        }
        else {
            partSize = this.partSize;
        }
        return this.upload(file, local, throttle, listener, status, callback,
                partSize < status.getLength() ? partSize : PreferencesFactory.get().getLong("b2.upload.largeobject.size.minimum"));
    }

    public BaseB2Response upload(final Path file, final Local local,
                                 final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status,
                                 final ConnectionCallback callback, final Long partSize) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("largeupload", concurrency);
        try {
            // Get the results of the uploads in the order they were submitted
            // this is important for building the manifest, and is not a problem in terms of performance
            // because we should only continue when all segments have uploaded successfully
            final List<B2UploadPartResponse> completed = new ArrayList<>();
            final Map<String, String> fileinfo = new HashMap<>(status.getMetadata());
            final Checksum checksum = status.getChecksum();
            if(Checksum.NONE != checksum) {
                switch(checksum.algorithm) {
                    case sha1:
                        fileinfo.put(X_BZ_INFO_LARGE_FILE_SHA1, status.getChecksum().hash);
                        break;
                }
            }
            if(null != status.getModified()) {
                fileinfo.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(status.getModified()));
            }
            if(null != status.getCreated()) {
                fileinfo.put(X_BZ_INFO_SRC_CREATION_DATE_MILLIS, String.valueOf(status.getCreated()));
            }
            final String fileId;
            if(status.isAppend()) {
                // Add already completed parts
                final B2LargeUploadPartService partService = new B2LargeUploadPartService(session, fileid);
                final List<B2FileInfoResponse> uploads = partService.find(file);
                if(uploads.isEmpty()) {
                    fileId = session.getClient().startLargeFileUpload(fileid.getVersionId(containerService.getContainer(file)),
                            containerService.getKey(file), status.getMime(), fileinfo).getFileId();
                }
                else {
                    fileId = uploads.iterator().next().getFileId();
                    completed.addAll(partService.list(fileId));
                }
            }
            else {
                fileId = session.getClient().startLargeFileUpload(fileid.getVersionId(containerService.getContainer(file)),
                        containerService.getKey(file), status.getMime(), fileinfo).getFileId();
            }
            // Full size of file
            final long size = status.getLength() + status.getOffset();
            // Submit file segments for concurrent upload
            final List<Future<B2UploadPartResponse>> parts = new ArrayList<>();
            long remaining = status.getLength();
            long offset = 0;
            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isAppend()) {
                    if(log.isInfoEnabled()) {
                        log.info("Determine if part number {} can be skipped", partNumber);
                    }
                    for(B2UploadPartResponse c : completed) {
                        if(c.getPartNumber().equals(partNumber)) {
                            if(log.isInfoEnabled()) {
                                log.info("Skip completed part number {}", partNumber);
                            }
                            skip = true;
                            offset += c.getContentLength();
                            break;
                        }
                    }
                }
                if(!skip) {
                    final long length = Math.min(Math.max((size / B2LargeUploadService.MAXIMUM_UPLOAD_PARTS), partSize), remaining);
                    // Submit to queue
                    parts.add(this.submit(pool, file, local, throttle, listener, status, fileId, partNumber, offset, length, callback));
                    if(log.isDebugEnabled()) {
                        log.debug("Part {} submitted with size {} and offset {}", partNumber, length, offset);
                    }
                    remaining -= length;
                    offset += length;
                }
            }
            completed.addAll(Interruptibles.awaitAll(parts));
            completed.sort(new Comparator<B2UploadPartResponse>() {
                @Override
                public int compare(final B2UploadPartResponse o1, final B2UploadPartResponse o2) {
                    return o1.getPartNumber().compareTo(o2.getPartNumber());
                }
            });
            final List<String> checksums = new ArrayList<>();
            for(B2UploadPartResponse part : completed) {
                checksums.add(part.getContentSha1());
            }
            final B2FinishLargeFileResponse response = session.getClient().finishLargeFileUpload(fileId, checksums.toArray(new String[checksums.size()]));
            if(log.isInfoEnabled()) {
                log.info("Finished large file upload {} with {} parts", file, completed.size());
            }
            fileid.cache(file, response.getFileId());
            // Mark parent status as complete
            status.withResponse(new B2AttributesFinderFeature(session, fileid).toAttributes(response)).setComplete();
            return response;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
        finally {
            pool.shutdown(false);
        }
    }

    private Future<B2UploadPartResponse> submit(final ThreadPool pool, final Path file, final Local local,
                                                final BandwidthThrottle throttle, final StreamListener listener,
                                                final TransferStatus overall,
                                                final String fileId, final int partNumber,
                                                final Long offset, final Long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info("Submit part {} of {} to queue with offset {} and length {}", partNumber, file, offset, length);
        }
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<B2UploadPartResponse>() {
            @Override
            public B2UploadPartResponse call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .withLength(length)
                        .withOffset(offset);
                final Map<String, String> requestParameters = new HashMap<>();
                requestParameters.put("fileId", fileId);
                status.setParameters(requestParameters);
                status.setHeader(overall.getHeader());
                status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
                status.setSegment(true);
                status.setPart(partNumber);
                return (B2UploadPartResponse) B2LargeUploadService.this.upload(file, local, throttle, counter, status, overall, status, callback);
            }
        }, overall, counter));
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        final B2LargeUploadPartService partService = new B2LargeUploadPartService(session, fileid);
        final List<B2FileInfoResponse> upload = partService.find(file);
        if(!upload.isEmpty()) {
            Long size = 0L;
            for(B2UploadPartResponse completed : partService.list(upload.iterator().next().getFileId())) {
                size += completed.getContentLength();
            }
            return new Write.Append(true).withStatus(status).withOffset(size);
        }
        return new Write.Append(false).withStatus(status);
    }

    @Override
    public Upload<BaseB2Response> withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
