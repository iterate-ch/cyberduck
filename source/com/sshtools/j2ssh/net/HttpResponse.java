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

import java.io.*;

import java.util.*;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class HttpResponse extends HttpHeader {
    private String version;
    private int status;
    private String reason;

    /**
     * Creates a new HttpResponse object.
     *
     * @param input
     *
     * @throws IOException
     */
    public HttpResponse(InputStream input) throws IOException {
        begin = readLine(input);

        while (begin.trim().length() == 0) {
            begin = readLine(input);
        }

        processResponse();
        processHeaderFields(input);
    }

    /**
     *
     *
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     *
     *
     * @return
     */
    public String getReason() {
        return reason;
    }

    private void processResponse() throws IOException {
        StringTokenizer tokens = new StringTokenizer(begin, white_SPACE, false);

        try {
            version = tokens.nextToken();
            status = Integer.parseInt(tokens.nextToken());
            reason = tokens.nextToken();
        } catch (NoSuchElementException e) {
            throw new IOException("Failed to read HTTP repsonse header");
        } catch (NumberFormatException e) {
            throw new IOException("Failed to read HTTP resposne header");
        }
    }

    /**
     *
     *
     * @return
     */
    public String getAuthenticationMethod() {
        String auth = getHeaderField("Proxy-Authenticate");
        String method = null;

        if (auth != null) {
            int n = auth.indexOf(' ');
            method = auth.substring(0, n);
        }

        return method;
    }

    /**
     *
     *
     * @return
     */
    public String getAuthenticationRealm() {
        String auth = getHeaderField("Proxy-Authenticate");
        String realm = null;

        if (auth != null) {
            int l;
            int r = auth.indexOf('=');

            while (r >= 0) {
                l = auth.lastIndexOf(' ', r);
                realm = auth.substring(l + 1, r);

                if (realm.equalsIgnoreCase("realm")) {
                    l = r + 2;
                    r = auth.indexOf('"', l);
                    realm = auth.substring(l, r);

                    break;
                }

                r = auth.indexOf('=', r + 1);
            }
        }

        return realm;
    }
}
