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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.features.Command;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.ethz.ssh2.StreamGobbler;

/**
 * @version $Id:$
 */
public class SFTPCommandFeature implements Command {
    private static final Logger log = Logger.getLogger(SFTPCommandFeature.class);

    private SFTPSession session;

    public SFTPCommandFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void send(final String command) throws BackgroundException {
        ch.ethz.ssh2.Session sess = null;
        try {
            sess = session.getClient().openSession();

            final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
            final BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));

            try {
                session.message(command);
                sess.execCommand(command, session.getEncoding());

                // Here is the output from stdout
                while(true) {
                    String line = stdoutReader.readLine();
                    if(null == line) {
                        break;
                    }
                    session.log(false, line);
                }
                // Here is the output from stderr
                StringBuilder error = new StringBuilder();
                while(true) {
                    String line = stderrReader.readLine();
                    if(null == line) {
                        break;
                    }
                    session.log(false, line);
                    // Standard error output contains all status messages, not only errors.
                    if(StringUtils.isNotBlank(error.toString())) {
                        error.append(" ");
                    }
                    error.append(line).append(".");
                }
                if(StringUtils.isNotBlank(error.toString())) {
                    throw new BackgroundException(error.toString(), null);
                }
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
            finally {
                IOUtils.closeQuietly(stdoutReader);
                IOUtils.closeQuietly(stderrReader);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            if(sess != null) {
                sess.close();
            }
        }
    }
}