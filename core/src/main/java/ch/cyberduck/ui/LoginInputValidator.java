package ch.cyberduck.ui;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;

public class LoginInputValidator implements InputValidator {
    private final Credentials credentials;
    private final Host bookmark;
    private final LoginOptions options;

    public LoginInputValidator(final Credentials credentials, final Host bookmark, final LoginOptions options) {
        this.credentials = credentials;
        this.bookmark = bookmark;
        this.options = options;
    }

    @Override
    public boolean validate() {
        return credentials.validate(bookmark.getProtocol(), options);
    }
}
