package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * Attributes with Data Lake Storage API for accounts with hierarchical namespace enabled. Directories are
 * resolved with the Data Lake Storage API and files with the Blob Storage API to include the blob type.
 */
public class AzureDataLakeAttributesFinderFeature implements AttributesFinder, AttributesAdapter<PathItem> {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();
    private final AzureBlobAttributesFinderFeature blob;

    public AzureDataLakeAttributesFinderFeature(final AzureSession session) {
        this.session = session;
        this.blob = new AzureBlobAttributesFinderFeature(session);
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot() || containerService.isContainer(file) || !file.isDirectory()) {
            return blob.find(file, listener);
        }
        try {
            return this.toAttributes(session.getClient().getDataLakeServiceClient()
                    .getFileSystemClient(containerService.getContainer(file).getName())
                    .getDirectoryClient(StringUtils.removeEnd(containerService.getKey(file), String.valueOf(Path.DELIMITER)))
                    .getProperties());
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributes(final PathProperties properties) {
        final PathAttributes attributes = new DefaultPathAttributes();
        attributes.setSize(properties.getFileSize());
        attributes.setModificationDate(properties.getLastModified().toInstant().toEpochMilli());
        if(properties.getContentMd5() != null) {
            attributes.setChecksum(Checksum.parse(Hex.encodeHexString(properties.getContentMd5())));
        }
        attributes.setETag(StringUtils.removeStart(StringUtils.removeEnd(properties.getETag(), "\""), "\""));
        return attributes;
    }

    @Override
    public PathAttributes toAttributes(final PathItem item) {
        final PathAttributes attributes = new DefaultPathAttributes();
        attributes.setSize(item.getContentLength());
        attributes.setModificationDate(item.getLastModified().toInstant().toEpochMilli());
        attributes.setETag(StringUtils.removeStart(StringUtils.removeEnd(item.getETag(), "\""), "\""));
        return attributes;
    }
}
