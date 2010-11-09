package ch.cyberduck.core.io;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 */
public class ThrottledInputStream extends InputStream {
    private static Logger log = Logger.getLogger(ThrottledInputStream.class);

    /**
     * The delegate.
     */
    private InputStream _delegate;

    /**
     * Limits throughput.
     */
    private BandwidthThrottle _throttle;

    public ThrottledInputStream(InputStream delegate, BandwidthThrottle throttle) {
        this._delegate = delegate;
        this._throttle = throttle;
    }

    /**
     * Read a single byte from this InputStream.
     *
     * @throws IOException if an I/O error occurs on the InputStream.
     */
    @Override
    public int read() throws IOException {
        return _delegate.read();
    }

    /**
     * Read an array of bytes from this InputStream.
     *
     * @param data   the bytes to read.
     * @param offset the index in the array to start at.
     * @param len    the number of bytes to read.
     * @throws IOException if an I/O error occurs on the InputStream.
     */
    @Override
    public int read(byte[] data, int offset, int len) throws IOException {
        return _delegate.read(data, offset, _throttle.request(len));
    }

    @Override
    public void close() throws IOException {
        _delegate.close();
    }
}