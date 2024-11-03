package ch.cyberduck.core.cloudfront;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class CloudFrontDistributionConfigurationPreloader extends OneTimeSchedulerFeature<Set<Distribution>> {
    private static final Logger log = LogManager.getLogger(CloudFrontDistributionConfigurationPreloader.class);

    private final S3Session session;

    public CloudFrontDistributionConfigurationPreloader(final S3Session session) {
        super(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        this.session = session;
    }

    @Override
    protected Set<Distribution> operate(final PasswordCallback callback, final Path file) throws BackgroundException {
        final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
        if(null == feature) {
            return Collections.emptySet();
        }
        final AttributedList<Path> containers = new S3BucketListService(session)
            .list(file, new DisabledListProgressListener());
        final Set<Distribution> distributions = new LinkedHashSet<>();
        for(Path container : containers) {
            for(Distribution.Method method : feature.getMethods(container)) {
                if(Distribution.WEBSITE.equals(method)) {
                    continue;
                }
                final Distribution distribution = feature.read(container, method, new DisabledLoginCallback());
                if(log.isInfoEnabled()) {
                    log.info("Cache distribution {}", distribution);
                }
                distributions.add(distribution);
            }
        }
        return distributions;
    }
}
