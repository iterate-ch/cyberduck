package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import com.google.api.services.storage.model.StorageObject;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class GoogleStorageWriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write<StorageObject> {

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public GoogleStorageWriteFeature(final GoogleStorageSession session) {
        super(new GoogleStorageAttributesFinderFeature(session));
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public HttpResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>(file) {
            @Override
            public StorageObject call(final HttpEntity entity) throws BackgroundException {
                try {
                    // POST /upload/storage/v1/b/myBucket/o
                    final StringBuilder uri = new StringBuilder(String.format("%supload/storage/v1/b/%s/o?uploadType=resumable",
                            session.getClient().getRootUrl(), containerService.getContainer(file).getName()));
                    if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                        uri.append(String.format("&userProject=%s", session.getHost().getCredentials().getUsername()));
                    }
                    if(!Acl.EMPTY.equals(status.getAcl())) {
                        if(status.getAcl().isCanned()) {
                            uri.append("&predefinedAcl=");
                            if(Acl.CANNED_PRIVATE.equals(status.getAcl())) {
                                uri.append("private");
                            }
                            else if(Acl.CANNED_PUBLIC_READ.equals(status.getAcl())) {
                                uri.append("publicRead");
                            }
                            else if(Acl.CANNED_PUBLIC_READ_WRITE.equals(status.getAcl())) {
                                uri.append("publicReadWrite");
                            }
                            else if(Acl.CANNED_AUTHENTICATED_READ.equals(status.getAcl())) {
                                uri.append("authenticatedRead");
                            }
                            else if(Acl.CANNED_BUCKET_OWNER_FULLCONTROL.equals(status.getAcl())) {
                                uri.append("bucketOwnerFullControl");
                            }
                            else if(Acl.CANNED_BUCKET_OWNER_READ.equals(status.getAcl())) {
                                uri.append("bucketOwnerRead");
                            }
                            // Reset in status to skip setting ACL in upload filter already applied as canned ACL
                            status.setAcl(Acl.EMPTY);
                        }
                    }
                    final HttpEntityEnclosingRequestBase request = new HttpPost(uri.toString());
                    final StringBuilder metadata = new StringBuilder();
                    metadata.append(String.format("{\"name\": \"%s\"", containerService.getKey(file)));
                    metadata.append(",\"metadata\": {");
                    for(Iterator<Map.Entry<String, String>> iter = status.getMetadata().entrySet().iterator(); iter.hasNext(); ) {
                        final Map.Entry<String, String> item = iter.next();
                        metadata.append(String.format("\"%s\": \"%s\"", item.getKey(), item.getValue()));
                        if(iter.hasNext()) {
                            metadata.append(",");
                        }
                    }
                    metadata.append("}");
                    if(StringUtils.isNotBlank(status.getMime())) {
                        metadata.append(String.format(", \"contentType\": \"%s\"", status.getMime()));
                    }
                    if(StringUtils.isNotBlank(status.getStorageClass())) {
                        metadata.append(String.format(", \"storageClass\": \"%s\"", status.getStorageClass()));
                    }
                    if(null != status.getModified()) {
                        metadata.append(String.format(", \"customTime\": \"%s\"",
                                new ISO8601DateFormatter().format(status.getModified(), TimeZone.getTimeZone("UTC"))));
                    }
                    metadata.append("}");
                    request.setEntity(new StringEntity(metadata.toString(),
                            ContentType.create("application/json", StandardCharsets.UTF_8.name())));
                    if(StringUtils.isNotBlank(status.getMime())) {
                        // Set to the media MIME type of the upload data to be transferred in subsequent requests.
                        request.addHeader("X-Upload-Content-Type", status.getMime());
                    }
                    request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                    final HttpClient client = session.getHttpClient();
                    final HttpResponse response = client.execute(request);
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                throw new DefaultHttpResponseExceptionMappingService().map(
                                        new HttpResponseException(response.getStatusLine().getStatusCode(),
                                                new GoogleStorageExceptionMappingService().parse(response)));
                        }
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                    if(response.containsHeader(HttpHeaders.LOCATION)) {
                        final String putTarget = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                        // Upload the file
                        final HttpPut put = new HttpPut(putTarget);
                        put.setEntity(entity);
                        final HttpResponse putResponse = client.execute(put);
                        try {
                            switch(putResponse.getStatusLine().getStatusCode()) {
                                case HttpStatus.SC_OK:
                                case HttpStatus.SC_CREATED:
                                    return session.getClient().getObjectParser().parseAndClose(new InputStreamReader(
                                            putResponse.getEntity().getContent(), StandardCharsets.UTF_8), StorageObject.class);
                                default:
                                    throw new DefaultHttpResponseExceptionMappingService().map(
                                            new HttpResponseException(putResponse.getStatusLine().getStatusCode(),
                                                    new GoogleStorageExceptionMappingService().parse(putResponse)));
                            }
                        }
                        finally {
                            EntityUtils.consume(putResponse.getEntity());
                        }
                    }
                    else {
                        throw new DefaultHttpResponseExceptionMappingService().map(
                                new HttpResponseException(response.getStatusLine().getStatusCode(),
                                        new GoogleStorageExceptionMappingService().parse(response)));
                    }
                }
                catch(IOException e) {
                    throw new GoogleStorageExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.sha256);
    }
}
