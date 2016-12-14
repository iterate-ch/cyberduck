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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.UploadSessionAppendV2Uploader;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishUploader;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxWriteFeature extends AbstractHttpWriteFeature<String> {
    private static final Logger log = Logger.getLogger(DropboxWriteFeature.class);

    private static final long DEFAULT_CHUNK_SIZE = 150000000L;

    private final DropboxSession session;

    private final Find finder;

    private final AttributesFinder attributes;

    private final Long chunksize;

    public DropboxWriteFeature(final DropboxSession session) {
        this(session, DEFAULT_CHUNK_SIZE);
    }

    public DropboxWriteFeature(final DropboxSession session, final Long chunksize) {
        this(session, session.getFeature(Find.class, new DefaultFindFeature(session)), session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)), chunksize);
    }

    public DropboxWriteFeature(final DropboxSession session, final Find finder, final AttributesFinder attributes, final Long chunksize) {
        super(finder, attributes);
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
        this.chunksize = chunksize;
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public HttpResponseOutputStream<String> write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final DbxUserFilesRequests files = new DbxUserFilesRequests(session.getClient());
            final UploadSessionStartUploader start = files.uploadSessionStart();
            start.getOutputStream().close();
            final String sessionId = start.finish().getSessionId();
            final UploadSessionAppendV2Uploader uploader = open(files, sessionId, 0L);
            return new SegmentingUploadProxyOutputStream(file, status, files, uploader, sessionId);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(DbxException ex) {
            throw new DropboxExceptionMappingService().map("Upload failed.", ex, file);
        }
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    private final class SegmentingUploadProxyOutputStream extends HttpResponseOutputStream<String> {

        private final Path file;
        private final TransferStatus status;
        private final DbxUserFilesRequests files;
        private final String sessionId;

        private Long offset = 0L;
        private Long written = 0L;
        private UploadSessionAppendV2Uploader uploader;

        public SegmentingUploadProxyOutputStream(final Path file, final TransferStatus status, final DbxUserFilesRequests client,
                                                 final UploadSessionAppendV2Uploader uploader, final String sessionId) throws DbxException {
            super(uploader.getOutputStream());
            this.file = file;
            this.status = status;
            this.files = client;
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
            // Next segment
            uploader = open(files, sessionId, written);
            // Replace stream
            out = uploader.getOutputStream();
            offset = 0L;
        }

        @Override
        protected void afterWrite(final int n) throws IOException {
            offset += n;
            written += n;
        }

        @Override
        public String getStatus() throws BackgroundException {
            return sessionId;
        }

        @Override
        public void close() throws IOException {
            try {
                DropboxWriteFeature.this.close(uploader);
                final UploadSessionFinishUploader finish = files.uploadSessionFinish(new UploadSessionCursor(sessionId, written), CommitInfo.newBuilder(file.getAbsolute())
                        .withClientModified(status.getTimestamp() != null ? new Date(status.getTimestamp()) : null)
                        .withMode(WriteMode.OVERWRITE)
                        .build()
                );
                finish.getOutputStream().close();
                finish.finish();
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
        return files.uploadSessionAppendV2(new UploadSessionCursor(sessionId, offset));
    }

    private void close(final UploadSessionAppendV2Uploader uploader) throws DbxException, IOException {
        uploader.getOutputStream().close();
        uploader.finish();
    }
}
