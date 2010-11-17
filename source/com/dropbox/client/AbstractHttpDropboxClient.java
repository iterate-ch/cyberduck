package com.dropbox.client;

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

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthException;
import ch.cyberduck.core.ConnectionCanceledException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractHttpDropboxClient {
    private static Logger log = Logger.getLogger(AbstractHttpDropboxClient.class);

    protected Authenticator auth;

    /**
     * Context
     */
    protected static final String ROOT = "dropbox";
    /**
     *
     */
    protected static final int BUFFER_SIZE = 2048;
    /**
     * This is set by Dropbox to indicate what version of the API you are using.
     */
    protected static final int API_VERSION = 0;

    /**
     *
     */
    private String api_host;
    /**
     *
     */
    private String content_host;
    private String protocol;
    private int port;

    protected HttpClient client;

    protected AbstractHttpDropboxClient(HttpClient client, String api_host, String content_host, String protocol, int port) {
        this.api_host = api_host;
        this.content_host = content_host;
        this.protocol = protocol;
        this.port = port;
        this.client = client;
        HttpParams params = this.client.getParams();
        HttpProtocolParams.setUseExpectContinue(params, false);
    }

    protected HttpClient getClient() {
        return client;
    }

    /**
     * Used when we have to authenticate from scratch
     *
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws OAuthException
     */
    public void authenticate(String key, String secret, String username, String password) throws IOException {
        String[] params = {"email", username, "password", password};

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(key, secret);
        HttpGet request = new HttpGet(this.getUrl("/token", params, false));
        try {
            consumer.sign(request);
        }
        catch(OAuthException e) {
            throw new IOException(e.getMessage());
        }

        JSONObject credentials = this.parse(this.execute(request));

        String token_key = credentials.get("token").toString();
        String token_secret = credentials.get("secret").toString();
        log.info("Obtained Token Key:" + token_key);
        log.info("Obtained Token Secret:" + token_secret);

        auth = new Authenticator(key, secret,
                this.getRequestPath("/oauth/request_token"), this.getRequestPath("/oauth/access_token"), this.getRequestPath("/oauth/authorize"),
                token_key, token_secret);
    }

    /**
     * Used internally to simplify making requests to the services, and handling an error, parsing
     * JSON, or returning a non-JSON body.  See executeRequest to see what's in the returned Map.
     */
    protected HttpResponse request(HttpUriRequest req) throws IOException {
        if(null == auth) {
            throw new ConnectionCanceledException();
        }
        try {
            auth.sign(req);
        }
        catch(OAuthException e) {
            throw new IOException(e.getMessage());
        }
        return this.execute(req);
    }


    /**
     * Used internally to URL encode a list of parameters, which makes it easier to do params than with a map.
     * If you want to use this, the params are organized as key=value pairs in a row, and should convert to Strings.
     */
    protected String urlencode(String[] params) {
        String result = "";
        try {
            boolean firstTime = true;
            for(int i = 0; i < params.length; i += 2) {
                if(params[i + 1] != null) {
                    if(firstTime) {
                        firstTime = false;
                    }
                    else {
                        result += "&";
                    }
                    result += URLEncoder.encode("" + params[i], "UTF-8") + "=" + URLEncoder.encode("" + params[i + 1], "UTF-8");
                }
            }

        }
        catch(UnsupportedEncodingException e) {
            // This shouldn't show up, since UTF-8 should always be supported.
            throw new RuntimeException(e);
        }
        return result;
    }


    /**
     * Used internally to simplify reading a response in buffered lines.
     */
    private String readResponse(HttpResponse response) throws IOException {
        HttpEntity ent = response.getEntity();
        BufferedReader in = new BufferedReader(new InputStreamReader(ent.getContent()), BUFFER_SIZE);
        String inputLine;
        String result = StringUtils.EMPTY;
        while((inputLine = in.readLine()) != null) {
            result += inputLine;
        }
        response.getEntity().consumeContent();
        return result;
    }

    /**
     * Verify response and consume any open resources.
     *
     * @param response
     * @throws IOException
     */
    protected void finish(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if(null != entity) {
            // Release all allocated resources
            entity.consumeContent();
        }
        this.verify(response);
    }

    /**
     * Check for 200 response code
     *
     * @param response
     * @throws IOException Throw exception if response status is not OK
     */
    protected void verify(HttpResponse response) throws IOException {
        if(response.getStatusLine().getStatusCode() != 200) {
            throw new IOException(response.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * Parse JSON response.
     *
     * @param response
     * @return
     * @throws IOException
     */
    protected JSONObject parse(HttpResponse response) throws IOException {
        try {
            this.verify(response);
            String body = this.readResponse(response);
            return (JSONObject) new JSONParser().parse(body);
        }
        catch(ParseException e) {
            throw new IOException("Invalid JSON response from server");
        }
        finally {
            this.finish(response);
        }
    }

    protected HttpResponse execute(HttpUriRequest req) throws IOException {
        return client.execute(req);
    }

    protected HttpUriRequest buildRequest(String method, String path) {
        return this.buildRequest(method, path, new String[]{});
    }

    /**
     * Used internally to construct a complete URL to a given host, which can sometimes
     * be the "API host" or the "content host" depending on the type of call.
     *
     * @param method
     * @param path
     * @param params
     * @return
     */
    protected HttpUriRequest buildRequest(String method, String path, String[] params) {
        return this.buildRequest(method, path, params, false);
    }

    /**
     * @param method
     * @param path
     * @param content
     * @return
     */
    protected HttpUriRequest buildRequest(String method, String path, boolean content) {
        return this.buildRequest(method, path, new String[]{}, content);
    }

    /**
     * @param method
     * @param path
     * @param params
     * @param content
     * @return
     */
    protected HttpUriRequest buildRequest(String method, String path, String[] params, boolean content) {
        if(method.equals(HttpGet.METHOD_NAME)) {
            return new HttpGet(this.getUrl(path, params, content));
        }
        else if(method.equals(HttpPost.METHOD_NAME)) {
            HttpPost post = new HttpPost(this.getUrl(path, params, content));
            if(params != null && params.length > 2) {
                List<BasicNameValuePair> form = new ArrayList<BasicNameValuePair>();
                for(int i = 0; i < params.length; i += 2) {
                    if(params[i + 1] != null) {
                        form.add(new BasicNameValuePair("" + params[i], "" + params[i + 1]));
                    }
                }
                try {
                    post.setEntity(new UrlEncodedFormEntity(form, HTTP.UTF_8));
                }
                catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return post;
        }
        throw new IllegalArgumentException(method);
    }

    /**
     * API host
     *
     * @param path
     * @return
     */
    protected String getUrl(String path) {
        return this.getUrl(path, new String[]{}, false);
    }

    /**
     * @param path
     * @param content URL for content server
     * @return
     */
    protected String getUrl(String path, String[] params, boolean content) {
        if(content) {
            return protocol + "://" + content_host + ":" + port + this.getRequestPath(path, params);
        }
        return protocol + "://" + api_host + ":" + port + this.getRequestPath(path, params);
    }

    /**
     * No parameters in URL
     *
     * @param target
     * @return
     */
    protected String getRequestPath(String target) {
        return this.getRequestPath(target, new String[]{});
    }

    /**
     * Used internally to build a URL path + params (if given) according to the API_VERSION.
     */
    protected String getRequestPath(String target, String[] params) {
        try {
            // we have to encode the whole line, then remove + and / encoding to get a good oauth url
            target = URLEncoder.encode("/" + API_VERSION + target, "UTF-8");
            target = target.replace("%2F", "/").replace("+", "%20");
            if(params != null && params.length > 0) {
                target += "?" + urlencode(params);
            }
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    /**
     * Get a file from the content server, returning the raw Apache HTTP Components response object
     * so you can stream it or work with it how you need.
     * You *must* call .getEntity().consumeContent() on the returned HttpResponse object or you might leak
     * connections.
     *
     * @param from_path
     * @return
     * @throws IOException
     */
    protected HttpResponse getFile(String from_path) throws IOException {
        return getFile(from_path, null);
    }

    /**
     * Get a file from the content server, returning the raw Apache HTTP Components response object
     * so you can stream it or work with it how you need.
     * You *must* call .getEntity().consumeContent() on the returned HttpResponse object or you might leak
     * connections.
     *
     * @param path
     * @param etag Version of the file
     * @return
     * @throws IOException
     */
    protected HttpResponse getFile(String path, String etag) throws IOException {
        HttpUriRequest req = this.buildRequest(HttpGet.METHOD_NAME, "/files/" + ROOT + path, true);
        if(StringUtils.isNotBlank(etag)) {
            req.addHeader("If-None-Match", etag);
        }
        return this.request(req);
    }
}
