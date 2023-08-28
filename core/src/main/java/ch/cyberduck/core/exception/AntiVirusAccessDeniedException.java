package ch.cyberduck.core.exception;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LocaleFactory;

public class AntiVirusAccessDeniedException extends LockedException {

    public AntiVirusAccessDeniedException() {
        super(LocaleFactory.localizedString("Threat detected", "SDS"),
                LocaleFactory.localizedString("Malicious content has been detected in this file. We recommend that you do not perform any further actions with the file and inform your system administrator immediately.", "SDS"));
    }

    public AntiVirusAccessDeniedException(final String detail) {
        super(LocaleFactory.localizedString("Threat detected", "SDS"), detail);
    }

    public AntiVirusAccessDeniedException(final String detail, final Throwable cause) {
        super(LocaleFactory.localizedString("Threat detected", "SDS"), detail, cause);
    }

    public AntiVirusAccessDeniedException(final String message, final String detail) {
        super(message, detail);
    }

    public AntiVirusAccessDeniedException(final String message, final String detail, final Throwable cause) {
        super(message, detail, cause);
    }
}
