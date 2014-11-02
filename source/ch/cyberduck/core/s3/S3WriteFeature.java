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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.shared.DefaultFindFeature;
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

import java.util.List;

/**
 * @version $Id$
 */
public class S3WriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write {
    private static final Logger log = Logger.getLogger(S3WriteFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private S3MultipartService multipartService;

    private Find finder;

    private Preferences preferences
            = Preferences.instance();

    /**
     * Storage class
     */
    private String storage
            = preferences.getProperty("s3.storage.class");

    /**
     * Encrytion algorithm
     */
    private String encryption
            = preferences.getProperty("s3.encryption.algorithm");

    /**
     * Default metadata for new files
     */
    private List<String> metadata
            = preferences.getList("s3.metadata.default");

    public S3WriteFeature(final S3Session session) {
        this(session, new S3MultipartService(session), new DefaultFindFeature(session));
    }

    public S3WriteFeature(final S3Session session, final S3MultipartService multipartService, final Find finder) {
        super(session);
        this.session = session;
        this.multipartService = multipartService;
        this.finder = finder;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        final S3Object object = this.getDetails(containerService.getKey(file), status);
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    session.getClient().putObjectWithRequestEntityImpl(
                            containerService.getContainer(file).getName(), object, entity, status.getParameters());
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Upload {0} failed", e, file);
                }
                return object;
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    /**
     * Add default metadata
     */
    protected S3Object getDetails(final String key, final TransferStatus status) {
        final S3Object object = new S3Object(key);
        final String mime = status.getMime();
        if(StringUtils.isNotBlank(mime)) {
            object.setContentType(mime);
        }
        final TransferStatus.Checksum checksum = status.getChecksum();
        if(null != checksum) {
            switch(checksum.algorithm) {
                case md5:
                    object.setMd5Hash(ServiceUtils.fromHex(checksum.hash));
                    break;
                case sha256:
                    object.addMetadata("x-amz-content-sha256", checksum.hash);
                    break;
            }
        }
        if(StringUtils.isNotBlank(storage)) {
            if(!S3Object.STORAGE_CLASS_STANDARD.equals(storage)) {
                // The default setting is STANDARD.
                object.setStorageClass(storage);
            }
        }
        if(StringUtils.isNotBlank(encryption)) {
            object.setServerSideEncryptionAlgorithm(encryption);
        }
        for(String m : metadata) {
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

    public S3WriteFeature withStorage(final String storage) {
        this.storage = storage;
        return this;
    }

    public S3WriteFeature withEncryption(final String encryption) {
        this.encryption = encryption;
        return this;
    }

    public S3WriteFeature withMetadata(final List<String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public Append append(final Path file, final Long length, final Cache cache) throws BackgroundException {
        if(length >= preferences.getLong("s3.upload.multipart.threshold")) {
            if(preferences.getBoolean("s3.upload.multipart")) {
                final MultipartUpload upload = multipartService.find(file);
                if(upload != null) {
                    Long size = 0L;
                    for(MultipartPart completed : multipartService.list(upload)) {
                        size += completed.getSize();
                    }
                    return new Append(size);
                }
            }
        }
        if(finder.withCache(cache).find(file)) {
            return Write.override;
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }
}
