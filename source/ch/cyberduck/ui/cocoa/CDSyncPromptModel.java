package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

/**
 * @version $Id$
 */
public class CDSyncPromptModel extends CDTransferPromptModel {

    public CDSyncPromptModel(CDWindowController c, Transfer transfer) {
        super(c, transfer);
    }

    /**
     * Filtering what files are displayed. Used to
     * decide which files to include in the prompt dialog
     */
    private PathFilter<Path> filter;

    @Override
    protected PathFilter<Path> filter() {
        if(null == filter) {
            filter = new PromptFilter() {
                @Override
                public boolean accept(Path child) {
                    log.debug("accept:" + child);
                    return super.accept(child);
                }
            };
        }
        return filter;
    }

    /**
     * A column indicating if the file will be uploaded or downloaded
     */
    protected static final String SYNC_COLUMN = "SYNC";
    /**
     * A column indicating if the file is missing and will be created
     */
    protected static final String CREATE_COLUMN = "CREATE";

    @Override
    protected NSObject objectValueForItem(final Path item, final String identifier) {
        final NSObject cached = modelCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(SIZE_COLUMN)) {
                SyncTransfer.Comparison compare = ((SyncTransfer) transfer).compare(item);
                return modelCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(Status.getSizeAsString(
                        compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER) ? item.attributes.getSize() : item.getLocal().attributes.getSize()),
                        CDTableCellAttributes.browserFontRightAlignment()));
            }
            if(identifier.equals(SYNC_COLUMN)) {
                SyncTransfer.Comparison compare = ((SyncTransfer) transfer).compare(item);
                if(compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER)) {
                    return modelCache.put(item, identifier, CDIconCache.iconNamed("arrowDown", 16));
                }
                if(compare.equals(SyncTransfer.COMPARISON_LOCAL_NEWER)) {
                    return modelCache.put(item, identifier, CDIconCache.iconNamed("arrowUp", 16));
                }
                return null;
            }
            if(identifier.equals(WARNING_COLUMN)) {
                if(item.attributes.isFile()) {
                    if(item.exists()) {
                        if(item.attributes.getSize() == 0) {
                            return modelCache.put(item, identifier, ALERT_ICON);
                        }
                    }
                    if(item.getLocal().exists()) {
                        if(item.getLocal().attributes.getSize() == 0) {
                            return modelCache.put(item, identifier, ALERT_ICON);
                        }
                    }
                }
                return null;
            }
            if(identifier.equals(CREATE_COLUMN)) {
                if(!(item.exists() && item.getLocal().exists())) {
                    return modelCache.put(item, identifier, CDIconCache.iconNamed("plus", 16));
                }
                return null;
            }
        }
        return super.objectValueForItem(item, identifier);
    }
}