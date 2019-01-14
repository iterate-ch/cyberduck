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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.TransferBackgroundActionState;
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
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.B2GetUploadUrlResponse;
import synapticloop.b2.response.B2StartLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2LargeUploadWriteFeature implements MultipartWrite<VersionId> {
    private static final Logger log = Logger.getLogger(B2LargeUploadWriteFeature.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final Find finder;
    private final AttributesFinder attributes;
    private final B2FileidProvider fileid;

    public B2LargeUploadWriteFeature(final B2Session session, final B2FileidProvider fileid) {
        this(session, fileid, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public B2LargeUploadWriteFeature(final B2Session session, final B2FileidProvider fileid, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.fileid = fileid;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public StatusOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final LargeUploadOutputStream proxy = new LargeUploadOutputStream(file, status);
        return new HttpResponseOutputStream<VersionId>(new MemorySegementingOutputStream(proxy,
            PreferencesFactory.get().getInteger("b2.upload.largeobject.size.minimum"))) {
            @Override
            public VersionId getStatus() throws BackgroundException {
                return proxy.getFileId();
            }
        };
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
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

        private VersionId version;
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
                    final B2GetUploadUrlResponse uploadUrl = session.getClient().getUploadUrl(fileid.getFileid(containerService.getContainer(file), new DisabledListProgressListener()));
                    final Checksum checksum = overall.getChecksum();
                    final B2FileResponse response = session.getClient().uploadFile(uploadUrl,
                        containerService.getKey(file),
                        new ByteArrayEntity(content, off, len), Checksum.NONE == checksum ? "do_not_verify" : checksum.hash,
                        overall.getMime(), overall.getMetadata());
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Upload finished for %s with response %s", file, response));
                    }
                    version = new VersionId(response.getFileId());
                }
                else {
                    if(0 == partNumber) {
                        final Map<String, String> fileinfo = new HashMap<>(overall.getMetadata());
                        if(null != overall.getTimestamp()) {
                            fileinfo.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(overall.getTimestamp()));
                        }
                        final B2StartLargeFileResponse response = session.getClient().startLargeFileUpload(fileid.getFileid(containerService.getContainer(file), new DisabledListProgressListener()),
                            containerService.getKey(file), overall.getMime(), fileinfo);
                        version = new VersionId(response.getFileId());
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Multipart upload started for %s with ID %s", file, version));
                        }
                    }
                    final int segment = ++partNumber;
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Write segment %d for upload %s", segment, version));
                    }
                    completed.add(new DefaultRetryCallable<B2UploadPartResponse>(new BackgroundExceptionCallable<B2UploadPartResponse>() {
                        @Override
                        public B2UploadPartResponse call() throws BackgroundException {
                            final TransferStatus status = new TransferStatus().length(len);
                            final ByteArrayEntity entity = new ByteArrayEntity(content, off, len);
                            final Checksum checksum = ChecksumComputeFactory.get(HashAlgorithm.sha1)
                                .compute(new ByteArrayInputStream(content, off, len), status);
                            try {
                                return session.getClient().uploadLargeFilePart(version.id, segment, entity, checksum.hash);
                            }
                            catch(B2ApiException e) {
                                throw new B2ExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                            catch(IOException e) {
                                throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                        }
                    }, new DisabledProgressListener(), new TransferBackgroundActionState(overall)).call());
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
                    if(null == version) {
                        // No single file upload and zero parts
                        version = new VersionId(new B2TouchFeature(session, fileid).touch(file, new TransferStatus()).attributes().getVersionId());
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
                    final B2FinishLargeFileResponse response = session.getClient().finishLargeFileUpload(version.id, checksums.toArray(new String[checksums.size()]));
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Finished large file upload %s with %d parts", file, completed.size()));
                    }
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
            return version;
        }
    }
}
