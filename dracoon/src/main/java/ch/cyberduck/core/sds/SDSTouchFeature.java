package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SDSTouchFeature extends DefaultTouchFeature<Node> {
    private static final Logger log = LogManager.getLogger(SDSTouchFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
            = new SDSPathContainerService();


    public SDSTouchFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        super(new SDSDelegatingWriteFeature(session, nodeid,
                new HostPreferences(session.getHost()).getBoolean("sds.upload.s3.enable") ?
                        new SDSDirectS3MultipartWriteFeature(session, nodeid) : new SDSMultipartWriteFeature(session, nodeid)));
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(containerService.getContainer(file))) {
            status.setFilekey(SDSTripleCryptEncryptorFeature.generateFileKey());
        }
        return super.touch(file, status);
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        if(workdir.isRoot()) {
            return false;
        }
        if(!validate(filename)) {
            log.warn(String.format("Validation failed for target name %s", filename));
            return false;
        }
        if(workdir.attributes().getQuota() != -1) {
            if(workdir.attributes().getQuota() <= workdir.attributes().getSize() + new HostPreferences(session.getHost()).getInteger("sds.upload.multipart.chunksize")) {
                log.warn(String.format("Quota %d exceeded with %d in %s", workdir.attributes().getQuota(), workdir.attributes().getSize(), workdir));
                return false;
            }
        }
        final SDSPermissionsFeature permissions = new SDSPermissionsFeature(session, nodeid);
        return permissions.containsRole(workdir, SDSPermissionsFeature.CREATE_ROLE)
                // For existing files the delete role is also required to overwrite
                && permissions.containsRole(workdir, SDSPermissionsFeature.DELETE_ROLE);
    }

    /**
     * Validate node name convention
     */
    public static boolean validate(final String filename) {
        // Empty argument if not known in validation
        if(StringUtils.isNotBlank(filename)) {
            if(StringUtils.length(filename) > 150) {
                // Node (room, folder, file) names are limited to 150 characters.
                return false;
            }
            // '\\', '<','>', ':', '\"', '|', '?', '*', '/', leading '-', trailing '.'
            if(StringUtils.containsAny(filename, '\\', '<', '>', ':', '"', '|', '?', '*', '/')) {
                return false;
            }
            if(StringUtils.startsWith(filename, "-")) {
                return false;
            }
            if(StringUtils.endsWith(filename, ".")) {
                return false;
            }
        }
        return true;
    }
}
