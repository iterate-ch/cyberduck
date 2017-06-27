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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2FileidProvider implements IdProvider {

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;

    private Cache<Path> cache = PathCache.empty();

    public B2FileidProvider(final B2Session session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        if(containerService.isContainer(file)) {
            final AttributedList<Path> list;
            if(!cache.isCached(file.getParent())) {
                list = new B2ListService(session, this).list(file.getParent(), new DisabledListProgressListener());
                cache.put(file.getParent(), list);
            }
            else {
                list = cache.get(file.getParent());
            }
            final Path found = list.find(new SimplePathPredicate(file));
            if(null == found) {
                throw new NotfoundException(file.getAbsolute());
            }
            return found.attributes().getVersionId();
        }
        try {
            final B2ListFilesResponse response = session.getClient().listFileNames(
                    this.getFileid(containerService.getContainer(file), listener),
                    file.isPlaceholder() ? String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER) : containerService.getKey(file), 2);
            for(B2FileInfoResponse info : response.getFiles()) {
                if(StringUtils.equals(file.isPlaceholder() ? String.format("%s%s", containerService.getKey(file), B2DirectoryFeature.PLACEHOLDER) : containerService.getKey(file), info.getFileName())) {
                    return info.getFileId();
                }
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public IdProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

}
