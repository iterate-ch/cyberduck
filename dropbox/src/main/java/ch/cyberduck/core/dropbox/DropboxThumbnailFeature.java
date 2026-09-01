package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Thumbnail;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CustomDbxUserGetThumbnailV2Requests;
import com.dropbox.core.v2.files.PathOrLink;
import com.dropbox.core.v2.files.PreviewResult;
import com.dropbox.core.v2.files.ThumbnailSize;

public class DropboxThumbnailFeature implements Thumbnail {

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxThumbnailFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService();
    }

    @Override
    public InputStream thumbnail(final Path file, final int size) throws BackgroundException {
        try {
            final DbxDownloader<PreviewResult> downloader = CustomDbxUserGetThumbnailV2Requests.getThumbnailV2(
                    session.getClient(file), PathOrLink.path(containerService.getKey(file)), toThumbnailSize(size));
            return downloader.getInputStream();
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    /**
     * @return Smallest thumbnail size with longest edge greater than or equal to the requested size
     */
    protected static ThumbnailSize toThumbnailSize(final int size) {
        if(size <= 32) {
            return ThumbnailSize.W32H32;
        }
        if(size <= 64) {
            return ThumbnailSize.W64H64;
        }
        if(size <= 128) {
            return ThumbnailSize.W128H128;
        }
        if(size <= 256) {
            return ThumbnailSize.W256H256;
        }
        if(size <= 480) {
            return ThumbnailSize.W480H320;
        }
        if(size <= 640) {
            return ThumbnailSize.W640H480;
        }
        if(size <= 960) {
            return ThumbnailSize.W960H640;
        }
        if(size <= 1024) {
            return ThumbnailSize.W1024H768;
        }
        if(size <= 2048) {
            return ThumbnailSize.W2048H1536;
        }
        return ThumbnailSize.W3200H2400;
    }

    /**
     * This method currently supports files with the following file extensions: jpg, jpeg, png, tiff, tif, gif,
     * webp, ppm and bmp. Photos that are larger than 20MB in size won't be converted to a thumbnail.
     */
    @Override
    public boolean isSupported(final Path file) {
        if(!file.isFile()) {
            return false;
        }
        if(file.attributes().getSize() > 20 * 1024 * 1024) {
            return false;
        }
        return StringUtils.equalsAnyIgnoreCase(FilenameUtils.getExtension(file.getName()),
                "jpg", "jpeg", "png", "tiff", "tif", "gif", "webp", "ppm", "bmp");
    }
}
