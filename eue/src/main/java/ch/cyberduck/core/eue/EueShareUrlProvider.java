package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.UserSharesModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collections;

public class EueShareUrlProvider implements UrlProvider {
    private static final Logger log = LogManager.getLogger(EueShareUrlProvider.class);

    private final Host host;
    private final UserSharesModel shares;

    public EueShareUrlProvider(final Host host, final UserSharesModel shares) {
        this.host = host;
        this.shares = shares;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        if(DescriptiveUrl.EMPTY == file.attributes().getLink()) {
            if(null == file.attributes().getFileId()) {
                return DescriptiveUrlBag.empty();
            }
            final DescriptiveUrl share = toUrl(host, EueShareFeature.findShareForResource(shares,
                    file.attributes().getFileId()));
            if(DescriptiveUrl.EMPTY == share) {
                return DescriptiveUrlBag.empty();
            }
            return new DescriptiveUrlBag(Collections.singleton(share));
        }
        return new DescriptiveUrlBag(Collections.singleton(file.attributes().getLink()));
    }

    protected static DescriptiveUrl toUrl(final Host host, final ShareCreationResponseEntity shareCreationResponse) {
        if(null == shareCreationResponse) {
            return DescriptiveUrl.EMPTY;
        }
        return new DescriptiveUrl(URI.create(EueShareFeature.toBrandedUri(shareCreationResponse.getGuestURI(),
                host.getProperty("share.hostname"))), DescriptiveUrl.Type.signed);
    }
}
