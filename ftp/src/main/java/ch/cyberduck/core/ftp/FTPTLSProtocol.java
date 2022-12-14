package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.TimestampComparisonService;

import org.apache.commons.lang3.StringUtils;

public class FTPTLSProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public Type getType() {
        return Type.ftp;
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", FTPTLSProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public Statefulness getStatefulness() {
        return Statefulness.stateful;
    }

    @Override
    public String getName() {
        return "FTP-SSL";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("FTP-SSL (Explicit AUTH TLS)");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.ftps;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public boolean isUTCTimezone() {
        return false;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return true;
    }

    @Override
    public boolean isCertificateConfigurable() {
        return true;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return true;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new TimestampComparisonService(), new TimestampComparisonService());
        }
        return super.getFeature(type);
    }
}
