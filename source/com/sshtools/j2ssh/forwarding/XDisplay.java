/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.forwarding;


/**
 * @author $author$
 * @version $Revision$
 */
public class XDisplay {
    private String host;
    private int display;
    private int screen;
    private int portOffset;

    /**
     * Creates a new XDisplay object.
     *
     * @param string
     */
    public XDisplay(String string) {
        this(string, 6000);
    }

    /**
     * Creates a new XDisplay object.
     *
     * @param string
     * @param portOffset
     */
    public XDisplay(String string, int portOffset) {
        setString(string);
        setPortOffset(portOffset);
    }

    /**
     * @param portOffset
     */
    public void setPortOffset(int portOffset) {
        this.portOffset = portOffset;
    }

    /**
     * @return
     */
    public int getPortOffset() {
        return portOffset;
    }

    /**
     * @param string
     */
    public void setString(String string) {
        int idx = string.indexOf(':');

        if (idx == -1) {
            display = 0;
            host = string;
        }
        else {
            host = string.substring(0, idx);

            String s = string.substring(idx + 1);
            idx = s.indexOf(".");

            if (idx == -1) {
                screen = 0;

                try {
                    display = Integer.parseInt(s);
                }
                catch (NumberFormatException nfe) {
                    display = 0;
                }
            }
            else {
                try {
                    display = Integer.parseInt(s.substring(0, idx));
                }
                catch (NumberFormatException nfe) {
                    display = 0;
                }

                try {
                    screen = Integer.parseInt(s.substring(idx + 1));
                }
                catch (NumberFormatException nfe) {
                    screen = 0;
                }
            }
        }
    }

    /**
     * @return
     */
    public int getPort() {
        return (getDisplay() < getPortOffset())
                ? (getDisplay() + getPortOffset()) : getDisplay();
    }

    /**
     * @return
     */
    public int getScreen() {
        return screen;
    }

    /**
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param display
     */
    public void setDisplay(int display) {
        this.display = display;
    }

    /**
     * @param screen
     */
    public void setScreen(int screen) {
        this.screen = screen;
    }

    /**
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * @return
     */
    public int getDisplay() {
        return display;
    }

    /**
     * @return
     */
    public String toString() {
        return getHost() + ":" + getDisplay() +
                ((getScreen() == 0) ? "" : ("." + getScreen()));
    }
}
