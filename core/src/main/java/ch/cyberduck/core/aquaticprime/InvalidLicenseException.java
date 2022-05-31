package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;

public class InvalidLicenseException extends BackgroundException {

    public InvalidLicenseException() {
        super(LocaleFactory.localizedString("Not a valid registration key", "License"), (String) null);
    }

    public InvalidLicenseException(final String detail) {
        super(LocaleFactory.localizedString("Not a valid registration key", "License"), detail);
    }
}
