package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.shared.DefaultAttributesFeature;

import java.io.IOException;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVAttributesFeature implements Attributes {

    private DAVSession session;

    public DAVAttributesFeature(DAVSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().list(new DAVPathEncoder().encode(file));
            for(final DavResource resource : resources) {
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
                attributes.setChecksum(resource.getEtag());
                attributes.setETag(resource.getEtag());
                return attributes;
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(SardineException e) {
            try {
                throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(InteroperabilityException i) {
                return new DefaultAttributesFeature(session).find(file);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public Attributes withCache(final Cache<Path> cache) {
        return this;
    }
}