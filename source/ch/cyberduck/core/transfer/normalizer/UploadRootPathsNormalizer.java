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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class UploadRootPathsNormalizer implements RootPathsNormalizer<List<Path>> {
    private static final Logger log = Logger.getLogger(UploadRootPathsNormalizer.class);

    @Override
    public List<Path> normalize(final List<Path> roots) {
        final List<Path> normalized = new Collection<Path>();
        for(final Path upload : roots) {
            boolean duplicate = false;
            for(Iterator<Path> iter = normalized.iterator(); iter.hasNext(); ) {
                Path n = iter.next();
                if(upload.getLocal().isChild(n.getLocal())) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.getLocal().isChild(upload.getLocal())) {
                    iter.remove();
                }
                if(upload.equals(n)) {
                    // The selected file has the same name; if uploaded as a root element
                    // it would overwrite the earlier
                    final Path parent = upload.getParent();
                    final String filename = upload.getName();
                    String proposal;
                    int no = 0;
                    int index = filename.lastIndexOf('.');
                    do {
                        no++;
                        if(index != -1 && index != 0) {
                            proposal = String.format("%s-%d%s", filename.substring(0, index), no, filename.substring(index));
                        }
                        else {
                            proposal = String.format("%s-%d", filename, no);
                        }
                        upload.setPath(parent, proposal);
                    }
                    while(false);//(upload.exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed name from %s to %s", filename, upload.getName()));
                    }
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(upload);
            }
        }
        return normalized;
    }
}