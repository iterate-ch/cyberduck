package ch.cyberduck.core.sds.triplecrypt;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import com.dracoon.sdk.crypto.CryptoException;

public class CryptoExceptionMappingService extends AbstractExceptionMappingService<CryptoException> {

    @Override
    public BackgroundException map(final CryptoException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        return new AccessDeniedException(buffer.toString(), failure);
    }
}
