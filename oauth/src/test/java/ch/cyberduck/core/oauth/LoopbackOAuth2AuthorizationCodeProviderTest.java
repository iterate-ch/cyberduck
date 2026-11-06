package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.DisabledBrowserLauncher;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LoopbackOAuth2AuthorizationCodeProviderTest {
    @Test
    public void testHostedCompletionRedirectDoesNotForwardCredentials() throws Exception {
        final String property = "factory.browserlauncher.class";
        final String previous = PreferencesFactory.get().getProperty(property);
        PreferencesFactory.get().setProperty(property, DisabledBrowserLauncher.class.getName());
        try {
            final String redirect = OAuth2AuthorizationService.toRedirectUri("http://127.0.0.1/oauth/callback");
            final Host host = new Host(new TestProtocol(), "site.files.test");
            host.setProperty("oauth.browser.success.url", "/desktopSetup?client_id=cyberduck");
            final LoopbackOAuth2AuthorizationCodeProvider provider = new LoopbackOAuth2AuthorizationCodeProvider() {
                @Override
                protected void open(final String url) {
                    // Requests are sent by the login callback once the listener is ready.
                }
            };
            final String code = provider.prompt(host, new DisabledLoginCallback() {
                @Override
                public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message) throws ConnectionCanceledException {
                    try {
                        final HttpURLConnection invalid = (HttpURLConnection) URI.create(redirect + "?state=wrong&code=secret-code").toURL().openConnection();
                        invalid.setInstanceFollowRedirects(false);
                        invalid.setConnectTimeout(5000);
                        invalid.setReadTimeout(5000);
                        assertEquals(400, invalid.getResponseCode());
                        assertNull(invalid.getHeaderField("Location"));
                        assertEquals(1, signal.getCount());
                        invalid.disconnect();

                        final HttpURLConnection valid = (HttpURLConnection) URI.create(redirect + "?state=expected&code=secret-code").toURL().openConnection();
                        valid.setInstanceFollowRedirects(false);
                        valid.setConnectTimeout(5000);
                        valid.setReadTimeout(5000);
                        assertEquals(302, valid.getResponseCode());
                        assertEquals("https://site.files.test/desktopSetup?client_id=cyberduck", valid.getHeaderField("Location"));
                        assertEquals("no-store", valid.getHeaderField("Cache-Control"));
                        assertEquals("no-referrer", valid.getHeaderField("Referrer-Policy"));
                        valid.disconnect();
                    }
                    catch(Exception e) {
                        throw new LoginCanceledException(e);
                    }
                }
            }, "https://site.files.test/auth/oauth/authorize?state=expected", redirect, "expected");
            assertEquals("secret-code", code);
        }
        finally {
            PreferencesFactory.get().setProperty(property, previous);
        }
    }

    @Test
    public void testDefaultRedirectAndStateValidation() throws Exception {
        final String property = "factory.browserlauncher.class";
        final String previous = PreferencesFactory.get().getProperty(property);
        PreferencesFactory.get().setProperty(property, DisabledBrowserLauncher.class.getName());
        try {
            final String redirect = OAuth2AuthorizationService.toRedirectUri("http://127.0.0.1/oauth/callback");
            final LoopbackOAuth2AuthorizationCodeProvider provider = new LoopbackOAuth2AuthorizationCodeProvider() {
                @Override
                protected void open(final String url) {
                    // Requests are sent by the login callback once the listener is ready.
                }
            };
            final String code = provider.prompt(new Host(new TestProtocol(), "localhost"), new DisabledLoginCallback() {
                @Override
                public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message) throws ConnectionCanceledException {
                    try {
                        final HttpURLConnection invalid = (HttpURLConnection) URI.create(redirect + "?state=wrong&code=secret-code").toURL().openConnection();
                        invalid.setConnectTimeout(5000);
                        invalid.setReadTimeout(5000);
                        assertEquals(400, invalid.getResponseCode());
                        assertNull(invalid.getHeaderField("Location"));
                        assertEquals(1, signal.getCount());
                        invalid.disconnect();

                        final HttpURLConnection valid = (HttpURLConnection) URI.create(redirect + "?state=expected&code=secret-code").toURL().openConnection();
                        valid.setInstanceFollowRedirects(false);
                        valid.setConnectTimeout(5000);
                        valid.setReadTimeout(5000);
                        assertEquals(302, valid.getResponseCode());
                        assertEquals("no-store", valid.getHeaderField("Cache-Control"));
                        assertEquals(OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI, valid.getHeaderField("Location"));
                        valid.disconnect();
                        assertTrue(signal.await(5, TimeUnit.SECONDS));
                    }
                    catch(Exception e) {
                        throw new LoginCanceledException(e);
                    }
                }
            }, "https://localhost/authorize", redirect, "expected");
            assertEquals("secret-code", code);
        }
        finally {
            PreferencesFactory.get().setProperty(property, previous);
        }
    }

    @Test
    public void testDeniedAuthorizationReturnsToApplicationWithoutShowingSuccess() throws Exception {
        for(boolean hosted : new boolean[]{false, true}) {
            final String redirect = OAuth2AuthorizationService.toRedirectUri("http://127.0.0.1/oauth/callback");
            final Host host = new Host(new TestProtocol(), "site.files.test");
            if(hosted) {
                host.setProperty("oauth.browser.success.url", "/desktopSetup?client_id=cyberduck");
            }
            final LoopbackOAuth2AuthorizationCodeProvider provider = new LoopbackOAuth2AuthorizationCodeProvider() {
                @Override
                protected void open(final String url) {
                    // Requests are sent by the login callback once the listener is ready.
                }
            };
            final String code = provider.prompt(host, new DisabledLoginCallback() {
                @Override
                public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message) throws ConnectionCanceledException {
                    try {
                        final HttpURLConnection denied = (HttpURLConnection) URI.create(redirect + "?state=expected&error=access_denied").toURL().openConnection();
                        denied.setInstanceFollowRedirects(false);
                        denied.setConnectTimeout(5000);
                        denied.setReadTimeout(5000);
                        assertEquals(302, denied.getResponseCode());
                        assertEquals(OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI, denied.getHeaderField("Location"));
                        assertEquals("no-store", denied.getHeaderField("Cache-Control"));
                        assertEquals("no-referrer", denied.getHeaderField("Referrer-Policy"));
                        denied.disconnect();
                        assertTrue(signal.await(5, TimeUnit.SECONDS));
                    }
                    catch(Exception e) {
                        throw new LoginCanceledException(e);
                    }
                }
            }, "https://site.files.test/auth/oauth/authorize", redirect, "expected");
            assertNull(code);
        }
    }
}
