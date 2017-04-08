package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveJsonResponse;
import org.nuxeo.onedrive.client.OneDriveRequest;
import org.nuxeo.onedrive.client.OneDriveResponse;

import java.io.IOException;
import java.net.URL;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class OneDriveWriteFeature extends AbstractHttpWriteFeature<Void> {

    private final OneDriveSession session;

    public OneDriveWriteFeature(final OneDriveSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(file.isRoot() || file.getParent().isRoot()) {
            throw new BackgroundException("Cannot create file here", "Create file in container");
        }
        OneDriveFile oneDriveFile = session.getFile(file);

        try {
            OneDriveUploadSession uploadSession = oneDriveFile.getUploadSession();

            // TODO Continue with uploadSession.getUploadUrl. See https://dev.onedrive.com/items/upload_large_files.htm
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }

        return null; // TODO
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
        return new DisabledChecksumCompute();
    }
}
