package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.PathRelativizer;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Locale;

public class B2UrlProvider implements UrlProvider {

    private final PathContainerService containerService;
    private final B2Session session;

    public B2UrlProvider(final B2Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file, final EnumSet<DescriptiveUrl.Type> types) {
        if(file.isVolume()) {
            return DescriptiveUrlBag.empty();
        }
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile() || file.isDirectory()) {
            if(types.contains(DescriptiveUrl.Type.http) && file.isFile()) {
                final String download = String.format("%s/file/%s/%s", session.getClient().getDownloadUrl(),
                    URIEncoder.encode(containerService.getContainer(file).getName()),
                    URIEncoder.encode(containerService.getKey(file)));
                list.add(new DescriptiveUrl(download, DescriptiveUrl.Type.http,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), Scheme.https.name().toUpperCase(Locale.ROOT))));
            }
            if(types.contains(DescriptiveUrl.Type.provider)) {
                final Path container = containerService.getContainer(file);
                if(!file.isRoot()) {
                    String cliUrl;
                    if(container.equals(file)) {
                        cliUrl = String.format("b2://%s/", container.getName());
                    }
                    else {
                        String key;
                        if(file.isDirectory()) {
                            key = PathRelativizer.relativize(container.getAbsolute(), file.getAbsolute());
                        }
                        else {
                            key = containerService.getKey(file);
                        }
                        if(file.isDirectory() && !key.endsWith("/")) {
                            key = key + "/";
                        }
                        cliUrl = String.format("b2://%s/%s", container.getName(), key);
                    }
                    list.add(new DescriptiveUrl(cliUrl, DescriptiveUrl.Type.provider,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), "B2 CLI")));
                }
            }
        }
        return list;
    }
}
