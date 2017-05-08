package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;

public class CryptoMultipartWriteFeature<Reply> extends CryptoWriteFeature<Reply> implements MultipartWrite<Reply> {
    public CryptoMultipartWriteFeature(final Session<?> session, final Write<Reply> delegate, final CryptoVault vault) {
        super(session, delegate, vault);
    }

    public CryptoMultipartWriteFeature(final Session<?> session, final Write<Reply> delegate, final Find finder, final AttributesFinder attributes, final CryptoVault vault) {
        super(session, delegate, finder, attributes, vault);
    }
}
