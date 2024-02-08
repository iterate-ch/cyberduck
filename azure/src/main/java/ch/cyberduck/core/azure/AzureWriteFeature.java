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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.BlobType;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.core.SR;

public class AzureWriteFeature extends AppendWriteFeature<Void> implements Write<Void> {
    private static final Logger log = LogManager.getLogger(AzureWriteFeature.class);

    private final AzureSession session;
    private final OperationContext context;
    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();
    private final BlobType blobType;

    public AzureWriteFeature(final AzureSession session, final OperationContext context) {
        this(session, BlobType.valueOf(new HostPreferences(session.getHost()).getProperty("azure.upload.blobtype")), context);
    }

    public AzureWriteFeature(final AzureSession session, final BlobType blobType, final OperationContext context) {
        this.session = session;
        this.blobType = blobType;
        this.context = context;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        final Append append = super.append(file, status);
        if(append.append) {
            final PathAttributes attr = new AzureAttributesFinderFeature(session, context).find(file);
            if(BlobType.APPEND_BLOB == BlobType.valueOf(attr.getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE))) {
                return append;
            }
        }
        return Write.override;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final CloudBlob blob;
            if(status.isExists()) {
                if(new HostPreferences(session.getHost()).getBoolean("azure.upload.snapshot")) {
                    session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlobReferenceFromServer(containerService.getKey(file)).createSnapshot();
                }
                if(status.isAppend()) {
                    // Existing append blob type
                    blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getAppendBlobReference(containerService.getKey(file));
                }
                else {
                    // Existing block blob type
                    final PathAttributes attr = new AzureAttributesFinderFeature(session, context).find(file);
                    if(BlobType.APPEND_BLOB == BlobType.valueOf(attr.getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE))) {
                        blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getAppendBlobReference(containerService.getKey(file));
                    }
                    else {
                        blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlockBlobReference(containerService.getKey(file));
                    }
                }
            }
            else {
                // Create new blob with default type set in defaults
                switch(blobType) {
                    case APPEND_BLOB:
                        blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getAppendBlobReference(containerService.getKey(file));
                        break;
                    default:
                        blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlockBlobReference(containerService.getKey(file));
                }
            }
            if(StringUtils.isNotBlank(status.getMime())) {
                blob.getProperties().setContentType(status.getMime());
            }
            // Add previous metadata when overwriting file
            final HashMap<String, String> headers = new HashMap<>(status.getMetadata());
            blob.setMetadata(headers);
            // Remove additional headers not allowed in metadata and move to properties
            if(headers.containsKey(HttpHeaders.CACHE_CONTROL)) {
                blob.getProperties().setCacheControl(headers.get(HttpHeaders.CACHE_CONTROL));
                headers.remove(HttpHeaders.CACHE_CONTROL);
            }
            if(headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                blob.getProperties().setContentType(headers.get(HttpHeaders.CONTENT_TYPE));
                headers.remove(HttpHeaders.CONTENT_TYPE);
            }
            final Checksum checksum = status.getChecksum();
            if(Checksum.NONE != checksum) {
                switch(checksum.algorithm) {
                    case md5:
                        headers.remove(HttpHeaders.CONTENT_MD5);
                        blob.getProperties().setContentMD5(status.getChecksum().base64);
                        break;
                }
            }
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setConcurrentRequestCount(1);
            options.setStoreBlobContentMD5(new HostPreferences(session.getHost()).getBoolean("azure.upload.md5"));
            final BlobOutputStream out;
            if(status.isAppend()) {
                options.setStoreBlobContentMD5(false);
                if(blob instanceof CloudAppendBlob) {
                    out = ((CloudAppendBlob) blob).openWriteExisting(AccessCondition.generateEmptyCondition(), options, context);
                }
                else {
                    throw new NotfoundException(String.format("Unexpected blob type for %s", blob.getName()));
                }
            }
            else {
                if(blob instanceof CloudAppendBlob) {
                    out = ((CloudAppendBlob) blob).openWriteNew(AccessCondition.generateEmptyCondition(), options, context);
                }
                else {
                    out = ((CloudBlockBlob) blob).openOutputStream(AccessCondition.generateEmptyCondition(), options, context);
                }
            }
            return new VoidStatusOutputStream(out) {
                @Override
                protected void handleIOException(final IOException e) throws IOException {
                    if(StringUtils.equals(SR.STREAM_CLOSED, e.getMessage())) {
                        log.warn(String.format("Ignore failure %s", e));
                        return;
                    }
                    final Throwable cause = ExceptionUtils.getRootCause(e);
                    if(cause instanceof StorageException) {
                        throw new IOException(e.getMessage(), new AzureExceptionMappingService().map((StorageException) cause));
                    }
                    throw e;
                }
            };
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
