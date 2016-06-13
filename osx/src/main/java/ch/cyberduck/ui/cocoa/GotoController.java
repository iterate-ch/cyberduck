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
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSComboBox;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.features.Home;
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

    private Cache<Path> cache;

    private final class FolderComboboxModel extends ProxyController implements NSComboBox.DataSource {

        private Path workdir;

        private final Comparator<Path> comparator = new NullComparator<Path>();

        private FolderComboboxModel(final Path workdir) {
            this.workdir = workdir;
        }

        private final Filter<Path> filter = new Filter<Path>() {
            @Override
            public boolean accept(Path p) {
                return p.isDirectory();
            }
        };

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

    public GotoController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, NSAlert.alert(
                LocaleFactory.localizedString("Go to folder", "Goto"),
                LocaleFactory.localizedString("Enter the pathname to list:", "Goto"),
                LocaleFactory.localizedString("Go", "Goto"),
                null,
                LocaleFactory.localizedString("Cancel", "Goto")
        ));
        this.cache = cache;
        alert.setIcon(IconCacheFactory.<NSImage>get().folderIcon(64));
        folderCombobox = NSComboBox.textfieldWithFrame(new NSRect(0, 26));
        folderCombobox.setCompletes(true);
        folderCombobox.setUsesDataSource(true);
        folderComboboxModel = new FolderComboboxModel(parent.workdir());
        folderCombobox.setDataSource(folderComboboxModel.id());
        folderCombobox.setStringValue(parent.workdir().getAbsolute());
        this.setAccessoryView(folderCombobox);
    }

    @Override
    protected void focus() {
        // Focus accessory view.
        folderCombobox.selectText(null);
        window.makeFirstResponder(folderCombobox);
    }

    @Override
    public void invalidate() {
        folderCombobox.setDelegate(null);
        folderCombobox.setDataSource(null);
        super.invalidate();
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final String filename = folderCombobox.stringValue();
            final BrowserController controller = (BrowserController) parent;
            final Path workdir = controller.workdir();
            final Path directory = controller.getSession().getFeature(Home.class).find(workdir, filename);
            if(workdir.getParent().equals(directory)) {
                controller.setWorkdir(directory, workdir);
            }
            else {
                controller.setWorkdir(directory);
            }
        }
    }

    @Override
    protected boolean validateInput() {
        return StringUtils.isNotBlank(folderCombobox.stringValue());
    }

    protected Session getSession() {
        return ((BrowserController) parent).getSession();
    }
}