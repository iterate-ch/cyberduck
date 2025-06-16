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
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSComboBox;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.Comparator;

public class GotoController extends AlertController {

    @Outlet
    private NSComboBox folderCombobox;
    @Delegate
    private ProxyController folderComboboxModel;

    private final BrowserController parent;
    private final Cache<Path> cache;

    public GotoController(final BrowserController parent, final Cache<Path> cache) {
        this.parent = parent;
        this.cache = cache;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Go to folder", "Goto"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the pathname to list:", "Goto"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Go", "Goto"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Goto"));
        alert.setIcon(IconCacheFactory.<NSImage>get().folderIcon(64));
        return alert;
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        this.folderCombobox = NSComboBox.textfieldWithFrame(new NSRect(0, 26));
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderComboboxModel = new FolderComboboxModel(parent.workdir());
        this.folderCombobox.setDataSource(folderComboboxModel.id());
        this.folderCombobox.setStringValue(parent.workdir().getAbsolute());
        return folderCombobox;
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        folderCombobox.selectText(null);
    }

    @Override
    public void invalidate() {
        folderCombobox.setDelegate(null);
        folderCombobox.setDataSource(null);
        super.invalidate();
    }

    @Override
    public void callback(final int returncode) {
        switch(returncode) {
            case DEFAULT_OPTION:
                final String filename = folderCombobox.stringValue();
                final Path workdir = parent.workdir();
                final Path directory = PathNormalizer.compose(workdir, filename);
                if(workdir.getParent().equals(directory)) {
                    parent.setWorkdir(directory, workdir);
                }
                else {
                    parent.setWorkdir(directory);
                }
                break;
        }
    }

    @Override
    public boolean validate(final int option) {
        return StringUtils.isNotBlank(folderCombobox.stringValue());
    }

    private final class FolderComboboxModel extends ProxyController implements NSComboBox.DataSource {

        private final Path workdir;

        private final Comparator<Path> comparator = new NullComparator<Path>();
        private final Filter<Path> filter = new NullFilter<Path>() {
            @Override
            public boolean accept(Path p) {
                return p.isDirectory();
            }
        };

        private FolderComboboxModel(final Path workdir) {
            this.workdir = workdir;
        }

        @Override
        public NSInteger numberOfItemsInComboBox(NSComboBox combo) {
            return new NSInteger(cache.get(workdir).filter(comparator, filter).size());
        }

        @Override
        public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final NSInteger row) {
            return NSString.stringWithString(cache.get(workdir)
                    .filter(comparator, filter).get(row.intValue()).getName());
        }
    }
}
