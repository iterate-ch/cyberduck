package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class GmxcloudDirectoryFeatureTest extends AbstractGmxcloudTest {

    @Test(expected = InteroperabilityException.class)
    public void testProhibitedName() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        try {
            new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(String.format("%s.", new AlphanumericRandomStringService().random()),
                    EnumSet.of(Path.Type.directory)), new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Paths may not end with a . Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }
}