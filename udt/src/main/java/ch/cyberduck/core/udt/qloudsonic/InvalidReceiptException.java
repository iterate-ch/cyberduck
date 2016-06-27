package ch.cyberduck.core.udt.qloudsonic;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.BackgroundException;

public class InvalidReceiptException extends BackgroundException {
    private static final long serialVersionUID = -4289506919518959293L;

    public InvalidReceiptException() {
        super(LocaleFactory.localizedString("Invalid receipt", "Error"),
                LocaleFactory.localizedString(String.format("Purchase a transfer plan from https://qloudsonic.io to route uploads and downloads to S3 through Qloudsonic %s.",
                        LocaleFactory.localizedString("UDT (UDP-based Data Transfer Protocol)", "S3")), "Support"));
    }
}
