package ch.cyberduck.core.transfer.normalizer;

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

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CopyRootPathsNormalizer implements RootPathsNormalizer<Map<Path, Path>> {
    private static final Logger log = Logger.getLogger(CopyRootPathsNormalizer.class);

    /**
     * Prunes the map of selected files. Files which are a child of an already included directory
     * are removed from the returned map.
     */
    @Override
    public Map<Path, Path> normalize(final Map<Path, Path> files) {
        final Map<Path, Path> normalized = new HashMap<Path, Path>();
        Iterator<Path> sourcesIter = files.keySet().iterator();
        Iterator<Path> destinationsIter = files.values().iterator();
        while(sourcesIter.hasNext()) {
            Path source = sourcesIter.next();
            Path destination = destinationsIter.next();
            boolean duplicate = false;
            for(Iterator<Path> normalizedIter = normalized.keySet().iterator(); normalizedIter.hasNext(); ) {
                Path n = normalizedIter.next();
                if(source.isChild(n)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Remove path %s already included by directory", source.getAbsolute()));
                    }
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.isChild(source)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Remove path %s already included by directory", n.getAbsolute()));
                    }
                    // Remove the previously added file as it is a child
                    // of the currently evaluated file
                    normalizedIter.remove();
                }
            }
            if(!duplicate) {
                normalized.put(source, destination);
            }
        }
        return normalized;
    }
}
