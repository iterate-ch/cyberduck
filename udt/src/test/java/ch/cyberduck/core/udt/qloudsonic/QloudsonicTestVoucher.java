package ch.cyberduck.core.udt.qloudsonic;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.aquaticprime.DonationKey;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

import java.util.UUID;

public class QloudsonicTestVoucher extends DonationKey {
    public QloudsonicTestVoucher() throws LocalAccessDeniedException {
        super(new Local(UUID.randomUUID().toString()));
    }

    @Override
    public String getValue(final String property) {
        return "u9zTIKCXHTWPO9WA4fBsIaQ5SjEH5von";
    }

    @Override
    public boolean verify() {
        return true;
    }
}
