package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.util.Locale;


public class DropBoxSession extends HttpSession<DropBoxClient> {

    private static final Logger log = Logger.getLogger(DropBoxSession.class);

    private String token;
    private DropBoxClient client;

    private Preferences preferences = PreferencesFactory.get();

    public DropBoxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected DropBoxClient connect(HostKeyCallback key) throws BackgroundException {
        client = new DropBoxClient();
        return client;
    }

    @Override
    public void login(HostPasswordStore keychain, LoginCallback prompt, CancelCallback cancel, Cache<Path> cache) throws BackgroundException {

        String accessToken = keychain.getPassword(host.getProtocol().getScheme(),
                host.getPort(), "www.dropbox.com", "Drop Box OAuth2 Access Token");

        DbxRequestConfig config = new DbxRequestConfig(
                "Drop Box Test", Locale.getDefault().toString());

        if(StringUtils.isEmpty(accessToken)) {

            DbxAppInfo appInfo = new DbxAppInfo(preferences.getProperty("dropbox.client.id"),
                    preferences.getProperty("dropbox.client.secret"));
            DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
            final String url = webAuth.start();
            BrowserLauncherFactory.get().open(url);
            prompt.prompt(host, host.getCredentials(),
                    LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"), url,
                    new LoginOptions().keychain(false).user(false)
            );

            try {
                DbxAuthFinish authFinish = webAuth.finish(host.getCredentials().getPassword());
                token = authFinish.getAccessToken();

                // Save for future use
                keychain.addPassword(host.getProtocol().getScheme(),
                        host.getPort(), "www.dropbox.com", "Drop Box OAuth2 Access Token",
                        token);

            } catch(DbxException ex) {
                ex.printStackTrace();
            }
        }
        token = accessToken;
        client.setDbxClient(new DbxClientV2(config, token));


    }

    @Override
    protected void logout() throws BackgroundException {

    }

    @Override
    public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
        return new DropBoxListService(this).list(directory, listener);
    }

    public <T> T getFeature(Class<T> type) {
        /*if (type == Read.class){

        } else if (type == Write.class){

        } else if (type == Upload.class){

        } else if (type == Directory.class){

        } else if (type == Delete.class){

        } else if (type == Move.class){

        } else if (type == Copy.class){

        } else if (type == Touch.class){

        } else if (type == UrlProvider.class){

        } else if (type == Home.class){

        } else {*/
            return super.getFeature(type);
        //}
    }
}
