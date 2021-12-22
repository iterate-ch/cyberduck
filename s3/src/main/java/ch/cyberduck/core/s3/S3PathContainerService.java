package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

public class S3PathContainerService extends DirectoryDelimiterPathContainerService {

    private final Host host;

    public S3PathContainerService(final Host host) {
        this.host = host;
    }

    @Override
    public boolean isContainer(final Path file) {
        if(StringUtils.isEmpty(RequestEntityRestStorageService.findBucketInHostname(host))) {
            return super.isContainer(file);
        }
        return false;
    }
}
