package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.oauth.OAuth2TokenListener;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;

public class CTERASession extends DAVSession {
    private static final Logger log = Logger.getLogger(CTERASession.class);

    public final BrowserLauncher browser = BrowserLauncherFactory.get();

    public CTERASession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(this.getPublicInfo().hasWebSSO) {
            this.startWebSSOFlow(cancel);
        }
        else {
            //TODO
        }
    }

    private void startWebSSOFlow(final CancelCallback cancel) throws BackgroundException {
        final String url = String.format("%s/ServicesPortal/activate?scheme=%s",
            new HostUrlProvider().withUsername(false).withPath(false).get(host), CTERAProtocol.CTERA_REDIRECT_URI
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!browser.open(url)) {
            log.warn(String.format("Failed to launch web browser for %s", url));
        }
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        final CountDownLatch signal = new CountDownLatch(1);
        final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
        registry.register(StringUtils.EMPTY, new OAuth2TokenListener() {
            @Override
            public void callback(final String code) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Callback with code %s", code));
                }
                if(!StringUtils.isBlank(code)) {
                    authenticationCode.set(code);
                }
                signal.countDown();
            }
        });
        while(!Uninterruptibles.awaitUninterruptibly(signal, 500, TimeUnit.MILLISECONDS)) {
            cancel.verify();
        }
    }

    private PublicInfo getPublicInfo() {
        final HttpGet request = new HttpGet("/ServicesPortal/public/publicInfo?format=jsonext");
        try {
            return client.execute(request, new AbstractResponseHandler<PublicInfo>() {
                @Override
                public PublicInfo handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), PublicInfo.class);
                }
            });
        }
        catch(IOException e) {
            //TODO
            throw new RuntimeException();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class PublicInfo {
        public boolean hasWebSSO;
    }
}
