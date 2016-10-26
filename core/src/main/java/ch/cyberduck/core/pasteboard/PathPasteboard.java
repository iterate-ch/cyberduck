package ch.cyberduck.core.pasteboard;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;

public final class PathPasteboard extends Collection<Path> implements Pasteboard<Path> {
    private static final long serialVersionUID = -6390582952938739270L;

    private boolean cut;

    private final Session session;

    protected PathPasteboard(final Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setCut(boolean cut) {
        this.cut = cut;
    }

    public void setCopy(boolean copy) {
        this.cut = !copy;
    }

    public boolean isCut() {
        return cut;
    }

    public boolean isCopy() {
        return !cut;
    }
}
