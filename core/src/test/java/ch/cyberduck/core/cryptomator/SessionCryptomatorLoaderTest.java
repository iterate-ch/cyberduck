package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.util.EnumSet;

import static org.junit.Assert.fail;

public class SessionCryptomatorLoaderTest {

    @Test
    public void testLoad() throws Exception {
        final SessionCryptomatorLoader loader = new SessionCryptomatorLoader(new NullSession(new Host(new TestProtocol())) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
                            final String masterKey = "{\"version\":5,\"scryptSalt\":\"JdjFoskbyIE=\",\"scryptCostParam\":16384,\"scryptBlockSize\":8,"
                                    + "\"primaryMasterKey\":\"h+5DIMCFiMTa1lBbd/i4jsORzQXe5YcqUME5Cmza4raqBpFQ+lkqaQ==\","
                                    + "\"hmacMasterKey\":\"qSdfm+JwGLfapvNrqmqo32WVS8idB76nPLxo611DIfdgCFxGbrAlZQ==\","
                                    + "\"versionMac\":\"ALE/39EGv6oLi5/LPtTVVTxPuzrmtRqUJGzMZJ5zyIc=\"}";
                            return IOUtils.toInputStream(masterKey);
                        }

                        @Override
                        public boolean offset(final Path file) throws BackgroundException {
                            return false;
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
        try {
            loader.load(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledLoginCallback() {
                @Override
                public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    throw new LoginCanceledException();
                }
            });
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
        try {
            loader.load(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledLoginCallback() {
                @Override
                public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    credentials.setPassword("null");
                }
            });
            fail();
        }
        catch(CryptoAuthenticationException e) {
            //
        }
        loader.load(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("coke4you");
            }
        });
    }

}