package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.RegistryBackgroundAction;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

public class CreateSymlinkController extends FileController {

    public CreateSymlinkController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new symbolic link", "File"),
                StringUtils.EMPTY,
                LocaleFactory.localizedString("Create", "File"),
                null,
                LocaleFactory.localizedString("Cancel", "File")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().aliasIcon(null, 64));
        final Path selected = this.getSelected();
        inputField.setStringValue(FilenameUtils.getBaseName(selected.getName()));
        this.setMessage(MessageFormat.format(LocaleFactory.localizedString("Enter the name for the new symbolic link for {0}:", "File"),
                selected.getName()));
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.run(this.getSelected(), inputField.stringValue(), false);
        }
    }

    protected void run(final Path selected, final String symlink, final boolean edit) {
        final Path link = new Path(this.getWorkdir(), symlink, EnumSet.of(Path.Type.file));
        parent.background(new RegistryBackgroundAction<Path>(parent, parent.getSession()) {
            @Override
            public Path run(final Session<?> session) throws BackgroundException {
                // Symlink pointing to existing file
                final Symlink feature = session.getFeature(Symlink.class);
                if(PreferencesFactory.get().getBoolean(
                        String.format("%s.symlink.absolute", session.getHost().getProtocol().getScheme().name()))) {
                    feature.symlink(link, selected.getAbsolute());
                }
                else {
                    feature.symlink(link, selected.getName());
                }
                return link;
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                        symlink);
            }

            @Override
            public void cleanup() {
                super.cleanup();
                if(symlink.charAt(0) == '.') {
                    parent.setShowHiddenFiles(true);
                }
                parent.reload(parent.workdir(), Collections.singletonList(link), Collections.singletonList(link));
            }
        });
    }
}