package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;

public abstract class FileController extends AlertController {

    private final Path workdir;
    private final Path selected;
    private final Cache<Path> cache;

    @Outlet
    protected final NSTextField inputField;

    public FileController(final Path workdir, final Path selected, final Cache<Path> cache, final NSAlert alert) {
        super(alert, NSAlert.NSInformationalAlertStyle);
        this.workdir = workdir;
        this.selected = selected;
        this.cache = cache;
        this.inputField = NSTextField.textfieldWithFrame(new NSRect(window.frame().size.width.doubleValue(), 22));
        this.inputField.cell().setPlaceholderString(alert.informativeText());
        alert.setShowsHelp(true);
    }

    @Override
    public NSView getAccessoryView() {
        return inputField;
    }

    @Override
    protected void focus() {
        super.focus();
        window.makeFirstResponder(inputField);
        inputField.selectText(null);
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        return workdir;
    }

    protected Path getSelected() {
        return selected;
    }

    @Override
    public boolean validate() {
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
