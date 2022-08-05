package ch.cyberduck.core.onedrive.features;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.nuxeo.onedrive.client.ODataQuery;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;

public class GraphQuotaFeature implements Quota {

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphQuotaFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new DefaultHomeFinderService(session).find();
        if(!session.isAccessible(home)) {
            // not accessible (important for Sharepoint)
            return unknown;
        }
        final Drive.Metadata metadata;
        try {
            // retrieve OneDriveItem from home
            final DriveItem item = session.getItem(home, true);
            // returns drive, which can then query metadata.
            metadata = item.getDrive().getMetadata(new ODataQuery().select(Drive.Property.Quota));
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, home);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, home);
        }
        final org.nuxeo.onedrive.client.types.Quota quota = metadata.getQuota();
        if(quota != null) {
            Long used = quota.getUsed();
            if(used != null) {
                Long remaining = quota.getRemaining();
                if(remaining != null && (used != 0 || remaining != 0)) {
                    return new Space(used, remaining);
                }
                Long total = quota.getTotal();
                if(total != null && (used != 0 || total != 0)) {
                    return new Space(used, total - used);
                }
            }
        }
        return unknown;
    }
}
