package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;

import org.apache.commons.lang3.StringUtils;

public class StoregateIdProvider implements IdProvider {

    private final StoregateSession session;

    private Cache<Path> cache = PathCache.empty();

    public StoregateIdProvider(final StoregateSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.find(new SimplePathPredicate(file));
            if(null != found) {
                if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                    return this.set(file, found.attributes().getVersionId());
                }
            }
        }
        try {
            final File f = new FilesApi(session.getClient()).filesGet_1(file.getAbsolute());
            if(null == f) {
                throw new NotfoundException(file.getAbsolute());
            }
            return this.set(file, new StoregateAttributesFinderFeature(session).toAttributes(f).getVersionId());
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map(e);
        }
    }

    protected String set(final Path file, final String id) {
        file.attributes().setVersionId(id);
        return id;
    }

    @Override
    public StoregateIdProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
