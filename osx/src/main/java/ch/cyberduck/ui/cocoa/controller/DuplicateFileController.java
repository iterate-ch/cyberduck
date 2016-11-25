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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.transfer.CopyTransfer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class DuplicateFileController extends FileController {

    public DuplicateFileController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Duplicate File", "Duplicate"),
                LocaleFactory.localizedString("Enter the name for the new file:", "Duplicate"),
                LocaleFactory.localizedString("Duplicate", "Duplicate"),
                null,
                LocaleFactory.localizedString("Cancel", "Duplicate")
        ));
        final Path selected = this.getSelected();
        alert.setIcon(IconCacheFactory.<NSImage>get().fileIcon(selected, 64));
        String proposal = MessageFormat.format(PreferencesFactory.get().getProperty("browser.duplicate.format"),
                FilenameUtils.getBaseName(selected.getName()),
                UserDateFormatterFactory.get().getShortFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                StringUtils.isNotEmpty(selected.getExtension()) ? "." + selected.getExtension() : StringUtils.EMPTY);
        inputField.setStringValue(proposal);
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.duplicate(this.getSelected(), inputField.stringValue());
        }
    }

    public void duplicate(final Path selected, final String filename) {
        final Path duplicate = new Path(selected.getParent(), filename, selected.getType());
        this.duplicate(selected, duplicate);
    }

    /**
     * @param source      The original file to duplicate
     * @param destination The destination of the duplicated file
     */
    public void duplicate(final Path source, final Path destination) {
        this.duplicate(Collections.singletonMap(source, destination));
    }

    /**
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     */
    public void duplicate(final Map<Path, Path> selected) {
        new OverwriteController(parent).overwrite(new ArrayList<Path>(selected.values()), new DefaultMainAction() {
            @Override
            public void run() {
                final Host target = parent.getSession().getHost();
                parent.transfer(new CopyTransfer(parent.getSession().getHost(),
                                SessionFactory.create(target), selected),
                        new ArrayList<Path>(selected.values()), true);
            }
        });
    }

}