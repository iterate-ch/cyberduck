package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class DriveWriteFeature extends AbstractHttpWriteFeature<File> implements Write<File> {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveWriteFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        super(new DriveAttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
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
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>(file) {
            @Override
            public File call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    // Initiate a resumable upload
                    final HttpEntityEnclosingRequestBase request;
                    if(status.isExists()) {
                        final String fileid = DriveWriteFeature.this.fileid.getFileId(file);
                        request = new HttpPatch(String.format("%supload/drive/v3/files/%s?supportsAllDrives=true&fields=%s",
                                session.getClient().getRootUrl(), fileid, DriveAttributesFinderFeature.DEFAULT_FIELDS));
                        if(StringUtils.isNotBlank(status.getMime())) {
                            request.setHeader(HttpHeaders.CONTENT_TYPE, status.getMime());
                        }
                        // Upload the file
                        request.setEntity(entity);
                    }
                    else {
                        request = new HttpPost(String.format("%supload/drive/v3/files?uploadType=resumable&supportsAllDrives=%s&fields=%s",
                                session.getClient().getRootUrl(), new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"),
                                DriveAttributesFinderFeature.DEFAULT_FIELDS));
                        final StringBuilder metadata = new StringBuilder("{");
                        metadata.append(String.format("\"name\":\"%s\"", file.getName()));
                        if(null != status.getModified()) {
                            metadata.append(String.format(",\"modifiedTime\":\"%s\"",
                                    new ISO8601DateFormatter().format(status.getModified(), TimeZone.getTimeZone("UTC"))));
                        }
                        if(null != status.getCreated()) {
                            metadata.append(String.format(",\"createdTime\":\"%s\"",
                                    new ISO8601DateFormatter().format(status.getCreated(), TimeZone.getTimeZone("UTC"))));
                        }
                        if(StringUtils.isNotBlank(status.getMime())) {
                            metadata.append(String.format(",\"mimeType\":\"%s\"", status.getMime()));
                        }
                        metadata.append(String.format(",\"parents\":[\"%s\"]", fileid.getFileId(file.getParent())));
                        metadata.append("}");
                        request.setEntity(new StringEntity(metadata.toString(),
                                ContentType.create("application/json", StandardCharsets.UTF_8.name())));
                        if(StringUtils.isNotBlank(status.getMime())) {
                            // Set to the media MIME type of the upload data to be transferred in subsequent requests.
                            request.addHeader("X-Upload-Content-Type", status.getMime());
                        }
                    }
                    request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                    final HttpClient client = session.getHttpClient();
                    final HttpResponse postResponse = client.execute(request);
                    try {
                        switch(postResponse.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                if(status.isExists()) {
                                    final File f = session.getClient().getObjectParser().parseAndClose(
                                            new InputStreamReader(postResponse.getEntity().getContent(), StandardCharsets.UTF_8), File.class);
                                    if(null != status.getModified()) {
                                        new DriveTimestampFeature(session, fileid).setTimestamp(file, status);
                                        f.setModifiedTime(new DateTime(status.getModified()));
                                    }
                                    return f;
                                }
                                break;
                            default:
                                throw new DefaultHttpResponseExceptionMappingService().map(
                                        new HttpResponseException(postResponse.getStatusLine().getStatusCode(), postResponse.getStatusLine().getReasonPhrase()));
                        }
                    }
                    finally {
                        EntityUtils.consume(postResponse.getEntity());
                    }
                    if(!status.isExists()) {
                        if(postResponse.containsHeader(HttpHeaders.LOCATION)) {
                            final String putTarget = postResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
                            // Upload the file
                            final HttpPut put = new HttpPut(putTarget);
                            put.setEntity(entity);
                            final HttpResponse putResponse = client.execute(put);
                            try {
                                switch(putResponse.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        final File response = session.getClient().getObjectParser().parseAndClose(
                                                new InputStreamReader(putResponse.getEntity().getContent(), StandardCharsets.UTF_8), File.class);
                                        fileid.cache(file, response.getId());
                                        return response;
                                    default:
                                        throw new DefaultHttpResponseExceptionMappingService().map(
                                                new HttpResponseException(putResponse.getStatusLine().getStatusCode(), putResponse.getStatusLine().getReasonPhrase()));
                                }
                            }
                            finally {
                                EntityUtils.consume(putResponse.getEntity());
                            }
                        }
                        else {
                            throw new DefaultHttpResponseExceptionMappingService().map(
                                    new HttpResponseException(postResponse.getStatusLine().getStatusCode(), postResponse.getStatusLine().getReasonPhrase()));
                        }
                    }
                    return null;
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService(fileid).map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }
}
