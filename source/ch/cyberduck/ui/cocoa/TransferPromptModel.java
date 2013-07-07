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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.RepeatableBackgroundAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSOutlineView;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.threading.PanelAlertCallback;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class TransferPromptModel extends OutlineDataSource {

    protected final Transfer transfer;

    /**
     * The root nodes to be included in the prompt dialog
     */
    protected final AttributedList<Path> roots
            = new AttributedList<Path>();

    private TransferPromptController controller;

    /**
     * @param c        The parent window to attach the prompt
     * @param transfer Transfer
     */
    public TransferPromptModel(final TransferPromptController c, final Transfer transfer) {
        this.controller = c;
        this.transfer = transfer;
    }

    public void add(final Path p) throws BackgroundException {
        roots.add(p);
    }

    protected static class PromptFilter implements Filter<Path> {
        @Override
        public boolean accept(final Path file) {
            return true;
        }
    }

    protected Path lookup(PathReference reference) {
        return transfer.lookup(reference);
    }

    protected static final String INCLUDE_COLUMN = "INCLUDE";
    protected static final String WARNING_COLUMN = "WARNING";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, final NSObject value,
                                                                 final NSTableColumn tableColumn, final NSObject item) {
        final String identifier = tableColumn.identifier();
        if(identifier.equals(INCLUDE_COLUMN)) {
            final Path path = this.lookup(new NSObjectPathReference(item));
            final int state = Rococoa.cast(value, NSNumber.class).intValue();
            transfer.setSelected(path, state == NSCell.NSOnState);
            outlineView.setNeedsDisplay(true);
        }
    }

    /**
     * @return The filter to apply to the file listing in the prompt dialog
     */
    protected abstract Filter<Path> filter();

    /**
     * Container for all paths currently being listed in the background
     */
    private final List<Path> isLoadingListingInBackground = new Collection<Path>();

    /**
     * If no cached listing is available the loading is delayed until the listing is
     * fetched from a background thread
     *
     * @param path Folder
     * @return The list of child items for the parent folder. The listing is filtered
     *         using the standard regex exclusion and the additional passed filter
     */
    protected AttributedList<Path> children(final Path path) {
        synchronized(isLoadingListingInBackground) {
            if(transfer.cache().containsKey(path.getReference())) {
                return transfer.cache().get(path.getReference()).filter(new NullComparator<Path>(), filter());
            }
            // Check first if it hasn't been already requested so we don't spawn
            // a multitude of unecessary threads
            if(!isLoadingListingInBackground.contains(path)) {
                isLoadingListingInBackground.add(path);
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                controller.background(new RepeatableBackgroundAction(new PanelAlertCallback(controller),
                        controller, controller, new DisabledLoginController(), new DefaultHostKeyController()) {
                    @Override
                    public void run() throws BackgroundException {
                        transfer.cache().put(path.getReference(), transfer.children(path));
                    }

                    @Override
                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                                path.getName());
                    }

                    @Override
                    public void cleanup() {
                        super.cleanup();
                        synchronized(isLoadingListingInBackground) {
                            isLoadingListingInBackground.remove(path);
                            if(isLoadingListingInBackground.isEmpty()) {
                                controller.reloadData();
                            }
                        }
                    }

                    @Override
                    public List<Session<?>> getSessions() {
                        return transfer.getSessions();
                    }
                });
            }
            return transfer.cache().get(path.getReference()).filter(new FilenameComparator(true), filter());
        }
    }

    /**
     * Second cache because it is expensive to create proxy instances
     */
    protected AttributeCache<Path> tableViewCache = new AttributeCache<Path>(
            Preferences.instance().getInteger("browser.model.cache.size")
    );

    protected NSObject objectValueForItem(final Path item, final String identifier) {
        final NSObject cached = tableViewCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(INCLUDE_COLUMN)) {
                // Not included if the particular path should be skipped or skip
                // existing is selected as the default transfer action for duplicate files
                final boolean included = !transfer.isSkipped(item) && transfer.isSelected(item) && !controller.getAction().equals(TransferAction.ACTION_SKIP);
                return NSNumber.numberWithInt(included ? NSCell.NSOnState : NSCell.NSOffState);
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.getName(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return tableViewCache.put(item, identifier, NSString.stringWithString(item.getName()));
            }
            throw new IllegalArgumentException(String.format("Unknown identifier %s", identifier));
        }
        return cached;
    }

    @Override
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        if(null == item) {
            return false;
        }
        return this.lookup(new NSObjectPathReference(item)).attributes().isDirectory();
    }

    @Override
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, NSObject item) {
        if(null == item) {
            return new NSInteger(roots.size());
        }
        return new NSInteger(this.children(this.lookup(new NSObjectPathReference(item))).size());
    }

    @Override
    public NSObject outlineView_child_ofItem(final NSOutlineView view, NSInteger index, NSObject item) {
        if(null == item) {
            return (NSObject) roots.get(index.intValue()).getReference().unique();
        }
        final AttributedList<Path> children = this.children(this.lookup(new NSObjectPathReference(item)));
        if(children.isEmpty()) {
            return null;
        }
        return (NSObject) children.get(index.intValue()).getReference().unique();
    }

    @Override
    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn tableColumn, NSObject item) {
        if(null == item) {
            return null;
        }
        return this.objectValueForItem(this.lookup(new NSObjectPathReference(item)), tableColumn.identifier());
    }

    /**
     * Clear the view cache
     */
    protected void clear() {
        tableViewCache.clear();
    }

    @Override
    protected void invalidate() {
        this.clear();
        super.invalidate();
    }
}