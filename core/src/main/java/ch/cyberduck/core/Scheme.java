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

import org.apache.commons.lang3.StringUtils;

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
    scp {
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
    },
    rtmp {
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public int getPort() {
            return 1935;
        }
    },
    udt {
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public int getPort() {
            return 4280;
        }
    },
    udts {
        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public int getPort() {
            return 42443;
        }
    },
    irods {
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public int getPort() {
            return 1247;
        }
    },
    file {
        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public int getPort() {
            return 0;
        }
    };

    public abstract boolean isSecure();

    public abstract int getPort();

    /**
     * @param str Determine if URL can be handled by a registered protocol
     * @return True if known URL
     */
    public static boolean isURL(final String str) {
        if(StringUtils.isNotBlank(str)) {
            for(Scheme scheme : Scheme.values()) {
                if(scheme.equals(Scheme.file)) {
                    continue;
                }
                if(str.startsWith(scheme + "://")) {
                    return true;
                }
            }
        }
        return false;
    }
}
