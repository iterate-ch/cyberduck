package ch.cyberduck.core.dropbox.client;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 *
 * Derived from Official Dropbox API client for Java.
 * http://bitbucket.org/dropboxapi/dropbox-client-java
 */

import org.apache.http.HttpRequest;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthException;

/**
 * @version $Id$
 */
public class Authenticator {

    private String consumer_key;
    private String consumer_secret;

    private OAuthConsumer consumer;

    public Authenticator(String consumer_key, String consumer_secret, String request_token_url, String access_token_url,
                         String authorization_url, String access_token_key, String access_token_secret) {
        this.consumer_key = consumer_key;
        this.consumer_secret = consumer_secret;

        this.consumer = new DefaultOAuthConsumer(consumer_key, consumer_secret);
        this.consumer.setTokenWithSecret(access_token_key, access_token_secret);
    }

    public void sign(HttpRequest request) throws OAuthException {
        // create a consumer object and configure it with the access
        // token and token secret obtained from the service provider
        OAuthConsumer c = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
        c.setTokenWithSecret(consumer.getToken(), consumer.getTokenSecret());
        // sign the request
        c.sign(request);
    }
}
