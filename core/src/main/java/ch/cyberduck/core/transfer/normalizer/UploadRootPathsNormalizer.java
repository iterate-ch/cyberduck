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
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UploadRootPathsNormalizer implements RootPathsNormalizer<List<TransferItem>> {
    private static final Logger log = Logger.getLogger(UploadRootPathsNormalizer.class);

    @Override
    public List<TransferItem> normalize(final List<TransferItem> roots) {
        final List<TransferItem> normalized = new ArrayList<TransferItem>();
        for(TransferItem upload : roots) {
            boolean duplicate = false;
            for(Iterator<TransferItem> iter = normalized.iterator(); iter.hasNext(); ) {
                TransferItem n = iter.next();
                if(upload.local.isChild(n.local)) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.local.isChild(upload.local)) {
                    iter.remove();
                }
                if(upload.remote.equals(n.remote)) {
                    // The selected file has the same name; if uploaded as a root element
                    // it would overwrite the earlier
                    final Path parent = upload.remote.getParent();
                    final String filename = upload.remote.getName();
                    String proposal;
                    int no = 0;
                    int index = filename.lastIndexOf('.');
                    Path remote;
                    do {
                        no++;
                        if(index != -1 && index != 0) {
                            proposal = String.format("%s-%d%s", filename.substring(0, index), no, filename.substring(index));
                        }
                        else {
                            proposal = String.format("%s-%d", filename, no);
                        }
                        remote = new Path(parent, proposal, upload.remote.getType());
                    }
                    while(false);//(upload.exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed name from %s to %s", filename, remote.getName()));
                    }
                    upload.remote = remote;
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(new TransferItem(upload.remote, upload.local));
            }
        }
        return normalized;
    }
}