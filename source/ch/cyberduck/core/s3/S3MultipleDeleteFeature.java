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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.i18n.Locale;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.container.ObjectKeyAndVersion;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * @version $Id$
 */
public class S3MultipleDeleteFeature implements Delete {

    private S3Session session;
    private LoginController prompt;

    private PathContainerService containerService
            = new PathContainerService();

    public S3MultipleDeleteFeature(final S3Session session, final LoginController prompt) {
        this.session = session;
        this.prompt = prompt;
    }

    public void delete(final List<Path> files) throws BackgroundException {
        final Map<Path, List<ObjectKeyAndVersion>> map = new HashMap<Path, List<ObjectKeyAndVersion>>();
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                continue;
            }
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            final Path container = containerService.getContainer(file);
            final List<ObjectKeyAndVersion> keys = new ArrayList<ObjectKeyAndVersion>();
            if(file.attributes().isDirectory()) {
                // Because we normalize paths and remove a trailing delimiter we add it here again as the
                // default directory placeholder formats has the format `/placeholder/' as a key.
                keys.add(new ObjectKeyAndVersion(containerService.getKey(file) + Path.DELIMITER,
                        file.attributes().getVersionId()));
                // Always returning 204 even if the key does not exist.
                // Fallback to legacy directory placeholders with metadata instead of key with trailing delimiter
                keys.add(new ObjectKeyAndVersion(containerService.getKey(file),
                        file.attributes().getVersionId()));
                // AWS does not return 404 for non-existing keys
            }
            else {
                keys.add(new ObjectKeyAndVersion(containerService.getKey(file), file.attributes().getVersionId()));
            }
            if(map.containsKey(container)) {
                map.get(container).addAll(keys);
            }
            else {
                map.put(container, keys);
            }
        }
        for(Map.Entry<Path, List<ObjectKeyAndVersion>> entry : map.entrySet()) {
            this.delete(entry.getKey(), entry.getValue());
        }
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        file.getName()));
                // Finally delete bucket itself
                try {
                    session.getClient().deleteBucket(containerService.getContainer(file).getName());
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Cannot delete {0}", e, file);
                }
            }
        }
    }

    /**
     * @param container Bucket
     * @param keys      Key and version ID for versioned object or null
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException
     *          Authentication canceled for MFA delete
     */
    protected void delete(final Path container, final List<ObjectKeyAndVersion> keys)
            throws BackgroundException {
        try {
            if(new S3VersioningFeature(session).getConfiguration(container).isMultifactor()) {
                final Credentials factor = new S3VersioningFeature(session).getToken(prompt);
                session.getClient().deleteMultipleObjectsWithMFA(container.getName(),
                        keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                        factor.getUsername(),
                        factor.getPassword(),
                        true);
            }
            else {
                // Request contains a list of up to 1000 keys that you want to delete
                for(List<ObjectKeyAndVersion> sub : Lists.partition(keys, 1000)) {
                    session.getClient().deleteMultipleObjects(container.getName(),
                            keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                            true);
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot delete {0}", e, container);
        }
    }

}
