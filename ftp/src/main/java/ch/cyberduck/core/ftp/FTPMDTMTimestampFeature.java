package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.TimeZone;

public class FTPMDTMTimestampFeature extends DefaultTimestampFeature implements Timestamp {
    private static final Logger log = LogManager.getLogger(FTPMDTMTimestampFeature.class);

    private final FTPSession session;

    private FTPException failure;

    public FTPMDTMTimestampFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        if(failure != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip setting timestamp for %s due to previous failure %s", file, failure.getMessage()));
            }
            throw new FTPExceptionMappingService().map("Cannot change timestamp of {0}", failure, file);
        }
        try {
            final MDTMSecondsDateFormatter formatter = new MDTMSecondsDateFormatter();
            if(!FTPReply.isPositiveCompletion(session.getClient().sendCommand(FTPCmd.MDTM, String.format("%s %s",
                    formatter.format(status.getTimestamp(), TimeZone.getTimeZone("UTC")), file.getAbsolute())))) {
                throw failure = new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
    }
}
