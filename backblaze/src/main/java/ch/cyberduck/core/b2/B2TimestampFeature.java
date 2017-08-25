package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTimestampFeature;

import java.util.Map;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2TimestampFeature extends DefaultTimestampFeature implements Timestamp {

    private final B2Session session;

    public B2TimestampFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        final Map<String, String> metadata = PreferencesFactory.get().getMap("b2.metadata.default");
        metadata.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(modified));
        new B2MetadataFeature(session).setMetadata(file, metadata);
    }
}

