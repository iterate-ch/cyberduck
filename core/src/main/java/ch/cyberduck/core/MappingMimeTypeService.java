package ch.cyberduck.core;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.utils.Mimetypes;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class MappingMimeTypeService implements MimeTypeService {
    private static final Logger log = LogManager.getLogger(MappingMimeTypeService.class);

    private static final Mimetypes types
        = Mimetypes.getInstance();

    private static final String MIME_FILE = "core.mime.types";

    static {
        try {
            final ClassLoader loader = MappingMimeTypeService.class.getClassLoader();
            final Enumeration<URL> resources = loader.getResources(MIME_FILE);
            if(!resources.hasMoreElements()) {
                log.warn("No file {} in classpath {}", MIME_FILE, loader);
            }
            while(resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                log.info("Loading MIME types from {}", url);
                types.loadAndReplaceMimetypes(url.openStream());
                break;
            }
        }
        catch(IOException e) {
            log.error("Failure loading core.mime.types. {}", e.getMessage());
        }
    }

    @Override
    public String getMime(final String filename) {
        if(StringUtils.startsWith(filename, "._")) {
            return DEFAULT_CONTENT_TYPE;
        }
        // Reads from core.mime.types in classpath
        return types.getMimetype(StringUtils.lowerCase(filename));
    }
}
