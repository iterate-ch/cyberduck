package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.hierynomus.smbj.session.Session;
import com.rapid7.client.dcerpc.mssrvs.ServerService;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo0;
import com.rapid7.client.dcerpc.transport.RPCTransport;
import com.rapid7.client.dcerpc.transport.SMBTransportFactories;

public class SMBRootListService implements ListService {
    private static final Logger log = LogManager.getLogger(SMBRootListService.class);

    private final SMBSession session;
    private final PasswordCallback prompt;
    private final Session context;

    public SMBRootListService(final SMBSession session, final PasswordCallback prompt, final Session context) {
        this.session = session;
        this.prompt = prompt;
        this.context = context;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            if(StringUtils.isNotBlank(session.getHost().getProtocol().getContext())) {
                // Use share name from context in profile
                final Path share = new Path(session.getHost().getProtocol().getContext(), EnumSet.of(Path.Type.directory, Path.Type.volume));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Connect to share %s from profile context", share));
                }
                return new AttributedList<>(Collections.singleton(share.withAttributes(new SMBAttributesFinderFeature(session).find(share))));
            }
            else {
                try {
                    if(log.isDebugEnabled()) {
                        log.debug("Attempt to list available shares");
                    }
                    // An SRVSVC_HANDLE pointer that identifies the server.
                    final RPCTransport transport = SMBTransportFactories.SRVSVC.getTransport(context);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Obtained transport %s", transport));
                    }
                    final ServerService lookup = new ServerService(transport);
                    final List<NetShareInfo0> info = lookup.getShares0();
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved share info %s", info));
                    }
                    final AttributedList<Path> result = new AttributedList<>();
                    for(final String s : info.stream().map(NetShareInfo::getNetName).collect(Collectors.toSet())) {
                        final Path share = new Path(s, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
                        try {
                            result.add(share.withAttributes(new SMBAttributesFinderFeature(session).find(share)));
                        }
                        catch(UnsupportedException e) {
                            if(log.isWarnEnabled()) {
                                log.warn(String.format("Skip unsupprted share %s", s));
                            }
                        }
                    }
                    return result;
                }
                catch(IOException e) {
                    if(log.isWarnEnabled()) {
                        log.warn(String.format("Failure %s getting share info from server", e));
                    }
                    final Credentials name = prompt.prompt(session.getHost(),
                            LocaleFactory.localizedString("SMB Share"),
                            LocaleFactory.localizedString("Enter the pathname to list:", "Goto"),
                            new LoginOptions().icon(session.getHost().getProtocol().disk()).keychain(true)
                                    .passwordPlaceholder(LocaleFactory.localizedString("SMB Share"))
                                    .password(false));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Connect to share %s from user input", name.getPassword()));
                    }
                    if(name.isSaved()) {
                        session.getHost().setDefaultPath(name.getPassword());
                    }
                    final Path share = new Path(name.getPassword(), EnumSet.of(Path.Type.directory, Path.Type.volume));
                    return new AttributedList<>(Collections.singleton(share.withAttributes(new SMBAttributesFinderFeature(session).find(share))));
                }
            }
        }
        return new SMBListService(session).list(directory, listener);
    }
}
