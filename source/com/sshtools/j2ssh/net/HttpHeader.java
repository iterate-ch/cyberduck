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
package com.sshtools.j2ssh.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @author $author$
 * @version $Revision$
 */
public abstract class HttpHeader {
    /**  */
    protected final static String white_SPACE = " \t\r";
    HashMap fields;

    /**  */
    protected String begin;

    /**
     * Creates a new HttpHeader object.
     */
    protected HttpHeader() {
        fields = new HashMap();
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    protected String readLine(InputStream in) throws IOException {
        StringBuffer lineBuf = new StringBuffer();
        int c;

        while (true) {
            c = in.read();

            if (c == -1) {
                throw new IOException("Failed to read expected HTTP header line");
            }

            if (c == '\n') {
                continue;
            }

            if (c != '\r') {
                lineBuf.append((char) c);
            }
            else {
                break;
            }
        }

        return new String(lineBuf);
    }

    /**
     * @return
     */
    public String getStartLine() {
        return begin;
    }

    /**
     * @return
     */
    public Map getHeaderFields() {
        return fields;
    }

    /**
     * @return
     */
    public Set getHeaderFieldNames() {
        return fields.keySet();
    }

    /**
     * @param headerName
     * @return
     */
    public String getHeaderField(String headerName) {
        return (String) fields.get(headerName.toLowerCase());
    }

    /**
     * @param headerName
     * @param value
     */
    public void setHeaderField(String headerName, String value) {
        fields.put(headerName.toLowerCase(), value);
    }

    /**
     * @return
     */
    public String toString() {
        String str = begin + "\r\n";
        Iterator it = getHeaderFieldNames().iterator();

        while (it.hasNext()) {
            String fieldName = (String) it.next();
            str += (fieldName + ": " + getHeaderField(fieldName) + "\r\n");
        }

        str += "\r\n";

        return str;
    }

    /**
     * @param in
     * @throws IOException
     */
    protected void processHeaderFields(InputStream in)
            throws IOException {
        fields = new HashMap();

        StringBuffer lineBuf = new StringBuffer();
        String lastHeaderName = null;
        int c;

        while (true) {
            c = in.read();

            if (c == -1) {
                throw new IOException("The HTTP header is corrupt");
            }

            if (c == '\n') {
                continue;
            }

            if (c != '\r') {
                lineBuf.append((char) c);
            }
            else {
                if (lineBuf.length() != 0) {
                    String line = lineBuf.toString();
                    lastHeaderName = processNextLine(line, lastHeaderName);
                    lineBuf.setLength(0);
                }
                else {
                    break;
                }
            }
        }

        c = in.read();
    }

    private String processNextLine(String line, String lastHeaderName)
            throws IOException {
        String name;
        String value;
        char c = line.charAt(0);

        if ((c == ' ') || (c == '\t')) {
            name = lastHeaderName;
            value = getHeaderField(lastHeaderName) + " " + line.trim();
        }
        else {
            int n = line.indexOf(':');

            if (n == -1) {
                throw new IOException("HTTP Header encoutered a corrupt field: '" + line + "'");
            }

            name = line.substring(0, n).toLowerCase();
            value = line.substring(n + 1).trim();
        }

        setHeaderField(name, value);

        return name;
    }
}
