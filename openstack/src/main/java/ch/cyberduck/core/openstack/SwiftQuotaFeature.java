package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;

public class SwiftQuotaFeature implements Quota {
    private static final Logger log = Logger.getLogger(SwiftQuotaFeature.class);

    private final SwiftSession session;

    public SwiftQuotaFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        long used = 0L;
        try {
            for(Region region : session.getClient().getRegions()) {
                final long bytes = session.getClient().getAccountInfo(region).getBytesUsed();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Add %d used in region %s", bytes, region));
                }
                used += bytes;
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
        return new Space(used, Long.MAX_VALUE);
    }
}
