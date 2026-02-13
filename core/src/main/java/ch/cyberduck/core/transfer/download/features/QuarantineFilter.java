package ch.cyberduck.core.transfer.download.features;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.QuarantineService;
import ch.cyberduck.core.local.QuarantineServiceFactory;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class QuarantineFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(QuarantineFilter.class);

    private final QuarantineService quarantine = QuarantineServiceFactory.get();

    private final Session<?> session;

    public QuarantineFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(local.isPresent()) {
            final DescriptiveUrlBag provider = session.getFeature(UrlProvider.class).toUrl(file).filter(DescriptiveUrl.Type.provider, DescriptiveUrl.Type.http);
            for(DescriptiveUrl url : provider) {
                try {
                    // Set quarantine attributes
                    quarantine.setQuarantine(local.get(), new HostUrlProvider().withUsername(false).get(session.getHost()), url.getUrl());
                    // Set quarantine attributes
                    quarantine.setWhereFrom(local.get(), url.getUrl());
                }
                catch(LocalAccessDeniedException e) {
                    log.warn("Failure to quarantine file {}. {}", file, e.getMessage());
                }
                break;
            }
        }
    }
}
