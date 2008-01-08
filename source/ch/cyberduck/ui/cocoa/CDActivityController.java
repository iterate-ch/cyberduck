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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;

import ch.cyberduck.core.Collection;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

/**
 * @version $Id:$
 */
public class CDActivityController extends CDWindowController {

    private static CDActivityController instance;

    public static CDActivityController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if (null == instance) {
                instance = new CDActivityController();
            }
            return instance;
        }
    }

    private CDActivityController() {
        this.loadBundle();
    }
    
    public void awakeFromNib() {

    }

    protected String getBundleName() {
        return "Activity";
    }

    private Collection activities
            = new Collection();

    /**
     *
     * @param view
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return activities.size();
    }

    /**
     *
     * @param view
     * @param tableColumn
     * @param row
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        return null;
    }

    public void add(BackgroundAction action) {
        activities.add(action);
    }

    public void remove(BackgroundAction action) {
        activities.remove(action);
    }
}