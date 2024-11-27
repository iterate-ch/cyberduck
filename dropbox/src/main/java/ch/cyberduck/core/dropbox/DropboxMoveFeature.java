package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.RelocationResult;

public class DropboxMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(DropboxMoveFeature.class);

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxMoveFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService();
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                log.warn("Delete file {} to be replaced with {}", renamed, file);
                new DropboxDeleteFeature(session).delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
            }
            final RelocationResult result = new DbxUserFilesRequests(session.getClient(file)).moveV2(containerService.getKey(file), containerService.getKey(renamed));
            return renamed.withAttributes(new DropboxAttributesFinderFeature(session).toAttributes(result.getMetadata()));
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Cannot move {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(!DropboxTouchFeature.validate(target.getName())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), target.getName()));
        }
    }
}
