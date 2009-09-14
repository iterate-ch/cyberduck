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

import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.*;

/**
 * @version $Id$
 */
public class CDActivityController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDActivityController.class);

    private static CDActivityController instance = null;

    public static CDActivityController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new CDActivityController();
            }
            return instance;
        }
    }

    private final Map<BackgroundAction, CDTaskController> tasks
            = Collections.synchronizedMap(new LinkedHashMap<BackgroundAction, CDTaskController>());

    private CDActivityController() {
        this.loadBundle();
        // Initialize to listen for background tasks
        this.init();
    }

    @Override
    protected void invalidate() {
        BackgroundActionRegistry.instance().removeListener(backgroundActionListener);
        table.setDataSource(null);
        table.setDelegate(null);
        super.invalidate();
    }

    private final AbstractCollectionListener<BackgroundAction> backgroundActionListener = new AbstractCollectionListener<BackgroundAction>() {
        @Override
        public void collectionItemAdded(final BackgroundAction action) {
            invoke(new WindowMainAction(CDActivityController.this) {
                public void run() {
                    log.debug("collectionItemAdded:" + action);
                    tasks.put(action, new CDTaskController(action));
                    reload();
                }
            });
        }

        @Override
        public void collectionItemRemoved(final BackgroundAction action) {
            invoke(new WindowMainAction(CDActivityController.this) {
                public void run() {
                    log.debug("collectionItemRemoved:" + action);
                    final CDTaskController controller = tasks.remove(action);
                    if(null == controller) {
                        return;
                    }
                    controller.invalidate();
                    reload();
                }
            });
        }
    };

    private void init() {
        BackgroundActionRegistry.instance().addListener(backgroundActionListener);
        // Add already running background actions
        final BackgroundAction[] actions = BackgroundActionRegistry.instance().toArray(
                new BackgroundAction[BackgroundActionRegistry.instance().size()]);
        for(final BackgroundAction action : actions) {
            tasks.put(action, new CDTaskController(action));
        }
        this.reload();
    }

    /**
     *
     */
    private void reload() {
        while(table.subviews().count().intValue() > 0) {
            (Rococoa.cast(table.subviews().lastObject(), NSView.class)).removeFromSuperviewWithoutNeedingDisplay();
        }
        table.reloadData();
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setTitle(Locale.localizedString("Activity"));
        super.setWindow(window);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    private static class TableColumnFactory extends HashMap<String,NSTableColumn> {
        private NSTableColumn create(String identifier) {
            if(!this.containsKey(identifier)) {
                this.put(identifier, NSTableColumn.tableColumnWithIdentifier(identifier));
            }
            return this.get(identifier);
        }
    }

    @Outlet
    private NSTableView table;
    private CDListDataSource model;
    private CDAbstractTableDelegate<CDTaskController> delegate;

    public void setTable(NSTableView table) {
        this.table = table;
        this.table.setDataSource((model = new CDListDataSource() {
            /**
             * @param view
             */
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(tasks.size());
            }

            /**
             * @param view
             * @param tableColumn
             * @param row
             */
            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, NSInteger row) {
                return null;
            }
        }).id());
        this.table.setDelegate((delegate = new CDAbstractTableDelegate<CDTaskController>() {
            public void enterKeyPressed(final ID sender) {
            }

            public void deleteKeyPressed(final ID sender) {
            }

            public String tooltip(CDTaskController c) {
                return null;
            }

            @Override
            public boolean tableView_shouldSelectRow(NSTableView view, int row) {
                return false;
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
            }

            @Override
            public void selectionDidChange(NSNotification notification) {
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSCell cell, NSTableColumn tableColumn, NSInteger row) {
                final Collection<CDTaskController> values = tasks.values();
                Rococoa.cast(cell, CDControllerCell.class).setView(values.toArray(new CDTaskController[values.size()])[row.intValue()].view());
            }
        }).id());
        {
            NSTableColumn c = tableColumnsFactory.create("Default");
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(prototype);
            this.table.addTableColumn(c);
        }
        this.table.sizeToFit();
    }

    private final NSCell prototype = CDControllerCell.controllerCell();

    @Override
    protected String getBundleName() {
        return "Activity";
    }
}