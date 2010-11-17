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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * @version $Id$
 */
public class ListEntryResponse extends AbstractResponse {

    private long length = -1;
    private String hash;
    private String icon;
    private boolean directory;
    private String modified;
    private String path;
    private String root;
    private String size;
    private String mime;
    private long revision = -1;
    private boolean thumbnail;

    private ArrayList<ListEntryResponse> contents
            = new ArrayList<ListEntryResponse>();

    public ListEntryResponse(JSONObject map) {
        length = this.getLong(map, "bytes");
        hash = this.getString(map, "hash");
        icon = this.getString(map, "icon");
        directory = getBoolean(map, "is_dir");
        modified = this.getString(map, "modified");
        path = this.getString(map, "path");
        root = this.getString(map, "root");
        size = this.getString(map, "size");
        mime = this.getString(map, "mime_type");
        revision = this.getLong(map, "revision");
        thumbnail = this.getBoolean(map, "thumb_exists");

        Object json_contents = map.get("contents");
        if(json_contents instanceof JSONArray) {
            for(Object entry : ((JSONArray) json_contents)) {
                if(entry instanceof JSONObject) {
                    contents.add(new ListEntryResponse((JSONObject) entry));
                }
            }
        }
    }

    public ArrayList<ListEntryResponse> getContents() {
        return contents;
    }

    public long getLength() {
        return length;
    }

    public String getHash() {
        return hash;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getModified() {
        return modified;
    }

    public String getPath() {
        return path;
    }

    public String getRoot() {
        return root;
    }

    public String getSize() {
        return size;
    }

    public String getMime() {
        return mime;
    }

    public long getRevision() {
        return revision;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }
}
