package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

public class FoundationProgressIconServiceTest {

    @Test
    public void set() {
        final Local f = TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random());
        final FoundationProgressIconService service = new FoundationProgressIconService();
        service.set(f, new TransferStatus().length(1L));
        service.set(f, new TransferStatus().length(1L).skip(1L));
        service.remove(f);
    }
}
