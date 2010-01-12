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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.application.NSEvent;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Map;

/**
 * @version $Id$
 */
public abstract class EditMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(EditMenuDelegate.class);

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        final int n = EditorFactory.getInstalledEditors(this.getSelected()).size();
        if(0 == n) {
            return new NSInteger(1);
        }
        return new NSInteger(n);
    }

    protected abstract Local getSelected();

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean shouldCancel) {
        if(shouldCancel) {
            return false;
        }
        if(super.shouldSkipValidation(menu, index.intValue())) {
            return false;
        }
        final Map<String, String> editors = EditorFactory.getInstalledEditors(this.getSelected());
        if(editors.size() == 0) {
            item.setTitle(Locale.localizedString("No external editor available"));
            return false;
        }
        String defaultEditor = EditorFactory.defaultEditor(this.getSelected());
        String identifier = editors.values().toArray(new String[editors.size()])[index.intValue()];
        item.setRepresentedObject(identifier);
        String editor = editors.keySet().toArray(new String[editors.size()])[index.intValue()];
        item.setTitle(editor);
        if(identifier.equalsIgnoreCase(defaultEditor)) {
            item.setKeyEquivalent("k");
            item.setKeyEquivalentModifierMask(NSEvent.NSCommandKeyMask);
        }
        else {
            item.setKeyEquivalent("");
        }
        final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
        if(StringUtils.isNotEmpty(path)) {
            item.setImage(IconCache.instance().iconForPath(LocalFactory.createLocal(path), 16));
        }
        else {
            // Used to provide a custom icon for the edit menu and disable the menu
            // if no external editor can be found
            item.setImage(IconCache.iconNamed("pencil.tiff"));
        }
        item.setAction(Foundation.selector("editMenuClicked:"));
        return !shouldCancel;
    }
}
