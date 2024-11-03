package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpHead;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.HeadersResponseHandler;

public class DAVAttributesFinderFeature implements AttributesFinder, AttributesAdapter<DavResource> {
    private static final Logger log = LogManager.getLogger(DAVAttributesFinderFeature.class);

    private final DAVSession session;

    private final RFC1123DateFormatter rfc1123
            = new RFC1123DateFormatter();

    public DAVAttributesFinderFeature(DAVSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            try {
                for(final DavResource resource : this.list(file)) {
                    if(resource.isDirectory()) {
                        if(!file.getType().contains(Path.Type.directory)) {
                            throw new NotfoundException(String.format("File %s has set MIME type %s",
                                    file.getAbsolute(), DavResource.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE));
                        }
                    }
                    else {
                        if(!file.getType().contains(Path.Type.file)) {
                            throw new NotfoundException(String.format("File %s has set MIME type %s",
                                    file.getAbsolute(), resource.getContentType()));
                        }
                    }
                    return this.toAttributes(resource);
                }
                throw new NotfoundException(file.getAbsolute());
            }
            catch(SardineException e) {
                try {
                    throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                }
                catch(InteroperabilityException | ConflictException i) {
                    // PROPFIND Method not allowed
                    if(log.isWarnEnabled()) {
                        log.warn("Failure with PROPFIND request for {}. {}", file, i.getMessage());
                    }
                    final PathAttributes attr = this.head(file);
                    if(PathAttributes.EMPTY == attr) {
                        throw i;
                    }
                    return attr;
                }
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    protected PathAttributes head(final Path file) throws IOException {
        final Map<String, String> headers = session.getClient().execute(
                new HttpHead(new DAVPathEncoder().encode(file)), new HeadersResponseHandler());
        final PathAttributes attributes = new PathAttributes();
        try {
            attributes.setModificationDate(rfc1123.parse(headers.get(HttpHeaders.LAST_MODIFIED)).getTime());
        }
        catch(InvalidDateException p) {
            log.warn("{} is not RFC 1123 format {}", headers.get(HttpHeaders.LAST_MODIFIED), p.getMessage());
        }
        if(!headers.containsKey(HttpHeaders.CONTENT_ENCODING)) {
            // Set size unless response is compressed
            attributes.setSize(NumberUtils.toLong(headers.get(HttpHeaders.CONTENT_LENGTH), -1));
        }
        if(headers.containsKey(HttpHeaders.ETAG)) {
            attributes.setETag(headers.get(HttpHeaders.ETAG));
            // Setting checksum is disabled. See #8798
            // attributes.setChecksum(Checksum.parse(headers.get(HttpHeaders.ETAG)));
        }
        if(headers.containsKey(HttpHeaders.CONTENT_MD5)) {
            attributes.setChecksum(Checksum.parse(headers.get(HttpHeaders.CONTENT_MD5)));
        }
        return attributes;
    }

    protected List<DavResource> list(final Path file) throws IOException, BackgroundException {
        return session.getClient().list(new DAVPathEncoder().encode(file), 0,
                Stream.of(
                                DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                                DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).
                        collect(Collectors.toSet())
        );
    }

    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final PathAttributes attributes = new PathAttributes();
        final Map<QName, String> properties = resource.getCustomPropsNS();
        if(null != properties && properties.containsKey(DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE)) {
            final String value = properties.get(DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE);
            if(StringUtils.isNotBlank(value)) {
                try {
                    if(properties.containsKey(DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE)) {
                        final String svalue = properties.get(DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE);
                        if(StringUtils.isNotBlank(svalue)) {
                            final Date server = rfc1123.parse(svalue);
                            if(server.equals(resource.getModified())) {
                                // file not touched with a different client
                                attributes.setModificationDate(
                                        rfc1123.parse(value).getTime());
                            }
                            else {
                                // file touched with a different client, use default modified date from server
                                if(resource.getModified() != null) {
                                    attributes.setModificationDate(resource.getModified().getTime());
                                }
                            }
                        }
                        else {
                            if(log.isDebugEnabled()) {
                                log.debug("Missing value for property {}", DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE);
                            }
                            if(resource.getModified() != null) {
                                attributes.setModificationDate(resource.getModified().getTime());
                            }
                        }
                    }
                    else {
                        attributes.setModificationDate(
                                rfc1123.parse(value).getTime());
                    }
                }
                catch(InvalidDateException e) {
                    log.warn("Failure parsing property {} with value {}", DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE, value);
                    if(resource.getModified() != null) {
                        attributes.setModificationDate(resource.getModified().getTime());
                    }
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Missing value for property {}", DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE);
                }
                if(resource.getModified() != null) {
                    attributes.setModificationDate(resource.getModified().getTime());
                }
            }
            // Validate value with fallback to server side modified date
            if(attributes.getModificationDate() == 0) {
                if(log.isDebugEnabled()) {
                    log.debug("Invalid value for property {}", DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE);
                }
                if(resource.getModified() != null) {
                    attributes.setModificationDate(resource.getModified().getTime());
                }
            }
        }
        else if(resource.getModified() != null) {
            attributes.setModificationDate(resource.getModified().getTime());
        }
        if(resource.getCreation() != null) {
            attributes.setCreationDate(resource.getCreation().getTime());
        }
        if(resource.getContentLength() != null) {
            attributes.setSize(resource.getContentLength());
        }
        if(StringUtils.isNotBlank(resource.getEtag())) {
            attributes.setETag(resource.getEtag());
        }
        if(StringUtils.isNotBlank(resource.getDisplayName())) {
            attributes.setDisplayname(resource.getDisplayName());
        }
        attributes.setLockId(resource.getLockToken());
        return attributes;
    }
}
