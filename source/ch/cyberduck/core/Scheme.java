package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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
 * @version $Id$
 */
public enum Scheme {
    ftp {
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public int getPort() {
            return 21;
        }
    },
    ftps {
        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public int getPort() {
            return 21;
        }
    },
    sftp {
        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public int getPort() {
            return 22;
        }
    },
    http {
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public int getPort() {
            return 80;
        }
    },
    https {
        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public int getPort() {
            return 443;
        }
    }

    ;

    public abstract boolean isSecure();
    
    public abstract int getPort();
}
