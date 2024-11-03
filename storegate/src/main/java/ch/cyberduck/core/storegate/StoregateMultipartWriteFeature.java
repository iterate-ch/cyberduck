package ch.cyberduck.core.storegate;

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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StoregateMultipartWriteFeature implements MultipartWrite<File> {
    private static final Logger log = LogManager.getLogger(StoregateMultipartWriteFeature.class);

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateMultipartWriteFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }

    @Override
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String location = new StoregateWriteFeature(session, fileid).start(file, status);
        final MultipartOutputStream proxy = new MultipartOutputStream(location, file, status);
        return new HttpResponseOutputStream<File>(new MemorySegementingOutputStream(proxy,
                new HostPreferences(session.getHost()).getInteger("storegate.upload.multipart.chunksize")),
                new StoregateAttributesFinderFeature(session, fileid), status) {
            @Override
            public File getStatus() {
                return proxy.getResult();
            }
        };
    }

    private final class MultipartOutputStream extends OutputStream {
        private final String location;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<File> result = new AtomicReference<>();

        private Long offset = 0L;
        private final Long length;

        public MultipartOutputStream(final String location, final Path file, final TransferStatus status) {
            this.location = location;
            this.file = file;
            this.overall = status;
            this.length = status.getOffset() + status.getLength();
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                if(null != canceled.get()) {
                    throw canceled.get();
                }
                final byte[] content = Arrays.copyOfRange(b, off, len);
                new DefaultRetryCallable<Void>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                    @Override
                    public Void call() throws BackgroundException {
                        final StoregateApiClient client = session.getClient();
                        try {
                            final HttpEntity entity = EntityBuilder.create().setBinary(content).build();
                            final HttpPut put = new HttpPut(location);
                            put.setEntity(entity);
                            if(0L != overall.getLength() && 0 != content.length) {
                                final HttpRange range = HttpRange.byLength(offset, content.length);
                                final String header;
                                if(overall.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                                    header = String.format("%d-%d/*", range.getStart(), range.getEnd());
                                }
                                else {
                                    header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), length);
                                }
                                put.addHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s", header));
                            }
                            final HttpResponse response = client.getClient().execute(put);
                            try {
                                switch(response.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        final File metadata = new JSON().getContext(File.class).readValue(
                                                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8), File.class);
                                        result.set(metadata);
                                        fileid.cache(file, metadata.getId());
                                    case HttpStatus.SC_NO_CONTENT:
                                        // Upload complete
                                        offset += content.length;
                                        break;
                                    default:
                                        final ApiException failure = new ApiException(response.getStatusLine().getStatusCode(),
                                                response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                EntityUtils.toString(response.getEntity()));
                                        throw new StoregateExceptionMappingService(fileid).map("Upload {0} failed", failure, file);
                                }
                            }
                            catch(BackgroundException e) {
                                new StoregateWriteFeature(session, fileid).cancel(file, location);
                                canceled.set(e);
                                throw e;
                            }
                            finally {
                                EntityUtils.consume(response.getEntity());
                            }
                        }
                        catch(IOException e) {
                            throw new DefaultIOExceptionMappingService().map(e);
                        }
                        return null; //Void
                    }
                }, overall) {
                    @Override
                    public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
                        if(super.retry(failure, progress, cancel)) {
                            canceled.set(null);
                            return true;
                        }
                        return false;
                    }
                }.call();
            }
            catch(BackgroundException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                if(close.get()) {
                    log.warn("Skip double close of stream {}", this);
                    return;
                }
                if(null != canceled.get()) {
                    log.warn("Skip closing with previous failure {}", canceled.get());
                    return;
                }
                if(TransferStatus.UNKNOWN_LENGTH == overall.getLength() || 0L == overall.getLength()) {
                    final StoregateApiClient client = session.getClient();
                    final HttpPut put = new HttpPut(location);
                    // Use Content-Length = 0 and Content-Range = */Length to query upload status.
                    put.addHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes */%d", 0));
                    final HttpResponse response = client.getClient().execute(put);
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                            case HttpStatus.SC_CREATED:
                                final File metadata = new JSON().getContext(File.class).readValue(
                                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8),
                                        File.class);
                                result.set(metadata);
                                fileid.cache(file, metadata.getId());
                            case HttpStatus.SC_NO_CONTENT:
                                break;
                            default:
                                final ApiException failure = new ApiException(response.getStatusLine().getStatusCode(),
                                        response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                        EntityUtils.toString(response.getEntity()));
                                throw new StoregateExceptionMappingService(fileid).map("Upload {0} failed", failure, file);
                        }
                    }
                    catch(BackgroundException e) {
                        throw new IOException(e);
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                }
            }
            finally {
                close.set(true);
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("id='").append(location).append('\'');
            sb.append(", file=").append(file);
            sb.append('}');
            return sb.toString();
        }

        public File getResult() {
            return result.get();
        }
    }
}
