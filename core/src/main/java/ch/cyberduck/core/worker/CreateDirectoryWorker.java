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
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;

public class CreateDirectoryWorker extends Worker<Boolean> {

    private final Path folder;

    private String region;

    public CreateDirectoryWorker(final Path folder, final String region) {
        this.folder = folder;
        this.region = region;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Directory feature = session.getFeature(Directory.class);
        feature.mkdir(folder, region, new TransferStatus());
        return true;
    }

    @Override
    public Boolean initialize() {
        return false;
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
