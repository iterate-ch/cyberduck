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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVListService implements ListService {

    private DAVSession session;

    public DAVListService(final DAVSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final List<DavResource> resources = session.getClient().list(new DAVPathEncoder().encode(file));
            for(final DavResource resource : resources) {
                // Try to parse as RFC 2396
                final String href = PathNormalizer.normalize(resource.getHref().getPath(), true);
                if(href.equals(file.getAbsolute())) {
                    continue;
                }
                final Path p = new Path(file, PathNormalizer.name(href), resource.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                if(resource.getModified() != null) {
                    p.attributes().setModificationDate(resource.getModified().getTime());
                }
                if(resource.getCreation() != null) {
                    p.attributes().setCreationDate(resource.getCreation().getTime());
                }
                if(resource.getContentLength() != null) {
                    p.attributes().setSize(resource.getContentLength());
                }
                p.attributes().setChecksum(resource.getEtag());
                p.attributes().setETag(resource.getEtag());
                children.add(p);
                listener.chunk(children);
            }
            return children;
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Listing directory failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
