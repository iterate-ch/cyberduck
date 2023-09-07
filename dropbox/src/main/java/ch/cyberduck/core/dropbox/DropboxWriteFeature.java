package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadSessionAppendV2Uploader;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishUploader;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxWriteFeature extends AbstractHttpWriteFeature<Metadata> {
    private static final Logger log = LogManager.getLogger(DropboxWriteFeature.class);

    private final DropboxSession session;
    private final Long chunksize;
    private final PathContainerService containerService;

    public DropboxWriteFeature(final DropboxSession session) {
        this(session, new HostPreferences(session.getHost()).getLong("dropbox.upload.chunksize"));
    }

    public DropboxWriteFeature(final DropboxSession session, final Long chunksize) {
        super(new DropboxAttributesFinderFeature(session));
        this.session = session;
        this.chunksize = chunksize;
        this.containerService = new DropboxPathContainerService(session);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public HttpResponseOutputStream<Metadata> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DbxUserFilesRequests files = new DbxUserFilesRequests(session.getClient(file));
            final UploadSessionStartUploader start = files.uploadSessionStart();
            new DefaultStreamCloser().close(start.getOutputStream());
            final String sessionId = start.finish().getSessionId();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained session id %s for upload %s", sessionId, file));
            }
            final UploadSessionAppendV2Uploader uploader = open(files, sessionId, 0L);
            return new SegmentingUploadProxyOutputStream(file, status, files, uploader, sessionId);
        }
        catch(DbxException ex) {
            throw new DropboxExceptionMappingService().map("Upload failed.", ex, file);
        }
    }

    @Override
    public boolean timestamp() {
        return true;
    }

    private final class SegmentingUploadProxyOutputStream extends HttpResponseOutputStream<Metadata> {

        private final Path file;
        private final TransferStatus status;
        private final DbxUserFilesRequests client;
        private final String sessionId;

        private final AtomicReference<Metadata> response = new AtomicReference<>();
        private Long offset = 0L;
        private Long written = 0L;
        private UploadSessionAppendV2Uploader uploader;

        public SegmentingUploadProxyOutputStream(final Path file, final TransferStatus status, final DbxUserFilesRequests client,
                                                 final UploadSessionAppendV2Uploader uploader, final String sessionId) {
            super(uploader.getOutputStream(), new DropboxAttributesFinderFeature(session), status);
            this.file = file;
            this.status = status;
            this.client = client;
            this.uploader = uploader;
            this.sessionId = sessionId;
        }

        @Override
        protected void beforeWrite(final int n) throws IOException {
            // A single request should not upload more than 150 MB of file contents.
            if(offset + n > chunksize) {
                try {
                    DropboxWriteFeature.this.close(uploader);
                    this.next();
                }
                catch(DbxException e) {
                    throw new IOException(new DropboxExceptionMappingService().map(e));
                }
            }
        }

        /**
         * Open next chunk
         */
        private void next() throws DbxException {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Open next segment for upload session %s for file %s", sessionId, file));
            }
            // Next segment
            uploader = open(client, sessionId, written);
            // Replace stream
            out = uploader.getOutputStream();
            offset = 0L;
        }

        @Override
        protected void afterWrite(final int n) {
            offset += n;
            written += n;
        }

        @Override
        public Metadata getStatus() {
            return response.get();
        }

        @Override
        public void close() throws IOException {
            try {
                DropboxWriteFeature.this.close(uploader);
                final UploadSessionFinishUploader finish = client.uploadSessionFinish(new UploadSessionCursor(sessionId, written),
                        CommitInfo.newBuilder(containerService.getKey(file))
                                .withClientModified(status.getModified() != null ? new Date(status.getModified()) : null)
                                .withMode(WriteMode.OVERWRITE)
                                .build()
                );
                finish.getOutputStream().close();
                response.set(finish.finish());
            }
            catch(IllegalStateException e) {
                // Already closed
            }
            catch(DbxException e) {
                throw new IOException("Upload failed.", new DropboxExceptionMappingService().map(e));
            }
            finally {
                super.close();
            }
        }
    }

    private UploadSessionAppendV2Uploader open(final DbxUserFilesRequests files, final String sessionId, final Long offset) throws DbxException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open next segment for upload session %s", sessionId));
        }
        return files.uploadSessionAppendV2(new UploadSessionCursor(sessionId, offset));
    }

    private void close(final UploadSessionAppendV2Uploader uploader) throws DbxException, IOException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Close uploader %s", uploader));
        }
        uploader.getOutputStream().close();
        uploader.finish();
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new DropboxChecksumCompute();
    }
}
