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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class DuplicateFileController extends FileController {

    public DuplicateFileController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Duplicate File", "Duplicate"),
                LocaleFactory.localizedString("Enter the name for the new file:", "Duplicate"),
                LocaleFactory.localizedString("Duplicate", "Duplicate"),
                null,
                LocaleFactory.localizedString("Cancel", "Duplicate")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().fileIcon(this.getSelected(), 64));
        final Path selected = this.getSelected();
        String proposal = MessageFormat.format(PreferencesFactory.get().getProperty("browser.duplicate.format"),
                FilenameUtils.getBaseName(selected.getName()),
                UserDateFormatterFactory.get().getShortFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                StringUtils.isNotEmpty(selected.getExtension()) ? "." + selected.getExtension() : StringUtils.EMPTY);
        inputField.setStringValue(proposal);
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.run(this.getSelected(), inputField.stringValue());
        }
    }

    private void run(final Path selected, final String filename) {
        final Path duplicate = new Path(selected.getParent(), filename, selected.getType());
        parent.duplicatePath(selected, duplicate);
    }
}