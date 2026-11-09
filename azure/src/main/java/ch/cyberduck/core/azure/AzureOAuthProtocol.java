package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Protocol;

import com.google.auto.service.AutoService;

/**
 * Authentication with Microsoft Entra ID
 */
@AutoService(Protocol.class)
public class AzureOAuthProtocol extends AzureProtocol {

    @Override
    public String getIdentifier() {
        return "azure-oauth";
    }

    @Override
    public Type getType() {
        return Type.azure;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "azure");
    }
}
