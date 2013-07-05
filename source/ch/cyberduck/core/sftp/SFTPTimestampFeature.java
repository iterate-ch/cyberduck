package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.SFTPExceptionMappingService;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @version $Id:$
 */
public class SFTPTimestampFeature implements Timestamp {
    private static final Logger log = Logger.getLogger(SFTPTimestampFeature.class);

    private SFTPSession session;

    public SFTPTimestampFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void udpate(final Path file, final Long created, final Long modified, final Long accessed) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Changing timestamp of {0} to {1}", "Status"),
                    file.getName(), UserDateFormatterFactory.get().getShortFormat(modified)));

            SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
            int t = (int) (modified / 1000);
            // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
            attrs.atime = t;
            attrs.mtime = t;
            session.sftp().setstat(file.getAbsolute(), attrs);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot change timestamp", e, file);
        }

    }
}
