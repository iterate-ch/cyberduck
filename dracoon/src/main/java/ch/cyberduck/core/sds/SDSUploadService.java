package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UploadsApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadStatus;
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

public class SDSUploadService {
    private static final Logger log = LogManager.getLogger(SDSUploadService.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSUploadService(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    /**
     * @param file   Remote path
     * @param status Length and modification date for file uploaded
     * @return Uplaod URI
     */
    public CreateFileUploadResponse start(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final CreateFileUploadRequest body = new CreateFileUploadRequest()
                    .size(TransferStatus.UNKNOWN_LENGTH == status.getLength() ? null : status.getLength())
                    .parentId(Long.parseLong(nodeid.getVersionId(file.getParent())))
                    .name(file.getName())
                    .directS3Upload(null);
            if(status.getTimestamp() != null) {
                final SoftwareVersionData version = session.softwareVersion();
                final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(version.getRestApiVersion());
                if(matcher.matches()) {
                    if(new Version(matcher.group(1)).compareTo(new Version("4.22")) >= 0) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set modification timestamp to %d for %s", status.getTimestamp(), file));
                        }
                        body.timestampModification(new DateTime(status.getTimestamp()));
                    }
                }
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Start file upload for %s with %s", file, body));
            }
            return new NodesApi(session.getClient()).createFileUploadChannel(body, StringUtils.EMPTY);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }
    }

    /**
     * Complete file upload
     *
     * @param file        Remote path
     * @param uploadToken Upload token
     * @param status      Transfer status
     * @return Node Id from server
     */
    public Node complete(final Path file, final String uploadToken, final TransferStatus status) throws BackgroundException {
        try {
            final CompleteUploadRequest body = new CompleteUploadRequest()
                    .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep"))
                    .resolutionStrategy(CompleteUploadRequest.ResolutionStrategyEnum.OVERWRITE);
            if(status.getFilekey() != null) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Set file key to %s for %s", status.getFilekey(), file));
                }
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
                final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                        TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                        TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                );
                body.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Complete file upload for %s with token %s", file, uploadToken));
            }
            final Node upload = new UploadsApi(session.getClient()).completeFileUploadByToken(body, uploadToken, StringUtils.EMPTY);
            if(!upload.isIsEncrypted()) {
                final Checksum checksum = status.getChecksum();
                if(Checksum.NONE != checksum) {
                    final Checksum server = Checksum.parse(upload.getHash());
                    if(Checksum.NONE != server) {
                        if(checksum.algorithm.equals(server.algorithm)) {
                            if(!server.equals(checksum)) {
                                throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                        MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                                checksum.hash, server.hash));
                            }
                        }
                    }
                }
            }
            nodeid.cache(file, String.valueOf(upload.getId()));
            return upload;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }
        catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException | UnknownVersionException e) {
            throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    /**
     * Cancel file upload
     *
     * @param file        Remote path
     * @param uploadToken Upload token
     */
    public void cancel(final Path file, final String uploadToken) throws BackgroundException {
        if(log.isWarnEnabled()) {
            log.warn(String.format("Cancel failed upload %s for %s", uploadToken, file));
        }
        try {
            new UploadsApi(session.getClient()).cancelFileUploadByToken(uploadToken);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }
    }

    /**
     * Poll for upload status of direct upload
     *
     * @param file     Remote path
     * @param status   Transfer status
     * @param uploadId Upload Id
     * @return Latest status returned from server
     * @throws BackgroundException Error status received
     */
    public S3FileUploadStatus await(final Path file, final TransferStatus status, final String uploadId) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Await file upload for %s with upload ID %s", file, uploadId));
        }
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<S3FileUploadStatus> response = new AtomicReference<>();
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        final ScheduledThreadPool polling = new ScheduledThreadPool(new LoggingUncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                super.uncaughtException(t, e);
                failure.set(new BackgroundException(e));
                signal.countDown();
            }
        });
        final AtomicLong polls = new AtomicLong();
        final ScheduledFuture<?> f = polling.repeat(() -> {
                    try {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Query upload status for %s (%d)", uploadId, polls.incrementAndGet()));
                        }
                        final S3FileUploadStatus uploadStatus = new NodesApi(session.getClient())
                                .requestUploadStatusFiles(uploadId, StringUtils.EMPTY, null);
                        response.set(uploadStatus);
                        switch(uploadStatus.getStatus()) {
                            case "finishing":
                                // Expected
                                break;
                            case "transfer":
                                failure.set(new InteroperabilityException(uploadStatus.getStatus()));
                                signal.countDown();
                                break;
                            case "error":
                                log.warn(String.format("Error polling for upload status of %s (%d)", file, polls.incrementAndGet()));
                                if(null == uploadStatus.getErrorDetails()) {
                                    log.warn(String.format("Missing error details for upload status %s", uploadStatus));
                                    failure.set(new InteroperabilityException());
                                }
                                else {
                                    if(log.isDebugEnabled()) {
                                        log.debug(String.format("Error in upload status %s", uploadStatus));
                                    }
                                    failure.set(new InteroperabilityException(uploadStatus.getErrorDetails().getMessage()));
                                }
                                signal.countDown();
                                break;
                            case "done":
                                // Set node id in transfer status
                                nodeid.cache(file, String.valueOf(uploadStatus.getNode().getId()));
                                // Mark parent status as complete
                                status.withResponse(new SDSAttributesAdapter(session).toAttributes(uploadStatus.getNode())).setComplete();
                                signal.countDown();
                                break;
                        }
                    }
                    catch(ApiException e) {
                        failure.set(new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file));
                        signal.countDown();
                    }
                }, new HostPreferences(session.getHost()).getLong("sds.upload.s3.status.delay"),
                new HostPreferences(session.getHost()).getLong("sds.upload.s3.status.period"), TimeUnit.MILLISECONDS);
        final long timeout = new HostPreferences(session.getHost()).getLong("sds.upload.s3.status.interrupt.ms");
        final long start = System.currentTimeMillis();
        while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofSeconds(1))) {
            try {
                if(f.isDone()) {
                    Uninterruptibles.getUninterruptibly(f);
                }
                if(System.currentTimeMillis() - start > timeout) {
                    log.error(String.format("Cancel polling for upload status of %s after %dms (%d)",
                            file, System.currentTimeMillis() - start, polls.get()));
                    failure.set(new TransferCanceledException(new ConnectionTimeoutException(file.getAbsolute())));
                    signal.countDown();
                }
            }
            catch(ExecutionException e) {
                Throwables.throwIfInstanceOf(Throwables.getRootCause(e), BackgroundException.class);
                throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
            }
        }
        polling.shutdown();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Polling completed for %s with %d polls in %dms ",
                    file, polls.get(), System.currentTimeMillis() - start));
        }
        if(null != failure.get()) {
            throw failure.get();
        }
        status.setComplete();
        return response.get();
    }
}
