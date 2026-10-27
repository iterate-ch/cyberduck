package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Deny uploading files to virtual folders
 */
public class OwncloudWriteFeature<Reply> implements Write<Reply> {

    private final OwncloudPathContainerService containerService;
    private final Write<Reply> proxy;

    public OwncloudWriteFeature(final OwncloudSession session, final Write<Reply> proxy) {
        this.containerService = new OwncloudPathContainerService(session.ocs);
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<Reply> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return proxy.write(file, status, callback);
    }

    @Override
    public boolean random(final Path file) {
        return proxy.random(file);
    }

    @Override
    public boolean timestamp(final Path file) {
        return proxy.timestamp(file);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(containerService.isVirtual(file.getParent())) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                    "Upload {0} failed", "Error"), file.getName())).withFile(file.getParent());
        }
        proxy.preflight(file);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return proxy.features(file);
    }
}
