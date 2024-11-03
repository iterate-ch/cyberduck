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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

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
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        if(!validate(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename));
        }
        final SDSPermissionsFeature permissions = new SDSPermissionsFeature(session, nodeid);
        if(!permissions.containsRole(workdir, SDSPermissionsFeature.CREATE_ROLE)
                // For existing files the delete role is also required to overwrite
                || !permissions.containsRole(workdir, SDSPermissionsFeature.DELETE_ROLE)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        if(workdir.attributes().getQuota() != SDSQuotaFeature.unknown) {
            if(workdir.attributes().getQuota().available <= workdir.attributes().getSize() + new HostPreferences(session.getHost()).getInteger("sds.upload.multipart.chunksize")) {
                log.warn("Quota {} exceeded with {} in {}", workdir.attributes().getQuota().available, workdir.attributes().getSize(), workdir);
                throw new QuotaException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
            }
        }
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
