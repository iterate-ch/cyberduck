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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.model.UserSharesModel;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class EueShareUrlProviderTest {

    @Test
    public void toUrl() {
        assertEquals(DescriptiveUrl.EMPTY, new EueShareUrlProvider(new Host(new EueProtocol()), new UserSharesModel()).toUrl(
                new Path("/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed));
    }
}