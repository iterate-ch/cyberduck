package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.Collections;

public class S3MoveFeature implements Move {
    private static final Logger log = Logger.getLogger(S3MoveFeature.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;
    private final S3AccessControlListFeature accessControlListFeature;

    private Delete delete;

    public S3MoveFeature(final S3Session session) {
        this(session, new S3AccessControlListFeature(session));
    }

    public S3MoveFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
        this.delete = new S3DefaultDeleteFeature(session);
    }

    @Override
    public void move(final Path source, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        new S3ThresholdCopyFeature(session, accessControlListFeature).copy(source, renamed, status.length(source.attributes().getSize()));
        delete.delete(Collections.singletonList(source), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source);
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

}