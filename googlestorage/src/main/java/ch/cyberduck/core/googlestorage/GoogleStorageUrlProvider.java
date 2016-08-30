package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import java.net.URI;
import java.text.MessageFormat;

public class GoogleStorageUrlProvider implements UrlProvider {

    private GoogleStorageSession session;

    public GoogleStorageUrlProvider(final GoogleStorageSession session) {
        this.session = session;
    }

    /**
     * This creates an URL that uses Cookie-based Authentication. The ACLs for the given Google user account
     * has to be setup first.
     * <p>
     * Google Storage lets you provide browser-based authenticated downloads to users who do not have
     * Google Storage accounts. To do this, you apply Google account-based ACLs to the object and then
     * you provide users with a URL that is scoped to the object.
     */
    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DefaultUrlProvider(session.getHost()).toUrl(file);
        if(file.isFile()) {
            // Authenticated browser download using cookie-based Google account authentication in conjunction with ACL
            list.add(new DescriptiveUrl(URI.create(String.format("https://storage.cloud.google.com%s",
                    URIEncoder.encode(file.getAbsolute()))), DescriptiveUrl.Type.authenticated,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Authenticated"))));
        }
        return list;
    }
}