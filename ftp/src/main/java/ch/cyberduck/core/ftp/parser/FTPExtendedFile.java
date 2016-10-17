package ch.cyberduck.core.ftp.parser;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.apache.commons.net.ftp.FTPFile;

public class FTPExtendedFile extends FTPFile {
    private static final long serialVersionUID = 6829009965929769052L;

    /**
     * set user ID upon execution
     */
    private boolean setuid;

    /**
     * set group ID upon execution
     */
    private boolean setgid;

    /**
     * Sticky bit
     */
    private boolean sticky;

    public boolean isSetuid() {
        return setuid;
    }

    public void setSetuid(final boolean setuid) {
        this.setuid = setuid;
    }

    public boolean isSetgid() {
        return setgid;
    }

    public void setSetgid(final boolean setgid) {
        this.setgid = setgid;
    }

    public boolean isSticky() {
        return sticky;
    }

    public void setSticky(final boolean sticky) {
        this.sticky = sticky;
    }
}
