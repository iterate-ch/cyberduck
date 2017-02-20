package ch.cyberduck.core.onedrive;

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

import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;
import org.nuxeo.onedrive.client.OneDriveJsonResponse;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class JsonObjectIteratorPort implements Iterator<JsonObject> {
    private final OneDriveAPI api;
    private URL url;
    private boolean hasMorePages;
    private Iterator<JsonValue> currentPage;

    public JsonObjectIteratorPort(OneDriveAPI api, URL url) {
        this.api = api;
        this.url = url;
        this.hasMorePages = true;
    }

    public boolean hasNext() throws OneDriveRuntimeException {
        if(this.currentPage != null && this.currentPage.hasNext()) {
            return true;
        }
        else if(!this.hasMorePages) {
            return false;
        }
        else {
            this.loadNextPage();
            return this.currentPage != null && this.currentPage.hasNext();
        }
    }

    public JsonObject next() throws OneDriveRuntimeException {
        if(this.hasNext()) {
            return this.currentPage.next().asObject();
        }
        else {
            throw new NoSuchElementException();
        }
    }

    private void loadNextPage() throws OneDriveRuntimeException {
        try {
            OneDriveJsonRequest e = new OneDriveJsonRequest(this.api, this.url, "GET");
            OneDriveJsonResponse response = e.send();
            JsonObject json = response.getContent();
            JsonValue values = json.get("value");
            if(values.isNull()) {
                this.currentPage = Collections.emptyIterator();
            }
            else {
                this.currentPage = values.asArray().iterator();
            }

            JsonValue nextUrl = json.get("@odata.nextLink");
            this.hasMorePages = nextUrl != null && !nextUrl.isNull();
            if(this.hasMorePages) {
                this.url = new URL(nextUrl.asString());
            }

        }
        catch(OneDriveAPIException var6) {
            throw new OneDriveRuntimeException("An error occurred during connection with OneDrive API.", var6);
        }
        catch(MalformedURLException var7) {
            this.hasMorePages = false;
            throw new OneDriveRuntimeException("Next url returned from OneDrive API is malformed.", var7);
        }
    }
}
