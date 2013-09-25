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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathReference;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSOutlineView;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.threading.PanelAlertCallback;
import ch.cyberduck.ui.comparator.FilenameComparator;
import ch.cyberduck.ui.threading.ControllerBackgroundAction;

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

    public enum Column {
        include,
        warning,
        filename,
        size,
    }

    /**
     * @param c        The parent window to attach the prompt
     * @param transfer Transfer
     */
    public TransferPromptModel(final TransferPromptController c, final Transfer transfer) {
        this.controller = c;
        this.transfer = transfer;
    }

    public void add(final Path p) {
        roots.add(p);
    }

    protected Path lookup(final PathReference reference) {
        return transfer.lookup(reference);
    }

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, final NSObject value,
                                                                 final NSTableColumn tableColumn, final NSObject item) {
        final String identifier = tableColumn.identifier();
        if(identifier.equals(Column.include.name())) {
            final Path path = this.lookup(new NSObjectPathReference(item));
            final int state = Rococoa.cast(value, NSNumber.class).intValue();
            transfer.setSelected(path, state == NSCell.NSOnState);
            outlineView.setNeedsDisplay(true);
        }
    }

    /**
     * If no cached listing is available the loading is delayed until the listing is
     * fetched from a background thread
     *
     * @param directory Folder
     * @return The list of child items for the parent folder. The listing is filtered
     *         using the standard regex exclusion and the additional passed filter
     */
    protected AttributedList<Path> children(final Path directory) {
        final Cache cache = transfer.cache();
        if(!cache.isCached(directory.getReference())) {
            controller.background(new ControllerBackgroundAction(controller, new PanelAlertCallback(controller), controller, controller) {
                @Override
                public Boolean run() throws BackgroundException {
                    transfer.cache().put(directory.getReference(), transfer.children(directory));
                    return true;
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                            directory.getName());
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    controller.reloadData();
                }

                @Override
                public List<Session<?>> getSessions() {
                    return transfer.getSessions();
                }
            });
        }
        return this.get(directory);
    }

    protected AttributedList<Path> get(final Path directory) {
        final Cache cache = transfer.cache();
        return cache.get(directory.getReference()).filter(new FilenameComparator(true), transfer.getRegexFilter());
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
            if(identifier.equals(Column.include.name())) {
                // Not included if the particular path should be skipped or skip existing is selected as the default transfer action for duplicate files
                final boolean included = !transfer.isSkipped(item) && transfer.isSelected(item) && !controller.getAction().equals(TransferAction.ACTION_SKIP);
                return NSNumber.numberWithInt(included ? NSCell.NSOnState : NSCell.NSOffState);
            }
            if(identifier.equals(Column.filename.name())) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.getName(),
                        TableCellAttributes.browserFontLeftAlignment()));
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
        final AttributedList<Path> children = this.get(this.lookup(new NSObjectPathReference(item)));
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