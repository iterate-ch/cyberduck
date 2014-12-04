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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.foundation.NSRange;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public abstract class FileController extends AlertController {

    @Outlet
    protected NSTextField inputField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    public void setInputField(final NSTextField inputField) {
        this.inputField = inputField;
    }

    private Cache<Path> cache;

    public FileController(final WindowController parent, final Cache<Path> cache, final NSAlert alert) {
        super(parent, alert);
        this.cache = cache;
        alert.setShowsHelp(true);
    }

    @Override
    public void beginSheet() {
        this.setAccessoryView(inputField);
        super.beginSheet();
    }

    @Override
    protected void focus() {
        this.focus(inputField);
    }

    protected void focus(final NSTextField control) {
        // Focus accessory view.
        control.selectText(null);
        this.window().makeFirstResponder(control);
        control.currentEditor().setSelectedRange(NSRange.NSMakeRange(
                new NSUInteger(0), new NSUInteger(FilenameUtils.getBaseName(control.stringValue()).length())
        ));
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        if(((BrowserController) parent).getSelectionCount() == 1) {
            final Path selected = ((BrowserController) parent).getSelectedPath();
            if(null != selected) {
                return selected.getParent();
            }
        }
        return ((BrowserController) parent).workdir();
    }

    protected Path getSelected() {
        return ((BrowserController) parent).getSelectedPath();
    }

    protected Session getSession() {
        return ((BrowserController) parent).getSession();
    }

    @Override
    protected boolean validateInput() {
        if(StringUtils.contains(inputField.stringValue(), Path.DELIMITER)) {
            return false;
        }
        if(StringUtils.isNotBlank(inputField.stringValue())) {
            if(cache.lookup(new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.file)).getReference()) != null) {
                return false;
            }
            if(cache.lookup(new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.directory)).getReference()) != null) {
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
