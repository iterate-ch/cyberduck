package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ctera.directio.DirectIOInputStream;
import ch.cyberduck.core.ctera.model.DirectIO;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.READPERMISSION;
import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.assumeRole;

public class CteraDirectIOReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(CteraDirectIOReadFeature.class);

    private final CteraSession session;

    public CteraDirectIOReadFeature(final CteraSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DirectIO metadata = (DirectIO) status.getParameters().get(CteraBulkFeature.DIRECTIO_PARAMETER);
            log.debug("DirectIO metadata {} retrieved for {}", metadata, file);
            final String secretKey = session.getOrCreateAPIKeys().secretKey;
            if(status.getLength() == 0) {
                return new ChunkSequenceInputStream(Collections.emptyList(), metadata.encrypt_info, secretKey, status.getOffset());
            }
            return new ChunkSequenceInputStream(metadata.chunks, metadata.encrypt_info, secretKey, status.getOffset());
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        assumeRole(file, READPERMISSION);
    }

    private final class ChunkSequenceInputStream extends InputStream {
        private final Enumeration<DirectIO.Chunk> chunks;
        private final DirectIO.EncryptInfo key;
        private final String secretKey;
        private final long offset;

        private InputStream in;
        private long currentPosition = 0L;

        public ChunkSequenceInputStream(final List<DirectIO.Chunk> chunks, final DirectIO.EncryptInfo key, final String secretKey, final long offset) throws IOException {
            this.chunks = Collections.enumeration(chunks);
            this.key = key;
            this.secretKey = secretKey;
            this.offset = offset;
            this.peek();
        }

        private void nextStream() throws IOException {
            if(in != null) {
                in.close();
            }
            this.peek();
        }

        /**
         * Peek at the next chunk in the sequence
         */
        private void peek() throws IOException {
            while(chunks.hasMoreElements()) {
                final DirectIO.Chunk chunk = chunks.nextElement();
                final long chunkStart = currentPosition;
                final long chunkEnd = currentPosition + chunk.len;
                // Skip chunks that are entirely before the offset
                if(chunkEnd <= offset) {
                    log.debug("Skipping chunk {} entirely before offset {}", chunk, offset);
                    currentPosition = chunkEnd;
                    continue;
                }
                log.debug("Request chunk {}", chunk);
                // Open the stream for this chunk
                in = new DirectIOInputStream(new HttpMethodReleaseInputStream(
                        session.getClient().getClient().execute(new HttpGet(chunk.url)),
                        new TransferStatus().setOffset(0L).setLength(chunk.len)), key, secretKey);
                // If this chunk contains the offset, skip bytes before the offset
                if(chunkStart < offset) {
                    final long bytesToSkip = offset - chunkStart;
                    log.debug("Skipping {} bytes in chunk {} to reach offset {}", bytesToSkip, chunk, offset);
                    IOUtils.skip(in, bytesToSkip);
                }
                currentPosition = chunkEnd;
                return;
            }
            in = null;
        }

        @Override
        public int available() throws IOException {
            if(in == null) {
                return 0;
            }
            return in.available();
        }

        @Override
        public int read() throws IOException {
            while(in != null) {
                final int c = in.read();
                if(c != IOUtils.EOF) {
                    return c;
                }
                this.nextStream();
            }
            return IOUtils.EOF;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if(in == null) {
                return IOUtils.EOF;
            }
            else if(b == null) {
                throw new NullPointerException();
            }
            else if(off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            else if(len == 0) {
                return 0;
            }
            do {
                final int n = in.read(b, off, len);
                if(n > 0) {
                    return n;
                }
                this.nextStream();
            }
            while(in != null);
            return IOUtils.EOF;
        }

        @Override
        public void close() throws IOException {
            if(in != null) {
                in.close();
            }
        }
    }
}
