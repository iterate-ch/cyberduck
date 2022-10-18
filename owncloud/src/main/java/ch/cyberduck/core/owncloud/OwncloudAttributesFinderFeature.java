package ch.cyberduck.core.owncloud;

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
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.nextcloud.NextcloudAttributesFinderFeature;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;

public class OwncloudAttributesFinderFeature extends NextcloudAttributesFinderFeature {

    private final DAVSession session;

    public OwncloudAttributesFinderFeature(DAVSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected List<DavResource> list(final Path file) throws IOException {
        final String url;
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            url = String.format("%s/%s/v/%s",
                    new OwncloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions).getAbsolute(),
                    file.attributes().getFileId(), file.attributes().getVersionId());
        }
        else {
            url = new DAVPathEncoder().encode(file);
        }
        return session.getClient().list(url, 0,
                Stream.of(FILEID_CUSTOM_NAMESPACE,
                        DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                        DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).collect(Collectors.toSet()));
    }
}
