package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.text.MessageFormat;

public class CreateSymlinkWorker extends Worker<Path> {

    private final Preferences preferences = PreferencesFactory.get();

    private final Path link;
    private final Path selected;

    public CreateSymlinkWorker(final Path link, final Path selected) {
        this.link = link;
        this.selected = selected;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        // Symlink pointing to existing file
        final Symlink feature = session.getFeature(Symlink.class);
        if(preferences.getBoolean(String.format("%s.symlink.absolute", session.getHost().getProtocol().getScheme().name()))) {
            // Use absolute symlink targets
            feature.symlink(link, selected.getAbsolute());
        }
        else {
            // Use relative symlink targets
            feature.symlink(link, selected.getName());
        }
        return link;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"), link.getName());
    }
}
