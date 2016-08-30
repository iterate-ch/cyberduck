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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DownloadRootPathsNormalizer implements RootPathsNormalizer<List<TransferItem>> {
    private static final Logger log = Logger.getLogger(DownloadRootPathsNormalizer.class);

    @Override
    public List<TransferItem> normalize(final List<TransferItem> roots) {
        final List<TransferItem> normalized = new ArrayList<TransferItem>();
        for(final TransferItem download : roots) {
            boolean duplicate = false;
            for(Iterator<TransferItem> iter = normalized.iterator(); iter.hasNext(); ) {
                TransferItem n = iter.next();
                if(download.remote.isChild(n.remote)) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.remote.isChild(download.remote)) {
                    iter.remove();
                }
                if(download.local.equals(n.local)) {
                    // The selected file has the same name; if downloaded as a root element
                    // it would overwrite the earlier
                    final String parent = download.local.getParent().getAbsolute();
                    //TODO returns \ instead of / on Windows
                    final String filename = download.remote.getName();
                    String proposal;
                    int no = 0;
                    Local local;
                    do {
                        no++;
                        proposal = String.format("%s-%d", FilenameUtils.getBaseName(filename), no);
                        if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                            proposal += "." + FilenameUtils.getExtension(filename);
                        }
                        local = LocalFactory.get(parent, proposal);
                    }
                    while(local.exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed local name from %s to %s", filename, local.getName()));
                    }
                    download.local = local;
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(new TransferItem(download.remote, download.local));
            }
        }
        return normalized;
    }
}
