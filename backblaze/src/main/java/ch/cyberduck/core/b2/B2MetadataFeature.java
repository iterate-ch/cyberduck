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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;

public class B2MetadataFeature implements Metadata {
    private static final Logger log = LogManager.getLogger(B2MetadataFeature.class);

    public static final String X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS = "src_last_modified_millis";
    public static final String X_BZ_INFO_SRC_CREATION_DATE_MILLIS = "src_creation_date_millis";
    public static final String X_BZ_INFO_LARGE_FILE_SHA1 = "large_file_sha1";

    /**
     * Maximum size of source file for b2_copy_file
     */
    private static final long MAXIMUM_COPY_SIZE = 5L * 1000 * 1000 * 1000;

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2MetadataFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        return HostPreferencesFactory.get(session.getHost()).getMap("b2.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        return new B2AttributesFinderFeature(session, fileid).find(file).getMetadata();
    }

    /**
     * Replace file info by copying the file onto itself with metadata directive <code>REPLACE</code>. Creates a new
     * file version.
     */
    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(file) || file.isDirectory()) {
            log.warn("Skip writing metadata for {}", file);
            return;
        }
        try {
            final B2FileResponse info = session.getClient().getFileInfo(fileid.getVersionId(file));
            if(info.getContentLength() > MAXIMUM_COPY_SIZE) {
                throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Failure to write attributes of {0}", "Error"),
                        file.getName())).withFile(file);
            }
            String contentType = info.getContentType();
            final Map<String, String> fileInfo = new HashMap<>();
            for(Map.Entry<String, String> entry : status.getMetadata().entrySet()) {
                if(HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                    if(StringUtils.isNotBlank(entry.getValue())) {
                        contentType = entry.getValue();
                    }
                    continue;
                }
                fileInfo.put(entry.getKey(), entry.getValue());
            }
            // Preserve timestamps and checksum unless explicitly set
            for(String key : new String[]{X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, X_BZ_INFO_SRC_CREATION_DATE_MILLIS, X_BZ_INFO_LARGE_FILE_SHA1}) {
                if(info.getFileInfo().containsKey(key)) {
                    fileInfo.putIfAbsent(key, info.getFileInfo().get(key));
                }
            }
            log.debug("Replace metadata of {} with {}", file, fileInfo);
            final B2FileResponse response = new B2CopyFileReplaceMetadataRequest(session.getHttpClient(), session.getAuthorization(),
                    info.getFileId(), info.getBucketId(), info.getFileName(), contentType, fileInfo).getResponse();
            fileid.cache(file, response.getFileId());
            status.setResponse(new B2AttributesFinderFeature(session, fileid).toAttributes(response));
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
