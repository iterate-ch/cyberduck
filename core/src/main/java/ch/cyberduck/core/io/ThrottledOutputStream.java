package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.apache.commons.io.output.ProxyOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class ThrottledOutputStream extends ProxyOutputStream {

    /**
     * The delegate.
     */
    private OutputStream delegate;
    /**
     * Limits throughput.
     */
    private BandwidthThrottle throttle;

    /**
     * Wraps the delegate stream with the given throttle.
     *
     * @param delegate the underlying stream for all IO
     * @param throttle limits throughput.  May be shared with other streams.
     */
    public ThrottledOutputStream(final OutputStream delegate, final BandwidthThrottle throttle) {
        super(delegate);
        this.delegate = delegate;
        this.throttle = throttle;
    }

    /**
     * Write a single byte to the delegate stream, possibly blocking if
     * necessary to ensure that throughput doesn't exceed the limits.
     *
     * @param b the byte to write.
     * @throws IOException if an I/O error occurs on the OutputStream.
     */
    @Override
    public void write(final int b) throws IOException {
        int allow = throttle.request(1); //Note that _request never returns zero.
        delegate.write(b);
    }

    /**
     * Write bytes[offset...offset+totalLength-1] to the delegate stream,
     * possibly blocking if necessary to ensure that throughput doesn't exceed
     * the limits.
     *
     * @param data        the bytes to write.
     * @param offset      the index in the array to start at.
     * @param totalLength the number of bytes to write.
     * @throws IOException if an I/O error occurs on the OutputStream.
     */
    @Override
    public void write(byte[] data, int offset, int totalLength) throws IOException {
        //Note that we delegate directly to out.  Do NOT call super.write();
        //that calls this.write() resulting in HALF the throughput.
        while(totalLength > 0) {
            int length = throttle.request(totalLength);
            delegate.write(data, offset, length);
            totalLength -= length;
            offset += length;
        }
    }
}
