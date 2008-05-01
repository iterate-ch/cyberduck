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

import com.apple.cocoa.foundation.NSAttributedString;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDUploadPromptModel extends CDTransferPromptModel {

    public CDUploadPromptModel(CDWindowController c, Transfer transfer) {
        super(c, transfer);
    }

    /**
     * Filtering what files are displayed. Used to
     * decide which files to include in the prompt dialog
     */
    private PathFilter filter;

    protected PathFilter filter() {
        if(null == filter) {
            filter = new PromptFilter() {
                public boolean accept(AbstractPath child) {
                    log.debug("accept:" + child);
                    if(transfer.exists((Path)child)) {
                        return super.accept(child);
                    }
                    return false;
                }
            };
        }
        return filter;
    }

    protected Object objectValueForItem(final Path item, final String identifier) {
        if(null != item) {
            if(identifier.equals(CDTransferPromptModel.WARNING_COLUMN)) {
                if(item.attributes.isFile()) {
                    if(item.getLocal().attributes.getSize() == 0) {
                        return ALERT_ICON;
                    }
                    if(item.attributes.getSize() > item.getLocal().attributes.getSize()) {
                        return ALERT_ICON;
                    }
                }
                return null;
            }
            if(identifier.equals(CDTransferPromptModel.SIZE_COLUMN)) {
                return new NSAttributedString(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
            }
        }
        return super.objectValueForItem(item, identifier);
    }
}