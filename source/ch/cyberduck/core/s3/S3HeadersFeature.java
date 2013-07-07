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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.Headers;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @version $Id$
 */
public class S3HeadersFeature implements Headers {
    private static final Logger log = Logger.getLogger(S3HeadersFeature.class);

    private S3Session session;

    public S3HeadersFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        if(file.attributes().isFile() || file.attributes().isPlaceholder()) {
            final StorageObject target = new S3ObjectDetailService(session).getDetails(file);
            HashMap<String, String> metadata = new HashMap<String, String>();
            Map<String, Object> source = target.getModifiableMetadata();
            for(Map.Entry<String, Object> entry : source.entrySet()) {
                metadata.put(entry.getKey(), entry.getValue().toString());
            }
//            file.attributes().setEncryption(target.getServerSideEncryptionAlgorithm());
            return metadata;
        }
        return Collections.emptyMap();
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        if(file.attributes().isFile() || file.attributes().isPlaceholder()) {
            try {
                final StorageObject target = new S3ObjectDetailService(session).getDetails(file);
                target.replaceAllMetadata(new HashMap<String, Object>(metadata));
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
                target.setAcl(acl.convert(acl.read(file)));
                session.getClient().updateObjectMetadata(file.getContainer().getName(), target);
                target.setMetadataComplete(false);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot write file attributes", e, file);
            }
        }
    }
}
