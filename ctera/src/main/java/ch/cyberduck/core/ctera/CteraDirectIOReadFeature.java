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
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ctera.directio.DirectIOInputStream;
import ch.cyberduck.core.ctera.directio.EncryptInfo;
import ch.cyberduck.core.ctera.model.DirectIO;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.Range;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CteraDirectIOReadFeature implements Read {

    private final CteraSession session;
    private final CteraFileIdProvider fileid;

    public CteraDirectIOReadFeature(final CteraSession session, final CteraFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DirectIO directio = this.getMetadata(file);
            final EncryptInfo key = new EncryptInfo(directio.wrapped_key, System.getenv("CTERA_SECRET_KEY"), true);
            final TransferInfo info = this.getTransferInfo(directio, status);
            final ChunkSequenceInputStream stream = new ChunkSequenceInputStream(info.chunks, key);
            // Skip to requested position in first relevant chunk
            stream.skip(info.offset);
            return stream;
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    private DirectIO getMetadata(final Path file) throws IOException, BackgroundException {
        final HttpGet request = new HttpGet(String.format("%s/directio/%s", new HostUrlProvider().withPath(false).get(session.getHost()), fileid.getFileId(file)));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("CTERA_ACCESS_KEY")); // access token
        final HttpResponse response = session.getClient().getClient().execute(request);
        ObjectMapper mapper = new ObjectMapper();
        final HttpMethodReleaseInputStream stream = new HttpMethodReleaseInputStream(response, new TransferStatus());
        final DirectIO directio = mapper.readValue(stream, DirectIO.class);
        stream.close();
        return directio;
    }

    protected TransferInfo getTransferInfo(final DirectIO directio, final TransferStatus status) {
        long fileSize = directio.chunks.stream().mapToLong(chunk -> chunk.len).sum();
        if(status.isAppend()) {
            final List<DirectIO.Chunk> chunks = new ArrayList<>();
            final Range<Long> requestedRange;
            if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                requestedRange = Range.of(status.getOffset(), fileSize);
            }
            else {
                requestedRange = Range.of(status.getOffset(), status.getOffset() + status.getLength());
            }
            long offsetInFirstChunk = 0;
            long position = 0;
            for(DirectIO.Chunk chunk : directio.chunks) {
                final Range<Long> chunkRange = Range.of(position, position + chunk.len);
                if(chunkRange.getMaximum() > requestedRange.getMinimum()) {
                    if(chunkRange.contains(requestedRange.getMinimum())) {
                        // First relevant chunk - calculate offset
                        offsetInFirstChunk = requestedRange.getMinimum() - position;
                    }
                    final Range<Long> readRangeInChunk = Range.of(position, Math.min(position + chunk.len, requestedRange.getMaximum()));
                    final long toRead = readRangeInChunk.getMaximum() - readRangeInChunk.getMinimum();
                    if(toRead > 0) {
                        chunks.add(chunk);
                    }
                    else {
                        return new TransferInfo(chunks, offsetInFirstChunk);
                    }
                    if(chunkRange.getMaximum() > requestedRange.getMaximum()) {
                        // Partial read of chunk
                        return new TransferInfo(chunks, offsetInFirstChunk);
                    }
                }
                position += chunk.len;
            }
            return new TransferInfo(chunks, offsetInFirstChunk);
        }
        return new TransferInfo(directio.chunks, 0);
    }

    protected static final class TransferInfo {

        public List<DirectIO.Chunk> chunks;
        public long offset;

        /***
         * @param chunks Normalized list of chunks
         * @param offset Offset for first chunk
         */
        public TransferInfo(final List<DirectIO.Chunk> chunks, final long offset) {
            this.chunks = chunks;
            this.offset = offset;
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
                if(c != -1) {
                    return c;
                }
                this.nextStream();
            }
            return -1;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if(in == null) {
                return -1;
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
                nextStream();
            }
            while(in != null);
            return -1;
        }

        @Override
        public void close() throws IOException {
            if(in != null) {
                in.close();
            }
        }
    }
}
