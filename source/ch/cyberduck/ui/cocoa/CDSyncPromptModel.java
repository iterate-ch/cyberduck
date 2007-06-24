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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.SyncTransfer;
import ch.cyberduck.core.Transfer;

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
                    SyncTransfer.Comparison compare = ((SyncTransfer) transfer).compare((Path) child);
                    if(!SyncTransfer.COMPARISON_EQUAL.equals(compare)) {
                        if(compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER)) {
                            if(((SyncTransfer) transfer).getAction().equals(SyncTransfer.ACTION_UPLOAD)) {
                                return false;
                            }
                            return super.accept(child);
                        }
                        else if(compare.equals(SyncTransfer.COMPARISON_LOCAL_NEWER)) {
                            if(((SyncTransfer) transfer).getAction().equals(SyncTransfer.ACTION_DOWNLOAD)) {
                                return false;
                            }
                            return super.accept(child);
                        }
                    }
                    return child.attributes.isDirectory();
                }
            };
        }
        return filter;
    }

    public void clear() {
        //Hack to make CDTransferPromptModel#childs filter from scratch
        filter = null;
        super.clear();
    }

    public List build() {
        if(_roots.isEmpty()) {
            log.debug("build");
            for(Iterator iter = transfer.getRoots().iterator(); iter.hasNext();) {
                Path next = (Path) iter.next();
                if(this.filter().accept(next)) {
                    _roots.addAll(this.childs(next));
                }
            }
        }
        return _roots;
    }

    public int outlineViewNumberOfChildrenOfItem(final NSOutlineView view, Path item) {
        if(null == item) {
            return this.build().size();
        }
        return super.outlineViewNumberOfChildrenOfItem(view, item);
    }

    public Path outlineViewChildOfItem(final NSOutlineView outlineView, int index, Path item) {
        if(null == item) {
            return (Path) this.build().get(index);
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
        if(identifier.equals(SIZE_COLUMN)) {
            SyncTransfer.Comparison compare = ((SyncTransfer) transfer).compare(item);
            return new NSAttributedString(Status.getSizeAsString(
                    compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER) ? item.attributes.getSize() : item.getLocal().attributes.getSize()),
                    CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
        }
        if(identifier.equals(SYNC_COLUMN)) {
            SyncTransfer.Comparison compare = ((SyncTransfer) transfer).compare(item);
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