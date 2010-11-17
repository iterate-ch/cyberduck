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

import oauth.signpost.exception.OAuthException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DropboxClient extends AbstractHttpDropboxClient {
    private static Logger log = Logger.getLogger(DropboxClient.class);

    /**
     * @param client
     */
    public DropboxClient(HttpClient client, String protocol, int port) {
        super(client, "api.getdropbox.com", "api-content.getdropbox.com", protocol, port);
    }

    /**
     * Copy a file from one path to another, with root being either "sandbox" or "dropbox".
     */
    public void copy(String from_path, String to_path) throws IOException {
        String[] params = {"root", ROOT, "from_path", from_path,
                "to_path", to_path};

        HttpResponse response = request(this.buildRequest(HttpPost.METHOD_NAME, "/fileops/copy", params));
        this.finish(response);
    }

    /**
     * Create a folder at the given path.
     */
    public void create(String path) throws IOException {
        String[] params = {"root", ROOT, "path", path};

        HttpResponse response = request(this.buildRequest(HttpPost.METHOD_NAME, "/fileops/create_folder", params));
        this.finish(response);
    }

    /**
     * Delete a file.
     */
    public void delete(String path) throws IOException {

        String[] params = {"root", ROOT, "path", path};
        HttpResponse response = request(this.buildRequest(HttpPost.METHOD_NAME, "/fileops/delete", params));
        this.finish(response);
    }

    /**
     * Move a file.
     */
    public void move(String from_path, String to_path) throws IOException {
        String[] params = {"root", ROOT, "from_path", from_path,
                "to_path", to_path};

        HttpResponse response = request(this.buildRequest(HttpPost.METHOD_NAME, "/fileops/move", params));
        this.finish(response);
    }

    /**
     * Does not actually talk to Dropbox, but instead crafts a properly formatted URL that you can
     * put into your UI which will link a user to that file in their own account.  They will need
     * to login to their Dropbox account on the website to access the file.
     */
    public String links(String path) throws IOException {
        return getUrl("/links/" + ROOT + path);
    }


    /**
     * Get metadata about directories and files, such as file listings and such.
     */
    public ListEntryResponse metadata(String path, int file_limit, String hash, boolean list,
                                      boolean status_in_response, String callback) throws IOException {
        String[] params = {"file_limit", "" + file_limit,
                "hash", hash,
                "list", "" + list,
                "status_in_response", "" + status_in_response,
                "callback", callback};

        HttpResponse response = request(this.buildRequest(HttpGet.METHOD_NAME, "/files/" + ROOT + path, params));
        return new ListEntryResponse(this.parse(response));
    }

    /**
     * Put a file in the user's Dropbox.
     */
    public void put(String to, ContentBody content) throws IOException {
        HttpClient client = getClient();

        HttpPost req = (HttpPost) buildRequest(HttpPost.METHOD_NAME, "/files/" + ROOT + to, true);

        // this has to be done this way because of how oauth signs params
        // first we add a "fake" param of file=path of *uploaded* file, THEN we sign that.
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("file", content.getFilename()));
        req.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        try {
            auth.sign(req);
        }
        catch(OAuthException e) {
            throw new IOException(e.getMessage());
        }
        // now we can add the real file multipart and we're good
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        entity.addPart("file", content);

        // this resets it to the new entity with the real file
        req.setEntity(entity);

        this.finish(client.execute(req));
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    public ListEntryResponse list(String path) throws IOException {
        String[] params = {
                "list", String.valueOf(true)
        };
        HttpResponse response = request(this.buildRequest(HttpGet.METHOD_NAME, "/metadata/" + ROOT + path, params));
        return new ListEntryResponse(this.parse(response));
    }

    /**
     * @param path
     * @return
     * @throws IOException
     * @throws OAuthException
     */
    public ListEntryResponse metadata(String path) throws IOException {
        String[] params = {
                "list", String.valueOf(false)
        };
        HttpResponse response = request(this.buildRequest(HttpGet.METHOD_NAME, "/metadata/" + ROOT + path, params));
        return new ListEntryResponse(this.parse(response));
    }

    /**
     * The account/info API call to Dropbox for getting info about an account attached to the access token.
     *
     * @return
     * @throws IOException
     * @throws OAuthException
     */
    public Account account() throws IOException {
        HttpResponse response = request(this.buildRequest(HttpGet.METHOD_NAME, "/account/info"));
        return new Account(this.parse(response));
    }

    /**
     * @param dbPath
     * @param etag
     * @return
     * @throws IOException
     * @throws OAuthException
     */
    public InputStream get(String dbPath, String etag) throws IOException {
        HttpResponse response = this.getFile(dbPath, etag);
        this.verify(response);
        return response.getEntity().getContent();
    }
}