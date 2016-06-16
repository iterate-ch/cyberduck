package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.drive.model.About;

public class DriveQuotaFeature implements Quota {

    private final DriveSession session;

    public DriveQuotaFeature(final DriveSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final About about = session.getClient().about().get().setFields("user, storageQuota").execute();
            final Long used = null == about.getStorageQuota().getUsage() ? 0L
                    : about.getStorageQuota().getUsage();
            final Long available = null == about.getStorageQuota().getLimit() ? Long.MAX_VALUE
                    : about.getStorageQuota().getLimit() - used;
            return new Space(used, available);
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to read attributes of {0}", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
    }
}
