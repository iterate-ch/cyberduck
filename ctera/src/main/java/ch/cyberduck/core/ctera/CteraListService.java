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

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

public class CteraListService extends DAVListService {

    public CteraListService(final CteraSession session) {
        super(session, new CteraAttributesFinderFeature(session));
    }
//        @Override
//        public void preflight(Path file) throws BackgroundException {
//            super.preflight(file);
//            // TODO CTERA-136 add preflight in listing and find?
//        }

    @Override
    protected List<DavResource> list(final Path directory) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(directory), 1, Collections.unmodifiableSet(Stream.concat(
                // N.B. Timestamp feature disabled in CteraSession.getFeature(Timestamp.class)
                Stream.of(new QName(CTERA_NAMESPACE_URI, CTERA_GUID, CTERA_NAMESPACE_PREFIX)),
                allCteraCustomACLQn.stream()
        ).collect(Collectors.toSet())));
    }
}
