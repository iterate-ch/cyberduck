package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.BlobType;

public class AzureUploadFeature extends DefaultUploadFeature {

    private final AzureSession session;
    private final OperationContext context;

    public AzureUploadFeature(final AzureSession session, final OperationContext context) {
        super(new AzureWriteFeature(session, context));
        this.session = session;
        this.context = context;
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        final Write.Append append = new Write.Append(status.isExists()).withStatus(status);
        if(append.append) {
            final PathAttributes attr = new AzureAttributesFinderFeature(session, context).find(file);
            if(BlobType.APPEND_BLOB == BlobType.valueOf(attr.getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE))) {
                return append;
            }
        }
        return Write.override;
    }
}
