package ch.cyberduck.core.http;

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

import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.mime.MIME;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelayedHttpMultipartEntity extends DelayedHttpEntity {
    private static final Logger log = LogManager.getLogger(DelayedHttpMultipartEntity.class);

    public static final String DEFAULT_BOUNDARY = "------------------------d8ad73fe428d737a";

    private final TransferStatus status;
    private final CountDownLatch exit = new CountDownLatch(1);

    private static final String CR_LF = "\r\n";
    private static final String TWO_DASHES = "--";

    private final byte[] header;
    private final byte[] footer;


    public DelayedHttpMultipartEntity(final String filename, final TransferStatus status) {
        this(filename, status, DEFAULT_BOUNDARY);
    }

    /**
     * @param status Length
     */
    public DelayedHttpMultipartEntity(final String filename, final TransferStatus status, final String boundary) {
        this.status = status;
        final StringBuilder multipartHeader = new StringBuilder();
        multipartHeader.append(TWO_DASHES);
        multipartHeader.append(boundary);
        multipartHeader.append(CR_LF);
        multipartHeader.append(String.format("Content-Disposition: form-data; name=\"file\"; filename=\"%s\"", filename));
        multipartHeader.append(CR_LF);
        multipartHeader.append(String.format("%s: %s", HTTP.CONTENT_TYPE, StringUtils.isBlank(status.getMime()) ? MimeTypeService.DEFAULT_CONTENT_TYPE : status.getMime()));
        multipartHeader.append(CR_LF);
        multipartHeader.append(CR_LF);
        header = encode(MIME.DEFAULT_CHARSET, multipartHeader.toString()).buffer();
        final StringBuilder multipartFooter = new StringBuilder();
        multipartFooter.append(CR_LF);
        multipartFooter.append(TWO_DASHES);
        multipartFooter.append(boundary);
        multipartFooter.append(TWO_DASHES);
        multipartFooter.append(CR_LF);
        footer = encode(MIME.DEFAULT_CHARSET, multipartFooter.toString()).buffer();
    }

    private static ByteArrayBuffer encode(final Charset charset, final String input) {
        final ByteBuffer encoded = charset.encode(CharBuffer.wrap(input));
        final ByteArrayBuffer bab = new ByteArrayBuffer(encoded.remaining());
        bab.append(encoded.array(), encoded.position(), encoded.remaining());
        return bab;
    }

    /**
     * HTTP stream to write to
     */
    private OutputStream stream;
    /**
     *
     */
    private boolean consumed = false;

    public long getContentLength() {
        return header.length + status.getLength() + footer.length;
    }

    /**
     * @return The stream to write to after the entry signal was received.
     */
    public OutputStream getStream() {
        if(null == stream) {
            // Nothing to write
            return NullOutputStream.NULL_OUTPUT_STREAM;
        }
        return stream;
    }

    public void writeTo(final OutputStream out) throws IOException {
        try {
            stream = new OutputStream() {
                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public void write(final int b) throws IOException {
                    out.write(b);
                }

                @Override
                public void write(final byte[] b) throws IOException {
                    out.write(b);
                }

                @Override
                public void close() throws IOException {
                    if(close.get()) {
                        log.warn(String.format("Skip double close of stream %s", this));
                        return;
                    }
                    try {
                        out.write(footer);
                        super.close();
                    }
                    finally {
                        // Signal finished writing to stream
                        exit.countDown();
                        close.set(true);
                    }
                }
            };
            stream.write(header);
        }
        finally {
            final CountDownLatch entry = this.getStreamOpen();
            // Signal stream is ready for writing
            entry.countDown();
        }
        // Wait for signal when content has been written to the pipe
        Interruptibles.await(exit, IOException.class);
        // Entity written to server
        consumed = true;
    }

    public boolean isStreaming() {
        return !consumed;
    }
}
