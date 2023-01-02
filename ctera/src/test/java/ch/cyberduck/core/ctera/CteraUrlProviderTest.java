package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class CteraUrlProviderTest {

    @Test
    public void toUrl() {
        final Host host = new Host(new CteraProtocol(), "mountainduck.ctera.me");
        host.setDefaultPath("/ServicesPortal/webdav");
        final String filename = new AlphanumericRandomStringService().random();
        final Path test = new Path(new Path("/ServicesPortal/webdav/My Files", EnumSet.of(Path.Type.directory)),
                filename, EnumSet.of(Path.Type.file));
        assertEquals("https://mountainduck.ctera.me/ServicesPortal/#/cloudDrive/My%20Files/" + filename,
                new CteraUrlProvider(host).toUrl(test).find(DescriptiveUrl.Type.provider).getUrl());
    }
}