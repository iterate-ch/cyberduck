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
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Preload CDN configuration
 */
public class SwiftDistributionConfigurationLoader extends OneTimeSchedulerFeature<Map<Path, Distribution>> {
    private static final Logger log = Logger.getLogger(SwiftDistributionConfigurationLoader.class);

    private final SwiftSession session;

    public SwiftDistributionConfigurationLoader(final SwiftSession session) {
        super(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        this.session = session;
    }

    @Override
    protected Map<Path, Distribution> operate(final PasswordCallback callback, final Path file) throws BackgroundException {
        final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
        final AttributedList<Path> containers = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion(session.getHost().getRegion())).list(
                new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener()
        );
        final Map<Path, Distribution> distributions = new ConcurrentHashMap<>();
        for(Path container : containers) {
            for(Distribution.Method method : feature.getMethods(container)) {
                final Distribution distribution = feature.read(container, method, new DisabledLoginCallback());
                if(log.isInfoEnabled()) {
                    log.info(String.format("Cache distribution %s", distribution));
                }
                distributions.put(container, distribution);
            }
        }
        return distributions;
    }

    @Override
    public void shutdown() {
        //
    }
}
