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

/**
 * @version $Id$
 */
public class Version implements Comparable {
    private int major = 0;
    private int minor = 0;
    private int revision = 0;
    private String suffix = "";

    public Version(String version_string) {
        this.parse(version_string);
    }

    /**
        * parses major string in the form major[.minor[.subrevision[extension]]]
     * into this instance.
     */
    private void parse(String version_string) {
        major = 0;
        minor = 0;
        revision = 0;
        suffix = "";
        int pos = 0;
        int startpos = 0;
        int endpos = version_string.length();
        while ( (pos < endpos) && Character.isDigit(version_string.charAt(pos))) {
            pos++;
        }
        major = Integer.parseInt(version_string.substring(startpos,pos));
        if ((pos < endpos) && version_string.charAt(pos)=='.') {
            startpos = ++pos;
            while ( (pos < endpos) && Character.isDigit(version_string.charAt(pos))) {
                pos++;
            }
            minor = Integer.parseInt(version_string.substring(startpos,pos));
        }
        if ((pos < endpos) && version_string.charAt(pos)=='.') {
            startpos = ++pos;
            while ( (pos < endpos) && Character.isDigit(version_string.charAt(pos))) {
                pos++;
            }
            revision = Integer.parseInt(version_string.substring(startpos,pos));
        }
        if (pos < endpos) {
            suffix = version_string.substring(pos);
        }
    }

    /**
     * @return string representation of this major
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(10);
        sb.append(major);
        sb.append('.');
        sb.append(minor);
        sb.append('.');
        sb.append(revision);
        sb.append(suffix);
        return sb.toString();
    }

    /**
        * Compares with other major. Does not take extension into account,
     * as there is no reliable way to order them.
     * @return <0 if this is older major that other,
     *         0 if its same major,
     *         >0 if it's newer major than other
     */
    public int compareTo(Object o) {
        if (null == o)
            throw new NullPointerException();
        if(o  instanceof Version) {
            Version other = (Version)o;
            if (this.major < other.major) return -1;
            if (this.major > other.major) return 1;
            if (this.minor < other.minor) return -1;
            if (this.minor > other.minor) return 1;
            if (this.revision < other.revision) return -1;
            if (this.revision > other.revision) return 1;
            if("".equals(this.suffix)) return 1;
            if("".equals(other.suffix)) return -1;
            return this.suffix.compareToIgnoreCase(other.suffix);
        }
        throw new IllegalArgumentException();
    }
}
