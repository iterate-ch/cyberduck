package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptOutputStream;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.NullOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoSystemException;
import com.dracoon.sdk.crypto.InvalidFileKeyException;

public class SDSEncryptionBulkFeature implements Bulk<Void> {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSEncryptionBulkFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default: {
                final Map<Path, Boolean> rooms = this.getRoomEncryptionStatus(files);
                for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                    final Path container = new PathContainerService().getContainer(entry.getKey().remote);
                    if(rooms.get(container)) {
                        final TransferStatus status = entry.getValue();
                        final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey());
                        status.setFilekey(nodeid.getFileKey(fileKey));
                        if(PreferencesFactory.get().getBoolean("sds.upload.s3.enable")) {
                            if(session.configuration().stream().anyMatch(property -> "use_s3_storage".equals(property.getKey()) && String.valueOf(true).equals(property.getValue()))) {
                                // Pre-compute file key tag for upload to S3 with multiple parts
                                final InputStream in = entry.getKey().local.getInputStream();
                                final OutputStream out;
                                try {
                                    out = new TripleCryptOutputStream<>(session, new StatusOutputStream<TransferStatus>(NullOutputStream.NULL_OUTPUT_STREAM) {
                                        @Override
                                        public TransferStatus getStatus() {
                                            return status;
                                        }
                                    }, Crypto.createFileEncryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)), status);
                                }
                                catch(CryptoSystemException | InvalidFileKeyException e) {
                                    throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, entry.getKey().remote);
                                }
                                new StreamCopier(status, new TransferStatus()).transfer(in, out);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Map<Path, Boolean> getRoomEncryptionStatus(final Map<TransferItem, TransferStatus> files) {
        final Map<Path, Boolean> rooms = new HashMap<>();
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            final Path container = new PathContainerService().getContainer(entry.getKey().remote);
            if(rooms.containsKey(container)) {
                continue;
            }
            rooms.put(container, nodeid.isEncrypted(entry.getKey().remote));
        }
        return rooms;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default:
                if(PreferencesFactory.get().getBoolean("sds.encryption.missingkeys.upload")) {
                    if(session.userAccount().isEncryptionEnabled()) {
                        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature();
                        final Map<Path, Boolean> rooms = this.getRoomEncryptionStatus(files);
                        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                            final Path file = entry.getKey().remote;
                            final Path container = new PathContainerService().getContainer(file);
                            if(rooms.get(container)) {
                                final VersionId version = entry.getValue().getVersion();
                                if(null != version) {
                                    background.operate(session, callback, file.withAttributes(new PathAttributes(file.attributes()).withVersionId(version.id)));
                                }
                            }
                        }
                    }
                }
        }
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }

    @Override
    public Bulk<Void> withCache(final Cache<Path> cache) {
        nodeid.withCache(cache);
        return this;
    }
}
