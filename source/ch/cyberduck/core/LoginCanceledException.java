package ch.cyberduck.core;

import ch.cyberduck.core.i18n.Locale;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

/**
 * To be used if a login attempt is canceled by the user
 * @version $Id$
 */
public class LoginCanceledException extends ConnectionCanceledException {

    public LoginCanceledException() {
        super(Locale.localizedString("Login canceled", "Credentials"));
    }
}
