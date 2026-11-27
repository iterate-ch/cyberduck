package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.MDTMSecondsDateFormatter;

import java.io.IOException;
import java.util.TimeZone;

public class FTPMFMTTimestampFeature extends AbstractFTPTimestampFeature {

    public FTPMFMTTimestampFeature(final FTPSession session) {
        super(session);
    }

    @Override
    protected boolean send(final Path file, final long modified) throws IOException {
        return session.getClient().setModificationTime(file.getAbsolute(),
                new MDTMSecondsDateFormatter().format(modified, TimeZone.getTimeZone("UTC")));
    }
}
