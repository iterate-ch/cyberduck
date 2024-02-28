package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class EueLargeUploadService extends HttpUploadFeature<EueWriteFeature.Chunk, MessageDigest> {
    private static final Logger log = LogManager.getLogger(EueLargeUploadService.class);

    private final EueSession session;
    private final Long chunksize;
    private final Integer concurrency;
    private final EueResourceIdProvider fileid;

    private Write<EueWriteFeature.Chunk> writer;

    public EueLargeUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueWriteFeature.Chunk> writer) {
        this(session, fileid, writer, new HostPreferences(session.getHost()).getLong("eue.upload.multipart.size"),
                new HostPreferences(session.getHost()).getInteger("eue.upload.multipart.concurrency"));
    }

    public EueLargeUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueWriteFeature.Chunk> writer, final Long chunksize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.writer = writer;
        this.chunksize = chunksize;
        this.concurrency = concurrency;
        this.fileid = fileid;
    }

    @Override
    public EueWriteFeature.Chunk upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                        final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            final List<Future<EueWriteFeature.Chunk>> parts = new ArrayList<>();
            long offset = 0;
            long remaining = status.getLength();
            final String resourceId;
            final String uploadUri;
            if(status.isExists()) {
                resourceId = fileid.getFileId(file);
                uploadUri = EueUploadHelper.updateResource(session, resourceId, status, UploadType.CHUNKED).getUploadURI();
            }
            else {
                final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = EueUploadHelper.
                        createResource(session, fileid.getFileId(file.getParent()), file.getName(),
                                status, UploadType.CHUNKED);
                resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
                uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            }
            for(int partNumber = 1; remaining > 0; partNumber++) {
                final long length = Math.min(chunksize, remaining);
                parts.add(this.submit(pool, file, local, throttle, listener, status,
                        uploadUri, resourceId, partNumber, offset, length, callback));
                remaining -= length;
                offset += length;
            }
            // Checksums for uploaded segments
            final List<EueWriteFeature.Chunk> chunks = Interruptibles.awaitAll(parts);
            // Full size of file
            final long size = status.getOffset() + status.getLength();
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            chunks.stream().sorted(Comparator.comparing(EueWriteFeature.Chunk::getPartnumber)).forEach(chunk -> {
                try {
                    messageDigest.update(Hex.decodeHex(chunk.getChecksum().hash));
                }
                catch(DecoderException e) {
                    log.error(String.format("Failure %s decoding hash %s", e, chunk.getChecksum()));
                }
                messageDigest.update(ChunkListSHA256ChecksumCompute.intToBytes(chunk.getLength().intValue()));
            });
            final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
            final EueUploadHelper.UploadResponse completedUploadResponse = new EueMultipartUploadCompleter(session)
                    .getCompletedUploadResponse(uploadUri, size, cdash64);
            if(!StringUtils.equals(cdash64, completedUploadResponse.getCdash64())) {
                if(file.getType().contains(Path.Type.encrypted)) {
                    log.warn(String.format("Skip checksum verification for %s with client side encryption enabled", file));
                }
                else {
                    throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                            MessageFormat.format("Mismatch between {0} hash {1} of uploaded data and ETag {2} returned by the server",
                                    HashAlgorithm.cdash64, cdash64, completedUploadResponse.getCdash64()));
                }
            }
            final EueWriteFeature.Chunk object = new EueWriteFeature.Chunk(resourceId, size, cdash64);
            // Mark parent status as complete
            status.withResponse(new EueAttributesAdapter().toAttributes(object)).setComplete();
            return object;
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private Future<EueWriteFeature.Chunk> submit(final ThreadPool pool, final Path file, final Local local,
                                                 final BandwidthThrottle throttle, final StreamListener listener,
                                                 final TransferStatus overall, final String url, final String resourceId,
                                                 final int partNumber, final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s to queue with offset %d and length %d", file, offset, length));
        }
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<EueWriteFeature.Chunk>() {
            @Override
            public EueWriteFeature.Chunk call() throws BackgroundException {
                overall.validate();
                final Map<String, String> parameters = new HashMap<>();
                parameters.put(EueWriteFeature.RESOURCE_ID, resourceId);
                final TransferStatus status = new TransferStatus()
                        .segment(true)
                        .withOffset(offset)
                        .withLength(length)
                        .withParameters(parameters);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
                status.setUrl(url);
                final EueWriteFeature.Chunk chunk = EueLargeUploadService.this.upload(
                        file, local, throttle, listener, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response %s for part %d", chunk, partNumber));
                }
                return chunk;
            }
        }, overall, counter));
    }

    @Override
    public Upload<EueWriteFeature.Chunk> withWriter(final Write<EueWriteFeature.Chunk> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
