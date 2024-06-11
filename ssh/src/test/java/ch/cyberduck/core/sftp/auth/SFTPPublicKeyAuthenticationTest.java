package ch.cyberduck.core.sftp.auth;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPPublicKeyAuthenticationTest extends AbstractSFTPTest {

    @Test
    public void testAuthenticateKeyNoPassword() throws Exception {
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            session.getHost().getCredentials().setIdentity(key);
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader("-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEA0oUHL9fx0o91DHadQq0npjbWpOx61EmHsDBenBcR6U7oI3xg\n" +
                "SE5/YqYp1nkFlIpBeffIplxxpVqxVLazzQqt73vkCdCsILFsFGfs+2LPlh0nL5Bx\n" +
                "hKKCbxbHZKk9IqYXYQbMjGEWexenLPaUQiJWZO4lZRYU2WyLnIxDyULarWE81umv\n" +
                "VXNy+yBIs5j58WWMPpEpDv8+OK0CnXLF5+MUS+3afUvhNyheC52FgoEx7hDEhFb2\n" +
                "W0/cURGoFsq0zih7Xbu0z6c5DaQxfJgLR6tKGPkKdYcW19oQcz4/wzqlyrXp2h0U\n" +
                "LFfFkyJ2WVkmcC75FPB0FQdxWLsEfHVwj5rGWQIDAQABAoIBABaBPhlxPlrCF0Mz\n" +
                "0u/zzs9IAX40AZV8HtNI+HQ7SyDj3JEelLRmH9wN418YJO6SMRm20W5nZ4bDwT1L\n" +
                "IsGCqMF3PxOFiUEddm5gBceB8qsbhrrg+CGztbnmd35dUTnJA6rNrUQ8hMi36Jk3\n" +
                "6vV+8jlr8/dwet4banlbzUtQqEvsbWT32/IiIiflWzDGEpiftDWQ2hoGV/siEKCu\n" +
                "tbNH9S2QJ8/GkCNfFLax6wImy7nxuHtzhjM2OnApXZHxrots5HTNqEv6gETM+1nz\n" +
                "N2nNa19CnxKKVVkZIeg8yQp395QmSTmVZKbzBwLLkyX00EZH1VD+lySkIwOoYOo1\n" +
                "K+qlDwECgYEA/BJ1vba0bndxsqhAmrNSIGc7gRc5FzijyfhcHV7/rpuwfJu/8zV5\n" +
                "KuB0QmqVRCdF/DTKAJb82sf5K4Y4rwOHC8/FUE63UEXzTEHs1LYVugjOT1Jrv9dP\n" +
                "xYHEwL/r9lGZ8ZeC7qRBik4Bj1O5klZuRg+864gsMjcBJ+gZDOBL+EkCgYEA1czP\n" +
                "sv2Z9JTyXWcDxKcCEiAVbkaJybIhfh3qEL9YdHfYnKjSmfd7T1ALKfOI6Npl19WB\n" +
                "pTSiBLyf10hSR2CJ6INvTNbG+a/k7Eit8L1m5QfWBNUm4/6fYE5C28dq9zvMxg8O\n" +
                "2NuhFdwEjKONFK6KRO5nV51TZy/s/zK4+jyb/ZECgYEAzqrD5fs6C6fajEPuU+fZ\n" +
                "2b9JxhkoMaW6hAkgXORadOoo1MFNqzWYVlMYfqusHeilNJaDV+BzB10VKV4ucy9N\n" +
                "NZ3vhFxFZ4cyZr9WUT/IqHI31EkbFnqNaL1wdyfjalyU/sLeEpKRObejB8P5TcWX\n" +
                "O9IDboiC09jNFc7xWuE66CECgYEAgFq6YtnvgK+fJcX/tCMTkjAizZaZiDZh9yYc\n" +
                "bLuDbkC5Eawk63xxUjdazG/BxRHAzKgIclc74Z4gVA2+H83sXWPzAO18fVBFHjmv\n" +
                "+3Sp/lwK99HDzGMD6bxyVSHOrOkA0fMeVcDdX5M8Q/a4by8uIZ4C+s2f0bYrETax\n" +
                "aMHUYJECgYAtpNPOcW8gou019Ys+RaRc0c0k8R5WAgxP0lqEujn+OS/PIdjdWOdD\n" +
                "ZDuIcmUpyqcqqD/+wfgS/HBu011Mi9NSzcw6mogsXFerk28usMUfKBnibyLgtfiM\n" +
                "jo5i4PI+2gDrScmRkBSmm86ewCo1J7Iq1Vk/0FL/oiEA2et7J8vIYA==\n" +
                "-----END RSA PRIVATE KEY-----\n"), key.getOutputStream(false), StandardCharsets.UTF_8);
            // Reconnect
            session.disconnect();
            session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            assertTrue(new SFTPPublicKeyAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    fail();
                    throw new LoginCanceledException();
                }
            }, new DisabledCancelCallback()));

        }
        finally {
            key.delete();
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testAuthenticatePuTTYKeyWithWrongPassword() throws Exception {
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader("PuTTY-User-Key-File-2: ssh-rsa\n" +
                "Encryption: aes256-cbc\n" +
                "Comment: rsa-key-20121215\n" +
                "Public-Lines: 4\n" +
                "AAAAB3NzaC1yc2EAAAABJQAAAIB7KdUyuvGb2ne9G9YDAjaYvX/Mq6Q6ppGjbEQo\n" +
                "bac66VUazxVpZsnAWikcdYAU7odkyt3jg7Nn1NgQS1a5mpXk/j77Ss5C9W4rymrU\n" +
                "p32cmbgB/KIV80DnOyZyOtDWDPM0M0RRXqQvAO6TsnmsNSnBa8puMLHqCtrhvvJD\n" +
                "KU+XEw==\n" +
                "Private-Lines: 8\n" +
                "4YMkPgLQJ9hOI1L1HsdOUnYi57tDy5h9DoPTHD55fhEYsn53h4WaHpxuZH8dTpbC\n" +
                "5TcV3vYTfhh+aFBY0p/FI8L1hKfABLRxhkqkkc7xMmOGlA6HejAc8oTA3VArgSeG\n" +
                "tRBuQRmBAC1Edtek/U+s8HzI2whzTw8tZoUUnT6844oc4tyCpWJUy5T8l+O3/03s\n" +
                "SceJ98DN2k+L358VY8AXgPxP6NJvHvIlwmIo+PtcMWsyZegfSHEnoXN2GN4N0ul6\n" +
                "298RzA9R+I3GSKKxsxUvWfOVibLq0dDM3+CTwcbmo4qvyM2xrRRLhObB2rVW07gL\n" +
                "7+FZpHxf44QoQQ8mVkDJNaT1faF+h/8tCp2j1Cj5yEPHMOHGTVMyaz7gqhoMw5RX\n" +
                "sfSP4ZaCGinLbouPrZN9Ue3ytwdEpmqU2MelmcZdcH6kWbLCqpWBswsxPfuhFdNt\n" +
                "oYhmT2+0DKBuBVCAM4qRdA==\n" +
                "Private-MAC: 40ccc8b9a7291ec64e5be0c99badbc8a012bf220\n" +
                "tp.key.openssh.rsa=-----BEGIN RSA PRIVATE KEY-----\n" +
                "Proc-Type: 4,ENCRYPTED\n" +
                "DEK-Info: AES-128-CBC,356A353DAFDC2E16E8BD0EE23A6201A2\n" +
                "\n" +
                "pvpC2RA1TteVsp584fSZ6RYFz97CF8tJXfyP4/8UdzpIVM8VXXlk4g3AnyI9/JD0\n" +
                "4/0dzNqGvg/84xUpDpdJp/w8fWJ8IzE7RXf1xDfg0xavr2iegp2aZBd48KVKImwU\n" +
                "yJlzy27VmVvIvWID2zPrNhOWzr4AdnP/NprLfhHPQDHV5gRcS92s6vFktZOPzNtQ\n" +
                "3O+j3O5MAyf/MpgPH4BTubOjcuZuZg3AJCjEPxLlwrRfxqXkRXXMB7XxDFdK7LQ/\n" +
                "fQnJzikcrYXFio8+DJhBg7OyOnlAmC0I85YomZJ+8C3A3bye9PakIxHJn/qNIQew\n" +
                "BujHxPVmnezjFzStr/SyfLE2a+RZu84Jm6u9+DuJYF5/Vo6yv6+zubsVaflXp5fS\n" +
                "SAogS0quWfoqoiUfhgCuOZlqv/aeo/BEetUEdHVi4KTdeSpcfrJa4CphXd8TwEPN\n" +
                "L4NFSc+8CeGayO45o5lXeQiKa4UH2oPEBaANHK4SQPKJ9NdyTlFN/O1c77kCvG4W\n" +
                "4thchQkUvwqwTYXwx9jNW3x7FBytJwmhi9DpzHMa/LFRrnedarFPDgep4E40NjRB\n" +
                "fy877Wd+KJTlrHjyQR13wgtlGZdcTO5QzLseztxqdaD14Dn7jPF/YJBDaj65Jw1N\n" +
                "+G6EB0zN70WL7Y3+2HnSLNZWEnLhletzfwbjVqr+Vg4XB2HQKH52gCyh+ITPEjqR\n" +
                "wU00oMJvGf518U+6awxzb3zwnoxMrFwcnaLqwsZNQ5CjmYVE/yERSK47OMYCNQl4\n" +
                "0Xxa9mWYBqWlfdMurkGCD6OuUWMx5t4mcpSg30UEQNBEVfrVk6t480iztgVJprEO\n" +
                "vhepM2nw326PH5VYAoXH+OmEezjI1AmHKqpbB/y9UQv6ZjEyUT70Tbs9JBtU4dze\n" +
                "Yha1Dc0+eYkUvZ5AjENQ/Bvfdyit4xxbDrU6TbFmyHpHwMPCNkcgO0u/Mgtc5Hmc\n" +
                "Gi6RaxUaxSZ2IlpJDNkqAzmv1Xr+M9TxbF2gZY+TJHUt/mc1rFpTl2qZ/tK/Ei1U\n" +
                "8TBVJHcNNwHiHtm/NpREYTmzu0s8X602JgXrkBxkM40NGVRqd08jaULhxdWcTmzW\n" +
                "pweib9WhIrvjTNZTAjjGku625qLihDt5jtbJxspM2dLGfcG4zgYgRr4u9HA+60oD\n" +
                "l1oNjz8IfBuJLJ3rwENI6oX9FW7huKc/XV1hP72/l2VhfuxtTufdjbaiwwiwObRA\n" +
                "O+zwB8NPWRG6UYj9IAWjASoPXOoyhk/f1fzvTH7xeO35QjkCICln095T+hNMZRiC\n" +
                "VpCCKsQGY2O30D9OJlnTpylBAq/Q/HXNL8Jj2f/rZRqDGzidj2No5mun/pZ3uwzr\n" +
                "CRrEpvfFuf8g1EnPQXmdlYRi/nmtBKsiQr0GWVzIOzNRi/tgsV0tyUgBT9QL4JKt\n" +
                "/z54PrlBK74I9SWcBv9EwCAfL9YdZ7mW0iWrmUUmcpuJcRUXnKvTynTpq/l6GE8+\n" +
                "Ld5saHMVWt7GlEbM3Fjqfvj7/dbtcy3TTmy0Vx4GbKzsaPytAb2jgLGn8bQfjQzp\n" +
                "hnPC1l+r7ebV7tBR216+6PmsXQu7atqgbGjb7Dh+GP8Ak73F8v6LPtyz+tAOYwpB\n" +
                "-----END RSA PRIVATE KEY-----\n"), key.getOutputStream(false), StandardCharsets.UTF_8);
            // Reconnect
            session.disconnect();
            session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            final AtomicBoolean p = new AtomicBoolean();
            session.getHost().getCredentials().setIdentity(key);
            assertFalse(new SFTPPublicKeyAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    p.set(true);
                    throw new LoginCanceledException();
                }
            }, new DisabledCancelCallback()));
            assertTrue(p.get());

        }
        finally {
            key.delete();
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testAuthenticateOpenSSHKeyWithPassword() throws Exception {
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader("-----BEGIN RSA PRIVATE KEY-----\n" +
                "Proc-Type: 4,ENCRYPTED\n" +
                "DEK-Info: AES-128-CBC,356A353DAFDC2E16E8BD0EE23A6201A2\n" +
                "\n" +
                "pvpC2RA1TteVsp584fSZ6RYFz97CF8tJXfyP4/8UdzpIVM8VXXlk4g3AnyI9/JD0\n" +
                "4/0dzNqGvg/84xUpDpdJp/w8fWJ8IzE7RXf1xDfg0xavr2iegp2aZBd48KVKImwU\n" +
                "yJlzy27VmVvIvWID2zPrNhOWzr4AdnP/NprLfhHPQDHV5gRcS92s6vFktZOPzNtQ\n" +
                "3O+j3O5MAyf/MpgPH4BTubOjcuZuZg3AJCjEPxLlwrRfxqXkRXXMB7XxDFdK7LQ/\n" +
                "fQnJzikcrYXFio8+DJhBg7OyOnlAmC0I85YomZJ+8C3A3bye9PakIxHJn/qNIQew\n" +
                "BujHxPVmnezjFzStr/SyfLE2a+RZu84Jm6u9+DuJYF5/Vo6yv6+zubsVaflXp5fS\n" +
                "SAogS0quWfoqoiUfhgCuOZlqv/aeo/BEetUEdHVi4KTdeSpcfrJa4CphXd8TwEPN\n" +
                "L4NFSc+8CeGayO45o5lXeQiKa4UH2oPEBaANHK4SQPKJ9NdyTlFN/O1c77kCvG4W\n" +
                "4thchQkUvwqwTYXwx9jNW3x7FBytJwmhi9DpzHMa/LFRrnedarFPDgep4E40NjRB\n" +
                "fy877Wd+KJTlrHjyQR13wgtlGZdcTO5QzLseztxqdaD14Dn7jPF/YJBDaj65Jw1N\n" +
                "+G6EB0zN70WL7Y3+2HnSLNZWEnLhletzfwbjVqr+Vg4XB2HQKH52gCyh+ITPEjqR\n" +
                "wU00oMJvGf518U+6awxzb3zwnoxMrFwcnaLqwsZNQ5CjmYVE/yERSK47OMYCNQl4\n" +
                "0Xxa9mWYBqWlfdMurkGCD6OuUWMx5t4mcpSg30UEQNBEVfrVk6t480iztgVJprEO\n" +
                "vhepM2nw326PH5VYAoXH+OmEezjI1AmHKqpbB/y9UQv6ZjEyUT70Tbs9JBtU4dze\n" +
                "Yha1Dc0+eYkUvZ5AjENQ/Bvfdyit4xxbDrU6TbFmyHpHwMPCNkcgO0u/Mgtc5Hmc\n" +
                "Gi6RaxUaxSZ2IlpJDNkqAzmv1Xr+M9TxbF2gZY+TJHUt/mc1rFpTl2qZ/tK/Ei1U\n" +
                "8TBVJHcNNwHiHtm/NpREYTmzu0s8X602JgXrkBxkM40NGVRqd08jaULhxdWcTmzW\n" +
                "pweib9WhIrvjTNZTAjjGku625qLihDt5jtbJxspM2dLGfcG4zgYgRr4u9HA+60oD\n" +
                "l1oNjz8IfBuJLJ3rwENI6oX9FW7huKc/XV1hP72/l2VhfuxtTufdjbaiwwiwObRA\n" +
                "O+zwB8NPWRG6UYj9IAWjASoPXOoyhk/f1fzvTH7xeO35QjkCICln095T+hNMZRiC\n" +
                "VpCCKsQGY2O30D9OJlnTpylBAq/Q/HXNL8Jj2f/rZRqDGzidj2No5mun/pZ3uwzr\n" +
                "CRrEpvfFuf8g1EnPQXmdlYRi/nmtBKsiQr0GWVzIOzNRi/tgsV0tyUgBT9QL4JKt\n" +
                "/z54PrlBK74I9SWcBv9EwCAfL9YdZ7mW0iWrmUUmcpuJcRUXnKvTynTpq/l6GE8+\n" +
                "Ld5saHMVWt7GlEbM3Fjqfvj7/dbtcy3TTmy0Vx4GbKzsaPytAb2jgLGn8bQfjQzp\n" +
                "hnPC1l+r7ebV7tBR216+6PmsXQu7atqgbGjb7Dh+GP8Ak73F8v6LPtyz+tAOYwpB\n" +
                "-----END RSA PRIVATE KEY-----\n"), key.getOutputStream(false), StandardCharsets.UTF_8);
            final AtomicBoolean b = new AtomicBoolean();
            // Reconnect
            session.disconnect();
            session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.getHost().getCredentials().setIdentity(key);
            assertTrue(new SFTPPublicKeyAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    b.set(true);
                    throw new LoginCanceledException();
                }
            }, new DisabledCancelCallback()));
            assertTrue(b.get());

        }
        finally {
            key.delete();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testUnknownFormat() throws Exception {
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader("--unknown format"), key.getOutputStream(false), StandardCharsets.UTF_8);
            // Reconnect
            session.disconnect();
            session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.getHost().getCredentials().setIdentity(key);
            assertTrue(new SFTPPublicKeyAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    fail();
                    throw new LoginCanceledException();
                }
            }, new DisabledCancelCallback()));

        }
        finally {
            key.delete();
        }
    }
}
