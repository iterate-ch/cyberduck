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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class SDSDelegatingCopyFeature implements Copy {

    private final SDSSession session;
    private final SDSCopyFeature proxy;

    private final PathContainerService containerService
            = new PathContainerService();

    public SDSDelegatingCopyFeature(final SDSSession session, final SDSCopyFeature proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            new DefaultCopyFeature(session).copy(source, target, status, callback);
        }
        else {
            proxy.copy(source, target, status, callback);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            return new DefaultCopyFeature(session).isRecursive(source, target);
        }
        return proxy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            return new DefaultCopyFeature(session).isSupported(source, target);
        }
        return proxy.isSupported(source, target);
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        proxy.withTarget(session);
        return this;
    }
}
