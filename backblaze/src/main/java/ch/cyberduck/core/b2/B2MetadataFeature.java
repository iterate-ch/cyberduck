package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

public class B2MetadataFeature implements Headers {

    public static final String X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS = "src_last_modified_millis";
    public static final String X_BZ_INFO_LARGE_FILE_SHA1 = "large_file_sha1";

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2MetadataFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Map<String, String> getDefault(final Local file) {
        return new HostPreferences(session.getHost()).getMap("b2.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        return new B2AttributesFinderFeature(session, fileid).find(file).getMetadata();
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) {
        // Only in file upload
    }
}

