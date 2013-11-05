package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @version $Id$
 */
public class S3WriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write {
    private static final Logger log = Logger.getLogger(S3WriteFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new PathContainerService();

    public S3WriteFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        return this.write(file, this.createObjectDetails(file), status.getLength(), Collections.<String, String>emptyMap());
    }

    public ResponseOutputStream<StorageObject> write(final Path file, final StorageObject part, final Long contentLength,
                                                     final Map<String, String> requestParams) throws BackgroundException {
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    session.getClient().putObjectWithRequestEntityImpl(
                            containerService.getContainer(file).getName(), part, entity, requestParams);
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Upload failed", e, file);
                }
                return part;
            }

            @Override
            public long getContentLength() {
                return contentLength;
            }
        };
        return session.write(file, command);
    }

    protected S3Object createObjectDetails(final Path file) throws BackgroundException {
        final S3Object object = new S3Object(containerService.getKey(file));
        object.setContentType(new MappingMimeTypeService().getMime(file.getName()));
        if(Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            object.setMd5Hash(ServiceUtils.fromHex(file.getLocal().attributes().getChecksum()));
        }
        // Storage class
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.storage.class"))) {
            if(!S3Object.STORAGE_CLASS_STANDARD.equals(Preferences.instance().getProperty("s3.storage.class"))) {
                // The default setting is STANDARD.
                object.setStorageClass(Preferences.instance().getProperty("s3.storage.class"));
            }
        }
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.encryption.algorithm"))) {
            object.setServerSideEncryptionAlgorithm(Preferences.instance().getProperty("s3.encryption.algorithm"));
        }
        // Default metadata for new files
        for(String m : Preferences.instance().getList("s3.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in header %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in header %s", m));
                continue;
            }
            object.addMetadata(name, value);
        }
        return object;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public Append append(final Path file, final TransferStatus status, final Cache cache) throws BackgroundException {
        if(status.getLength() > Preferences.instance().getLong("s3.upload.multipart.threshold")) {
            final S3MultipartUploadService multipart = new S3MultipartUploadService(session);
            final MultipartUpload upload = multipart.find(file);
            if(upload != null) {
                Long size = 0L;
                for(MultipartPart completed : multipart.list(upload)) {
                    size += completed.getSize();
                }
                return new Append(size);
            }
        }
        return new Append();
    }
}
