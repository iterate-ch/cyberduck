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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.SardineExceptionMappingService;
import ch.cyberduck.core.features.Headers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVHeadersFeature implements Headers {

    private DAVSession session;

    public DAVHeadersFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        if(file.attributes().isFile()) {
            try {
                final List<DavResource> resources = session.getClient().list(URIEncoder.encode(file.getAbsolute()));
                for(DavResource resource : resources) {
                    return resource.getCustomProps();
                }
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot read file attributes", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        if(file.attributes().isFile()) {
            try {
                session.getClient().setCustomProps(URIEncoder.encode(file.getAbsolute()),
                        metadata, Collections.<java.lang.String>emptyList());
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot write file attributes", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
        }
    }


}
