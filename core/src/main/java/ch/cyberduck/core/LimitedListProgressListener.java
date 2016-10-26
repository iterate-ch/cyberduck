package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class LimitedListProgressListener implements ListProgressListener {

    private final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Limit for containers
     */
    private Integer container
            = preferences.getInteger("browser.list.limit.container");

    /**
     * Limit for regular directories
     */
    private Integer directory
            = preferences.getInteger("browser.list.limit.directory");

    private final ProgressListener delegate;

    public LimitedListProgressListener(final ProgressListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void message(final String message) {
        delegate.message(message);
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ListCanceledException {
        if(folder.isRoot()) {
            if(list.size() >= container) {
                // Allow another chunk until limit is reached again
                container += preferences.getInteger("browser.list.limit.container");
                throw new ListCanceledException(list);
            }
        }
        if(list.size() >= directory) {
            // Allow another chunk until limit is reached again
            directory += preferences.getInteger("browser.list.limit.directory");
            throw new ListCanceledException(list);
        }
    }

    protected void disable() {
        PreferencesFactory.get().setProperty("browser.list.limit.directory", Integer.MAX_VALUE);
        PreferencesFactory.get().setProperty("browser.list.limit.container", Integer.MAX_VALUE);
    }
}
