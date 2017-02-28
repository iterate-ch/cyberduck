package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.output.ThresholdingOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class SegmentingOutputStream extends ThresholdingOutputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
            PreferencesFactory.get().getInteger("openstack.upload.largeobject.size.minimum"));
    private final OutputStream proxy;
    private int threshold;

    public SegmentingOutputStream(final OutputStream proxy, final int threshold) {
        super(threshold);
        this.proxy = proxy;
        this.threshold = threshold;
    }

    @Override
    protected OutputStream getStream() throws IOException {
        return buffer;
    }

    @Override
    protected void thresholdReached() throws IOException {
        // This implementation may trigger the event before the threshold is actually reached, since it triggers
        // when a pending write operation would cause the threshold to be exceeded.
        if(this.getByteCount() >= this.getThreshold()) {
            this.copy();
        }
    }

    @Override
    public void close() throws IOException {
        this.copy();
        proxy.close();
    }

    private void copy() throws IOException {
        final byte[] content = buffer.toByteArray();
        for(int off = 0; off < content.length; off += threshold) {
            int len = Math.min(threshold, content.length - off);
            proxy.write(Arrays.copyOfRange(content, off, off + len));
        }
        // Re-use buffer
        buffer.reset();
        // Wait for trigger of next threshold
        this.resetByteCount();
    }

}
