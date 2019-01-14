package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

public abstract class AbstractMantaTest {
    private static final Logger log = Logger.getLogger(AbstractMantaTest.class);

    protected MantaSession session;
    protected Path testPathPrefix;

    @Before
    public void setup() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new MantaProtocol()))).read(
            new Local("../profiles/Joyent Triton Object Storage (us-east).cyberduckprofile"));

        final String hostname;
        final Local file;
        if(ObjectUtils.allNotNull(System.getProperty("manta.key_path"), System.getProperty("manta.url"))) {
            file = new Local(System.getProperty("manta.key_path"));
            hostname = new URL(System.getProperty("manta.url")).getHost();
        }
        else {
            final String key = "-----BEGIN RSA PRIVATE KEY-----\nMIIEogIBAAKCAQEAvlJTtsPpgDlSvoYwmWh9h6RMJBiVPQXUwqPgmdQIGjnYCQGD\nxJ7q778mikHA4igWXYOY6jOZ34jxfi+45Hcrloh538/Qa12t+XNHBnuAO6BpcBnh\niIRamr8bJISQZX9KfQmsbZ+8360/N02eOIYX03cMxd4soqQ7q56Jzdt9hyQDVZRT\npUN8CSc3YCkvNINnDBU/jGbDMLX4UB8hGL95sBLqJGkm0i+zihXpXma0/PVaAKnn\npPkNfPwEBDrhsFTd4obOfs5XtcrTOL9lO2GexsjrI4Vhu2CX63SAZ/DFdtvyeydW\nXRtIFVne+4A6xh9/13Z4iE5Onsl0liMQk1Q9KwIDAQABAoIBAEfC9Qu5zSZq9tcd\n8980NfjaK1eE6Wir9TA66Go4N6Hj46BpsMyHe2BQu/BvoJHluaEjCJpuQHu3wA7r\nYZTLlmTZKtMIIbcKCJpBLCu2j4BsGLWLHK4D8cHdgxd+4I9Usrp41koza90PDwIE\nQz9e2EcE4Y0OG9hrgpBQY/d55lf4xaVmQQiHEcEDVkIyMXq4W14rPTulrbd+F0pw\nMRIheWAa9rg9nm/qG9Am1S4ESIsUN39yQfkc+ArKRQwWP4VLF8lwcaQDt2Oa+836\n0aTUjIlZBmuKUU4u/wmJ04M9ZSw5S/Met5x+kVuEUxROt/8eXF4Wr81gwXQ7aoEz\nlVxtLikCgYEA+HKTPL4hNNjVMf6fDSFiu31EVgcitBC40ONV08WZInpzcNc+sl0+\nty0Ch8Wn3fsZ2MRtp2qGROA8Z7pdY7aL4dLWKMLaGmJMZu8qtPaIu6GUl279zW3N\n4lXOlLoXa7oyUB8yYS10zVDN3XHCvRa8n0GvZFTBKGGrDrcuZdUONPUCgYEAxBtq\nB4Kx6cjItcAZSaERgKcVKyFOMpBc+UGiCIW1yXi63enNSFbkdAwTkHSVyeik7xqh\nfhJ9y2F/bvikQixwHiZVhap+gc+pe/j0aAC9e19o+kjFJiHUwCAQ96zNu6QAZTjR\n7IChdOSVIxuDs3mDBMG2e9RRlR6PDXxEi5S8VZ8CgYAHGL677XJlYAw28V75sQpw\n8JMTIgELw66DyPqaofpN0dGaV4ui7Kbt9Ist9adl39ZNKs83CQPs07rl+5zPTFeS\ni8MyRt6UAlrMVeiSYrhlI6hq6vC0/X30CR9tgCNLIHZvc3Ss8e90LeqzeJxnak7Y\n/bdU1lbuIFwSf4kDv6I4QQKBgF6OmWFls0N2fNCl/4txDm9qINrbBEl9Mlc9PlO9\npRmwDOpTgZgPzbfm2sgcbt0cP+rKfHO9lsoqCLgJS6pcovLmqPX6b2VILACK2c4M\nDVEfgA6uZ+ErDtpUm9nQiKKhQU+NRiszGqayUPbMnYQ8YuA4RzUN+whb474s3SAw\nZ18hAoGAXsOEFOFjaaGtbXGOZyyqdYoD5G22Qfi89tbEled5OeSsuFayix6vMt4T\nItr+gF82ED4fX42FIrg6igJ34z2pzsP4hIqH3yNRfc9rwv7GWBKYWzUDrBxEg5cx\nbt2CJUfZImSP9K1uahfc8MP7FFMdnnWPlkOpNc76G8FIpchIsNU=\n-----END RSA PRIVATE KEY----- ";
            file = TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random());
            LocalTouchFactory.get().touch(file);
            IOUtils.write(key, file.getOutputStream(false), Charset.defaultCharset());
            hostname = profile.getDefaultHostname();
        }

        final String user = System.getProperty("manta.user");
        final Host host = new Host(profile, hostname, new Credentials(user).withIdentity(file));
        session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final String testRoot = "cyberduck-test-" + new AlphanumericRandomStringService().random();
        testPathPrefix = new Path(new MantaAccountHomeInfo(host.getCredentials().getUsername(), host.getDefaultPath()).getAccountPrivateRoot(), testRoot, EnumSet.of(Type.directory));
        session.getClient().putDirectory(testPathPrefix.getAbsolute());
    }

    @After
    public void disconnect() throws Exception {
        log.debug("cleaning up test directory: " + testPathPrefix);
        session.getClient().deleteRecursive(testPathPrefix.getAbsolute());
        session.close();
    }

    protected Path randomFile() {
        return new Path(
            testPathPrefix,
            UUID.randomUUID().toString(),
            EnumSet.of(Type.file));
    }

    protected Path randomDirectory() {
        return new Path(
            testPathPrefix,
            UUID.randomUUID().toString(),
            EnumSet.of(Type.directory));
    }
}
