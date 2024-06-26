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
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;

public class SDSDelegatingMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(SDSDelegatingMoveFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final SDSMoveFeature proxy;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSDelegatingMoveFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSMoveFeature proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public Path move(final Path source, final Path target, final TransferStatus status, final Delete.Callback callback,
                     final ConnectionCallback connectionCallback) throws BackgroundException {
        if(containerService.isContainer(source)) {
            if(new SimplePathPredicate(source.getParent()).test(target.getParent())) {
                // Rename only
                return proxy.move(source, target, status, callback, connectionCallback);
            }
        }
        if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(source) ^ new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(containerService.getContainer(target))) {
            // Moving into or from an encrypted room
            final Copy copy = new SDSDelegatingCopyFeature(session, nodeid, new SDSCopyFeature(session, nodeid));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Move %s to %s using copy feature %s", source, target, copy));
            }
            final Path c = copy.copy(source, target, status, connectionCallback, new DisabledStreamListener());
            // Delete source file after copy is complete
            final Delete delete = new SDSDeleteFeature(session, nodeid);
            if(delete.isSupported(source)) {
                log.warn(String.format("Delete source %s copied to %s", source, target));
                delete.delete(Collections.singletonMap(source, status), connectionCallback, callback);
            }
            return c;
        }
        else {
            return proxy.move(source, target, status, callback, connectionCallback);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        if(SDSAttributesAdapter.isEncrypted(source.attributes()) ^ SDSAttributesAdapter.isEncrypted(containerService.getContainer(target).attributes())) {
            if(session.getFeature(Copy.class).features(source, target).contains(Copy.Flags.recursive)) {
                return EnumSet.of(Flags.recursive);
            }
        }
        return proxy.features(source, target);
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        if(SDSAttributesAdapter.isEncrypted(source.attributes()) ^ SDSAttributesAdapter.isEncrypted(containerService.getContainer(directory).attributes())) {
            session.getFeature(Copy.class).preflight(source, directory, filename);
        }
        else {
            proxy.preflight(source, directory, filename);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SDSDelegatingMoveFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
