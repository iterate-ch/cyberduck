/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.util;


/**
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will. This
 * software comes with no guarantees or warranties but with plenty of
 * well-wishing instead! Please visit <a
 * href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 *
 * @author Robert Harder
 * @version 1.3.2
 *
 * @created 20 December 2002
 */
public class JVMUtil {

    /**
     * Return the major version number of the JVM
     *
     * @return major version
     */
    public static int getMajorVersion() {
        return 1;
    }

    /**
     * Return the minor version number of the JVM
     *
     * @return minor version
     */
    public static int getMinorVersion() {
        return 4;
    }

    public static void main(String[] args) {
        System.getProperties().list(System.out);
        System.out.println("Major=" + getMajorVersion());
        System.out.println("Minor=" + getMinorVersion());
    }
}


// end class Base64
