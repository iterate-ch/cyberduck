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
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.List;

public class NextcloudWriteFeature extends DAVWriteFeature {

    public NextcloudWriteFeature(final DAVSession session) {
        super(session);
    }

    @Override
    protected List<Header> getHeaders(final Path file, final TransferStatus status) throws UnsupportedException {
        final List<Header> headers = super.getHeaders(file, status);
        if(null != status.getModified()) {
            headers.add(new BasicHeader("X-OC-Mtime", String.valueOf(status.getModified() / 1000)));
        }
        return headers;
    }

    @Override
    public boolean timestamp() {
        return true;
    }
}
