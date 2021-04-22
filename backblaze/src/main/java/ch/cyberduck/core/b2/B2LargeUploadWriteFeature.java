package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.B2GetUploadUrlResponse;
import synapticloop.b2.response.B2StartLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2LargeUploadWriteFeature implements MultipartWrite<VersionId> {
    private static final Logger log = Logger.getLogger(B2LargeUploadWriteFeature.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2LargeUploadWriteFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public StatusOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
        final LargeUploadOutputStream proxy = new LargeUploadOutputStream(file, status);
        return new HttpResponseOutputStream<VersionId>(new MemorySegementingOutputStream(proxy,
            PreferencesFactory.get().getInteger("b2.upload.largeobject.size.minimum"))) {
            @Override
            public VersionId getStatus() {
                return proxy.getFileId();
            }
        };
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    private final class LargeUploadOutputStream extends OutputStream {
        final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();

        private int partNumber;

        public LargeUploadOutputStream(final Path file, final TransferStatus status) {
            this.file = file;
            this.overall = status;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] content, final int off, final int len) throws IOException {
            try {
                if(0 == partNumber && len < PreferencesFactory.get().getInteger("b2.upload.largeobject.size.minimum")) {
                    // Write single upload
                    final B2GetUploadUrlResponse uploadUrl = session.getClient().getUploadUrl(fileid.getVersionId(containerService.getContainer(file), new DisabledListProgressListener()));
                    final Checksum checksum = overall.getChecksum();
                    final B2FileResponse response = session.getClient().uploadFile(uploadUrl,
                        containerService.getKey(file),
                        new ByteArrayEntity(content, off, len),
                        checksum.algorithm == HashAlgorithm.sha1 ? checksum.hash : "do_not_verify",
                        overall.getMime(), overall.getMetadata());
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Upload finished for %s with response %s", file, response));
                    }
                    overall.setVersionId(response.getFileId());
                    close.set(true);
                }
                else {
                    if(0 == partNumber) {
                        final Map<String, String> fileinfo = new HashMap<>(overall.getMetadata());
                        if(null != overall.getTimestamp()) {
                            fileinfo.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(overall.getTimestamp()));
                        }
                        final B2StartLargeFileResponse response = session.getClient().startLargeFileUpload(fileid.getVersionId(containerService.getContainer(file), new DisabledListProgressListener()),
                            containerService.getKey(file), overall.getMime(), fileinfo);
                        overall.setVersionId(response.getFileId());
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Multipart upload started for %s with ID %s", file, response.getFileId()));
                        }
                    }
                    final int segment = ++partNumber;
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Write segment %d for upload %s", segment, overall.getVersionId()));
                    }
                    completed.add(new DefaultRetryCallable<B2UploadPartResponse>(session.getHost(), new BackgroundExceptionCallable<B2UploadPartResponse>() {
                        @Override
                        public B2UploadPartResponse call() throws BackgroundException {
                            final TransferStatus status = new TransferStatus().withLength(len);
                            final ByteArrayEntity entity = new ByteArrayEntity(content, off, len);
                            final Checksum checksum = ChecksumComputeFactory.get(HashAlgorithm.sha1)
                                .compute(new ByteArrayInputStream(content, off, len), status);
                            try {
                                return session.getClient().uploadLargeFilePart(overall.getVersionId(), segment, entity, checksum.hash);
                            }
                            catch(B2ApiException e) {
                                throw new B2ExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                            catch(IOException e) {
                                throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                        }
                    }, overall).call());
                }
            }
            catch(BackgroundException e) {
                throw new IOException(e.getMessage(), e);
            }
            catch(B2ApiException e) {
                throw new IOException(new B2ExceptionMappingService().map("Upload {0} failed", e, file));
            }
        }

        @Override
        public void close() throws IOException {
            try {
                if(close.get()) {
                    log.warn(String.format("Skip double close of stream %s", this));
                    return;
                }
                if(completed.isEmpty()) {
                    if(null == overall.getVersionId()) {
                        // No single file upload and zero parts
                        overall.setVersionId(new B2TouchFeature(session, fileid).touch(file, new TransferStatus()).attributes().getVersionId());
                    }
                    else {
                        // Cancel upload
                        session.getClient().cancelLargeFileUpload(overall.getVersionId());
                    }
                }
                else {
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
                    session.getClient().finishLargeFileUpload(overall.getVersionId(), checksums.toArray(new String[checksums.size()]));
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Finished large file upload %s with %d parts", file, completed.size()));
                    }
                    fileid.cache(file, overall.getVersionId());
                }
            }
            catch(BackgroundException e) {
                throw new IOException(e);
            }
            catch(B2ApiException e) {
                throw new IOException(new B2ExceptionMappingService().map("Upload {0} failed", e, file));
            }
            finally {
                close.set(true);
            }
        }

        public VersionId getFileId() {
            return new VersionId(overall.getVersionId());
        }
    }
}
