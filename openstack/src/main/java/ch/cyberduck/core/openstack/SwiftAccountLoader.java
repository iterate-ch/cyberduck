package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

public class SwiftAccountLoader extends OneTimeSchedulerFeature<Map<Region, AccountInfo>> {
    private static final Logger log = Logger.getLogger(SwiftAccountLoader.class);

    private final SwiftSession session;

    public SwiftAccountLoader(final SwiftSession session) {
        super(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        this.session = session;
    }

    @Override
    protected Map<Region, AccountInfo> operate(final PasswordCallback callback, final Path file) throws BackgroundException {
        try {
            final Map<Region, AccountInfo> accounts = new ConcurrentHashMap<>();
            for(Region region : session.getClient().getRegions()) {
                final AccountInfo info = session.getClient().getAccountInfo(region);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Signing key is %s", info.getTempUrlKey()));
                }
                accounts.put(region, info);
            }
            return accounts;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
