package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.VersionIdProvider;

import org.apache.commons.lang3.StringUtils;

public class DefaultVersionIdProvider implements VersionIdProvider {

    private final Session<?> session;

    public DefaultVersionIdProvider(final Session<?> session) {
        this.session = session;
    }

    @Override
    public String getVersionId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        return session.getFeature(AttributesFinder.class).find(file).getVersionId();
    }
}
