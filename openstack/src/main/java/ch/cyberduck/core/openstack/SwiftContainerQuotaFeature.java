package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2026 AiNET and Blomp. All rights reserved.
 * https://ai.net/
 * https://www.blomp.com/
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.ContainerMetadata;
import ch.iterate.openstack.swift.model.Region;

/**
 * Quota of a single container from its bytes used and the {@code X-Container-Meta-Quota-Bytes} metadata set by the
 * container quotas middleware.
 */
public class SwiftContainerQuotaFeature implements Quota {
    private static final Logger log = LogManager.getLogger(SwiftContainerQuotaFeature.class);

    static final String QUOTA_BYTES = "X-Container-Meta-Quota-Bytes";

    private final SwiftSession session;
    private final SwiftRegionService regionService;
    private final Path container;

    public SwiftContainerQuotaFeature(final SwiftSession session, final SwiftRegionService regionService, final Path container) {
        this.session = session;
        this.regionService = regionService;
        this.container = container;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final Region region = regionService.lookup(container);
            final Client client = session.getClient();
            final ContainerInfo info = client.getContainerInfo(region, container.getName());
            // Metadata names are matched case-insensitively by the client library
            final ContainerMetadata metadata = client.getContainerMetaData(region, container.getName());
            return toSpace(info.getTotalSize(), metadata.getMetaData().get(QUOTA_BYTES));
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, container);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, container);
        }
    }

    /**
     * @param used  Bytes used by the container
     * @param quota Value of the quota metadata or null if not set
     * @return Bytes used and available from quota. Available is unlimited if no quota is set.
     */
    static Space toSpace(final long used, final String quota) {
        final long limit = NumberUtils.toLong(StringUtils.trim(quota), -1L);
        if(limit < 0) {
            log.debug("No quota set for container");
            return new Space(Math.max(0L, used), Long.MAX_VALUE);
        }
        return new Space(Math.max(0L, used), Math.max(0L, limit - Math.max(0L, used)));
    }
}
