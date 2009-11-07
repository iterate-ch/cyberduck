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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSOutlineView;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class CDTransferPromptModel extends CDOutlineDataSource {
    protected static Logger log = Logger.getLogger(CDTransferPromptModel.class);

    /**
     *
     */
    protected final Transfer transfer;

    /**
     * The root nodes to be included in the prompt dialog
     */
    protected final List<Path> roots = new Collection<Path>();

    /**
     *
     */
    private CDTransferPrompt controller;

    /**
     * @param c        The parent window to attach the prompt
     * @param transfer
     */
    public CDTransferPromptModel(CDTransferPrompt c, final Transfer transfer) {
        this.controller = c;
        this.transfer = transfer;
    }

    @Override
    protected void invalidate() {
        tableViewCache.clear();
        cache.clear();
        super.invalidate();
    }

    public void add(Path p) {
        roots.add(p);
    }

    protected abstract static class PromptFilter implements PathFilter<Path> {
        public boolean accept(Path file) {
            if(file.exists()) {
                if(file.attributes.getSize() == -1) {
                    file.readSize();
                }
                if(file.attributes.getModificationDate() == -1) {
                    file.readTimestamp();
                }
            }
            return true;
        }
    }

    /**
     * @param reference
     * @return
     */
    protected Path lookup(NSObject reference) {
        if(roots.contains(reference)) {
            return roots.get(roots.indexOf(reference));
        }
        return cache.lookup(new OutlinePathReference(reference));
    }

    protected static final String INCLUDE_COLUMN = "INCLUDE";
    protected static final String WARNING_COLUMN = "WARNING";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, NSObject value,
                                                                 final NSTableColumn tableColumn, NSObject item) {
        String identifier = tableColumn.identifier();
        if(identifier.equals(INCLUDE_COLUMN)) {
            final Path path = this.lookup(item);
            transfer.setSkipped(path, Rococoa.cast(value, NSNumber.class).intValue() == NSCell.NSOffState);
            if(path.attributes.isDirectory()) {
                outlineView.setNeedsDisplay(true);
            }
        }
    }

    /**
     * The filter to apply to the file listing in the prompt dialog
     *
     * @return
     */
    protected abstract PathFilter<Path> filter();

    /**
     * File listing cache for children of the root paths
     */
    private final Cache<Path> cache = new Cache<Path>();

    /**
     * Container for all paths currently being listed in the background
     */
    private final List<Path> isLoadingListingInBackground = new Collection<Path>();

    /**
     * If no cached listing is available the loading is delayed until the listing is
     * fetched from a background thread
     *
     * @param path
     * @return The list of child items for the parent folder. The listing is filtered
     *         using the standard regex exclusion and the additional passed filter
     */
    protected AttributedList<Path> childs(final Path path) {
        synchronized(isLoadingListingInBackground) {
            // Check first if it hasn't been already requested so we don't spawn
            // a multitude of unecessary threads
            if(!isLoadingListingInBackground.contains(path)) {
                if(cache.containsKey(path)) {
                    return cache.get(path, new NullComparator<Path>(), filter());
                }
                isLoadingListingInBackground.add(path);
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                controller.background(new AbstractBackgroundAction() {
                    public void run() {
                        cache.put(path, transfer.childs(path));
                    }

                    @Override
                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                                path.getName());
                    }

                    @Override
                    public void cleanup() {
                        synchronized(isLoadingListingInBackground) {
                            isLoadingListingInBackground.remove(path);
                        }
                        controller.reloadData();
                    }

                    @Override
                    public Object lock() {
                        return transfer.getSession();
                    }
                });
            }
            return cache.get(path, new NullComparator<Path>(), filter());
        }
    }

    protected final NSImage ALERT_ICON = CDIconCache.iconNamed("alert.tiff");

    /**
     * Second cache because it is expensive to create proxy instances
     */
    protected AttributeCache<Path> tableViewCache = new AttributeCache<Path>(
            Preferences.instance().getInteger("browser.model.cache.size")
    );

    /**
     * @param item
     * @param identifier
     * @return
     */
    protected NSObject objectValueForItem(final Path item, final String identifier) {
        final NSObject cached = tableViewCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(INCLUDE_COLUMN)) {
                // Not included if the particular path should be skipped or skip
                // existing is selected as the default transfer action for duplicate
                // files
                final boolean skipped = !transfer.isIncluded(item)
                        || controller.getAction().equals(TransferAction.ACTION_SKIP);
                return NSNumber.numberWithInt(skipped ? NSCell.NSOffState : NSCell.NSOnState);
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.getName(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return tableViewCache.put(item, identifier, NSString.stringWithString(item.getName()));
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return cached;
    }

    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        if(null == item) {
            return false;
        }
        return this.lookup(item).attributes.isDirectory();
    }

    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, NSObject item) {
        if(null == item) {
            return new NSInteger(roots.size());
        }
        return new NSInteger(this.childs(this.lookup(item)).size());
    }

    public NSObject outlineView_child_ofItem(final NSOutlineView view, NSInteger index, NSObject item) {
        if(null == item) {
            return roots.get(index.intValue()).<NSObject>getReference().unique();
        }
        final AttributedList<Path> childs = this.childs(this.lookup(item));
        if(childs.isEmpty()) {
            return null;
        }
        return childs.get(index.intValue()).<NSObject>getReference().unique();
    }

    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, NSObject item) {
        return this.objectValueForItem(this.lookup(item), tableColumn.identifier());
    }
}