package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerData;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class SDSQuotaFeature implements Quota {
    private static final Logger log = LogManager.getLogger(SDSQuotaFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSQuotaFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final Path home = new DefaultHomeFinderService(session).find();
            if(!home.isRoot()) {
                if(TransferStatus.UNKNOWN_LENGTH == home.attributes().getQuota()) {
                    log.warn(String.format("No quota set for node %s", home));
                }
                else {
                    return new Space(home.attributes().getSize(), home.attributes().getQuota() - home.attributes().getSize());
                }
            }
            final CustomerData info = new UserApi(session.getClient()).requestCustomerInfo(StringUtils.EMPTY);
            return new Space(info.getSpaceUsed(), info.getSpaceLimit() - info.getSpaceUsed());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to read attributes of {0}", e,
                new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
    }
}
