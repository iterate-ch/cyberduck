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
package com.sshtools.j2ssh.connection;

/**
 * This class implements the reposonse for a connecion protocol global request.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version 1.0
 */
public class GlobalRequestResponse {
    /** The global request failed */
    public static final GlobalRequestResponse REQUEST_FAILED =
        new GlobalRequestResponse(false);

    /** The global request succeeded */
    public static final GlobalRequestResponse REQUEST_SUCCEEDED =
        new GlobalRequestResponse(true);
    private byte responseData[] = null;
    private boolean succeeded;

    /**
     * Creates a new GlobalRequestResponse object.
     *
     * @param succeeded <tt>true</tt> if the request succeeded otherwise
     *        <tt>false</tt>
     */
    protected GlobalRequestResponse(boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * Sets the response data for a successful request
     *
     * @param responseData the response data
     */
    public void setResponseData(byte responseData[]) {
        this.responseData = responseData;
    }

    /**
     * Gets the response data for a successful request.
     *
     * @return an array of response data
     */
    public byte[] getResponseData() {
        return responseData;
    }

    /**
     * Call to determine whether the response indicates success or failure
     *
     * @return <tt>true</tt> if the request succeeded otherwise <tt>false</tt>
     */
    public boolean hasSucceeded() {
        return succeeded;
    }
}
