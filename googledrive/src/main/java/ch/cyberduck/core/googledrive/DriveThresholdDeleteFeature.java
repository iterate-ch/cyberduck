package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;

public class DriveThresholdDeleteFeature implements Delete {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveThresholdDeleteFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        if(files.size() == 1) {
            new DriveDeleteFeature(session, fileid).delete(files, prompt, callback);
        }
        else {
            new DriveBatchDeleteFeature(session, fileid).delete(files, prompt, callback);
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(file.isPlaceholder()) {
            // Disable for application/vnd.google-apps
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file)).withFile(file);
        }
        if(file.getType().contains(Path.Type.shared)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file)).withFile(file);
        }
    }
}
