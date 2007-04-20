package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDMainController;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class HistoryMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    public static final Local HISTORY_FOLDER
            = new Local(Preferences.instance().getProperty("application.support.path"), "History");

    static {
        HISTORY_FOLDER.mkdir(true);
    }

    private static final NSImage DOCUMENT_ICON_SMALL;

    static {
        DOCUMENT_ICON_SMALL = NSImage.imageNamed("bookmark16.tiff");
        DOCUMENT_ICON_SMALL.setScalesWhenResized(true);
        DOCUMENT_ICON_SMALL.setSize(new NSSize(16f, 16f));
    }

    private final List cache = new Collection();

    private List history() {
        return HISTORY_FOLDER.childs(
                new NullComparator(),
                new PathFilter() {
                    public boolean accept(AbstractPath file) {
                        return file.getName().endsWith(".duck");
                    }
                }
        );
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        List history = this.history();
        if(!(cache.size() == history.size())) {
            Collections.sort(history, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Local f1 = (Local) o1;
                    Local f2 = (Local) o2;
                    if(f1.attributes.getModificationDate() < f2.attributes.getModificationDate()) {
                        return 1;
                    }
                    if(f1.attributes.getModificationDate() > f2.attributes.getModificationDate()) {
                        return -1;
                    }
                    return 0;
                }
            });
            cache.clear();
            CDMainController m = (CDMainController) NSApplication.sharedApplication().delegate();
            for(Iterator iter = history.iterator(); iter.hasNext();) {
                // Add the imported bookmark to the cache
                cache.add(m.importBookmark((Local) iter.next()));
            }
        }
        if(cache.size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return cache.size() + 2;
        }
        return 1;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        if(cache.size() == 0) {
            sender.setTitle(NSBundle.localizedString("No recently connected servers available", ""));
            sender.setTarget(null);
            sender.setAction(null);
            sender.setImage(null);
            sender.setEnabled(false);
            return false;
        }
        if(index < cache.size()) {
            Host h = (Host) cache.get(index);
            // This is a hack. We insert a new NSMenuItem as NSMenu has
            // a bug caching old entries since we introduced the separator item below
            menu.removeItemAtIndex(index);
            NSMenuItem bookmark = new NSMenuItem();
            bookmark.setTitle(h.getNickname());
            bookmark.setRepresentedObject(h);
            bookmark.setTarget(this);
            bookmark.setEnabled(true);
            bookmark.setImage(DOCUMENT_ICON_SMALL);
            bookmark.setAction(new NSSelector("historyMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(bookmark, index);
            return !shouldCancel;
        }
        if(index == cache.size()) {
            menu.removeItemAtIndex(index);
            // There is no way in this wonderful API to add a separator item
            // without creating a new NSMenuItem first
            NSMenuItem separator = new NSMenuItem().separatorItem();
            menu.insertItemAtIndex(separator, index);
            return !shouldCancel;
        }
        if(index == cache.size() + 1) {
            menu.removeItemAtIndex(index);
            NSMenuItem clear = new NSMenuItem();
            clear.setTitle(NSBundle.localizedString("Clear Menu", ""));
            clear.setTarget(this);
            clear.setEnabled(true);
            clear.setAction(new NSSelector("clearMenuItemClicked", new Class[]{NSMenuItem.class}));
            menu.insertItemAtIndex(clear, index);
            return !shouldCancel;
        }
        return true;
    }

    public void historyMenuItemClicked(NSMenuItem sender) {
        CDBrowserController controller
                = ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        controller.mount((Host) sender.representedObject());
    }

    public void clearMenuItemClicked(NSMenuItem sender) {
        // Delete all bookmark files
        for(Iterator iter = this.history().iterator(); iter.hasNext();) {
            ((AbstractPath) iter.next()).delete();
        }
    }
}
