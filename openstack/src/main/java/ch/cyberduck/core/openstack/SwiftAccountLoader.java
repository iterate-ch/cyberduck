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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.OneTimeSchedulerFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

public class SwiftAccountLoader extends OneTimeSchedulerFeature<Map<Region, AccountInfo>> {
    private static final Logger log = LogManager.getLogger(SwiftAccountLoader.class);

    private final SwiftSession session;

    public SwiftAccountLoader(final SwiftSession session) {
        super(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        this.session = session;
    }

    @Override
    protected Map<Region, AccountInfo> operate(final PasswordCallback callback, final Path file) throws BackgroundException {
        final Map<Region, AccountInfo> accounts = new ConcurrentHashMap<>();
        for(Region region : session.getClient().getRegions()) {
            try {
                final AccountInfo info = session.getClient().getAccountInfo(region);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Signing key is %s", info.getTempUrlKey()));
                }
                if(StringUtils.isBlank(info.getTempUrlKey())) {
                    // Update account info setting temporary URL key
                    try {
                        final String key = new AsciiRandomStringService().random();
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set acccount temp URL key to %s", key));
                        }
                        session.getClient().updateAccountMetadata(region, Collections.singletonMap("X-Account-Meta-Temp-URL-Key", key));
                        info.setTempUrlKey(key);
                    }
                    catch(GenericException e) {
                        log.warn(String.format("Ignore failure %s updating account metadata", e));
                    }
                }
                accounts.put(region, info);
            }
            catch(GenericException e) {
                if(e.getHttpStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                    log.warn(String.format("Ignore failure %s for region %s", e, region));
                    continue;
                }
                throw new SwiftExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        return accounts;
    }
}
