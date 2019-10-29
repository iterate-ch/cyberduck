package ch.cyberduck.core.dav.microsoft;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MicrosoftIISDAVFindFeature extends DAVFindFeature {

    private final Set<Header> headers = new HashSet<>(
        // Request the source of the URI not the processed resource
        // https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wdvse/501879f9-3875-4d7a-ab88-3cecab440034
        // https://docs.oracle.com/cd/E19146-01/821-1828/gczya/index.html
        Collections.singleton(new BasicHeader("Translate", "f")));

    public MicrosoftIISDAVFindFeature(final DAVSession session) {
        super(session);
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            return super.find(file);
        }
        catch(AccessDeniedException e) {
            headers.clear();
            return super.find(file);
        }

    }

    @Override
    public Set<Header> headers() {
        return headers;
    }
}
