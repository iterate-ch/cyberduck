package ch.cyberduck.core.threading;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Controller;

public abstract class ControllerMainAction extends MainAction {

    private Controller controller;

    public ControllerMainAction(Controller c) {
        this.controller = c;
    }

    /**
     * @return True if hte window is still on screen
     */
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object lock() {
        return controller;
    }
}