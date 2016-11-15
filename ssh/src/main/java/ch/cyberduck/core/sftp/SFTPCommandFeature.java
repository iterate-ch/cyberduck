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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.io.StreamGobbler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.schmizz.sshj.connection.channel.direct.Session;

public class SFTPCommandFeature implements Command {
    private static final Logger log = Logger.getLogger(SFTPCommandFeature.class);

    private final SFTPSession session;

    public SFTPCommandFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void send(final String command, final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Send command %s", command));
        }
        final Session sess;
        try {
            sess = session.getClient().startSession();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
        try {
            listener.message(command);
            final Session.Command exec = sess.exec(command);

            final BufferedReader stdoutReader = new BufferedReader(
                    new InputStreamReader(new StreamGobbler(exec.getInputStream()), Charset.forName(session.getEncoding())));
            final BufferedReader stderrReader = new BufferedReader(
                    new InputStreamReader(new StreamGobbler(exec.getErrorStream()), Charset.forName(session.getEncoding())));

            try {
                // Here is the output from stdout
                while(true) {
                    final String line = stdoutReader.readLine();
                    if(null == line) {
                        break;
                    }
                    transcript.log(TranscriptListener.Type.response, line);
                }
                // Here is the output from stderr
                final StringBuilder error = new StringBuilder();
                while(true) {
                    String line = stderrReader.readLine();
                    if(null == line) {
                        break;
                    }
                    transcript.log(TranscriptListener.Type.response, line);
                    // Standard error output contains all status messages, not only errors.
                    if(StringUtils.isNotBlank(error.toString())) {
                        error.append(" ");
                    }
                    error.append(line).append(".");
                }
                if(StringUtils.isNotBlank(error.toString())) {
                    throw new InteroperabilityException(error.toString());
                }
                else {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Command %s returned no errors", command));
                    }
                }
            }
            finally {
                IOUtils.closeQuietly(stdoutReader);
                IOUtils.closeQuietly(stderrReader);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}