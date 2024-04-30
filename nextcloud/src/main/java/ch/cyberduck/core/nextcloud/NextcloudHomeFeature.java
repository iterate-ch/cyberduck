package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.AbstractHomeFeature;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class NextcloudHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(NextcloudHomeFeature.class);

    private final Home delegate;
    private final Host bookmark;
    private final String root;

    public NextcloudHomeFeature(final Host bookmark) {
        this(new DefaultPathHomeFeature(bookmark), bookmark);
    }

    public NextcloudHomeFeature(final Home delegate, final Host bookmark) {
        this(delegate, bookmark, new HostPreferences(bookmark).getProperty("nextcloud.root.default"));
    }

    /**
     * @param root WebDAV root
     */
    public NextcloudHomeFeature(final Home delegate, final Host bookmark, final String root) {
        this.delegate = delegate;
        this.bookmark = bookmark;
        this.root = root;
    }

    @Override
    public Path find() throws BackgroundException {
        return this.find(Context.files);
    }

    public Path find(final Context files) throws BackgroundException {
        final String username = bookmark.getCredentials().getUsername();
        if(StringUtils.isBlank(username)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Missing username for %s", bookmark));
            }
            return delegate.find();
        }
        // Custom path setting
        final Path workdir;
        final Path defaultpath = new DelegatingHomeFeature(delegate).find();
        if(!defaultpath.isRoot() && StringUtils.isNotBlank(StringUtils.removeStart(defaultpath.getAbsolute(), root))) {
            workdir = new Path(new Path(String.format("%s/%s/%s", root, files.name(), username), EnumSet.of(Path.Type.directory)),
                    StringUtils.removeStart(defaultpath.getAbsolute(), root), EnumSet.of(Path.Type.directory));
        }
        else {
            workdir = new Path(new Path(String.format("%s/%s", root, files.name()), EnumSet.of(Path.Type.directory)),
                    username, EnumSet.of(Path.Type.directory));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use home directory %s", workdir));
        }
        return workdir;
    }

    public enum Context {
        files,
        versions,
        meta
    }
}
