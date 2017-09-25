package ch.cyberduck.core.sds;/*
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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

public class SDSProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "sds";
    }

    @Override
    public String getName() {
        return "SDS";
    }

    @Override
    public String getDescription() {
        return "SSP Secure Data Space";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String getContext() {
        return "/api/v4";
    }

    @Override
    public String getAuthorization() {
        return Authorization.sql.name();
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", SDSProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return StringUtils.isBlank(this.getOAuthClientId());
    }

    @Override
    public boolean isCertificateConfigurable() {
        return false;
    }

    public enum Authorization {
        sql,
        radius,
        active_directory,
        oauth
    }
}
