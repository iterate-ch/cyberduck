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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.odb.Editor;

import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class EditMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(EditMenuDelegate.class);

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        int n = Editor.INSTALLED_EDITORS.size();
        if(0 == n) {
            return 1;
        }
        return n;
    }

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
        if(Editor.INSTALLED_EDITORS.size() == 0) {
            item.setTitle(NSBundle.localizedString("No external editor available"));
            return false;
        }
        String identifier = (String) Editor.INSTALLED_EDITORS.values().toArray(
                new String[Editor.INSTALLED_EDITORS.size()])[index];
        String editor = (String) Editor.INSTALLED_EDITORS.keySet().toArray(
                new String[Editor.INSTALLED_EDITORS.size()])[index];
        item.setTitle(editor);
        if(editor.equals(Preferences.instance().getProperty("editor.name"))) {
            item.setKeyEquivalent("j");
            item.setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        }
        else {
            item.setKeyEquivalent("");
        }
        String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                identifier);
        if(path != null) {
            NSImage icon = NSWorkspace.sharedWorkspace().iconForFile(path);
            icon.setSize(new NSSize(16f, 16f));
            item.setImage(icon);
        }
        else {
            // Used to provide a custom icon for the edit menu and disable the menu
            // if no external editor can be found
            item.setImage(NSImage.imageNamed("pencil.tiff"));
        }
        item.setAction(new NSSelector("editMenuClicked", new Class[]{Object.class}));
        return !shouldCancel;
    }
}
