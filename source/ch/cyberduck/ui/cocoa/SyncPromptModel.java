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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.synchronisation.SyncTransfer;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.resources.IconCache;

/**
 * @version $Id$
 */
public class SyncPromptModel extends TransferPromptModel {

    public SyncPromptModel(TransferPromptController c, Transfer transfer) {
        super(c, transfer);
    }

    @Override
    public void add(Path p) throws BackgroundException {
        for(Path child : transfer.children(p)) {
            super.add(child);
        }
    }

    /**
     * Filtering what files are displayed. Used to decide which files to include in the prompt.
     */
    private Filter<Path> filter = new PromptFilter();

    @Override
    protected Filter<Path> filter() {
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
        final NSObject cached = tableViewCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(SIZE_COLUMN)) {
                final Comparison compare = ((SyncTransfer) transfer).compare(item);
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        SizeFormatterFactory.get().format(
                                compare.equals(Comparison.REMOTE_NEWER) ? item.attributes().getSize() : item.getLocal().attributes().getSize()),
                        TableCellAttributes.browserFontRightAlignment()));
            }
            if(identifier.equals(SYNC_COLUMN)) {
                final Comparison compare = ((SyncTransfer) transfer).compare(item);
                if(item.attributes().isDirectory()) {
                    if(transfer.cache().lookup(item.getReference()) != null && item.getLocal().exists()) {
                        return null;
                    }
                }
                if(compare.equals(Comparison.REMOTE_NEWER)) {
                    return tableViewCache.put(item, identifier, IconCache.iconNamed("transfer-download.tiff", 16));
                }
                if(compare.equals(Comparison.LOCAL_NEWER)) {
                    return tableViewCache.put(item, identifier, IconCache.iconNamed("transfer-upload.tiff", 16));
                }
                return null;
            }
            if(identifier.equals(WARNING_COLUMN)) {
                if(item.attributes().isFile()) {
                    if(transfer.cache().lookup(item.getReference()) != null) {
                        if(item.attributes().getSize() == 0) {
                            return tableViewCache.put(item, identifier, IconCache.iconNamed("alert.tiff"));
                        }
                    }
                    if(item.getLocal().exists()) {
                        if(item.getLocal().attributes().getSize() == 0) {
                            return tableViewCache.put(item, identifier, IconCache.iconNamed("alert.tiff"));
                        }
                    }
                }
                return null;
            }
            if(identifier.equals(CREATE_COLUMN)) {
                if(!(transfer.cache().lookup(item.getReference()) != null && item.getLocal().exists())) {
                    return tableViewCache.put(item, identifier, IconCache.iconNamed("plus.tiff", 16));
                }
                return null;
            }
            return super.objectValueForItem(item, identifier);
        }
        return cached;
    }
}