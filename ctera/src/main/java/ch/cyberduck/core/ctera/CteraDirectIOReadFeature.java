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
import ch.cyberduck.core.ctera.directio.EncryptInfo;
import ch.cyberduck.core.ctera.model.DirectIO;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class CteraDirectIOReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(CteraDirectIOReadFeature.class);

    public static final String CTERA_WRAPPEDKEY = "wrapped_key";

    private final CteraSession session;

    public CteraDirectIOReadFeature(final CteraSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final EncryptInfo key = new EncryptInfo(status.getParameters().get(CTERA_WRAPPEDKEY), session.getOrCreateAPIKeys().secretKey);
            final List<DirectIO.Chunk> chunks = new ArrayList<>();
            final DirectIO.Chunk chunk = new DirectIO.Chunk();
            chunk.url = status.getUrl();
            chunk.len = status.getLength();
            chunks.add(chunk);
            log.debug("Return chunk {} for file {}", chunk, file);
            return new ChunkSequenceInputStream(chunks, key);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    private final class ChunkSequenceInputStream extends InputStream {

        private final Enumeration<DirectIO.Chunk> chunks;
        private final EncryptInfo key;
        private InputStream in;

        public ChunkSequenceInputStream(final List<DirectIO.Chunk> chunks, final EncryptInfo key) throws IOException {
            this.chunks = Collections.enumeration(chunks);
            this.key = key;
            this.peekNextStream();
        }

        private void nextStream() throws IOException {
            if(in != null) {
                in.close();
            }
            this.peekNextStream();
        }

        private void peekNextStream() throws IOException {
            if(chunks.hasMoreElements()) {
                in = getStream(chunks.nextElement());
            }
            else {
                in = null;
            }
        }

        private InputStream getStream(final DirectIO.Chunk chunk) throws IOException {
            log.debug("Request chunk {}", chunk);
            final HttpGet chunkRequest = new HttpGet(chunk.url);
            final HttpResponse chunkResponse = session.getClient().getClient().execute(chunkRequest);
            return new DirectIOInputStream(new HttpMethodReleaseInputStream(chunkResponse, new TransferStatus()), key);
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
