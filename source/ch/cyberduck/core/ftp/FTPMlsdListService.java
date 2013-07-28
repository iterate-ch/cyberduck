package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.net.ftp.FTPCmd;

import java.io.IOException;
import java.util.List;

/**
 * @version $Id$
 */
public class FTPMlsdListService implements ListService {

    private FTPSession session;

    public FTPMlsdListService(final FTPSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            if(!session.getClient().changeWorkingDirectory(file.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            if(!session.getClient().setFileType(FTPClient.ASCII_FILE_TYPE)) {
                // Set transfer type for traditional data socket file listings. The data transfer is over the
                // data connection in type ASCII or type EBCDIC.
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            final List<String> list = new FTPDataFallback(session).data(file, new DataConnectionAction<List<String>>() {
                @Override
                public List<String> execute() throws IOException, FTPInvalidListException {
                    return session.getClient().list(FTPCmd.MLSD);
                }
            });
            return new FTPMlsdListResponseReader().read(session, file, list);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }
}
