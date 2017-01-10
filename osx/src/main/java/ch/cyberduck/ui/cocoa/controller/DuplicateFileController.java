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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

public class DuplicateFileController extends FileController {

    private final Path selected;
    private final Callback callback;

    public DuplicateFileController(final Path workdir, final Path selected, final Cache<Path> cache, final Callback callback) {
        super(workdir, selected, cache);
        this.selected = selected;
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Duplicate File", "Duplicate"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the name for the new file", "Duplicate"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Duplicate", "Duplicate"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Duplicate"));
        alert.setIcon(IconCacheFactory.<NSImage>get().fileIcon(selected, 64));
        super.loadBundle(alert);
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        final NSView view = super.getAccessoryView(alert);
        String proposal = MessageFormat.format(PreferencesFactory.get().getProperty("browser.duplicate.format"),
                FilenameUtils.getBaseName(selected.getName()),
                UserDateFormatterFactory.get().getShortFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                StringUtils.isNotEmpty(selected.getExtension()) ? "." + selected.getExtension() : StringUtils.EMPTY);
        this.updateField(inputField, proposal);
        return view;
    }

    @Override
    public void callback(final int returncode, final Path file) {
        callback.callback(Collections.singletonMap(selected, file));
    }

    public interface Callback {
        void callback(final Map<Path, Path> selected);
    }
}