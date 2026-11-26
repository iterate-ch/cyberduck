package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Remembers failures for the remainder of the session to skip sending a command the server does not support. Failures
 * are remembered per path type because servers may support setting the timestamp for files but not for directories.
 */
public abstract class AbstractFTPTimestampFeature implements Timestamp {
    private static final Logger log = LogManager.getLogger(AbstractFTPTimestampFeature.class);

    protected final FTPSession session;

    /**
     * Previous failure by path type for command not supported by server
     */
    private final Map<Path.Type, FTPException> failures = Collections.synchronizedMap(new EnumMap<>(Path.Type.class));

    public AbstractFTPTimestampFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        final Path.Type type = file.isDirectory() ? Path.Type.directory : Path.Type.file;
        final FTPException failure = failures.get(type);
        if(failure != null) {
            log.debug("Skip setting timestamp for {} due to previous failure {}", file, failure.getMessage());
            throw new FTPExceptionMappingService().map("Cannot change timestamp of {0}", failure, file);
        }
        try {
            if(null != status.getModified()) {
                if(!this.send(file, status.getModified())) {
                    final int code = session.getClient().getReplyCode();
                    final String reply = session.getClient().getReplyString();
                    if(this.isUnsupported(code)) {
                        log.warn("Server does not support setting modification time for {}: {}", type, reply);
                        final FTPException unsupported = new FTPException(FTPReply.COMMAND_NOT_IMPLEMENTED, reply);
                        failures.put(type, unsupported);
                        throw unsupported;
                    }
                    throw new FTPException(code, reply);
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
    }

    /**
     * @param file     File or directory
     * @param modified Modification date in milliseconds
     * @return True for positive completion reply
     */
    protected abstract boolean send(Path file, long modified) throws IOException;

    /**
     * @param reply Negative reply code
     * @return True if the reply means the server does not support the command. The failure is then remembered for all
     * further paths of the same type.
     */
    protected boolean isUnsupported(final int reply) {
        switch(reply) {
            case FTPReply.UNRECOGNIZED_COMMAND:
            case FTPReply.SYNTAX_ERROR_IN_ARGUMENTS:
            case FTPReply.COMMAND_NOT_IMPLEMENTED:
            case FTPReply.COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER:
                return true;
        }
        return false;
    }
}
