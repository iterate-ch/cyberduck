package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

public class DAVListService implements ListService {
    private static final Logger log = LogManager.getLogger(DAVListService.class);

    private final DAVSession session;
    private final DAVAttributesFinderFeature attributes;

    public DAVListService(final DAVSession session) {
        this(session, new DAVAttributesFinderFeature(session));
    }

    public DAVListService(final DAVSession session, final DAVAttributesFinderFeature attributes) {
        this.session = session;
        this.attributes = attributes;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            for(List<DavResource> list : ListUtils.partition(this.list(directory),
                    new HostPreferences(session.getHost()).getInteger("webdav.listing.chunksize"))) {
                for(final DavResource resource : list) {
                    if(new SimplePathPredicate(new Path(resource.getHref().getPath(), EnumSet.of(Path.Type.directory))).test(directory)) {
                        log.warn("Ignore resource {}", resource);
                        // Do not include self
                        if(resource.isDirectory()) {
                            continue;
                        }
                        throw new NotfoundException(directory.getAbsolute());
                    }
                    final PathAttributes attr = attributes.toAttributes(resource);
                    final Path file = new Path(directory, PathNormalizer.name(resource.getHref().getPath()),
                            resource.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attr);
                    children.add(file);
                    listener.chunk(directory, children);
                }
            }
            return children;
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, directory);
        }
    }

    protected List<DavResource> list(final Path directory) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(directory), 1,
                Stream.of(
                                DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                                DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).
                        collect(Collectors.toSet()));
    }
}
