package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ObjectExpiration;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class SDSSharesUrlProviderTest {

    @Test
    public void toUrl() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toUrl(test,
                new CreateDownloadShareRequest()
                        .expiration(new ObjectExpiration().enableExpiration(false))
                        .notifyCreator(false)
                        .password(null)
                        .mailBody(null)
                        .mailRecipients(null)
                        .mailSubject(null)
                        .maxDownloads(null));
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/api/v4/public/shares/downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}