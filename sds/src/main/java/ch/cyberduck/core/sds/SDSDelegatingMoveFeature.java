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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;

public class SDSDelegatingMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SDSDelegatingMoveFeature.class);

    private final SDSSession session;
    private final SDSMoveFeature proxy;

    private final PathContainerService containerService
        = new PathContainerService();

    public SDSDelegatingMoveFeature(final SDSSession session, final SDSMoveFeature proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public Path move(final Path source, final Path target, final TransferStatus status, final Delete.Callback callback,
                     final ConnectionCallback connectionCallback) throws BackgroundException {
        if(containerService.isContainer(source)) {
            if(new SimplePathPredicate(source.getParent()).test(target.getParent())) {
                return proxy.move(source, target, status, callback, connectionCallback);
            }
        }
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(StringUtils.equals(srcContainer.getName(), targetContainer.getName())) {
                return proxy.move(source, target, status, callback, connectionCallback);
            }
            else {
                // Moving into or from an encrypted room
                final Copy copy = session.getFeature(Copy.class);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Move %s to %s using copy feature %s", source, target, copy));
                }
                final Path c = copy.copy(source, target, status, connectionCallback);
                // Delete source file after copy is complete
                final Delete delete = session.getFeature(Delete.class);
                if(delete.isSupported(source)) {
                    delete.delete(Collections.singletonList(source), connectionCallback, callback);
                }
                return c;
            }
        }
        else {
            return proxy.move(source, target, status, callback, connectionCallback);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            if(new SimplePathPredicate(source.getParent()).test(target.getParent())) {
                return proxy.isRecursive(source, target);
            }
        }
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(!StringUtils.equals(srcContainer.getName(), targetContainer.getName())) {
                return session.getFeature(Copy.class).isRecursive(source, target);
            }
        }
        return proxy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            if(new SimplePathPredicate(source.getParent()).test(target.getParent())) {
                return proxy.isSupported(source, target);
            }
        }
        final Path srcContainer = containerService.getContainer(source);
        final Path targetContainer = containerService.getContainer(target);
        if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
            if(!StringUtils.equals(srcContainer.getName(), targetContainer.getName())) {
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
