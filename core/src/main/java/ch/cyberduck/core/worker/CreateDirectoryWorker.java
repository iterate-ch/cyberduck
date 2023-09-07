package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;

public class CreateDirectoryWorker extends Worker<Path> {
    private static final Logger log = LogManager.getLogger(CreateDirectoryWorker.class);

    private final Path folder;
    private final String region;

    public CreateDirectoryWorker(final Path folder, final String region) {
        this.folder = folder;
        this.region = region;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        final Directory feature = session.getFeature(Directory.class);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run with feature %s", feature));
        }
        final TransferStatus status = new TransferStatus().withLength(0L);
        final Encryption encryption = session.getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(folder));
        }
        status.setModified(System.currentTimeMillis());
        if(PreferencesFactory.get().getBoolean("touch.permissions.change")) {
            final UnixPermission permission = session.getFeature(UnixPermission.class);
            if(permission != null) {
                status.setPermission(permission.getDefault(EnumSet.of(Path.Type.directory)));
            }
            final AclPermission acl = session.getFeature(AclPermission.class);
            if(acl != null) {
                status.setAcl(acl.getDefault(EnumSet.of(Path.Type.directory)));
            }
        }
        status.setRegion(region);
        return feature.mkdir(folder, status);
    }

    @Override
    public Path initialize() {
        return folder;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
            folder.getName());
    }


    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final CreateDirectoryWorker that = (CreateDirectoryWorker) o;
        if(!new SimplePathPredicate(folder).test(that.folder)) {
            return false;
        }
        return Objects.equals(region, that.region);

    }

    @Override
    public int hashCode() {
        int result = folder != null ? new SimplePathPredicate(folder).hashCode() : 0;
        result = 31 * result + (region != null ? region.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateDirectoryWorker{");
        sb.append("folder=").append(folder);
        sb.append(", region='").append(region).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
