package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.BaseB2Response;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_LARGE_FILE_SHA1;
import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2AttributesFinderFeature implements AttributesFinder, AttributesAdapter<BaseB2Response> {
    private static final Logger log = LogManager.getLogger(B2AttributesFinderFeature.class);

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2AttributesFinderFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(file.getType().contains(Path.Type.upload)) {
            // Pending large file upload
            final Write.Append append = new B2WriteFeature(session, fileid).append(file, new TransferStatus());
            if(append.append) {
                return new PathAttributes().withSize(append.size);
            }
            return PathAttributes.EMPTY;
        }
        if(containerService.isContainer(file)) {
            try {
                final B2BucketResponse info = session.getClient().listBucket(file.getName());
                if(null == info) {
                    throw new NotfoundException(file.getAbsolute());
                }
                return this.toAttributes(info);
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        else {
            try {
                final PathAttributes attr = this.toAttributes(session.getClient().getFileInfo(fileid.getVersionId(file)));
                if(attr.isDuplicate()) {
                    // Throw failure if latest version has hide marker set
                    if(StringUtils.isBlank(file.attributes().getVersionId())) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Latest version of %s is duplicate", file));
                        }
                        throw new NotfoundException(file.getAbsolute());
                    }
                }
                return attr;
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }

    @Override
    public PathAttributes toAttributes(final BaseB2Response response) {
        if(response instanceof B2FileResponse) {
            return this.toAttributes((B2FileResponse) response);
        }
        if(response instanceof B2FileInfoResponse) {
            return this.toAttributes((B2FileInfoResponse) response);
        }
        if(response instanceof B2BucketResponse) {
            return this.toAttributes((B2BucketResponse) response);
        }
        if(response instanceof B2FinishLargeFileResponse) {
            return this.toAttributes((B2FinishLargeFileResponse) response);
        }
        log.error(String.format("Unknown type %s", response));
        return PathAttributes.EMPTY;
    }

    protected PathAttributes toAttributes(final B2FileInfoResponse response) {
        final PathAttributes attributes = new PathAttributes();
        if(response.getFileInfo().containsKey(X_BZ_INFO_LARGE_FILE_SHA1)) {
            attributes.setChecksum(Checksum.parse(response.getFileInfo().get(X_BZ_INFO_LARGE_FILE_SHA1)));
        }
        else {
            attributes.setChecksum(Checksum.parse(StringUtils.removeStart(StringUtils.lowerCase(response.getContentSha1(), Locale.ROOT), "unverified:")));
        }
        if(!response.getFileInfo().isEmpty()) {
            attributes.setMetadata(new HashMap<>(response.getFileInfo()));
        }
        attributes.setVersionId(response.getFileId());
        final long timestamp = response.getUploadTimestamp();
        if(response.getFileInfo().containsKey(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)) {
            final String value = response.getFileInfo().get(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS);
            try {
                attributes.setModificationDate(Long.parseLong(value));
            }
            catch(NumberFormatException e) {
                log.warn(String.format("Failure parsing src_last_modified_millis with value %s", value));
            }
        }
        else {
            attributes.setModificationDate(timestamp);
        }
        if(response.getAction() != null) {
            switch(response.getAction()) {
                case hide:
                    // File version marking the file as hidden, so that it will not show up in b2_list_file_names
                case start:
                    // Large file has been started, but not finished or canceled
                    attributes.setDuplicate(true);
                    break;
                default:
                    attributes.setSize(response.getContentLength());
                    break;
            }
        }
        return attributes;
    }

    protected PathAttributes toAttributes(final B2FileResponse response) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(response.getContentLength());
        if(response.getFileInfo().containsKey(X_BZ_INFO_LARGE_FILE_SHA1)) {
            attributes.setChecksum(Checksum.parse(response.getFileInfo().get(X_BZ_INFO_LARGE_FILE_SHA1)));
        }
        else {
            attributes.setChecksum(Checksum.parse(StringUtils.removeStart(StringUtils.lowerCase(response.getContentSha1(), Locale.ROOT), "unverified:")));
        }
        if(!response.getFileInfo().isEmpty()) {
            attributes.setMetadata(new HashMap<>(response.getFileInfo()));
        }
        attributes.setVersionId(response.getFileId());
        final long timestamp = response.getUploadTimestamp();
        if(response.getFileInfo().containsKey(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)) {
            final String value = response.getFileInfo().get(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS);
            try {
                attributes.setModificationDate(Long.parseLong(value));
            }
            catch(NumberFormatException e) {
                log.warn(String.format("Failure parsing src_last_modified_millis with value %s", value));
            }
        }
        else {
            attributes.setModificationDate(timestamp);
        }
        if(response.getAction() != null) {
            switch(response.getAction()) {
                case hide:
                    // File version marking the file as hidden, so that it will not show up in b2_list_file_names
                case start:
                    // Large file has been started, but not finished or canceled
                    attributes.setDuplicate(true);
                    break;
                default:
                    attributes.setSize(response.getContentLength());
                    break;
            }
        }
        return attributes;
    }

    protected PathAttributes toAttributes(final B2BucketResponse response) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setFileId(response.getBucketId());
        attributes.setRegion(response.getBucketType().name());
        switch(response.getBucketType()) {
            case allPublic:
                attributes.setAcl(new Acl(new Acl.GroupUser(Acl.GroupUser.EVERYONE, false), new Acl.Role(Acl.Role.READ)));
        }
        return attributes;
    }

    protected PathAttributes toAttributes(final B2FinishLargeFileResponse response) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(response.getContentLength());
        if(response.getFileInfo().containsKey(X_BZ_INFO_LARGE_FILE_SHA1)) {
            attributes.setChecksum(Checksum.parse(response.getFileInfo().get(X_BZ_INFO_LARGE_FILE_SHA1)));
        }
        else {
            attributes.setChecksum(Checksum.parse(StringUtils.removeStart(StringUtils.lowerCase(response.getContentSha1(), Locale.ROOT), "unverified:")));
        }
        if(!response.getFileInfo().isEmpty()) {
            attributes.setMetadata(new HashMap<>(response.getFileInfo()));
        }
        attributes.setVersionId(response.getFileId());
        if(response.getFileInfo().containsKey(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)) {
            attributes.setModificationDate(Long.parseLong(response.getFileInfo().get(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)));
        }
        return attributes;
    }
}