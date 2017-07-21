package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.Collections;

public class SDSDelegatingMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SDSDelegatingMoveFeature.class);

    private final Session<?> session;
    private final Move proxy;

    private final PathContainerService containerService
            = new PathContainerService();

    public SDSDelegatingMoveFeature(final Session<?> session, final Move proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public void move(final Path source, final Path target, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(srcContainer.equals(targetContainer)) {
                proxy.move(source, target, exists, callback);
            }
            else {
                // Moving from or into an encrypted room
                final Copy copy = session.getFeature(Copy.class);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Move %s to %s using copy feature %s", source, target, copy));
                }
                copy.copy(source, target, new TransferStatus().length(source.attributes().getSize()));
                // Delete source file after copy is complete
                final Delete delete = session.getFeature(Delete.class);
                if(delete.isSupported(source)) {
                    delete.delete(Collections.singletonList(source), new DisabledLoginCallback(), callback);
                }
            }
        }
        else {
            proxy.move(source, target, exists, callback);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(!srcContainer.equals(targetContainer)) {
                return session.getFeature(Copy.class).isRecursive(source, target);
            }
        }
        return proxy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(!srcContainer.equals(targetContainer)) {
                return session.getFeature(Copy.class).isSupported(source, target);
            }
        }
        return proxy.isSupported(source, target);
    }

    @Override
    public Move withDelete(final Delete delete) {
        proxy.withDelete(delete);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SDSDelegatingMoveFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
