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
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.Transfer;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class CDTransferPromptModel extends NSObject {
    private static Logger log = Logger.getLogger(CDTransferPromptModel.class);

    /**
     *
     */
    protected final Transfer transfer;

    /**
     * The root nodes to be included in the prompt dialog
     */
    private final Collection roots;

    /**
     *
     */
    private final PathFilter filter;

    private CDWindowController controller;

    /**
     *
     * @param c The parent window to attach the prompt
     * @param transfer
     */
    public CDTransferPromptModel(CDWindowController c, final Transfer transfer) {
        this.controller = c;
        this.transfer = transfer;
        this.roots = new Collection();
        // Only include the root nodes the user should see in the prompt (need a conflict resolution)
        this.filter = this.filter();
        for(Iterator iter = transfer.getRoots().iterator(); iter.hasNext(); ) {
            AbstractPath next = (AbstractPath) iter.next();
            if(filter.accept(next)) {
                roots.add(next);
            }
        }
    }

    protected abstract class PromptFilter implements PathFilter {
        public boolean accept(AbstractPath file) {
            if(transfer.exists(file)) {
                if(file.attributes.getSize() == -1) {
                    ((Path)file).readSize();
                }
                if(file.attributes.getModificationDate() == -1) {
                    ((Path)file).readTimestamp();
                }
                if(file.attributes.getPermission() == null) {
                    ((Path)file).readPermission();
                }
            }
            return true;
        }
    }

    protected static final String INCLUDE_COLUMN = "INCLUDE";
    protected static final String WARNING_COLUMN = "WARNING";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    /**
     * @see com.apple.cocoa.application.NSTableView.DataSource
     */
    public void outlineViewSetObjectValueForItem(final NSOutlineView outlineView, Object value,
                                                 final NSTableColumn tableColumn, Path item)
    {
        String identifier = (String) tableColumn.identifier();
        if(identifier.equals(INCLUDE_COLUMN)) {
            item.status.setSkipped(((Number) value).intValue() == NSCell.OffState);
        }
    }

    /**
     * The filter to apply to the file listing in the prompt dialog
     * @return
     */
    protected abstract PathFilter filter();

    /**
     * Container for all paths currently being listed in the background
     */
    private final List isLoadingListingInBackground = new Collection();

    /**
     * If no cached listing is available the loading is delayed until the listing is
     * fetched from a background thread
     * @param parent
     * @param filter
     * @return The list of child items for the parent folder. The listing is filtered
     * using the standard regex exclusion and the additional passed filter
     */
    protected List childs(final Path parent) {
        synchronized(isLoadingListingInBackground) {
            if(!isLoadingListingInBackground.contains(parent)) {
                if(!transfer.isCached(parent)) {
                    isLoadingListingInBackground.add(parent);
                    controller.background(new BackgroundActionImpl(controller) {
                        public void run() {
                            try {
                                transfer.childs(parent);
                            }
                            finally {
                                synchronized(isLoadingListingInBackground) {
                                    isLoadingListingInBackground.remove(parent);
                                }
                            }
                        }

                        public void cleanup() {
                            synchronized(isLoadingListingInBackground) {
                                if(transfer.isCached(parent) && isLoadingListingInBackground.isEmpty()) {
                                    ((CDTransferPrompt)controller).reloadData();
                                }
                            }
                        }
                    }, isLoadingListingInBackground);
                }
                else {
                    final List childs = transfer.childs(parent);
                    for(Iterator iter = childs.iterator(); iter.hasNext(); ) {
                        if(!filter.accept((AbstractPath)iter.next())) {
                            iter.remove();
                        }
                    }
                    return childs;
                }
            }
        }
        log.warn("No cached listing for " + parent.getName());
        return Collections.EMPTY_LIST;
    }

    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage ALERT_ICON = NSImage.imageNamed("alert.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    protected Object objectValueForItem(final Path item, final String identifier) {
        if(identifier.equals(INCLUDE_COLUMN)) {
            if(item.status.isSkipped()) {
                return new Integer(NSCell.OffState);
            }
            return new Integer(NSCell.OnState);
        }
        if(identifier.equals(FILENAME_COLUMN)) {
            return new NSAttributedString(item.getName(),
                    CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
        }
        if(identifier.equals(TYPEAHEAD_COLUMN)) {
            return item.getName();
        }
        log.warn("objectValueForItem:" + item + "," + identifier);
        return null;
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean outlineViewIsItemExpandable(final NSOutlineView view, final Path item) {
        if (null == item) {
            return false;
        }
        return item.attributes.isDirectory();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public int outlineViewNumberOfChildrenOfItem(final NSOutlineView view, Path item) {
        if (null == item) {
            return roots.size();
        }
        return this.childs(item).size();
    }

    /**
     * @see NSOutlineView.DataSource
     * Invoked by outlineView, and returns the child item at the specified index. Children
     * of a given parent item are accessed sequentially. If item is null, this method should
     * return the appropriate child item of the root object
     */
    public Path outlineViewChildOfItem(final NSOutlineView outlineView, int index, Path item) {
        if (null == item) {
            return (Path) roots.get(index);
        }
        List childs = this.childs(item);
        if (index < childs.size()) {
            return (Path) childs.get(index);
        }
        log.warn("outlineViewChildOfItem: Index "+index+" out of bounds for "+item);
        return null;
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public Object outlineViewObjectValueForItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, Path item) {
        return this.objectValueForItem(item, (String) tableColumn.identifier());
    }
}