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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.SegmentingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.AbstractRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;

public class B2LargeUploadWriteFeature implements MultipartWrite<List<B2UploadPartResponse>> {
    private static final Logger log = Logger.getLogger(B2LargeUploadWriteFeature.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;
    private final Find finder;
    private final AttributesFinder attributes;

    public B2LargeUploadWriteFeature(final B2Session session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public B2LargeUploadWriteFeature(final B2Session session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public StatusOutputStream<List<B2UploadPartResponse>> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final String fileid = session.getClient().startLargeFileUpload(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                    containerService.getKey(file), status.getMime(), status.getMetadata()).getFileId();
            // Save file id for use in part referencing this
            file.attributes().setVersionId(fileid);
            final LargeUploadOutputStream proxy = new LargeUploadOutputStream(file, fileid, status);
            return new HttpResponseOutputStream<List<B2UploadPartResponse>>(new SegmentingOutputStream(proxy,
                    PreferencesFactory.get().getInteger("b2.upload.largeobject.size.minimum"))) {
                @Override
                public List<B2UploadPartResponse> getStatus() throws BackgroundException {
                    return proxy.getCompleted();
                }
            };
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
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

    @Override
    public ChecksumCompute checksum() {
        return ChecksumComputeFactory.get(HashAlgorithm.sha1);
    }

    private final class LargeUploadOutputStream extends OutputStream {
        final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
        private final Path file;
        private final String fileid;
        private final TransferStatus status;
        private int partNumber;

        public LargeUploadOutputStream(final Path file, final String fileid, final TransferStatus status) {
            this.file = file;
            this.fileid = fileid;
            this.status = status;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] content, final int off, final int len) throws IOException {
            try {
                completed.add(new AbstractRetryCallable<B2UploadPartResponse>() {
                    @Override
                    public B2UploadPartResponse call() throws BackgroundException {
                        try {
                            final TransferStatus status = new TransferStatus().length(len);
                            final ByteArrayEntity entity = new ByteArrayEntity(content, off, len);
                            final Checksum checksum = B2LargeUploadWriteFeature.this.checksum()
                                    .compute(new ByteArrayInputStream(content, off, len), status);
                            try {
                                return session.getClient().uploadLargeFilePart(fileid, ++partNumber, entity, checksum.hash);
                            }
                            catch(B2ApiException e) {
                                throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
                            }
                            catch(IOException e) {
                                throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                            }
                        }
                        catch(BackgroundException e) {
                            if(this.retry(e, new DisabledProgressListener(), status)) {
                                return this.call();
                            }
                            else {
                                throw e;
                            }
                        }
                    }
                }.call());
            }
            catch(Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
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
                final B2FinishLargeFileResponse response = session.getClient().finishLargeFileUpload(fileid, checksums.toArray(new String[checksums.size()]));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Finished large file upload %s with %d parts", file, completed.size()));
                }
                // Mark parent status as complete
                status.setComplete();
            }
            catch(B2ApiException e) {
                throw new IOException(new B2ExceptionMappingService(session).map("Upload {0} failed", e, file));
            }
        }

        public List<B2UploadPartResponse> getCompleted() {
            return completed;
        }
    }
}
