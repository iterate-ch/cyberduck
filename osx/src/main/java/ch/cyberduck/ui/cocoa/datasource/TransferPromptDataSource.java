package ch.cyberduck.ui.cocoa.datasource;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.OutlineDataSource;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSNumber;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.TransferPromptFilterWorker;
import ch.cyberduck.core.worker.TransferPromptListWorker;
import ch.cyberduck.ui.cocoa.controller.TransferPromptController;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TransferPromptDataSource extends OutlineDataSource {
    private static final Logger log = Logger.getLogger(TransferPromptDataSource.class);

    private final TransferPromptController controller;

    private final SessionPool session;

    private final Transfer transfer;

    /**
     * Selected transfer action in prompt
     */
    private TransferAction action;

    private final Cache<TransferItem> cache;

    /**
     * Selection status map in the prompt
     */
    protected final Map<TransferItem, Boolean> selected
            = new HashMap<TransferItem, Boolean>();

    /**
     * Transfer status determined by filters
     */
    protected Map<TransferItem, TransferStatus> status
            = new HashMap<TransferItem, TransferStatus>();

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
    public TransferPromptDataSource(final TransferPromptController c, final SessionPool session,
                                    final Transfer transfer, final Cache<TransferItem> cache) {
        this.controller = c;
        this.session = session;
        this.transfer = transfer;
        this.cache = cache;
        this.action = TransferAction.forName(PreferencesFactory.get().getProperty(
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

    public boolean isSelected(final TransferItem file) {
        if(selected.containsKey(file)) {
            return selected.get(file);
        }
        return true;
    }

    public void setSelected(final TransferItem file, boolean state) {
        selected.put(file, state);
    }

    protected AttributedList<TransferItem> get(final TransferItem directory) {
        // Return list with filtered files included
        return cache.get(directory);
    }

    public TransferStatus getStatus(final TransferItem file) {
        if(!status.containsKey(file)) {
            // Transfer filter background task has not yet finished
            log.warn(String.format("Unknown transfer status for %s", file));
            return new TransferStatus();
        }
        return status.get(file);
    }

    protected AttributedList<TransferItem> children(final TransferItem directory) {
        if(null == directory) {
            // Root
            if(!cache.isCached(null)) {
                cache.put(null, new AttributedList<TransferItem>(transfer.getRoots()));
                this.filter();
            }
        }
        else if(!cache.isCached(directory)) {
            controller.background(new WorkerBackgroundAction<List<TransferItem>>(controller, session,
                    new TransferPromptListWorker(transfer, directory.remote, directory.local, controller) {
                        @Override
                        public void cleanup(final List<TransferItem> list) {
                            cache.put(directory, new AttributedList<TransferItem>(list));
                            filter();
                        }
                    }
            ));
        }
        return this.get(directory);
    }

    private void filter() {
        controller.background(new WorkerBackgroundAction<Map<TransferItem, TransferStatus>>(controller, session,
                new TransferPromptFilterWorker(transfer, action, cache, controller) {
                            @Override
                            public void cleanup(final Map<TransferItem, TransferStatus> accepted) {
                                status = accepted;
                                controller.reload();
                            }
                        }
                )
        );
    }

    protected NSObject objectValueForItem(final TransferItem file, final String identifier) {
        final TransferStatus status = this.getStatus(file);
        if(identifier.equals(Column.include.name())) {
            if(status.isRejected()) {
                return NSNumber.numberWithBoolean(false);
            }
            return NSNumber.numberWithBoolean(this.isSelected(file));
        }
        if(identifier.equals(Column.filename.name())) {
            return NSAttributedString.attributedStringWithAttributes(file.remote.getName(),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        if(identifier.equals(Column.size.name())) {
            return NSAttributedString.attributedStringWithAttributes(
                    SizeFormatterFactory.get().format(status.getLength()),
                    TableCellAttributes.browserFontRightAlignment());
        }
        if(identifier.equals(Column.warning.name())) {
            if(file.remote.isFile()) {
                if(status.getLength() == 0) {
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
            final TransferItem file = cache.lookup(new NSObjectPathReference(item));
            final int state = Rococoa.cast(value, NSNumber.class).intValue();
            this.setSelected(file, state == NSCell.NSOnState);
        }
    }

    @Override
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        return cache.lookup(new NSObjectPathReference(item)).remote.isDirectory();
    }

    @Override
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
        return new NSInteger(this.children(null == item ? null : cache.lookup(new NSObjectPathReference(item))).size());
    }

    @Override
    public NSObject outlineView_child_ofItem(final NSOutlineView view, final NSInteger index, final NSObject item) {
        final AttributedList<TransferItem> children = this.get(null == item ? null : cache.lookup(new NSObjectPathReference(item)));
        return NSObjectPathReference.get(children.get(index.intValue()).remote);
    }

    @Override
    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn tableColumn, final NSObject item) {
        return this.objectValueForItem(cache.lookup(new NSObjectPathReference(item)), tableColumn.identifier());
    }
}