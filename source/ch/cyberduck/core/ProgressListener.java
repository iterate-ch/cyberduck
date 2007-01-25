package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.enterprisedt.net.ftp.FTPException;
import com.sshtools.j2ssh.SshException;

import com.apple.cocoa.foundation.NSBundle;

import java.io.IOException;
import java.net.SocketException;

/**
 * @version $Id$
 */
public abstract class ProgressListener {

    protected String getErrorText(Exception failure) {
        String alert = failure.getMessage();
        String title = NSBundle.localizedString("Error", "");
        if(failure instanceof FTPException) {
            title = "FTP " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof SshException) {
            title = "SSH " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof SocketException) {
            title = "Network " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof IOException) {
            title = "I/O " + NSBundle.localizedString("Error", "");
        }
        return title + ": " + alert;
    }

    /**
     *
     * @param message
     */
    public abstract void message(final String message);
}
