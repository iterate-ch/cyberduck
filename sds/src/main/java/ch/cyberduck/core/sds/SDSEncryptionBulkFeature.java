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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAccount;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectWriter;
import eu.ssp_europe.sds.crypto.Crypto;

public class SDSEncryptionBulkFeature implements Bulk<Void> {

    private final SDSSession session;

    public SDSEncryptionBulkFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        try {
            switch(type) {
                case download:
                    break;
                default:
                    final UserAccount user = new UserApi(session.getClient()).getUserInfo(session.getToken(), null, false);
                    if(user.getIsEncryptionEnabled()) {
                        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
                            final TransferStatus status = entry.getValue();
                            final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey());
                            final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            writer.writeValue(out, fileKey);
                            status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
                        }
                    }
            }
            return null;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public void post(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default:
                new SDSBackgroundFeature(session).processMissingKeys(callback);
        }
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }
}
