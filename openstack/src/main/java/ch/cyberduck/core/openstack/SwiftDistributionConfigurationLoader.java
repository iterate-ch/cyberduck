package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Preload CDN configuration
 */
public class SwiftDistributionConfigurationLoader extends OneTimeSchedulerFeature<Map<Path, Set<Distribution>>> {
    private static final Logger log = LogManager.getLogger(SwiftDistributionConfigurationLoader.class);

    private final SwiftSession session;

    public SwiftDistributionConfigurationLoader(final SwiftSession session) {
        this.session = session;
    }

    @Override
    protected Map<Path, Set<Distribution>> operate(final PasswordCallback callback) throws BackgroundException {
        final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
        if(null == feature) {
            return Collections.emptyMap();
        }
        final AttributedList<Path> containers = new SwiftContainerListService(session,
                new SwiftLocationFeature.SwiftRegion(session.getHost().getRegion())).list(Home.ROOT, new DisabledListProgressListener());
        final Map<Path, Set<Distribution>> distributions = new HashMap<>();
        for(Path container : containers) {
            for(Distribution.Method method : feature.getMethods(container)) {
                final Distribution distribution = feature.read(container, method, new DisabledLoginCallback());
                log.info("Cache distribution {}", distribution);
                distributions.getOrDefault(container, new HashSet<>()).add(distribution);
            }
        }
        return distributions;
    }
}
