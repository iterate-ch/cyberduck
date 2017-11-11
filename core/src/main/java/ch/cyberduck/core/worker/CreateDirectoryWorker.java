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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;

public class CreateDirectoryWorker extends Worker<Path> {

    private final Path folder;
    private final String region;

    public CreateDirectoryWorker(final Path folder, final String region) {
        this.folder = folder;
        this.region = region;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        final Directory feature = session.getFeature(Directory.class);
        if(!feature.isSupported(folder.getParent(), folder.getName())) {
            throw new UnsupportedException();
        }
        final TransferStatus status = new TransferStatus();
        final Encryption encryption = session.getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(folder));
        }
        final Redundancy redundancy = session.getFeature(Redundancy.class);
        if(redundancy != null) {
            status.setStorageClass(redundancy.getDefault());
        }
        status.setTimestamp(System.currentTimeMillis());
        return feature.mkdir(folder, region, status);
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
        if(folder != null ? !folder.equals(that.folder) : that.folder != null) {
            return false;
        }
        return !(region != null ? !region.equals(that.region) : that.region != null);

    }

    @Override
    public int hashCode() {
        int result = folder != null ? folder.hashCode() : 0;
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
