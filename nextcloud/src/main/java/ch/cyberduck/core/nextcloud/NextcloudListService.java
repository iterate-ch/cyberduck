package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVListService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTimestampFeature;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;

public class NextcloudListService extends DAVListService {

    private final DAVSession session;

    public NextcloudListService(final DAVSession session) {
        this(session, new NextcloudAttributesFinderFeature(session));
    }

    public NextcloudListService(final DAVSession session, final DAVAttributesFinderFeature attributes) {
        super(session, attributes);
        this.session = session;
    }

    @Override
    protected List<DavResource> list(final Path directory) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(directory), 1,
                Stream.of(
                                NextcloudAttributesFinderFeature.OC_FILEID_CUSTOM_NAMESPACE,
                                NextcloudAttributesFinderFeature.OC_CHECKSUMS_CUSTOM_NAMESPACE,
                                NextcloudAttributesFinderFeature.OC_SIZE_CUSTOM_NAMESPACE,
                                DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                                DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).
                        collect(Collectors.toSet()));
    }
}
