package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.UrlProvider;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import com.google.api.services.drive.model.File;

public class DriveUrlProvider implements UrlProvider {

    private DriveSession session;

    public DriveUrlProvider(DriveSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile()) {
            try {
                if(StringUtils.isBlank(file.attributes().getVersionId())) {
                    return DescriptiveUrlBag.empty();
                }
                final File f = session.getClient().files().get(file.attributes().getVersionId()).execute();
                if(StringUtils.isNotBlank(f.getWebContentLink())) {
                    list.add(new DescriptiveUrl(URI.create(f.getWebContentLink()),
                            DescriptiveUrl.Type.http,
                            MessageFormat.format(LocaleFactory.localizedString("{0} URL"), "HTTP")));
                }
                if(StringUtils.isNotBlank(f.getWebViewLink())) {
                    list.add(new DescriptiveUrl(URI.create(f.getWebViewLink()),
                            DescriptiveUrl.Type.http,
                            MessageFormat.format(LocaleFactory.localizedString("{0} URL"), "Download")));
                }
            }
            catch(IOException e) {
                new DriveExceptionMappingService().map(e);
            }
        }
        return list;
    }
}
