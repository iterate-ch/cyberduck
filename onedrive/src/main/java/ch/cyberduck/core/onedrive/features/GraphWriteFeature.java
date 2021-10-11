package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonObject;
import org.nuxeo.onedrive.client.UploadSession;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class GraphWriteFeature implements Write<Void> {
    private static final Logger log = Logger.getLogger(GraphWriteFeature.class);

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphWriteFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DriveItem folder = session.getItem(file.getParent());
            final DriveItem oneDriveFile = new DriveItem(folder, URIEncoder.encode(file.getName()));
            final UploadSession upload = Files.createUploadSession(oneDriveFile);
            final ChunkedOutputStream proxy = new ChunkedOutputStream(upload, file, status);
            final int partsize = new HostPreferences(session.getHost()).getInteger("onedrive.upload.multipart.partsize.minimum")
                * new HostPreferences(session.getHost()).getInteger("onedrive.upload.multipart.partsize.factor");
            return new HttpResponseOutputStream<Void>(new MemorySegementingOutputStream(proxy, partsize)) {
                @Override
                public Void getStatus() {
                    return null;
                }
            };
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean temporary() {
        return false;
    }

    private final class ChunkedOutputStream extends OutputStream {
        private final UploadSession upload;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();

        private Long offset = 0L;
        private final Long length;

        public ChunkedOutputStream(final UploadSession upload, final Path file, final TransferStatus status) {
            this.upload = upload;
            this.file = file;
            this.overall = status;
            this.length = status.getOffset() + status.getLength();
        }

        @Override
        public void write(final int b) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            final byte[] content = Arrays.copyOfRange(b, off, len);
            final HttpRange range = HttpRange.byLength(offset, content.length);
            final String header;
            if(overall.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                header = String.format("%d-%d/*", range.getStart(), range.getEnd());
            }
            else {
                header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), length);
            }
            try {
                new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                    @Override
                    public Void call() throws BackgroundException {
                        try {
                            final OneDriveJsonObject response = upload.uploadFragment(header, content);
                            if(response instanceof DriveItem.Metadata) {
                                log.info(String.format("Completed upload for %s", file));
                                final String id = session.getFileId(((DriveItem.Metadata) response));
                                fileid.cache(file, id);
                            }
                            else {
                                log.debug(String.format("Uploaded fragment %s for file %s", header, file));
                            }
                        }
                        catch(OneDriveAPIException e) {
                            throw new GraphExceptionMappingService(fileid).map("Upload {0} failed", e, file);
                        }
                        catch(IOException e) {
                            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                        }
                        return null;
                    }
                }, overall).call();
            }
            catch(BackgroundException e) {
                throw new IOException(e.getMessage(), e);
            }
            offset += content.length;
        }

        @Override
        public void close() throws IOException {
            try {
                if(close.get()) {
                    log.warn(String.format("Skip double close of stream %s", this));
                    return;
                }
                if(0L == offset) {
                    log.warn(String.format("Abort upload session %s with no completed parts", upload));
                    // Use touch feature for empty file upload
                    upload.cancelUpload();
                    new GraphTouchFeature(session, fileid).touch(file, new TransferStatus());
                }
            }
            catch(BackgroundException e) {
                throw new IOException(e);
            }
            finally {
                close.set(true);
            }
        }
    }
}
