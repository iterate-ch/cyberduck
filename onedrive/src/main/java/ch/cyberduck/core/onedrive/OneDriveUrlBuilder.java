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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;

import java.net.MalformedURLException;
import java.net.URL;

public class OneDriveUrlBuilder {

    private final PathContainerService containerService
            = new PathContainerService();

    private final StringBuilder builder;

    public OneDriveUrlBuilder(final OneDriveSession session) {
        this.builder = new StringBuilder(session.getClient().getBaseURL());
    }

    public OneDriveUrlBuilder resolveDriveQueryPath(final Path file) {
        builder.append("/drives"); // query single drive
        if(!file.isRoot()) {
            builder.append(String.format("/%s", containerService.getContainer(file).getName()));
            if(!containerService.isContainer(file)) {
                // Append path to item via pathContainerService with format :/path:
                builder.append(String.format("/root:/%s:", URIEncoder.encode(containerService.getKey(file))));
            }
        }
        return this;
    }

    public OneDriveUrlBuilder resolveChildrenPath(final Path directory) {
        if(containerService.isContainer(directory)) {
            builder.append("/root/children");
        }
        else if(!directory.isRoot()) {
            builder.append("/children");
        }
        return this;
    }

    public OneDriveUrlBuilder resolveContentPath(final Path directory) {
        if(!containerService.isContainer(directory) && !directory.isRoot()) {
            builder.append("/content");
        }
        return this;
    }

    public OneDriveUrlBuilder resolveUploadSession(final Path directory) {
        if(!containerService.isContainer(directory) && !directory.isRoot()) {
            builder.append("/createUploadSession");
        }
        return this;
    }

    public URL build() throws BackgroundException {
        try {
            return new URL(builder.toString());
        }
        catch(MalformedURLException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OneDriveUrlBuilder{");
        sb.append("builder=").append(builder);
        sb.append('}');
        return sb.toString();
    }
}
