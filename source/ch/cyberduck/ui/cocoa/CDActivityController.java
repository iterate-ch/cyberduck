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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDActivityController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDActivityController.class);

    private static CDActivityController instance;

    public static CDActivityController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new CDActivityController();
            }
            return instance;
        }
    }

    /**
     *
     */
    private final Map tasks = new LinkedHashMap();

    private CDActivityController() {
        this.loadBundle();
        // Initialize to listen for background tasks
        this.init();
    }

    private void init() {
        BackgroundActionRegistry.instance().addListener(new AbstractCollectionListener() {
            public void collectionItemAdded(final Object action) {
                synchronized(tasks) {
                    CDMainApplication.invoke(new WindowMainAction(CDActivityController.this) {
                        public void run() {
                            log.debug("collectionItemAdded" + action);
                            tasks.put(action, new CDTaskController(((BackgroundAction) action)));
                            reload();
                        }
                    });
                }
            }

            public void collectionItemRemoved(final Object action) {
                synchronized(tasks) {
                    CDMainApplication.invoke(new WindowMainAction(CDActivityController.this) {
                        public void run() {
                            log.debug("collectionItemRemoved" + action);
                            tasks.remove(action);
                            reload();
                        }
                    });
                }
            }
        });
        synchronized(tasks) {
            // Add already running background actions
            for(Iterator iter = BackgroundActionRegistry.instance().iterator(); iter.hasNext();) {
                final BackgroundAction action = (BackgroundAction) iter.next();
                tasks.put(action, new CDTaskController(action));
            }
        }
    }

    private void reload() {
        while(table.subviews().count() > 0) {
            ((NSView) table.subviews().lastObject()).removeFromSuperviewWithoutNeedingDisplay();
        }
        table.reloadData();
    }

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setReleasedWhenClosed(false);
        this.window.setDelegate(this);
        this.window.setTitle(NSBundle.localizedString("Activity", ""));
    }

    /**
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        // Do not call super as we are a singleton. super#windowWillClose would invalidate me
    }

    private NSTableView table;
    private CDAbstractTableDelegate delegate;

    public void setTable(NSTableView table) {
        this.table = table;
        this.table.setDataSource(this);
        this.table.setDelegate(this.delegate = new CDAbstractTableDelegate() {
            public boolean tableViewShouldSelectRow(NSTableView view, int row) {
                return false;
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {

            }

            public void tableRowDoubleClicked(Object sender) {

            }

            public void selectionDidChange(NSNotification notification) {

            }

            public void enterKeyPressed(Object sender) {

            }

            public void deleteKeyPressed(Object sender) {

            }
        });
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setMinWidth(80f);
            c.setWidth(300f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new CDControllerCell());
            this.table.addTableColumn(c);
        }
        this.table.sizeToFit();
    }

    public void awakeFromNib() {
        ;
    }

    protected String getBundleName() {
        return "Activity";
    }

    /**
     * @param view
     */
    public int numberOfRowsInTableView(NSTableView view) {
        synchronized(tasks) {
            return tasks.size();
        }
    }

    /**
     * @param view
     * @param tableColumn
     * @param row
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        synchronized(tasks) {
            if(row < this.numberOfRowsInTableView(view)) {
                final Collection values = tasks.values();
                return values.toArray(new CDTaskController[values.size()])[row];
            }
            log.warn("tableViewObjectValueForLocation:" + row + " == null");
        }
        return null;
    }
}