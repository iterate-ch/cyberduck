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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpExceptionMappingService;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

public class DAVListService implements ListService {

    private DAVSession session;

    public DAVListService(final DAVSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final List<DavResource> resources = session.getClient().list(new DAVPathEncoder().encode(directory));
            for(final DavResource resource : resources) {
                // Try to parse as RFC 2396
                final String href = PathNormalizer.normalize(resource.getHref().getPath(), true);
                if(href.equals(directory.getAbsolute())) {
                    continue;
                }
                final PathAttributes attributes = new PathAttributes();
                if(resource.getModified() != null) {
                    attributes.setModificationDate(resource.getModified().getTime());
                }
                if(resource.getCreation() != null) {
                    attributes.setCreationDate(resource.getCreation().getTime());
                }
                if(resource.getContentLength() != null) {
                    attributes.setSize(resource.getContentLength());
                }
                if(StringUtils.isNotBlank(resource.getEtag())) {
                    attributes.setETag(resource.getEtag());
                    // Setting checksum is disabled. See #8798
                    // attributes.setChecksum(Checksum.parse(resource.getEtag()));
                }
                final Path file = new Path(directory, PathNormalizer.name(href),
                        resource.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file),
                        attributes);
                if(StringUtils.isNotBlank(resource.getDisplayName())) {
                    attributes.setDisplayname(resource.getDisplayName());
                }
                children.add(file);
                listener.chunk(directory, children);
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
}
