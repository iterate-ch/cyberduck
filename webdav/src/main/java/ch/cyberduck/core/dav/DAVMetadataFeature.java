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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;

public class DAVMetadataFeature implements Headers {
    private static final Logger log = Logger.getLogger(DAVMetadataFeature.class);

    private final DAVSession session;

    public DAVMetadataFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return PreferencesFactory.get().getMap("webdav.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().list(new DAVPathEncoder().encode(file));
            for(DavResource resource : resources) {
                return resource.getCustomProps();
            }
            return Collections.emptyMap();
        }
        catch(SardineException e) {
            try {
                throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(InteroperabilityException | NotfoundException i) {
                log.warn(String.format("Failure to obtain attributes of %s. %s", file, i.getDetail()));
                // Workaround for #8902
                return Collections.emptyMap();
            }
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write metadata %s for file %s", metadata, file));
        }
        try {
            session.getClient().setCustomProps(new DAVPathEncoder().encode(file),
                    metadata, Collections.<java.lang.String>emptyList());
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }
}
