package ch.cyberduck.ui.action;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class MoveWorker extends Worker<Boolean> {

    private Session<?> session;

    private Map<Path, Path> files;

    public MoveWorker(final Session<?> session, final Map<Path, Path> files) {
        this.session = session;
        this.files = files;
    }

    @Override
    public Boolean run() throws BackgroundException {
        final Move feature = session.getFeature(Move.class);
        for(Map.Entry<Path, Path> entry : files.entrySet()) {
            feature.move(entry.getKey(), entry.getValue(), false);
        }
        return true;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Renaming {0} to {1}", "Status"),
                files.keySet().iterator().next().getName(), files.values().iterator().next().getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final MoveWorker that = (MoveWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }
}
