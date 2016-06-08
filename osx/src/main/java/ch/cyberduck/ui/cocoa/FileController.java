package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.EnumSet;

public abstract class FileController extends AlertController {

    @Outlet
    protected NSTextField inputField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    public void setInputField(final NSTextField inputField) {
        this.inputField = inputField;
    }

    protected BrowserController parent;

    private Cache<Path> cache;

    public FileController(final BrowserController parent, final Cache<Path> cache, final NSAlert alert) {
        super(parent, alert);
        this.parent = parent;
        this.cache = cache;
        alert.setShowsHelp(true);
    }

    @Override
    protected void beginSheet(final NSWindow window) {
        this.setAccessoryView(inputField);
        super.beginSheet(window);
    }

    @Override
    protected void focus() {
        this.focus(inputField);
    }

    protected void focus(final NSTextField control) {
        // Focus accessory view.
        control.selectText(null);
        window.makeFirstResponder(control);
        control.currentEditor().setSelectedRange(NSRange.NSMakeRange(
                new NSUInteger(0), new NSUInteger(FilenameUtils.getBaseName(control.stringValue()).length())
        ));
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        if(parent.getSelectionCount() == 1) {
            final Path selected = parent.getSelectedPath();
            if(null != selected) {
                return selected.getParent();
            }
        }
        return parent.workdir();
    }

    protected Path getSelected() {
        return parent.getSelectedPath();
    }

    protected Session getSession() {
        return parent.getSession();
    }

    @Override
    protected boolean validateInput() {
        if(StringUtils.contains(inputField.stringValue(), Path.DELIMITER)) {
            return false;
        }
        if(StringUtils.isNotBlank(inputField.stringValue())) {
            if(cache.get(this.getWorkdir()).contains(new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.file)))) {
                return false;
            }
            if(cache.get(this.getWorkdir()).contains(new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.directory)))) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void help() {
        final StringBuilder site = new StringBuilder(PreferencesFactory.get().getProperty("website.help"));
        site.append("/howto/browser");
        BrowserLauncherFactory.get().open(site.toString());
    }
}
