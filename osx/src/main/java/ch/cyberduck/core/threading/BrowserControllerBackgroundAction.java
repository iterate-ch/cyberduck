package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.ui.cocoa.BrowserController;

public abstract class BrowserControllerBackgroundAction<T> extends BrowserBackgroundAction<T> {

    public BrowserControllerBackgroundAction(final BrowserController controller) {
        super(controller, controller.getSession(), controller.getCache());
    }

    public BrowserControllerBackgroundAction(final Controller controller, final Session<?> session, final Cache<Path> cache) {
        super(controller, session, cache);
    }
}
