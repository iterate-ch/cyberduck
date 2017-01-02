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
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;

public abstract class FileController extends AlertController {

    private final Path workdir;
    private final Path selected;
    private final Cache<Path> cache;

    @Outlet
    protected NSTextField inputField;

    public FileController(final Path workdir, final Path selected, final Cache<Path> cache) {
        this.workdir = workdir;
        this.selected = selected;
        this.cache = cache;
    }

    @Override
    public void loadBundle(final NSAlert alert) {
        this.inputField = NSTextField.textfieldWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 22));
        this.inputField.cell().setPlaceholderString(alert.informativeText());
        super.loadBundle(alert);
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        return inputField;
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        alert.window().makeFirstResponder(inputField);
        inputField.selectText(null);
    }

    @Override
    public boolean validate() {
        if(StringUtils.contains(inputField.stringValue(), Path.DELIMITER)) {
            return false;
        }
        if(StringUtils.isNotBlank(inputField.stringValue())) {
            if(cache.get(workdir).contains(new Path(workdir, inputField.stringValue(), EnumSet.of(Path.Type.file)))) {
                return false;
            }
            if(cache.get(workdir).contains(new Path(workdir, inputField.stringValue(), EnumSet.of(Path.Type.directory)))) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void callback(final int returncode) {
        final Path directory = new UploadTargetFinder(workdir).find(selected);
        switch(returncode) {
            case DEFAULT_OPTION:
            case ALTERNATE_OPTION:
                this.callback(returncode, new Path(directory, inputField.stringValue(), EnumSet.of(Path.Type.file)));
                break;
        }
    }

    public abstract void callback(final int returncode, final Path file);

    @Override
    protected String help() {
        return new DefaultProviderHelpService().help("/howto/browser");
    }
}
