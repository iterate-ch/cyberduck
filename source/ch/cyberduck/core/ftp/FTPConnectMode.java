package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

/**
 * @version $Id:$
 */
public class FTPConnectMode {

    /**
     * Represents active connect mode
     */
    public static FTPConnectMode PORT = new FTPConnectMode() {
        @Override
        public String toString() {
            return "active";
        }
    };

    /**
     * Represents PASV connect mode
     */
    public static FTPConnectMode PASV = new FTPConnectMode() {
        @Override
        public String toString() {
            return "passive";
        }
    };

    /**
     * Private so no-one else can instantiate this class
     */
    private FTPConnectMode() {
    }
}
