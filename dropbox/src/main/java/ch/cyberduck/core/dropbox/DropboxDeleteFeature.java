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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;

public class DropboxDeleteFeature implements Delete {

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxDeleteFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService(session);
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            try {
                callback.delete(file);
                // Delete the file or folder at a given path. If the path is a folder, all its contents will be deleted too.
                if(file.attributes().isDuplicate()) {
                    new DbxUserFilesRequests(session.getClient(file)).permanentlyDelete(containerService.getKey(file),
                            file.attributes().getVersionId());
                }
                else {
                    new DbxUserFilesRequests(session.getClient(file)).deleteV2(containerService.getKey(file));
                }
            }
            catch(DbxException e) {
                throw new DropboxExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(file.attributes().isDuplicate()) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
        }
    }
}
