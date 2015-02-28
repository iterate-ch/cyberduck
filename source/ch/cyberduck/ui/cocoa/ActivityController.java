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

import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.view.ControllerCell;

import org.apache.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public final class ActivityController extends WindowController {
    private static Logger log = Logger.getLogger(ActivityController.class);


    private BackgroundActionRegistry registry
            = BackgroundActionRegistry.global();

    private final Map<BackgroundAction, TaskController> tasks
            = Collections.synchronizedMap(new LinkedHashMap<BackgroundAction, TaskController>());

    public ActivityController() {
        this.loadBundle();
        // Initialize to listen for background tasks
        this.init();
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
                log.debug(String.format("Add background action %s", action));
            }
            tasks.put(action, new TaskController(action));
            reload();
        }

        @Override
        public void collectionItemRemoved(final BackgroundAction action) {
            log.debug(String.format("Remove background action %s", action));
            final TaskController controller = tasks.remove(action);
            if(null == controller) {
                log.warn(String.format("Failed to find controller for action %s", action));
                return;
            }
            controller.invalidate();
            reload();
        }
    };

    private void init() {
        registry.addListener(backgroundActionListener);
        // Add already running background actions
        final BackgroundAction[] actions = registry.toArray(
                new BackgroundAction[registry.size()]);
        for(final BackgroundAction action : actions) {
            tasks.put(action, new TaskController(action));
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
    private AbstractTableDelegate<TaskController> delegate;

    public void setTable(NSTableView table) {
        this.table = table;
        this.table.setRowHeight(new CGFloat(42));
        {
            NSTableColumn c = tableColumnsFactory.create("Default");
            c.setMinWidth(80f);
            c.setWidth(300f);
            c.setResizingMask(NSTableColumn.NSTableColumnAutoresizingMask);
            c.setDataCell(prototype);
            this.table.addTableColumn(c);
        }
        this.table.setDataSource((model = new ListDataSource() {
            @Override
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(tasks.size());
            }
        }).id());
        this.table.setDelegate((delegate = new AbstractTableDelegate<TaskController>(
                table.tableColumnWithIdentifier("Default")
        ) {
            @Override
            public void enterKeyPressed(final ID sender) {
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
            }

            @Override
            public String tooltip(final TaskController c) {
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

            public void tableView_willDisplayCell_forTableColumn_row(final NSTableView view, final NSCell cell,
                                                                     final NSTableColumn column, final NSInteger row) {
                if(Factory.Platform.osversion.matches("10\\.(5|6).*")) {
                    final TaskController controller = getController(row);
                    Rococoa.cast(cell, ControllerCell.class).setView(controller.view());
                }
            }

            public NSView tableView_viewForTableColumn_row(final NSTableView view, final NSTableColumn tableColumn,
                                                           final NSInteger row) {
                if(!Factory.Platform.osversion.matches("10\\.(5|6).*")) {
                    // 10.7 or later supports view View-Based Table Views
                    final TaskController controller = getController(row);
                    return controller.view();
                }
                return null;
            }
        }).id());
        this.table.sizeToFit();
    }

    protected TaskController getController(final NSInteger row) {
        return tasks.values().toArray(new TaskController[tasks.size()])[row.intValue()];
    }

    private final NSCell prototype = ControllerCell.controllerCell();

    @Override
    protected String getBundleName() {
        return "Activity";
    }
}