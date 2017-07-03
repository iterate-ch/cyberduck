package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;

public class SDSNodeIdProvider implements IdProvider {

    private final SDSSession session;

    private static final String ROOT_NODE_ID = "0";

    public SDSNodeIdProvider(final SDSSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        if(file.isRoot()) {
            return ROOT_NODE_ID;
        }
        final String parentID = this.getFileid(file.getParent(), new DisabledListProgressListener());
        final String found = session.find(parentID, file.getName());
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return found;
    }

    @Override
    public IdProvider withCache(final Cache<Path> cache) {
        return this;
    }
}
