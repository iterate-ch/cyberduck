package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

public class NextcloudTimestampFeature extends DAVTimestampFeature {

    public NextcloudTimestampFeature(final NextcloudSession session) {
        super(session);
    }

    @Override
    protected Map<String, String> getCustomHeaders(final Path file, final TransferStatus status) {
        final Map<String, String> headers = super.getCustomHeaders(file, status);
        if(null != status.getTimestamp()) {
            headers.put("X-OC-Mtime", String.valueOf(status.getTimestamp()));
        }
        return headers;
    }
}
