package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SDSDelegatingCopyFeature implements Copy {

    private final SDSSession session;
    private final SDSCopyFeature proxy;
    private final DefaultCopyFeature copy;

    public SDSDelegatingCopyFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSCopyFeature proxy) {
        this.session = session;
        this.proxy = proxy;
        this.copy = new DefaultCopyFeature(session);
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(proxy.isSupported(source, target)) {
            return proxy.copy(source, target, status, callback);
        }
        // File key must be set for new upload
        this.setFileKey(status);
        return copy.copy(source, target, status, callback);
    }

    private void setFileKey(final TransferStatus status) throws BackgroundException {
        // copy between encrypted and unencrypted data room
        final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey());
        final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writer.writeValue(out, fileKey);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        if(proxy.isSupported(source, target)) {
            return proxy.isRecursive(source, target);
        }
        return copy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(proxy.isSupported(source, target)) {
            return true;
        }
        return copy.isSupported(source, target);
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        proxy.withTarget(session);
        return this;
    }
}
