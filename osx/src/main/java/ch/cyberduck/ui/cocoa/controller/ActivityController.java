package ch.cyberduck.ui.cocoa.controller;

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

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.ListDataSource;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ActivityController extends WindowController {
    private static final Logger log = LogManager.getLogger(ActivityController.class);

    private final BackgroundActionRegistry registry;

    private final Map<BackgroundAction, TaskController> tasks
            = Collections.synchronizedMap(new LinkedHashMap<>());

    public ActivityController() {
        this(BackgroundActionRegistry.global());
    }

    public ActivityController(final BackgroundActionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        // Initialize to listen for background tasks
        registry.addListener(backgroundActionListener);
        // Add already running background actions
        final BackgroundAction[] actions = registry.toArray(
                new BackgroundAction[registry.size()]);
        for(final BackgroundAction action : actions) {
            tasks.put(action, new TaskController(action));
        }
        this.reload();
    }

    @Override
    public void invalidate() {
        registry.removeListener(backgroundActionListener);
        table.setDataSource(null);
        table.setDelegate(null);
        super.invalidate();
    }

    private final AbstractCollectionListener<BackgroundAction> backgroundActionListener
            = new AbstractCollectionListener<BackgroundAction>() {

        @Override
        public void collectionItemAdded(final BackgroundAction action) {
            if(log.isDebugEnabled()) {
                log.debug("Add background action {}", action);
            }
            tasks.put(action, new TaskController(action));
            reload();
        }

        @Override
        public void collectionItemRemoved(final BackgroundAction action) {
            log.debug("Remove background action {}", action);
            final TaskController controller = tasks.remove(action);
            if(null == controller) {
                log.warn("Failed to find controller for action {}", action);
                return;
            }
            controller.invalidate();
            reload();
        }
    };

    /**
     *
     */
    private void reload() {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                while(table.subviews().count().intValue() > 0) {
                    (Rococoa.cast(table.subviews().lastObject(), NSView.class)).removeFromSuperviewWithoutNeedingDisplay();
                }
                table.reloadData();
            }
        });
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setContentMinSize(window.frame().size);
        window.setTitle(LocaleFactory.localizedString("Activity"));
        super.setWindow(window);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

    @Outlet
    private NSTableView table;

    @Delegate
    private ListDataSource model;

    @Delegate
    private AbstractTableDelegate<TaskController, ActivityColumn> delegate;

    public void setTable(NSTableView table) {
        this.table = table;
        this.table.setRowHeight(new CGFloat(42));
        {
            final NSTableColumn c = tableColumnsFactory.create(ActivityColumn.single.name());
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            this.table.addTableColumn(c);
        }
        this.table.setDataSource((model = new ListDataSource() {
            @Override
            public NSObject tableView_objectValueForTableColumn_row(final NSTableView view, final NSTableColumn tableColumn, final NSInteger row) {
                return null;
            }

            @Override
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(tasks.size());
            }
        }).id());
        this.table.setDelegate((delegate = new AbstractTableDelegate<TaskController, ActivityColumn>(
                table.tableColumnWithIdentifier("Default")
        ) {
            @Override
            public void enterKeyPressed(final ID sender) {
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
            }

            @Override
            public String tooltip(final TaskController c, final ActivityColumn column) {
                return null;
            }

            @Override
            public boolean tableView_shouldSelectRow(final NSTableView view, final NSInteger row) {
                return false;
            }

            @Override
            public void tableColumnClicked(final NSTableView view, final NSTableColumn tableColumn) {
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return false;
            }

            public NSView tableView_viewForTableColumn_row(final NSTableView view, final NSTableColumn column, final NSInteger row) {
                final TaskController controller = getController(row);
                if(null == controller) {
                    return null;
                }
                return controller.view();
            }
        }).id());
        this.table.sizeToFit();
    }

    private TaskController getController(final NSInteger row) {
        final TaskController[] controllers = tasks.values().toArray(new TaskController[tasks.size()]);
        if(row.intValue() >= controllers.length) {
            return null;
        }
        return controllers[row.intValue()];
    }

    @Override
    protected String getBundleName() {
        return "Activity";
    }

    private enum ActivityColumn {
        single
    }
}
