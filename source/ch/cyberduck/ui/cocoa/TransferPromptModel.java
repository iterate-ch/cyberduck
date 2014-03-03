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
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.action.TransferPromptFilterWorker;
import ch.cyberduck.ui.action.TransferPromptListWorker;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSOutlineView;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.resources.IconCacheFactory;
import ch.cyberduck.ui.threading.WorkerBackgroundAction;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class TransferPromptModel extends OutlineDataSource {
    private static Logger log = Logger.getLogger(TransferPromptModel.class);

    private TransferPromptController controller;

    private Session session;

    private Transfer transfer;

    /**
     * Selected transfer action in prompt
     */
    private TransferAction action;

    private Cache<Path> cache;

    /**
     * Selection status map in the prompt
     */
    protected Map<Path, Boolean> selected
            = new HashMap<Path, Boolean>();

    /**
     * Transfer status determined by filters
     */
    protected Map<Path, TransferStatus> status
            = new HashMap<Path, TransferStatus>();

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
    public TransferPromptModel(final TransferPromptController c, final Session session, final Transfer transfer, final Cache cache) {
        this.controller = c;
        this.session = session;
        this.transfer = transfer;
        this.cache = cache;
        this.action = TransferAction.forName(Preferences.instance().getProperty(
                String.format("queue.prompt.%s.action.default", transfer.getType().name())));
    }

    /**
     * Change transfer action and reload list of files
     *
     * @param action Transfer action
     */
    public void setAction(final TransferAction action) {
        this.action = action;
        this.filter();
    }

    public boolean isSelected(final Path file) {
        if(selected.containsKey(file)) {
            return selected.get(file);
        }
        return true;
    }

    public void setSelected(final Path file, boolean state) {
        selected.put(file, state);
    }

    /**
     * @param file File
     * @return False if transfer filter rejected file
     */
    public boolean isFiltered(final Path file) {
        return !status.containsKey(file);
    }

    protected AttributedList<Path> get(final Path directory) {
        // Return list with filtered files included
        return cache.get(null == directory ? null : directory.getReference());
    }

    public TransferStatus getStatus(final Path file) {
        if(!status.containsKey(file)) {
            // Transfer filter background task has not yet finished
            log.warn(String.format("Unknown transfer status for %s", file));
            return new TransferStatus();
        }
        return status.get(file);
    }

    protected AttributedList<Path> children(final Path directory) {
        if(null == directory) {
            // Root
            if(!cache.isCached(null)) {
                cache.put(null, new AttributedList<Path>(transfer.getRoots()));
                this.filter();
            }
        }
        else if(!cache.isCached(directory.getReference())) {
            controller.background(new WorkerBackgroundAction(controller, session,
                    new TransferPromptListWorker(session, transfer, directory) {
                        @Override
                        public void cleanup(final AttributedList<Path> list) {
                            cache.put(directory.getReference(), list);
                            filter();
                        }
                    }));
        }
        return this.get(directory);
    }

    private void filter() {
        controller.background(new WorkerBackgroundAction(controller, session,
                new TransferPromptFilterWorker(session, transfer, action, cache) {
                    @Override
                    public void cleanup(final Map<Path, TransferStatus> accepted) {
                        status = accepted;
                        controller.reloadData();
                    }
                })
        );
    }

    protected NSObject objectValueForItem(final Path file, final String identifier) {
        if(identifier.equals(Column.include.name())) {
            if(this.isFiltered(file)) {
                return NSNumber.numberWithBoolean(false);
            }
            return NSNumber.numberWithBoolean(this.isSelected(file));
        }
        if(identifier.equals(Column.filename.name())) {
            return NSAttributedString.attributedStringWithAttributes(file.getName(),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        if(identifier.equals(Column.size.name())) {
            return NSAttributedString.attributedStringWithAttributes(
                    SizeFormatterFactory.get().format(this.getStatus(file).getLength()),
                    TableCellAttributes.browserFontRightAlignment());
        }
        if(identifier.equals(Column.warning.name())) {
            if(file.attributes().isFile()) {
                if(this.getStatus(file).getLength() == 0) {
                    return IconCacheFactory.<NSImage>get().iconNamed("alert.tiff");
                }
            }
            return null;
        }
        throw new IllegalArgumentException(String.format("Unknown identifier %s", identifier));
    }

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, final NSObject value,
                                                                 final NSTableColumn tableColumn, final NSObject item) {
        final String identifier = tableColumn.identifier();
        if(identifier.equals(Column.include.name())) {
            final Path file = cache.lookup(new NSObjectPathReference(item));
            final int state = Rococoa.cast(value, NSNumber.class).intValue();
            this.setSelected(file, state == NSCell.NSOnState);
        }
    }

    @Override
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        return cache.lookup(new NSObjectPathReference(item)).attributes().isDirectory();
    }

    @Override
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
        return new NSInteger(this.children(null == item ? null : cache.lookup(new NSObjectPathReference(item))).size());
    }

    @Override
    public NSObject outlineView_child_ofItem(final NSOutlineView view, final NSInteger index, final NSObject item) {
        final AttributedList<Path> children = this.get(null == item ? null : cache.lookup(new NSObjectPathReference(item)));
        return (NSObject) children.get(index.intValue()).getReference().unique();
    }

    @Override
    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn tableColumn, final NSObject item) {
        return this.objectValueForItem(cache.lookup(new NSObjectPathReference(item)), tableColumn.identifier());
    }
}