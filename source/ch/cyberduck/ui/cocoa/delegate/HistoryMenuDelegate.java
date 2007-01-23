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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDMainController;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @version $Id$
 */
public class HistoryMenuDelegate extends NSObject {
    private static Logger log = Logger.getLogger(HistoryMenuDelegate.class);

    public static final File HISTORY_FOLDER
            = new File(Preferences.instance().getProperty("application.support.path"), "History");

    static {
        HISTORY_FOLDER.mkdirs();
    }

    private List cache = new ArrayList();

    private File[] listFiles() {
        return HISTORY_FOLDER.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".duck");
            }
        });
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        File[] files = this.listFiles();
        if(cache.size() != files.length) {
            Arrays.sort(files, new Comparator() {
                public int compare(Object o1, Object o2) {
                    File f1 = (File) o1;
                    File f2 = (File) o2;
                    if(f1.lastModified() < f2.lastModified()) {
                        return 1;
                    }
                    if(f1.lastModified() > f2.lastModified()) {
                        return -1;
                    }
                    return 0;
                }
            });
            cache.clear();
            for(int i = 0; i < files.length; i++) {
                cache.add(((CDMainController) NSApplication.sharedApplication().delegate()).importBookmark(files[i]));
            }
        }
        if(cache.size() > 0) {
            return cache.size();
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
        if(index >= cache.size()) {
            log.warn("Invalid index in menuUpdateItemAtIndex:" + index);
            return false;
        }
        Host h = (Host) cache.get(index);
        sender.setTitle(h.getNickname());
        sender.setRepresentedObject(h);
        sender.setTarget(this);
        sender.setEnabled(true);
        sender.setImage(NSImage.imageNamed("bookmark16.tiff"));
        sender.setAction(new NSSelector("historyMenuItemClicked", new Class[]{NSMenuItem.class}));
        return !shouldCancel;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public void historyMenuItemClicked(NSMenuItem sender) {
        CDBrowserController controller
                = ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        controller.mount((Host) sender.representedObject());
    }
}
