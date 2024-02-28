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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class BufferSegmentingOutputStream extends SegmentingOutputStream {
    private static final Logger log = LogManager.getLogger(BufferSegmentingOutputStream.class);

    private final OutputStream proxy;
    private final Buffer buffer;

    public BufferSegmentingOutputStream(final OutputStream proxy, final Long threshold, final Buffer buffer) {
        super(NullOutputStream.NULL_OUTPUT_STREAM, threshold, new BufferOutputStream(buffer));
        this.proxy = proxy;
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.close();
    }

    @Override
    public void flush() throws IOException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Copy buffer %s to output %s", buffer, proxy));
        }
        IOUtils.copy(new BufferInputStream(buffer), proxy);
        // Reuse buffer
        buffer.truncate(0L);
    }
}
