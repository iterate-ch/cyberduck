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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.application.NSEvent;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @version $Id$
 */
public abstract class EditMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(EditMenuDelegate.class);

    /**
     * Last selected extension
     */
    private String extension = null;

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        final int n = EditorFactory.getInstalledEditors(this.getSelectedFile()).size();
        if(0 == n) {
            return new NSInteger(1);
        }
        return new NSInteger(n);
    }

    protected abstract Local getSelectedFile();

    @Override
    protected boolean isPopulated() {
        final Local selected = this.getSelectedFile();
        if(selected != null && ObjectUtils.equals(extension, selected.getExtension())) {
            return true;
        }
        if(selected != null) {
            extension = selected.getExtension();
        }
        else {
            extension = null;
        }
        return false;
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        final Local selected = this.getSelectedFile();
        final Map<String, String> editors = EditorFactory.getInstalledEditors(selected);
        if(editors.size() == 0) {
            item.setTitle(Locale.localizedString("No external editor available"));
            return false;
        }
        String defaultEditor = EditorFactory.defaultEditor(selected);
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
        item.setImage(IconCache.instance().iconForApplication(identifier, 16));
        item.setAction(Foundation.selector("editMenuClicked:"));
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }
}
