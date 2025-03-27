package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVListService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.*;

public class CteraListService extends DAVListService {

    private final CteraSession session;

    public CteraListService(final CteraSession session) {
        super(session, new CteraAttributesFinderFeature(session));
        this.session = session;
    }

    @Override
    protected List<DavResource> propfind(final Path directory) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(directory), 1, Collections.unmodifiableSet(Stream.concat(Stream.concat(
                Stream.of(GUID_QN), ALL_ACL_QN.stream()), Stream.of(FILEID_QN)
        ).collect(Collectors.toSet())));
    }

    @Override
    public void preflight(final Path directory) throws BackgroundException {
        assumeRole(directory, READPERMISSION);
    }
}
