package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDSTimestampFeature extends DefaultTimestampFeature {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSTimestampFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final SoftwareVersionData version = session.softwareVersion();
            final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(version.getRestApiVersion());
            if(matcher.matches()) {
                if(new Version(matcher.group(1)).compareTo(new Version("4.22")) < 0) {
                    throw new UnsupportedException();
                }
            }
            if(containerService.isContainer(file)) {
                new NodesApi(session.getClient()).updateRoom(new UpdateRoomRequest().timestampModification(new DateTime(status.getTimestamp())),
                        Long.parseLong(nodeid.getVersionId(file)), StringUtils.EMPTY, null);
            }
            else if(file.isDirectory()) {
                new NodesApi(session.getClient()).updateFolder(new UpdateFolderRequest().timestampModification(new DateTime(status.getTimestamp())),
                        Long.parseLong(nodeid.getVersionId(file)), StringUtils.EMPTY, null);
            }
            else {
                new NodesApi(session.getClient()).updateFile(new UpdateFileRequest().timestampModification(new DateTime(status.getTimestamp())),
                        Long.parseLong(nodeid.getVersionId(file)), StringUtils.EMPTY, null);
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to write attributes of {0}", e, file);
        }
    }
}
