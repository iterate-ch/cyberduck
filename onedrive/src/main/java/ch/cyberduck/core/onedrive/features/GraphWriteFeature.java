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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveUploadSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class GraphWriteFeature implements Write<Void> {
    private static final Logger log = Logger.getLogger(GraphWriteFeature.class);

    private final Preferences preferences
        = PreferencesFactory.get();

    private final GraphSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public GraphWriteFeature(final GraphSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public GraphWriteFeature(final GraphSession session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final OneDriveFolder folder = session.toFolder(file.getParent());
            final OneDriveFile oneDriveFile = new OneDriveFile(session.getClient(), folder,
                URIEncoder.encode(file.getName()), OneDriveItem.ItemIdentifierType.Path);
            final OneDriveUploadSession upload = oneDriveFile.createUploadSession();
            final ChunkedOutputStream proxy = new ChunkedOutputStream(upload, file, new TransferStatus(status));
            return new HttpResponseOutputStream<Void>(new MemorySegementingOutputStream(proxy,
                preferences.getInteger("onedrive.upload.multipart.partsize.minimum"))) {
                @Override
                public Void getStatus() throws BackgroundException {
                    return null;
                }
            };
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Upload {0} failed", e, file);
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

    private final class ChunkedOutputStream extends OutputStream {
        private final OneDriveUploadSession upload;
        private final Path file;
        private final TransferStatus status;
        private final AtomicBoolean close = new AtomicBoolean();

        private Long offset = 0L;

        public ChunkedOutputStream(final OneDriveUploadSession upload, final Path file, final TransferStatus status) {
            this.upload = upload;
            this.file = file;
            this.status = status;
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
            if(status.getLength() == -1L) {
                header = String.format("%d-%d/*", range.getStart(), range.getEnd());
            }
            else {
                header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), status.getOffset() + status.getLength());
            }
            if(upload.uploadFragment(header, content) instanceof OneDriveFile.Metadata) {
                log.info(String.format("Completed upload for %s", file));
            }
            else {
                log.debug(String.format("Uploaded fragment %s for file %s", header, file));
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
                    new GraphTouchFeature(session).touch(file, new TransferStatus());
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
