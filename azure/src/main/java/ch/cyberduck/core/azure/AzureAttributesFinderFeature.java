package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerProperties;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureAttributesFinderFeature implements AttributesFinder, AttributesAdapter<CloudBlob> {

    private final AzureSession session;
    private final OperationContext context;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public static final String KEY_BLOB_TYPE = "blob_type";

    public AzureAttributesFinderFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            if(containerService.isContainer(file)) {
                final PathAttributes attributes = new PathAttributes();
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.downloadAttributes(null, null, context);
                final BlobContainerProperties properties = container.getProperties();
                attributes.setETag(properties.getEtag());
                attributes.setModificationDate(properties.getLastModified().getTime());
                return attributes;
            }
            if(file.isFile() || file.isPlaceholder()) {
                try {
                    final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlobReferenceFromServer(containerService.getKey(file));
                    final BlobRequestOptions options = new BlobRequestOptions();
                    blob.downloadAttributes(AccessCondition.generateEmptyCondition(), options, context);
                    return this.toAttributes(blob);
                }
                catch(StorageException e) {
                    switch(e.getHttpStatusCode()) {
                        case HttpStatus.SC_NOT_FOUND:
                            if(file.isPlaceholder()) {
                                // Ignore failure and look for common prefix
                                break;
                            }
                        default:
                            throw e;
                    }
                }
            }
            // Check for common prefix
            try {
                new AzureObjectListService(session, context).list(file, new CancellingListProgressListener());
                return PathAttributes.EMPTY;
            }
            catch(ListCanceledException l) {
                // Found common prefix
                return PathAttributes.EMPTY;
            }
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }

    @Override
    public PathAttributes toAttributes(final CloudBlob blob) {
        final PathAttributes attributes = new PathAttributes();
        final BlobProperties properties = blob.getProperties();
        attributes.setSize(properties.getLength());
        attributes.setModificationDate(properties.getLastModified().getTime());
        if(StringUtils.isNotBlank(properties.getContentMD5())) {
            attributes.setChecksum(Checksum.parse(Hex.encodeHexString(Base64.decodeBase64(properties.getContentMD5()))));
        }
        attributes.setETag(properties.getEtag());
        final Map<String, String> custom = new HashMap<>();
        custom.put(AzureAttributesFinderFeature.KEY_BLOB_TYPE, properties.getBlobType().name());
        attributes.setCustom(custom);
        return attributes;
    }
}
