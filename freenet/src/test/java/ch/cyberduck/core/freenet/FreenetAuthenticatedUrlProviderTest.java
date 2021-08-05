package ch.cyberduck.core.freenet;

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
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class FreenetAuthenticatedUrlProviderTest extends AbstractFreenetTest {

    @Test
    public void testToUrl() {
        final FreenetAuthenticatedUrlProvider provider = new FreenetAuthenticatedUrlProvider(session.getHost());
        final DescriptiveUrlBag urls = provider.toUrl(new Path("/", EnumSet.of(Path.Type.directory)));
        assertFalse(urls.isEmpty());
        assertNotEquals(DescriptiveUrl.EMPTY, urls.find(DescriptiveUrl.Type.authenticated));
    }
}
