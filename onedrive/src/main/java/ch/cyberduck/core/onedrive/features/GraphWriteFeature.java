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
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonObject;
import org.nuxeo.onedrive.client.UploadSession;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GraphWriteFeature implements Write<DriveItem.Metadata> {
    private static final Logger log = LogManager.getLogger(GraphWriteFeature.class);

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphWriteFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public StatusOutputStream<DriveItem.Metadata> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                throw new UnsupportedException("Content-Range with unknown file size is not supported");
            }
            final DriveItem folder = session.getItem(file.getParent());
            final DriveItem item;
            if(status.isExists()) {
                item = session.getItem(file);
            }
            else {
                item = new DriveItem(folder, URIEncoder.encode(file.getName()));
            }
            final UploadSession upload = Files.createUploadSession(item);
            final ChunkedOutputStream proxy = new ChunkedOutputStream(upload, file, status);
            final int partsize = new HostPreferences(session.getHost()).getInteger("onedrive.upload.multipart.partsize.minimum")
                    * new HostPreferences(session.getHost()).getInteger("onedrive.upload.multipart.partsize.factor");
            return new HttpResponseOutputStream<DriveItem.Metadata>(new MemorySegementingOutputStream(proxy, partsize), new GraphAttributesFinderFeature(session, fileid), status) {
                @Override
                public DriveItem.Metadata getStatus() {
                    return proxy.getStatus();
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

    private final class ChunkedOutputStream extends OutputStream {
        private final UploadSession upload;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<DriveItem.Metadata> response = new AtomicReference<>();

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
            final String header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), length);
            try {
                new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                    @Override
                    public Void call() throws BackgroundException {
                        try {
                            final OneDriveJsonObject reply = upload.uploadFragment(header, content);
                            if(reply instanceof DriveItem.Metadata) {
                                if(log.isInfoEnabled()) {
                                    log.info("Completed upload for {}", file);
                                }
                                final String id = session.getFileId(((DriveItem.Metadata) reply));
                                fileid.cache(file, id);
                                response.set((DriveItem.Metadata) reply);
                            }
                            else {
                                log.debug("Uploaded fragment {} for file {}", header, file);
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
                    log.warn("Skip double close of stream {}", this);
                    return;
                }
                if(0L == offset) {
                    log.warn("Abort upload session {} with no completed parts", upload);
                    // Use touch feature for empty file upload
                    upload.cancelUpload();
                    new GraphTouchFeature(session, fileid).touch(file, overall);
                }
            }
            catch(BackgroundException e) {
                throw new IOException(e);
            }
            finally {
                close.set(true);
            }
        }

        public DriveItem.Metadata getStatus() {
            return response.get();
        }
    }
}
