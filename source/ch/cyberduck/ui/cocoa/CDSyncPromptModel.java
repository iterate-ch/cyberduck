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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.foundation.NSAttributedString;

import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class CDSyncPromptModel extends CDTransferPromptModel {

    public CDSyncPromptModel(CDWindowController c, Transfer transfer) {
        super(c, transfer);
    }

    protected PathFilter filter() {
        return new PromptFilter() {
            public boolean accept(AbstractPath child) {
                if(child.attributes.isDirectory()) {
                    return true;
                }
                if(!((SyncTransfer)transfer).shouldCreateLocalFiles() && !transfer.exists(((Path)child).getLocal())) {
                    // The local file does not exist but no files should be created
                    return false;
                }
                if(!((SyncTransfer)transfer).shouldCreateRemoteFiles() && !transfer.exists(((Path)child))) {
                    // The remote file does not exist but no files should be created
                    return false;
                }
                if(!SyncTransfer.COMPARISON_EQUAL.equals(((SyncTransfer)transfer).compare((Path)child))) {
                    return super.accept(child);
                }
                return false;
            }
        };
    }

    private List _root = new AttributedList();

    private void _build() {
        for(Iterator iter = transfer.getRoots().iterator(); iter.hasNext(); ) {
            _root.addAll(this.childs((Path)iter.next()));
        }
    }

    public int outlineViewNumberOfChildrenOfItem(final NSOutlineView view, Path item) {
        if (null == item) {
            if(_root.isEmpty()) {
                this._build();
            }
            return _root.size();
        }
        return super.outlineViewNumberOfChildrenOfItem(view, item);
    }

    public Path outlineViewChildOfItem(final NSOutlineView outlineView, int index, Path item) {
        if (null == item) {
            if(_root.isEmpty()) {
                this._build();
            }
            return (Path)_root.get(index);
        }
        return super.outlineViewChildOfItem(outlineView, index, item);
    }

    /**
     * A column indicating if the file will be uploaded or downloaded
     */
    protected static final String SYNC_COLUMN = "SYNC";
    /**
     * A column indicating if the file is missing and will be created
     */
    protected static final String CREATE_COLUMN = "CREATE";

    private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUp16.tiff");
    private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDown16.tiff");
    private static final NSImage PLUS_ICON = NSImage.imageNamed("plus.tiff");

    protected Object objectValueForItem(final Path item, final String identifier) {
        if (identifier.equals(SIZE_COLUMN)) {
            SyncTransfer.Comparison compare = ((SyncTransfer)transfer).compare(item);
            return new NSAttributedString(Status.getSizeAsString(
                    compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER) ? item.attributes.getSize() : item.getLocal().attributes.getSize()),
                    CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
        }
        if(identifier.equals(SYNC_COLUMN)) {
            SyncTransfer.Comparison compare = ((SyncTransfer)transfer).compare(item);
            if(compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER)) {
                return ARROW_DOWN_ICON;
            }
            if(compare.equals(SyncTransfer.COMPARISON_LOCAL_NEWER)) {
                return ARROW_UP_ICON;
            }
            return null;
        }
        if(identifier.equals(WARNING_COLUMN)) {
            if(item.attributes.isFile()) {
                if(transfer.exists(item))
                    if(item.attributes.getSize() == 0)
                        return ALERT_ICON;
                if(transfer.exists(item.getLocal())) {
                    if(item.getLocal().attributes.getSize() == 0)
                        return ALERT_ICON;
                }
            }
            return null;
        }
        if(identifier.equals(CREATE_COLUMN)) {
            if(!(transfer.exists(item) && transfer.exists(item.getLocal()))) {
                return PLUS_ICON;
            }
            return null;
        }
        return super.objectValueForItem(item, identifier);
    }
}