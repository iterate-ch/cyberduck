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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
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
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
import com.google.common.util.concurrent.Uninterruptibles;

public class SDSUploadService {
    private static final Logger log = Logger.getLogger(SDSUploadService.class);

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
                .parentId(Long.parseLong(nodeid.getVersionId(file.getParent(), new DisabledListProgressListener())))
                .name(file.getName())
                .directS3Upload(null);
            if(status.getTimestamp() != null) {
                final SoftwareVersionData version = session.softwareVersion();
                final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(version.getRestApiVersion());
                if(matcher.matches()) {
                    if(new Version(matcher.group(1)).compareTo(new Version("4.22")) >= 0) {
                        body.timestampModification(new DateTime(status.getTimestamp()));
                    }
                }
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
                .keepShareLinks(status.isExists() ? new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep") : false)
                    .resolutionStrategy(status.isExists() ? CompleteUploadRequest.ResolutionStrategyEnum.OVERWRITE : CompleteUploadRequest.ResolutionStrategyEnum.FAIL);
            if(status.getFilekey() != null) {
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
                final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                        TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                        TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                );
                body.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
            }
            final Node node = new UploadsApi(session.getClient()).completeFileUploadByToken(body, uploadToken, StringUtils.EMPTY);
            if(!node.isIsEncrypted()) {
                final Checksum checksum = status.getChecksum();
                if(Checksum.NONE != checksum) {
                    final Checksum server = Checksum.parse(node.getHash());
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
            nodeid.cache(file, String.valueOf(node.getId()));
            // Workaround to await upload to backend storage
            this.poll(file, node.getId());
            return node;
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
        log.warn(String.format("Cancel failed upload %s for %s", uploadToken, file));
        try {
            new UploadsApi(session.getClient()).cancelFileUploadByToken(uploadToken);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }
    }

    private void poll(final Path file, final Long nodeId) throws BackgroundException {
        // Polling
        final ScheduledThreadPool polling = new ScheduledThreadPool();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        final ScheduledFuture f = polling.repeat(new Runnable() {
            @Override
            public void run() {
                try {
                    new NodesApi(session.getClient()).requestNode(nodeId, StringUtils.EMPTY, null);
                    done.countDown();
                }
                catch(ApiException e) {
                    switch(e.getCode()) {
                        case HttpStatus.SC_NOT_FOUND:
                            log.warn(String.format("Wait for node %d with error %s", nodeId, e));
                            break;
                        default:
                            failure.set(new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file));
                            done.countDown();
                    }
                }
            }
        }, new HostPreferences(session.getHost()).getLong("sds.upload.s3.status.period"), TimeUnit.MILLISECONDS);
        Uninterruptibles.awaitUninterruptibly(done);
        polling.shutdown();
        if(null != failure.get()) {
            throw failure.get();
        }
    }
}
